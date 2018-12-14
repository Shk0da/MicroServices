package com.github.shk0da.micro.smartvista.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.message.LimitCheck;
import com.github.shk0da.micro.main.util.CompletableFutureUtil;
import com.github.shk0da.micro.smartvista.config.CacheConfig;
import com.github.shk0da.micro.smartvista.domain.IsoMessageType;
import com.github.shk0da.micro.smartvista.domain.message.AuthorizationRq;
import com.github.shk0da.micro.smartvista.domain.message.AuthorizationRs;
import com.github.shk0da.micro.smartvista.domain.message.NetworkManagementRq;
import com.github.shk0da.micro.smartvista.domain.message.NetworkManagementRs;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.service.TcpListener;
import com.github.shk0da.micro.smartvista.util.SmartVistaUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.*;
import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.ResponseCode.LIMIT_EXCEEDED;
import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.ResponseCode.SUCCESSFUL_TRANSACTION;

@Service
public class SmartvistaService {

    private final static Logger log = LoggerFactory.getLogger(SmartvistaService.class);

    private final JmsService jmsService;
    private final CacheConfig cacheConfig;
    private final TcpListener tcpListener;
    private final MessageFactory<IsoMessage> isoMessageFactory;

    public SmartvistaService(JmsService jmsService, CacheConfig cacheConfig,
                             @Value("${smartvista.host}") String host, @Value("${smartvista.port}") Integer port) {
        this.jmsService = jmsService;
        this.cacheConfig = cacheConfig;
        tcpListener = new TcpListener(new InetSocketAddress(host, port), this::consumer);
        isoMessageFactory = new MessageFactory<>();
        isoMessageFactory.setCharacterEncoding(DEFAULT_CHARSET);
        isoMessageFactory.setUseBinaryBitmap(true);
        try {
            isoMessageFactory.setConfigPath("smartvista.xml");
        } catch (IOException ex) {
            log.error("ISOMessageFactory: ", ex.getMessage());
        }
    }

    /**
     * Обработчик сообщений для {@link #tcpListener}
     *
     * @param message полученное сообщение byte[]
     */
    private void consumer(final byte[] message) {
        log.debug("consumer: {}", message);
        try {
            IsoMessage isoMessage = isoMessageFactory.parseMessage(message, ISO_MESSAGE_HEADER_LENGTH);
            switch (IsoMessageType.fromType(isoMessage.getType())) {
                case NetworkManagementRq:
                    NetworkManagementRq networkManagementRq = parseIsoMessage(isoMessage, new NetworkManagementRq());
                    log.debug("{}", networkManagementRq);
                    NetworkManagementRs networkManagementRs = new NetworkManagementRs();
                    networkManagementRs.setTransmissionDateTime(networkManagementRq.getTransmissionDateTime());
                    networkManagementRs.setSystemsTraceAuditNumber(networkManagementRq.getSystemsTraceAuditNumber());
                    networkManagementRs.setNetworkManagementCode(networkManagementRq.getNetworkManagementCode());
                    networkManagementRs.setResponseCode(SUCCESSFUL_TRANSACTION.getValue());
                    log.debug("{}", networkManagementRs);
                    tcpListener.send(prepareIsoMessage(isoMessageFactory, networkManagementRs));
                    break;
                case AuthorizationRq:
                    AuthorizationRq authorizationRq = parseIsoMessage(isoMessage, new AuthorizationRq());
                    log.debug("{}", authorizationRq);
                    AuthorizationRs authorizationRs = new AuthorizationRs();
                    authorizationRs.setPrimaryAccountNumber(authorizationRq.getPrimaryAccountNumber());
                    authorizationRs.setProcessingCode(authorizationRq.getProcessingCode());
                    authorizationRs.setTransmissionDate(authorizationRq.getTransmissionDate());
                    authorizationRs.setSystemsTraceAuditNumber(authorizationRq.getSystemsTraceAuditNumber());
                    authorizationRs.setLocalTransactionDate(authorizationRq.getLocalTransactionDate());
                    authorizationRs.setDateExpiration(authorizationRq.getDateExpiration());
                    authorizationRs.setRetrievalReferenceNumber(authorizationRq.getRetrievalReferenceNumber());
                    authorizationRs.setAuthorisationIdentificationResponse(SmartVistaUtil.generateAuthorizationCode());
                    authorizationRs.setResponseCode(SUCCESSFUL_TRANSACTION);
                    authorizationRs.setCardID(authorizationRq.getCardID());
                    authorizationRs.setEmvData(authorizationRq.getEmvData());

                    boolean limitCheck = limitCheckRequest(authorizationRq);
                    if (!limitCheck) {
                        authorizationRs.setAuthorisationIdentificationResponse(DEFAULT_AUTHORIZATION_CODE);
                        authorizationRs.setResponseCode(LIMIT_EXCEEDED);
                    }

                    log.debug("{}", authorizationRs);
                    tcpListener.send(prepareIsoMessage(isoMessageFactory, authorizationRs));
                    break;
                default:
                    throw new IllegalArgumentException("This type of message is not allowed to be processed: " + Integer.toHexString(isoMessage.getType()));
            }
        } catch (Exception ex) {
            log.error("Failed processing incoming message from SmartVista: {}. Message: {}", ex.getMessage(), new String(message, Charset.forName(DEFAULT_CHARSET)));
        }
    }

    /**
     * Запрос на проверку лимитов
     *
     * @param authorizationRq {@link AuthorizationRq}
     * @return boolean
     */
    private boolean limitCheckRequest(final AuthorizationRq authorizationRq) {
        KafkaMessage limitCheckKafkaMessage = new KafkaMessage.Builder<LimitCheck>()
                .message(new LimitCheck(
                        authorizationRq.getPrimaryAccountNumber(),
                        authorizationRq.getDateExpiration(),
                        authorizationRq.getAmountTransaction(),
                        authorizationRq.getAmountAccount(),
                        authorizationRq.getCurrencyCodeTransaction(),
                        authorizationRq.getCurrencyCodeAccount()
                ))
                .build();
        jmsService.send(limitCheckKafkaMessage);

        final AtomicBoolean cancelled = new AtomicBoolean(false);
        CompletableFuture timeout = CompletableFutureUtil
                .timeout("Timeout: limit check request")
                .exceptionally(throwable -> {
                    cancelled.set(false);
                    log.error("{}", throwable.getMessage());
                    return null;
                });

        LimitCheck limitCheck;
        while (true) {
            limitCheck = (LimitCheck) cacheConfig.getFromReceiverCache(limitCheckKafkaMessage.getId());
            if (limitCheck != null) {
                timeout.cancel(true);
                break;
            }
            if (cancelled.get()) return false;
        }
        return limitCheck.isResult();
    }
}
