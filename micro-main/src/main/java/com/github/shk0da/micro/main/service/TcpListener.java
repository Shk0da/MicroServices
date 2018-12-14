package com.github.shk0da.micro.main.service;

import com.github.shk0da.micro.main.config.AsyncConfig;
import com.github.shk0da.micro.main.util.HexBinUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class TcpListener {

    private final static Logger log = LoggerFactory.getLogger(TcpListener.class);

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(AsyncConfig.AVAILABLE_TASK_THREADS);

    private final Lock readLock = new ReentrantReadWriteLock().readLock();
    private final AtomicReference<TransferQueue<byte[]>> outQueue = new AtomicReference<>(new LinkedTransferQueue<>());

    public TcpListener(InetSocketAddress address, final Consumer<byte[]> consumer) {
        NetClient netClient = Vertx
                .vertx()
                .exceptionHandler(event -> log.error("NetClient {}: {}", address, event.getMessage()))
                .createNetClient();

        AtomicBoolean connectSucceeded = new AtomicBoolean(false);
        Handler<AsyncResult<NetSocket>> connectHandler = event -> {
            connectSucceeded.set(event.succeeded());
            if (event.succeeded()) {
                NetSocket netSocket = event
                        .result()
                        .handler(request ->
                                CompletableFuture.runAsync(() -> {
                                    byte[] answer = request.getBytes();
                                    if (answer.length > 0) {
                                        log.debug("Get byte: {}", answer);
                                        log.debug("Get string: {}", new String(answer));
                                        log.debug("Get hex: {}", HexBinUtil.encode(answer));
                                        consumer.accept(answer);
                                    }
                                }, executor).exceptionally(throwable -> {
                                    connectSucceeded.set(false);
                                    log.error("NetSocketRequestHandler {}: {}", address, throwable.getMessage());
                                    return null;
                                }))
                        .exceptionHandler(throwable -> {
                            connectSucceeded.set(false);
                            log.error("NetSocketHandler {}: {}", address, throwable.getMessage());
                        });
                log.info("Connected: {}", netSocket.remoteAddress());
                CompletableFuture.runAsync(() -> {
                    while (true) {
                        readLock.lock();
                        try {
                            if (connectSucceeded.get() && !outQueue.get().isEmpty()) {
                                byte[] message = outQueue.get().poll();
                                if (message != null) {
                                    log.debug("Send byte: {}", message);
                                    log.debug("Send string: {}", new String(message));
                                    log.debug("Send hex: {}", HexBinUtil.encode(message));
                                    netSocket.write(Buffer.buffer(message));
                                }
                            }
                        } catch (Exception ex) {
                            connectSucceeded.set(false);
                            log.error("NetSocketSendHandler {}: {}", address, ex.getMessage());
                        } finally {
                            readLock.unlock();
                        }
                    }
                }, executor);
            } else {
                log.error("Unable to establish connection {}: {}", address, event.cause().getMessage());
            }
        };

        CompletableFuture.runAsync(() -> {
            while (true) {
                synchronized (this) {
                    if (!connectSucceeded.get()) {
                        netClient.connect(address.getPort(), address.getHostName(), connectHandler);
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }, executor);
    }

    public void send(byte[] message) {
        outQueue.get().add(message);
    }
}
