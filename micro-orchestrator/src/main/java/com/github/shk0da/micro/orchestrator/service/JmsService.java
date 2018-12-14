package com.github.shk0da.micro.orchestrator.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.KafkaMessageService;
import com.github.shk0da.micro.main.domain.message.CalculateMac;
import com.github.shk0da.micro.main.domain.message.CheckMac;
import com.github.shk0da.micro.main.domain.message.LimitCheck;
import com.github.shk0da.micro.main.domain.message.Visa2Message;
import com.github.shk0da.micro.orchestrator.config.ServicesBookConfig;
import com.github.shk0da.micro.main.util.CompletableFutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.orchestrator.config.CacheConfig;
import com.github.shk0da.micro.main.service.AbstractJmsService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@KafkaClient
public class JmsService extends AbstractJmsService {

    @Autowired
    private CacheConfig cacheConfig;

    @Autowired
    private ServicesBookConfig servicesBookConfig;

    @Autowired
    @Qualifier("cachedThreadPoolExecutor")
    private TaskExecutor executor;

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {
            switch (message.getType()) {
                case SERVICE_REGISTRATION:
                    servicesBookConfig.addService((KafkaMessageService) message.getMessage());
                    break;
                case CHECK_MAC:
                    prepareCheckMacMessage(message);
                    break;
                case CALCULATE_MAC:
                    prepareCalculateMacMessage(message);
                    break;
                case VISA2_MESSAGE:
                    try {
                        prepareVisa2Message(message);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case LIMIT_CHECK:
                    prepareLimitCheckMessage(message);
                    break;
                default:
                    log.warn("unidentified message: {}", message);
            }
        };
    }

    private void prepareLimitCheckMessage(KafkaMessage message) {
        final LimitCheck limitCheck = (LimitCheck) message.getMessage();
        // запрос от smartvista
        if (message.getService().getName().equals(ServicesBookConfig.Service.smartvista.getFullName())) {
            CompletableFuture
                    .supplyAsync(() -> {
                        KafkaMessageService service = servicesBookConfig.getService(ServicesBookConfig.Service.limits);
                        if (service != null) {
                            kafkaService.sendMessage(
                                    service.getTopicIn(),
                                    KafkaMessage.fromMessage(limitCheck).id(message.getId()).build()
                            );
                        } else {
                            log.error("Failed get service: {}", ServicesBookConfig.Service.cryptography);
                            return limitCheck;
                        }

                        final AtomicBoolean cancelled = new AtomicBoolean(false);
                        CompletableFuture timeout = CompletableFutureUtil
                                .timeout("No response received from " + ServicesBookConfig.Service.limits)
                                .exceptionally(throwable -> {
                                    cancelled.set(false);
                                    log.error("{}", throwable.getMessage());
                                    return limitCheck;
                                });

                        while (true) {
                            LimitCheck result = (LimitCheck) cacheConfig.getFromReceiverCache(message.getId(), ServicesBookConfig.Service.limits.name());
                            if (result != null) {
                                timeout.cancel(true);
                                limitCheck.setResult(result.isResult());
                                return limitCheck;
                            }
                            if (cancelled.get()) return limitCheck;
                        }
                    }, executor)
                    .thenAcceptAsync(result ->
                            kafkaService.sendMessage(
                                    message.getService().getTopicIn(),
                                    KafkaMessage.fromMessage(result).id(message.getId()).build()
                            )
                    );
        }
        // ответ от limits
        if (message.getService().getName().equals(ServicesBookConfig.Service.limits.getFullName())) {
            cacheConfig.setToReceiverCache(message.getId(), ServicesBookConfig.Service.limits.name(), limitCheck);
        }
    }

    private void prepareCheckMacMessage(KafkaMessage message) {
        CheckMac checkMac = (CheckMac) message.getMessage();
        // ответ от cryptography
        if (message.getService().getName().equals(ServicesBookConfig.Service.cryptography.getFullName())) {
            cacheConfig.setToReceiverCache(message.getId(), ServicesBookConfig.Service.cryptography.name(), checkMac);
        }
    }

    private void prepareCalculateMacMessage(KafkaMessage message) {
        final CalculateMac calculateMac = (CalculateMac) message.getMessage();
        // запрос от visa2
        if (message.getService().getName().equals(ServicesBookConfig.Service.visa2.getFullName())) {
            KafkaMessageService service = servicesBookConfig.getService(ServicesBookConfig.Service.cryptography);
            if (service != null) {
                CompletableFuture.runAsync(() -> {
                    kafkaService.sendMessage(
                            service.getTopicIn(),
                            KafkaMessage.fromMessage(calculateMac).id(message.getId()).build()
                    );

                    final AtomicBoolean cancelled = new AtomicBoolean(false);
                    CompletableFuture timeout = CompletableFutureUtil
                            .timeout("No response received from " + ServicesBookConfig.Service.cryptography)
                            .exceptionally(throwable -> {
                                cancelled.set(false);
                                log.error("{}", throwable.getMessage());
                                return null;
                            });
                    while (true) {
                        CalculateMac result = (CalculateMac) cacheConfig.getFromReceiverCache(message.getId(), ServicesBookConfig.Service.cryptography.name());
                        if (result != null) {
                            timeout.cancel(true);
                            kafkaService.sendMessage(
                                    message.getService().getTopicIn(),
                                    KafkaMessage.fromMessage(result).id(message.getId()).build()
                            );
                            break;
                        }
                        if (cancelled.get()) break;
                    }
                }, executor);
            } else {
                log.error("Failed get service: {}", ServicesBookConfig.Service.cryptography);
            }
        }
        // ответ от cryptography
        if (message.getService().getName().equals(ServicesBookConfig.Service.cryptography.getFullName())) {
            cacheConfig.setToReceiverCache(message.getId(), ServicesBookConfig.Service.cryptography.name(), calculateMac);
        }
    }

    private void prepareVisa2Message(KafkaMessage message) throws InterruptedException, ExecutionException {
        final Visa2Message visa2Message = (Visa2Message) message.getMessage();
        // проверка MAC
        CompletableFuture<Visa2Message> checkMac = CompletableFuture.supplyAsync(() -> {
            KafkaMessageService service = servicesBookConfig.getService(ServicesBookConfig.Service.cryptography);
            if (service != null) {
                kafkaService.sendMessage(
                        service.getTopicIn(),
                        KafkaMessage.fromMessage(visa2Message.getCheckMac()).id(message.getId()).build()
                );
            } else {
                log.error("Failed get service: {}", ServicesBookConfig.Service.cryptography);
                return visa2Message;
            }

            final AtomicBoolean cancelled = new AtomicBoolean(false);
            CompletableFuture timeout = CompletableFutureUtil
                    .timeout("No response received from " + ServicesBookConfig.Service.cryptography)
                    .exceptionally(throwable -> {
                        cancelled.set(false);
                        log.error("{}", throwable.getMessage());
                        return null;
                    });
            while (true) {
                CheckMac result = (CheckMac) cacheConfig.getFromReceiverCache(message.getId(), ServicesBookConfig.Service.cryptography.name());
                if (result != null) {
                    timeout.cancel(true);
                    visa2Message.getCheckMac().setResult(result.getResult());
                    return visa2Message;
                }
                if (cancelled.get()) return visa2Message;
            }
        }, executor);

        // проверка лимитов
        // проверка комисиий
        // проверка ...

        CompletableFuture.allOf(checkMac/*, checkLimits, ...*/).get();
        kafkaService.sendMessage(message.getService().getTopicIn(), KafkaMessage.fromMessage(visa2Message).id(message.getId()).build());
    }
}
