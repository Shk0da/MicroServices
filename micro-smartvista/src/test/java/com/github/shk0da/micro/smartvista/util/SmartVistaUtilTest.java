package com.github.shk0da.micro.smartvista.util;

import com.github.shk0da.micro.smartvista.domain.message.AuthorizationRq;
import com.github.shk0da.micro.smartvista.domain.message.AuthorizationRs;
import com.github.shk0da.micro.smartvista.domain.message.NetworkManagementRq;
import com.github.shk0da.micro.smartvista.domain.message.NetworkManagementRs;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.shk0da.micro.main.util.HexBinUtil;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.DEFAULT_CHARSET;
import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.ResponseCode.SUCCESSFUL_TRANSACTION;
import static com.github.shk0da.micro.smartvista.util.SmartVistaUtil.prepareIsoMessage;

public class SmartVistaUtilTest {

    private final static Logger log = LoggerFactory.getLogger(SmartVistaUtilTest.class);

    private final MessageFactory<IsoMessage> isoMessageFactory = new MessageFactory<>();

    private static AuthorizationRq authorizationRq;
    private static NetworkManagementRq networkManagementRq;

    @Before
    public void setUp() {
        isoMessageFactory.setCharacterEncoding(DEFAULT_CHARSET);
        isoMessageFactory.setUseBinaryBitmap(true);
        try {
            isoMessageFactory.setConfigPath("smartvista.xml");
        } catch (IOException ex) {
            log.error("ISOMessageFactory: ", ex.getMessage());
        }
    }

    @Test
    public void parseIsoMessage1() {
        String authorizationRqHex1 = "00D9313130307AB664010821D200313635343335353337303232303638333435303030303030303" +
                "0303030303030313530303030303030303030303032323039323530393030313037303134363636363134393034343138303" +
                "9323531323030313031383132313830393234353236323131323931303130313531313034343034313030313030303830373" +
                "036343735365054532033323720D090D0B4D0BCD0B8D0BDD0B8D181D182D120202020202020204D696E736B424C303130303" +
                "2373030343039393936343339373800000000000000003030348A020000";
        parseIsoMessageToAuthorizationRq(HexBinUtil.decode(authorizationRqHex1));
    }

    @Test
    public void prepareIsoMessage1() {
        if (authorizationRq == null) return;
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
        assertNotNull(authorizationRs);
        log.debug("{}", authorizationRs);
        byte[] data = prepareIsoMessage(isoMessageFactory, authorizationRs);
        assertNotNull(data);
        log.debug("{}", data);
    }

    @Test
    public void parseIsoMessage2() {
        String authorizationRqHex2 = "00D2313130307AB664010821D000313635343335353337303232303638333435303030303030303" +
                "0303030303030313530303030303030303030303032323039323530393030313030303030303030303134393034343138303" +
                "9323531323030313031383132313830393234353236323131323931303130313531313034343034313030313030303830373" +
                "036343735365054532033323720D090D0B4D0BCD0B8D0BDD0B8D181D182D120202020202020204D696E736B424C303130303" +
                "237303034303939393634333937380000000000000000";
        parseIsoMessageToAuthorizationRq(HexBinUtil.decode(authorizationRqHex2));
    }

    @Test
    public void parseIsoMessage3() {
        try {
            String echo = "0027313830348220000000000000040000000000000030393235303930313136313439303438383031";
            byte[] data = HexBinUtil.decode(echo);
            log.debug("length: {}", HexBinUtil.intFromByteArray(new byte[]{0, 0, data[0], data[1]}));
            log.debug("hex: {}", data);
            log.debug("byte: {}", HexBinUtil.encode(data));
            log.debug("string: {}", new String(data));
            IsoMessage isoMessage = isoMessageFactory.parseMessage(data, SmartVistaUtil.ISO_MESSAGE_HEADER_LENGTH);
            log.debug("isoMessage: {}", Integer.toHexString(isoMessage.getType()));
            assertNotNull(isoMessage);
            NetworkManagementRq networkManagementRq = SmartVistaUtil.parseIsoMessage(isoMessage, new NetworkManagementRq());
            assertNotNull(networkManagementRq);
            log.debug("networkManagementRq: {}", networkManagementRq);
            SmartVistaUtilTest.networkManagementRq = networkManagementRq;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void prepareIsoMessage3() {
        if (networkManagementRq == null) return;
        NetworkManagementRs networkManagementRs = new NetworkManagementRs();
        networkManagementRs.setTransmissionDateTime(networkManagementRq.getTransmissionDateTime());
        networkManagementRs.setSystemsTraceAuditNumber(networkManagementRq.getSystemsTraceAuditNumber());
        networkManagementRs.setNetworkManagementCode(networkManagementRq.getNetworkManagementCode());
        networkManagementRs.setResponseCode(SUCCESSFUL_TRANSACTION.getValue());
        assertNotNull(networkManagementRs);
        log.debug("networkManagementRs: {}", networkManagementRs);
        byte[] data = prepareIsoMessage(isoMessageFactory, networkManagementRs);
        assertNotNull(data);
        log.debug("{}", data);
    }

    private void parseIsoMessageToAuthorizationRq(byte[] data) {
        try {
            log.debug("length: {}", HexBinUtil.intFromByteArray(new byte[]{0, 0, data[0], data[1]}));
            log.debug("hex: {}", HexBinUtil.encode(data));
            log.debug("byte: {}", data);
            log.debug("string: {}", new String(data));
            IsoMessage isoMessage = isoMessageFactory.parseMessage(data, SmartVistaUtil.ISO_MESSAGE_HEADER_LENGTH);
            log.debug("isoMessage: {}", Integer.toHexString(isoMessage.getType()));
            AuthorizationRq authorizationRq = SmartVistaUtil.parseIsoMessage(isoMessage, new AuthorizationRq());
            assertNotNull(authorizationRq);
            log.debug("authorizationRq: {}", authorizationRq);
            SmartVistaUtilTest.authorizationRq = authorizationRq;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}