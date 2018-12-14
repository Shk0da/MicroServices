package com.github.shk0da.micro.main.service;

import com.github.shk0da.micro.main.annotation.HazelcastClient;
import com.github.shk0da.micro.main.config.AsyncConfig;
import com.github.shk0da.micro.main.util.CompletableFutureUtil;
import com.github.shk0da.micro.main.util.HexBinUtil;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@HazelcastClient
public abstract class AbstractTcpServerService {

    private final static Logger log = LoggerFactory.getLogger(AbstractTcpServerService.class);

    private final Set<NetServer> servers = Sets.newConcurrentHashSet();

    @Value("${server.tcp.timeout}")
    private int timeoutInSeconds;

    @Value("#{'${server.tcp.port}'.split(',')}")
    private List<Integer> ports;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    @Qualifier("cachedThreadPoolExecutor")
    private TaskExecutor executor;

    @PostConstruct
    private void initTcpServer() {
        Vertx.clusteredVertx(new VertxOptions()
                        .setHAEnabled(true)
                        .setClusterManager(new HazelcastClusterManager(hazelcastInstance))
                        .setWorkerPoolSize(AsyncConfig.AVAILABLE_TCP_THREADS), event -> {
                    if (event.failed()) throw new RuntimeException("Failed create TcpServer#VertxCluster! " + event.cause());
                    ports.forEach(port ->
                            servers.add(event.result()
                                    .createNetServer()
                                    .connectHandler(connectHandler())
                                    .exceptionHandler(exceptionHandler())
                                    .listen(port)));
                    log.debug("TCP Server ports: {}", ports);
                }
        );
    }

    private Handler<NetSocket> connectHandler() {
        return netSocket -> {
            log.debug("Incoming connection: {}", netSocket.remoteAddress());
            netSocket.handler(handler(netSocket));
        };
    }

    private Handler<Throwable> exceptionHandler() {
        return event -> log.error("TCP Server Exception: {}", event.getMessage());
    }

    private Handler<Buffer> handler(final NetSocket netSocket) {
        return inBuffer -> {
            final AtomicBoolean cancelled = new AtomicBoolean(false);
            CompletableFuture.supplyAsync(() -> {
                final byte[] inData = inBuffer.getBytes();
                log.debug("Incoming data: {}", inData);
                log.debug("Incoming data HEX: {}", HexBinUtil.encode(inData));
                log.debug("Incoming data String: {}", new String(inData));
                return processingIncomingMessage(inData, cancelled);
            }, executor).acceptEitherAsync(
                    CompletableFutureUtil.timeoutAfter("No response received from other systems", timeoutInSeconds, TimeUnit.SECONDS),
                    outData -> {
                        log.debug("Outgoing data: {}", outData);
                        log.debug("Outgoing data HEX: {}", HexBinUtil.encode(outData));
                        log.debug("Outgoing data String: {}", new String(outData));
                        netSocket.write(Buffer.buffer(outData));
                    }, executor
            ).exceptionally(throwable -> {
                cancelled.set(true);
                log.error(throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage());
                netSocket.write(Buffer.buffer());
                return null;
            });
        };
    }

    protected abstract byte[] processingIncomingMessage(final byte[] data, final AtomicBoolean cancelled);

    @PreDestroy
    private synchronized void preDestroy() {
        servers.forEach(server ->
                server.close(result -> log.info("TCP Server closed: {}", result.failed()
                        ? "failed"
                        : result.succeeded()
                        ? "succeeded"
                        : result.cause()))
        );
        servers.clear();
    }
}
