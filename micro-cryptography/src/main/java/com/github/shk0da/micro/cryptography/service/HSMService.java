package com.github.shk0da.micro.cryptography.service;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.github.shk0da.micro.cryptography.util.HSMUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.service.TcpClient;

import javax.annotation.PostConstruct;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.shk0da.micro.cryptography.util.HSMUtil.createRequest;

/**
 * Сервис для работы с HSM
 */
@Service
@ThreadSafe
@RefreshScope
public class HSMService {

    private final static Logger log = LoggerFactory.getLogger(HSMService.class);

    private static final Integer FIRST_INSTANCE = 0;

    private final AtomicInteger currentInstance = new AtomicInteger(FIRST_INSTANCE);
    private final Set<TcpClient> clientInstances = Sets.newConcurrentHashSet();

    @Value("#{'${hsm.cluster}'.split(';')}")
    private List<String> hsmCluster;

    @Value("${hsm.mac.key}")
    private String macKey;

    public static class HSMServiceException extends RuntimeException {
        public HSMServiceException(String message) {
            super(message);
        }
    }

    public HSMService(ApplicationContext context) {
        context.getBean(TaskScheduler.class)
                .scheduleWithFixedDelay(this::updateHSMInstanceList, TimeUnit.MINUTES.toMillis(10));
    }

    /**
     * Отдает инстанс HSM клиента.
     * Если настройки содеражат несколько инстансов для подключения, они ратируются и отдаются по очереди.
     * Отдаются только активные
     *
     * @return {@link TcpClient}
     */
    @GuardedBy("clientInstances")
    public TcpClient getClientInstance() {
        synchronized (clientInstances) {
            if (clientInstances.isEmpty()) throw new HSMServiceException("HSM Instances is empty!");

            // отдаем только активные
            int attempt = 0;
            TcpClient hsmClient = null;
            while (attempt++ <= clientInstances.size()) {
                if (currentInstance.get() >= clientInstances.size()) {
                    currentInstance.set(FIRST_INSTANCE);
                }
                hsmClient = (TcpClient) clientInstances.toArray()[currentInstance.getAndIncrement()];
                try {
                    hsmClient.checkSocket();
                    if (!hsmClient.isClosed()) break;
                    else hsmClient = null;
                } catch (Exception nothing) {
                    hsmClient = null;
                }
            }

            if (hsmClient == null) throw new HSMServiceException("TcpClient for HSM not found!");
            return hsmClient;
        }
    }

    /**
     * Генерация MAC
     *
     * @param message Сообщение
     * @return {@link String} MAC M6
     * @throws HSMServiceException if HSM command has error
     */
    public String generateMAC(byte[] message) throws HSMServiceException {
        String messageLength = StringUtils.leftPad(Integer.toHexString(message.length), 4, "0").toUpperCase();
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        byteArrayBuilder.write(("HSM:M600131FFF" + macKey + messageLength).getBytes());
        byteArrayBuilder.write(message);
        byteArrayBuilder.write("%01".getBytes());

        byte[] request = HSMUtil.createRequest(byteArrayBuilder.toByteArray());
        byte[] response = getClientInstance().send(request);

        if (HSMUtil.hasError(response)) {
            String error = (response.length >= 10)
                    ? new String(Arrays.copyOfRange(response, 8, 10))
                    : "*Empty Answer*";
            throw new HSMServiceException("HSM command M6 has error: " + error);
        }

        return new String(response).substring(10, response.length);
    }

    /**
     * Проверка MAC
     *
     * @param mac     MAC
     * @param message Сообщение
     * @return true/false
     * @throws HSMServiceException if HSM command has error or MAC is empty
     */
    public boolean verifyMAC(String mac, byte[] message) throws HSMServiceException {
        if (mac == null || mac.isEmpty()) {
            throw new HSMServiceException("MAC is empty");
        }

        String messageLength = StringUtils.leftPad(Integer.toHexString(message.length), 4, "0").toUpperCase();
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        byteArrayBuilder.write(("HSM:M800131FFF" + macKey + messageLength).getBytes());
        byteArrayBuilder.write(message);
        byteArrayBuilder.write((mac + "%01").getBytes());

        byte[] request = HSMUtil.createRequest(byteArrayBuilder.toByteArray());
        byte[] response = getClientInstance().send(request);

        if (HSMUtil.hasError(response)) {
            String error = (response.length >= 10)
                    ? new String(Arrays.copyOfRange(response, 8, 10))
                    : "*Empty Answer*";
            throw new HSMServiceException("HSM command M8 has error: " + error + ". MAC: " + mac + ". MAC Message: " + new String(message));
        }

        return true;
    }

    @PostConstruct
    @GuardedBy("clientInstances")
    public void updateHSMInstanceList() {
        if (hsmCluster == null) return;
        synchronized (clientInstances) {
            clientInstances.clear();
            for (String socket : hsmCluster) {
                try {
                    String[] conf = socket.split(":");
                    TcpClient tcpClient = new TcpClient(
                            new InetSocketAddress(conf[0], Integer.valueOf(conf[1]))
                    );
                    if (tcpClient.isEnable()) clientInstances.add(tcpClient);
                } catch (Exception ex) {
                    log.error("Bad HSM config: {}", socket);
                }
            }
        }
    }
}
