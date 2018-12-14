package com.github.shk0da.micro.cryptography.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.main.annotation.ServiceRegistration;
import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.message.CalculateMac;
import com.github.shk0da.micro.main.domain.message.CheckMac;
import com.github.shk0da.micro.main.service.AbstractJmsService;
import com.github.shk0da.micro.main.util.HexBinUtil;

@Service
@KafkaClient
@ServiceRegistration
public class JmsService extends AbstractJmsService {

    @Autowired
    private HSMService hsmService;

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {
            switch (message.getType()) {
                case CHECK_MAC:
                    prepareCheckMacMessage(message);
                    break;
                case CALCULATE_MAC:
                    prepareCalculateMacMessage(message);
                    break;
                default:
                    log.warn("unidentified message: {}", message);
            }
        };
    }

    private void prepareCalculateMacMessage(KafkaMessage message) {
        CalculateMac calculateMac = (CalculateMac) message.getMessage();
        try {
            calculateMac.setMac(hsmService.generateMAC(HexBinUtil.decode(calculateMac.getData())));
        } catch (HSMService.HSMServiceException ex) {
            log.error(ex.getMessage());
        }
        kafkaService.sendMessage(new KafkaMessage.Builder<>().message(calculateMac).id(message.getId()).build());
    }

    private void prepareCheckMacMessage(KafkaMessage message) {
        CheckMac checkMac = (CheckMac) message.getMessage();
        boolean result;
        try {
            result = hsmService.verifyMAC(checkMac.getMac(), HexBinUtil.decode(checkMac.getMessage()));
        } catch (HSMService.HSMServiceException ex) {
            log.error("HSMServiceException: {}. KafkaMessage: {}", ex.getMessage(), message);
            result = false;
        }
        checkMac.setResult(result);
        kafkaService.sendMessage(new KafkaMessage.Builder<>().message(checkMac).id(message.getId()).build());
    }
}
