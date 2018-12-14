package com.github.shk0da.micro.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import com.github.shk0da.micro.main.util.HexBinUtil;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TcpClient {

    private final static Logger log = LoggerFactory.getLogger(TcpClient.class);

    private int maxSendAttempts = 5;
    private int maxConnectAttempts = 5;
    private int reconnectInterval = 5;
    private int timeOut = (int) TimeUnit.SECONDS.toMillis(5);

    private final String host;
    private final int port;

    private final ThreadLocal<Socket> socketHolder = new ThreadLocal<>();
    private final ThreadLocal<Boolean> socketActiveHolder = new ThreadLocal<>();

    private AtomicLong sendAttempts = new AtomicLong(1);
    private AtomicLong connectAttempts = new AtomicLong(1);

    public TcpClient(InetSocketAddress socketAddress) {
        this.host = socketAddress.getHostName();
        this.port = socketAddress.getPort();
        openSocketConnection();
    }

    public CompletableFuture<byte[]> sendFuture(byte[] message) {
        return CompletableFuture.supplyAsync(() -> send(message));
    }

    public byte[] send(final byte[] message) {
        if (!checkSocket()) return new byte[0];
        byte[] answer;
        try {
            try (OutputStream os = socketHolder.get().getOutputStream(); InputStream is = socketHolder.get().getInputStream()) {
                log.debug("Request[String]: {}", new String(message));
                log.debug("Request[HEX]: {}", HexBinUtil.encode(message));
                os.write(message);

                int bytesRead;
                int position = 0;
                int msgsize = 0;
                int iteration = 0;
                byte[] response = new byte[1024];
                do {
                    bytesRead = is.read(response, position, response.length - position);
                    if (bytesRead > 0) {
                        position += bytesRead;
                    }
                    if (position >= 2) {
                        msgsize = (int) response[0] * 256 + (response[1] < 0 ? (int) response[1] + 256 : response[1]);
                    }
                    iteration++;
                } while ((msgsize + 2 < position || position == 0) && iteration < 5);

                answer = Arrays.copyOfRange(response, 0, position);
                log.debug("Response[String]: {}", new String(answer));
                log.debug("Response[HEX]: {}", HexBinUtil.encode(answer));
            }
        } catch (IOException e) {
            if (sendAttempts.get() <= maxSendAttempts) {
                log.warn("ATTEMPT: {} ({}:{}). Error sending request to/processing response from: [{}]",
                        sendAttempts.getAndIncrement(), host, port, e.getMessage());
                send(message);
            }

            sendAttempts.set(0);
            return new byte[0];
        }

        return answer;
    }

    public boolean checkSocket() {
        if (isClosed()) {
            openSocketConnection();
        }

        return isEnable();
    }

    public boolean isEnable() {
        return socketActiveHolder.get() == null ? false : socketActiveHolder.get();
    }

    private void openSocketConnection() {
        if (connectAttempts.get() > maxConnectAttempts) return;
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(false);
            socket.setReuseAddress(true);
            socket.setSoTimeout(timeOut);
            socketHolder.set(socket);
            connectAttempts.set(1);
            socketActiveHolder.set(true);
        } catch (SocketException e) {
            socketActiveHolder.set(false);
            connectAttempts.incrementAndGet();
            log.error("Connection to [{}:{}] lost: [{}]", host, port, e.getMessage());
            reconnect();
        } catch (IOException e) {
            socketActiveHolder.set(false);
            connectAttempts.incrementAndGet();
            log.error("Error opening connection to: [{}:{}], [{}]", host, port, e.getMessage());
            reconnect();
        }
    }

    private void reconnect() {
        new DefaultManagedTaskScheduler().schedule(() -> {
            closeSocketConnection();
            openSocketConnection();
        }, Date.from(Instant.now().plus(reconnectInterval, ChronoUnit.SECONDS)));
    }

    private void closeSocketConnection() {
        try {
            Socket socket = socketHolder.get();
            if (socket != null && !socket.isClosed()) {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            }
            socketHolder.set(null);
        } catch (IOException e) {
            log.error("Error closing connection to: [{}]", e.getMessage());
        }
    }

    public boolean isClosed() {
        return socketHolder.get() == null || socketHolder.get().isClosed();
    }

    @Override
    @PreDestroy
    public void finalize() {
        connectAttempts.set(maxConnectAttempts);
        try {
            Thread.currentThread().interrupt();
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "TcpClient{host='" + host + "', port=" + port + "}";
    }

    public static Builder builder(InetSocketAddress socketAddress) {
        return new TcpClient(socketAddress).new Builder();
    }

    public class Builder {

        private Builder() {
            // private constructor
        }

        public Builder maxSendAttempts(int count) {
            TcpClient.this.maxSendAttempts = count;
            return this;
        }

        public Builder maxConnectAttempts(int count) {
            TcpClient.this.maxConnectAttempts = count;
            return this;
        }

        public Builder reconnectInterval(int sec) {
            TcpClient.this.reconnectInterval = sec;
            return this;
        }

        public Builder timeOut(int ms) {
            TcpClient.this.timeOut = ms;
            return this;
        }

        public TcpClient build() {
            return TcpClient.this;
        }
    }
}
