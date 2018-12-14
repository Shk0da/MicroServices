package com.github.shk0da.micro.visa2.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.message.CalculateMac;
import com.github.shk0da.micro.main.domain.message.CheckMac;
import com.github.shk0da.micro.main.domain.message.Visa2Message;
import com.github.shk0da.micro.visa2.config.CacheConfig;
import com.github.shk0da.micro.visa2.domain.PosGateHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.util.HexBinUtil;
import com.github.shk0da.micro.visa2.domain.MainOperationRq;
import com.github.shk0da.micro.visa2.domain.MainOperationRs;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.shk0da.micro.visa2.util.Visa2Util.*;

@Service
public class Visa2Service {

    private final static Logger log = LoggerFactory.getLogger(Visa2Service.class);

    private final JmsService jmsService;
    private final CacheConfig cacheConfig;

    public Visa2Service(JmsService jmsService, CacheConfig cacheConfig) {
        this.jmsService = jmsService;
        this.cacheConfig = cacheConfig;
    }

    public byte[] processingIncomingMessage(final byte[] data, final AtomicBoolean cancelled) {
        MainOperationRq mainOperationRq;
        try {
            mainOperationRq = parseRequest(data, new MainOperationRq());
            log.debug("mainOperationRq: {}", mainOperationRq);
        } catch (Exception ex) {
            if (ex instanceof PosGatePackageException) {
                mainOperationRq = (MainOperationRq) ((PosGatePackageException) ex).getPosGatePackage();
                if (!OperationType.TYPE_54.getType().equals(mainOperationRq.getTypeOfOperation())) {
                    OperationType type = OperationType.ofType(mainOperationRq.getTypeOfOperation());
                    throw new RuntimeException("Unauthorized operation to the StandIn: " + type.getType() + ", " + type.getDescription());
                }
            }
            throw new RuntimeException("Failed parse incoming message: " + ex.getMessage());
        }

        // формируем ответ
        MainOperationRs.Builder builder = MainOperationRs.builder()
                .posGateHeader(new PosGateHeader(-1, mainOperationRq.getPosGateHeader().getTid()))
                .hostType("Y")
                .storeNumber(mainOperationRq.getStoreNumber())
                .terminalNumber(mainOperationRq.getTerminalNumber())
                .authorizationType(5)
                .requestSequenceNumber(mainOperationRq.getRequestSequenceNumber())
                .responseCode(Code.SUCCESS.getCode())
                .authorizationCode(generateAuthorizationCode())
                .dateOfOperation(getCurrentDateMMDDYY())
                .reserve1(0)
                .referenceNumber(mainOperationRq.getReferenceNumber());

        if (OperationType.TYPE_54.getType().equals(mainOperationRq.getTypeOfOperation())) {
            // запрос на обработку входящего сообщения
            KafkaMessage visa2KafkaMessage = KafkaMessage.fromMessage(
                    new Visa2Message(
                            mainOperationRq.getCurrencyTerminal(),
                            mainOperationRq.getTerminalID(),
                            mainOperationRq.getTypeOfOperation(),
                            mainOperationRq.getAmountTransaction(),
                            mainOperationRq.getSecondTrackCard(),
                            new CheckMac(mainOperationRq.getMac(), HexBinUtil.encode(calculateMac(mainOperationRq).getBytes()))
                    )
            ).build();
            jmsService.send(visa2KafkaMessage);

            // ждем ответа других систем
            Visa2Message visa2Message;
            while (true) {
                visa2Message = (Visa2Message) cacheConfig.getFromReceiverCache(visa2KafkaMessage.getId());
                if (visa2Message != null) break;
                if (cancelled.get()) return null;
            }

            // проверка MAC
            boolean checkMacResult = visa2Message.getCheckMac().getResult();
            if (!checkMacResult) {
                final Code code = Code.MAC_ERROR;
                builder = builder
                        .responseCode(code.getCode())
                        .authorizationCode("000000")
                        .messageToOperator(code.getDescription());
            }

            // проверка лимитов и т.д.
        } else {
            OperationType type = OperationType.ofType(mainOperationRq.getTypeOfOperation());
            log.error("Unauthorized operation to the StandIn: [{}, {}]", type.getType(), type.getDescription());
            final Code code = Code.REFUSAL_ERROR;
            builder = builder
                    .responseCode(code.getCode())
                    .authorizationCode("000000")
                    .messageToOperator(code.getDescription());
        }
        MainOperationRs mainOperationRs = builder.build();

        // запрос на расчет MAC
        KafkaMessage calculateMacKafkaMessage = KafkaMessage.fromMessage(
                new CalculateMac(HexBinUtil.encode(calculateMac(mainOperationRs).getBytes()))
        ).build();
        jmsService.send(calculateMacKafkaMessage);

        // ждем ответа с рассчитанным MAC
        CalculateMac calculateMac;
        while (true) {
            calculateMac = (CalculateMac) cacheConfig.getFromReceiverCache(calculateMacKafkaMessage.getId());
            if (calculateMac != null) break;
            if (cancelled.get()) return null;
        }
        mainOperationRs.setMac(calculateMac.getMac());

        try {
            log.debug("mainOperationRs: {}", mainOperationRs);
            return prepareResponse(mainOperationRs);
        } catch (Exception ex) {
            throw new RuntimeException("Failed prepare outgoing message: " + ex.getMessage());
        }
    }
}
