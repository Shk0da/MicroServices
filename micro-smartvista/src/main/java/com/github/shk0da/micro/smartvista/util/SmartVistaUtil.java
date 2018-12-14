package com.github.shk0da.micro.smartvista.util;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import org.apache.commons.lang3.RandomStringUtils;
import com.github.shk0da.micro.smartvista.domain.*;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public final class SmartVistaUtil {

    private static final SimpleDateFormat MMddHHmmss = new SimpleDateFormat("MMddHHmmss");
    private static final AtomicInteger systemsTraceAuditNumber = new AtomicInteger(0);

    public static final int ISO_MESSAGE_HEADER_LENGTH = 2;
    public static final String DEFAULT_AUTHORIZATION_CODE = "000000";
    public static final String DEFAULT_CHARSET = "Cp1251"; // Default: UTF8

    public enum ResponseCode {
        SUCCESSFUL_TRANSACTION("000", "Successful transaction"),

        // data errors
        FORMAT_ERROR("117", "Format error"),
        DUPLICATE_TRANSACTION("148", "Duplicate transaction"),
        INVALID_TRANSACTION("150", "Invalid transaction"),
        TRANSACTION_COUNTERS_MISMATCH("162", "Transaction counters mismatch"),
        TRANSACTION_DATA_MISMATCH_WITH_EMV_DATA("164", "Transaction data mismatch (with EMV data)"),
        RFU_175("175", "RFU"),
        RFU_192("192", "RFU"),

        // cryptographic errors
        CRYPTOGRAPHIC_FAILURE("204", "Cryptographic failure (commonly that means some problems with HSM)"),
        RFU_212("212", "RFU"),
        RFU_217("217", "RFU"),
        RFU_220("220", "RFU"),
        RFU_221("221", "RFU"),
        RFU_263("263", "RFU"),
        RFU_269("269", "RFU"),
        ONLINE_PIN_PROCESSING_ERROR("275", "Online PIN processing error"),
        ONLINE_PIN_VERIFICATION_FAILURE("280", "Online PIN verification failure"),
        MOBILE_PIN_PROCESSING_ERROR("287", "Mobile PIN processing error"),
        MOBILE_PIN_VERIFICATION_FAILURE("291", "Mobile PIN verification failure"),
        EMV_DATA_PROCESSING_ERROR("298", "EMV data processing error"),
        EMV_DATA_VERIFICATION_FAILURE("299", "EMV data verification failure"),

        // system errors
        SYSTEM_MALFUNCTION("513", "System malfunction"),

        // card restrictions
        BAD_CARD("607", "Bad card"),
        ISSUER_IS_UNKNOWN("608", "Issuer is unknown"),
        SERVICE_NOT_ALLOWED("614", "Service not allowed"),

        // additional checks
        TRANSACTION_NOT_PERMITTED_FOR_CARD("701", "Transaction not permitted for card"),
        LIMIT_EXCEEDED("709", "Limit exceeded (e.g. transaction activity limit)"),
        CANNOT_PROCESS_TRANSACTION("738", "Cannot process transaction"),
        RFU_758("758", "RFU"),
        RFU_762("762", "RFU"),
        RFU_764("764", "RFU");

        private final String value;
        private final String description;

        ResponseCode(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    private SmartVistaUtil() {
    }

    /**
     * @return текущая дата в формате MMddHHmmss
     */
    public static String MMddHHmmss() {
        return MMddHHmmss.format(new Date());
    }

    /**
     * @return Значение из циклического счётчика, в диапазоне 000000–999999
     */
    public static synchronized String getSystemsTraceAuditNumber() {
        int currentCount = systemsTraceAuditNumber.incrementAndGet();
        if (currentCount >= 999999) {
            systemsTraceAuditNumber.set(0);
        }

        return String.format("%06d", currentCount);
    }

    /**
     * Генерация авторизационного кода из шести символов
     *
     * @return AuthorizationCode
     */
    public static String generateAuthorizationCode() {
        String authorizationCode = DEFAULT_AUTHORIZATION_CODE;
        while (authorizationCode.equals(DEFAULT_AUTHORIZATION_CODE)) {
            authorizationCode = RandomStringUtils.random(6, "ACEFGHJKLMNPQRUVWXY1234567980").toUpperCase();
        }
        return authorizationCode;
    }

    /**
     * Подготовка IsoMessage для отправки
     *
     * @param message SmartVistaMessage
     * @return {@link byte[]}
     */
    @SuppressWarnings("unchecked")
    public static byte[] prepareIsoMessage(final MessageFactory<IsoMessage> isoMessageFactory, final Object message) {
        IsoMessageType isoMessageType = message.getClass().getAnnotation(IsoMsg.class).type();
        IsoMessage isoMessage = isoMessageFactory.newMessage(isoMessageType.getType());
        isoMessage.setIsoHeader("");

        Field48 customField48 = new Field48();
        Arrays.stream(message.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(IsoMessageField.class))
                .forEach(field -> {
                    try {
                        boolean isAccessible = field.isAccessible();
                        field.setAccessible(true);
                        Object value = field.get(message);
                        if (value != null) {
                            IsoMessageField isoMessageField = field.getAnnotation(IsoMessageField.class);
                            if (isoMessageField.value().getField() == Field48.FIELD_NUMBER && isoMessageField.value().getSubField() != 0) {
                                int length = isoMessageField.value().getLength();
                                customField48.setValue(
                                        isoMessageField.value(),
                                        new IsoValue<>(isoMessageField.value().getType(), value, length).toString()
                                );
                            } else {
                                isoMessage.setField(isoMessageField.value().getField(), new IsoValue(isoMessageField.value().getType(), value, isoMessageField.value().getLength()));
                            }
                        }
                        field.setAccessible(isAccessible);
                    } catch (Exception ex) {
                        throw new RuntimeException("PrepareIsoMessage: " + ex.getMessage());
                    }
                });

        if (!customField48.getValuesMap().isEmpty()) {
            isoMessage.setField(Field48.FIELD_NUMBER, new IsoValue(IsoType.LLLVAR, customField48.encodeField(customField48)));
        }

        byte[] out = isoMessage.writeData();
        ByteBuffer byteBuffer = ByteBuffer.allocate(out.length + ISO_MESSAGE_HEADER_LENGTH);
        short isoMessageLength = (short) out.length;
        byteBuffer.putShort(isoMessageLength);
        byteBuffer.position(ISO_MESSAGE_HEADER_LENGTH);
        byteBuffer.put(out);

        return byteBuffer.array();
    }

    /**
     * Парсинг IsoMessage
     *
     * @param isoMessage  {@link IsoMessage}
     * @param expectation {@link T}
     * @param <T>         SmartVistaMessage
     * @return SmartVistaMessage
     */
    public static <T> T parseIsoMessage(IsoMessage isoMessage, T expectation) throws Exception {
        String code = expectation.getClass().getAnnotation(IsoMsg.class).type().getCode();
        String processingCode = (isoMessage.hasField(IsoFieldType.FIELD_3.getField()))
                ? isoMessage.getField(IsoFieldType.FIELD_3.getField()).toString()
                : "";
        if (!processingCode.equals(code) && !code.isEmpty()) {
            throw new IllegalArgumentException("Found a discrepancy. ProcessingCode: " + processingCode + ", the expected code: " + code);
        }

        Field48 customField48 = new Field48();
        if (isoMessage.hasField(Field48.FIELD_NUMBER)) {
            customField48.decodeField(String.valueOf(isoMessage.getField(Field48.FIELD_NUMBER).getValue()));
        }

        Arrays.stream(expectation.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(IsoMessageField.class))
                .forEach(field -> {
                    try {
                        Object value;
                        IsoMessageField isoMessageField = field.getAnnotation(IsoMessageField.class);
                        if (isoMessageField.value().getField() == Field48.FIELD_NUMBER && isoMessageField.value().getSubField() != 0) {
                            value = customField48.getValue(isoMessageField.value());
                        } else {
                            value = isoMessage.hasField(isoMessageField.value().getField())
                                    ? isoMessage.getField(isoMessageField.value().getField()).getValue()
                                    : null;
                        }
                        field.setAccessible(true);
                        field.set(expectation, value);
                    } catch (Exception ex) {
                        throw new RuntimeException("ParseResponse: " + ex.getMessage());
                    }
                });

        return expectation;
    }
}
