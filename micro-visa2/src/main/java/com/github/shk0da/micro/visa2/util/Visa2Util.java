package com.github.shk0da.micro.visa2.util;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.github.shk0da.micro.visa2.annotation.Mac;
import com.github.shk0da.micro.visa2.annotation.Visa2Delimiter;
import com.github.shk0da.micro.visa2.annotation.Visa2Field;
import com.github.shk0da.micro.visa2.domain.PosGateHeader;
import com.github.shk0da.micro.visa2.domain.PosGatePackage;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Size;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class Visa2Util {

    private static final SimpleDateFormat MMDDYY = new SimpleDateFormat("MMddyy");

    /**
     * Управляющие символы
     */
    public enum ControlSymbol {
        STX((byte) 0x02, "Обозначает начало пакета"),
        ETX((byte) 0x03, "Обозначает конец пакета"),
        ENQ((byte) 0x05, "Приглашение к передаче"),
        ACK((byte) 0x06, "Подтверждение приема"),
        NAK((byte) 0x15, "Ошибка приема"),
        BEL((byte) 0x07, "Подтверждение приема (аналогично ack)"),
        SYN((byte) 0x16, "«Ждите ответа...»"),
        FS((byte) 0x1C, "Разделитель записей в сообщении"),
        ;

        private byte value;
        private String description;

        ControlSymbol(byte value, String description) {
            this.value = value;
            this.description = description;
        }

        public byte getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Тип операции
     */
    public enum OperationType {
        TYPE_54("54", "оплата покупки, завершение расчета, отмена авторизации"), // <- StandIn операции
        TYPE_55("55", "выдача наличных"),
        TYPE_81("81", "арест средств"),
        TYPE_82("82", "списание арестованных средств"),
        TYPE_83("83", "выдача наличных без карты"),
        TYPE_84("84", "предавторизация, добавочная предавторизация"),
        TYPE_85("85", "безналичный перевод"),
        TYPE_86("86", "предварительное зачисление на карту"),
        TYPE_87("87", "перевод с карты на карту"),
        TYPE_88("88", "подтверждение зачисления на карту"),
        TYPE_8A("8A", "загрузка средств MPAD"),
        TYPE_8B("8B", "подтверждение загрузки MPAD"),
        TYPE_8C("8C", "списание средств с карты при P2P"),
        TYPE_8D("8D", "зачисление средств на карту при P2P"),
        TYPE_94("94", "возврат покупки"),
        TYPE_95("95", "отмена операции"),
        TYPE_9A("9A", "запрос остатка"),
        TYPE_9B("9B", "установка или смена ПИН-кода"),
        TYPE_9C("9C", "блокировка карты"),
        TYPE_9D("9D", "минивыписка (FF04)"),
        TYPE_50("50", "сверка итогов"),
        TYPE_5F("5F", "сверка итогов с одновременной сменой ключей"),
        TYPE_UNKNOWN("null", "неизвестная операция"),
        ;

        private String type;
        private String description;

        OperationType(String type, String description) {
            this.type = type;
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public static OperationType ofType(String type) {
            return Arrays.stream(OperationType.values())
                    .filter(operationType -> operationType.type.equals(String.valueOf(type)))
                    .findFirst().orElse(TYPE_UNKNOWN);
        }
    }

    /**
     * Коды ответов
     */
    public enum Code {
        SUCCESS(0x00, "Одобрено"),
        CALL_TO_BANK(0x01, "Позвоните в банк"),
        CALL_TO_BANK_SUCCESS(0x02, "Позвоните в банк, узнать об успешности авторизации"),
        INVALID_TERMINAL(0x03, "Не верный терминал"),
        WITHDRAW_CARD(0x04, "Изымите карту"),
        REFUSAL_ERROR(0x05, "Отказано"),
        COMMON_MISTAKE(0x06, "Общая ошибка"),
        CALL_TO_AMEX(10, "Звоните в Amex"),
        TRANSACTION_INVALID(12, "Транзакция неверна"),
        AMOUNT_NOT_CORRECT(13, "Сумма не верна"),
        CARD_NOT_VALID(14, "Карта не верна"),
        ISSUER_WRONG(15, "Эмитент ошибочный"),
        REPEAT(19, "Повторите"),
        TRY_AGAIN_LATER(31, "Повторите позже"),
        NO_CREDIT_SCORE(39, "Нет кредитного счёта"),
        CARD_NO_COUNTED(48, "Карта не посчитана"),
        VERSION_IS_DEPRECATED(49, "Версия устарела"),
        INSUFFICIENT_FUNDS(51, "Недостаточно средств"),
        ACCOUNT_IS_INCORRECT(52, "Счёт неверен"),
        CARD_EXPIRED(54, "Карта просрочена"),
        INVALID_PIN(55, "Неверный ПИН"),
        PIN_BLOCKED(75, "ПИН заблокирован"),
        TRANSACTION_NOT_PERMITTED_CUSTOMER(57, "Транзакция неразрешена клиенту"),
        TRANSACTION_NOT_PERMITTED_TERMINAL(58, "Транзакция неразрешена терминалу"),
        LIMIT_OF_FUNDS_EXHAUSTED(61, "Исчерпан лимит средств"),
        CARD_IS_LIMITED(62, "Карта ограничена"),
        TRANSACTION_IS_ILLEGAL(63, "Транзакция незаконна"),
        NO_INITIAL_OPERATION(76, "Нет исходной операции"),
        ALREADY_RETURNED(0x00, "Уже возвращено"),
        INVALID_DATE(80, "Неверная дата"),
        PIN_IS_REQUIRED(83, "Требуется ПИН"),
        PARTIALLY_APPROVED(85, "Частично одобрено"),
        PIN_CASHIER_ERROR(86, "Ошибка ПИН кассира"),
        PIN_NOT_REQUIRED(87, "ПИН нетребуется"),
        MERCHANT_IS_INCORRECT(89, "Мерчант неверен"),
        CONTACT_INFORMATION_IS_INCORRECT(90, "Контактная информация неверна"),
        ISSUER_IS_NOT_AVAILABLE(91, "Эмитент недоступен"),
        ROUTE_IS_INCORRECT(92, "Маршрут неверен"),
        TRANSACTION_IS_NOT_ALLOWED(93, "Транзакция запрещена"),
        TRANSACTION_IS_INVALID(94, "Транзакция неверна"),
        SYSTEM_ERROR(96, "Системная ошибка"),
        MAC_ERROR(98, "МАК код неверен"),
        FORMAT_ERROR(99, "Ошибка формата"),
        ERROR_TILDA(0x06, "~"),
        QUERY_LIMIT_EXCEEDED(31, "Превышен лимит запросов"),
        POS_HUB_ERROR(96, "Ошибка системы на POS-концентраторе"),
        ERROR_SENDING_TO_HOST(0x06, "Ошибка отправления на хост"),
        DAY_ALREADY_CLOSED(0x00, "День уже закрыт"),
        OPERATION_ALREADY_CARRIED(0x00, "Операция уже была проведена"),
        AUTHORIZATION_REQUEST_NOT_FOUND(12, "Не найден авторизационный запрос"),
        LIMIT_EXCEEDED_CONFIRMATION(0x05, "Превышен лимит времени на подтверждение"),
        ORIGINAL_NOT_FOUND(0x00, "Оригинал не найден"),
        PACKET_TRANSFER(0x06, "Передача пакета"),
        HSM_TIMEOUT(96, "Тайм-аут ХСМ"),
        DB_TIMEOUT(96, "Тайм-аут базы данных"),
        DB_RESP_CODE_ERROR(96, "В БД нет значения resp_code"),
        TERMINAL_OFF(0x03, "Терминал выключен"),
        NO_MAC_KEY(98, "Нет МАК-ключа"),
        TRANSACTION_NOT_SUPPORTED(99, "Транзакция не поддерживается"),
        CODE_UNKNOWN(null, "неизвестный код");

        @Size(max = 2)
        private Integer code;
        @Size(max = 16)
        private String description;

        Code(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Code ofCode(Integer code) {
            return Arrays.stream(Code.values())
                    .filter(it -> it.code.equals(code))
                    .findFirst().orElse(CODE_UNKNOWN);
        }
    }

    /**
     * В случае, если выполняется операция «оплата покупки», «выдача наличных» или «предавторизация» по международной карте с чипом,
     * то поле «Дополнительный блок» должно содержать следующие обязательные элементы данных
     */
    public enum AdditionalBlock {
        /* Запрос */
        ARQC_TC("ARQC/TC", "Authorization cryptogram", "9F26", 8),
        CID("CID", "Cryptogram information data", "9F27", 1),
        IAPPD("IAPPD", "Issuer application data", "9F10", 8),
        ATC("ATC", "Application transaction counter", "9F36", 2),
        AIP("AIP", "Application interchange profile", "82", 2),
        UN("UN", "Unpredictable number", "9F37", 4),
        TVR("TVR", "Terminal verification results", "95", 5),
        TD("TD", "Transaction date", "9A", 3),
        TT("TT", "Transaction type", "9С", 1),
        AA("AA", "Amount, authorized", "9F02", 6),
        TCUR("TCUR", "Transaction currency code", "5F2A", 2),
        TCNT("TCNT", "Terminal country code", "9F1A", 2),
        PSN("PSN", "PAN sequence number", "5F34", 1),
        TCAP("TCAP", "Terminal capabilities", "9F33", 3),
        CVR("TCAP", "Cardholder verification result", "9F34", 3),
        FORM("FORM", "Form factor", "9F6E", 3),

        /* Ответ */
        IAUTD("IAUTD", "Issuer authentication data", "91", Short.MAX_VALUE),
        FF01("FF01", "Чиповые данные EMV", "FF01", Short.MAX_VALUE),
        FF02("FF02", "Шаблон дополнительной информации по текущей транзакции", "FF02", Short.MAX_VALUE),
        DF0D("DF0D", "Таблица номеров проданных билетов", "DF0D", Short.MAX_VALUE),
        DF0E("DF0E", "Код CVC2 при ручном вводе номера карты", "DF0E", 3),
        DF0F("DF0F", "Тип сервисной операции по карте", "DF0F", 1),
        DF10("DF10", "Номер карты клиента (пополнение счета)", "DF10", 10),
        DF11("DF11", "Номер карты получателя (перевод средств)", "DF11", 10),
        DF13("DF13", "Тип безналичного платежа", "DF13", 1),
        DF14("DF14", "Сумма окончательного расчета", "DF14", 4),
        DF15("DF15", "Вид операции с ручным вводом номера карты", "DF15", 1),
        DF16("DF16", "Контрольное слово клиента", "DF16", 6),
        DF19("DF19", "Сумма взятой комиссии", "DF19", 4),
        DF71("DF71", "Номер RRN, сформированный терминалом, и используемый им для подтверждения предавторизаций и отмен", "DF71", 12),
        DF72("DF72", "Идентификатор транзакции VISA", "DF72", 40),
        DF73("DF73", "Mastercard SLEV", "DF73", 3),
        DF1A("DF1A", "Одноразовое идентификационное значение клиента", "DF1A", 40),
        DF70("DF70", "0x01 – платеж является рекуррентным. Любое другое значение или отсутствие тега DF70 означают, что платеж не является рекуррентным", "DF70", 1),
        DF75("DF75", "Visa Transaction Identifier", "DF75", 8),
        FF0A("FF0A", "Составной тег, содержащий информацию о характере P2P перевода", "FF0A", Short.MAX_VALUE),
        FF0A_DF63("DF63", "Текстовое сообщение получателю", "DF63", 19),
        FF0A_DF65("DF65", "Источник средств", "DF65", 1),
        FF0A_DF67("DF65", "Цель перевода", "DF65", 1),
        FF08("FF08", "Составной тег, содержащий информацию о получателе P2P перевода", "FF08", Short.MAX_VALUE),
        FF09("FF09", "Составной тег, содержащий информацию об отправителе P2P перевода", "FF09", Short.MAX_VALUE),
        FF09_DF50("DF50", "Имя", "DF50", 30),
        FF09_DF51("DF51", "Отчество", "DF51", 30),
        FF09_DF52("DF52", "Фамилия", "DF52", 30),
        FF09_DF53("DF53", "Адрес (улица, дом)", "DF53", 30),
        FF09_DF54("DF54", "Адрес (город)", "DF54", 25),
        FF09_DF55("DF55", "Адрес (штат/провинция)", "DF54", 2),
        FF09_DF56("DF56", "Адрес (код страны)", "DF56", 3),
        FF09_DF57("DF57", "Адрес (индекс)", "DF57", 10),
        FF09_DF58("DF58", "Номер телефона", "DF58", 20),
        FF09_DF59("DF59", "Дата рождения", "DF59", 4),
        FF09_DF5A("DF5A", "Номер счета/карты", "DF5A", 20),
        FF09_DF5B("DF5B", "Тип документа", "DF5A", 1),
        FF09_DF5C("DF5C", "Номер документа", "DF5C", 20),
        FF09_DF5E("DF5E", "Код страны, выдавшей документ", "DF5E", 3),
        FF09_DF60("DF60", "Срок действия документа", "DF60", 4),
        FF09_DF61("DF61", "Текущее гражданство", "DF61", 3),
        FF09_DF62("DF62", "Код страны рождения", "DF62", 3),
        FF04("FF04", "История операций по карте клиента", "FF04", Short.MAX_VALUE),
        FF05("FF05", "Описание одной операции по карте клиент", "FF05", Short.MAX_VALUE),
        FF05_9A("9A", "Дата операции YYMMDD", "9A", 3),
        FF05_9F21("9F21", "Время операции hhmmss", "9F21", 3),
        FF05_9C("9C", "Тип транзакции", "9C", 1),
        FF05_9F02("9F02", "Сумма операции", "9F02", 6),
        FF05_5F2A("5F2A", "Код валюты", "5F2A", 2),
        FF05_9F4E("9F4E", "Название мерчанта", "9F4E", Short.MAX_VALUE),
        FF05_DF1B("DF1B", "Флаг отмены", "DF1B", 1),
        FF06("FF06", "Детокенизированный номер карты", "FF06", Short.MAX_VALUE),
        FF06_DF76("DF76", "Номер карты клиента", "DF76", 10),
        FF06_DF74("DF74", "Срок действия карты клиента", "DF74", 2),
        FF03("FF03", "«Почта» от терминала / к терминалу", "FF03", Short.MAX_VALUE),
        ;

        private String designation;
        private String name;
        private String tag;
        private int length;

        AdditionalBlock(String designation, String name, String tag, int length) {
            this.designation = designation;
            this.name = name;
            this.tag = tag;
            this.length = length;
        }

        public String getDesignation() {
            return designation;
        }

        public String getName() {
            return name;
        }

        public String getTag() {
            return tag;
        }

        public int getLength() {
            return length;
        }
    }

    public static class PosGatePackageException extends RuntimeException {

        private PosGatePackage posGatePackage;

        public PosGatePackageException() {
        }

        public PosGatePackageException(String message) {
            super(message);
        }

        public PosGatePackageException(String message, PosGatePackage posGatePackage) {
            super(message);
            this.posGatePackage = posGatePackage;
        }

        public PosGatePackage getPosGatePackage() {
            return posGatePackage;
        }
    }

    /**
     * Таблица для транслитирования сообщений
     * {@link #transliterateMessage}
     */
    private static final Map<Character, Character> cyrillicToLatin = new HashMap<Character, Character>() {{
        put('А', 'A');
        put('Б', 'b');
        put('В', 'B');
        put('Г', 'g');
        put('Д', 'd');
        put('Е', 'E');
        put('Ж', 'z');
        put('З', 's');
        put('И', 'i');
        put('Й', 'j');
        put('К', 'K');
        put('Л', 'l');
        put('М', 'M');
        put('Н', 'H');
        put('О', 'O');
        put('П', 'p');
        put('Р', 'P');
        put('С', 'C');
        put('Т', 'T');
        put('У', 'y');
        put('Ф', 'f');
        put('Х', 'X');
        put('Ц', 'c');
        put('Ч', 'q');
        put('Ш', 'h');
        put('Щ', 'x');
        put('Ъ', 'm');
        put('Ы', 'o');
        put('Ь', 'n');
        put('Э', 'u');
        put('Ю', 'w');
        put('Я', 'v');
        put(' ', ' ');
        put('.', '.');
    }};

    private Visa2Util() {
    }

    /**
     * Подготовка ответа для PosGate
     *
     * @param response объект для ответа
     * @param <T>      extends PosGatePackage
     * @return пакет для PosGate с {@link PosGateHeader}
     * @throws Exception в случае не возможности подготовить пакет
     */
    public static <T extends PosGatePackage> byte[] prepareResponse(T response) throws Exception {
        // prepare body
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        Arrays.stream(response.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Visa2Field.class))
                .forEach(field -> {
                    Visa2Field packetField = field.getAnnotation(Visa2Field.class);
                    try {
                        boolean isAccessible = field.isAccessible();
                        field.setAccessible(true);
                        Object value = field.get(response);
                        if (value != null) {
                            byte[] valueData = new byte[packetField.length()];
                            System.arraycopy(
                                    StringUtils.leftPad(String.valueOf(value), valueData.length, "0").getBytes(),
                                    0, valueData, 0, valueData.length
                            );
                            byteArrayBuilder.write(valueData);
                        }
                        field.setAccessible(isAccessible);
                        // разделитель если необходимо
                        if (field.isAnnotationPresent(Visa2Delimiter.class)) {
                            byteArrayBuilder.write(field.getAnnotation(Visa2Delimiter.class).value());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalArgumentException("Failed parse field (" + packetField.description() + "): " + e);
                    }
                });

        // prepare header
        byte[] header = new byte[12];
        System.arraycopy(StringUtils.leftPad(String.valueOf((byteArrayBuilder.size() + 8)), 4, "0").getBytes(), 0, header, 0, 4);
        System.arraycopy(StringUtils.leftPad(Optional.ofNullable(response.getPosGateHeader().getTid()).orElse(""), 8, "0").getBytes(), 0, header, 4, 8);

        // assembly of a package
        byte[] pckg = new byte[byteArrayBuilder.size() + header.length];
        System.arraycopy(header, 0, pckg, 0, header.length);
        System.arraycopy(byteArrayBuilder.toByteArray(), 0, pckg, 12, byteArrayBuilder.size());

        return pckg;
    }

    /**
     * Парсинг запроса от PosGate
     *
     * @param data    данные
     * @param request объект для заполнения
     * @param <T>     тип ожидаемого запроса
     * @return заполненный объект ожидаемого типа
     * @throws PosGatePackageException в случае не возможности распарсить пакет
     */
    public static <T extends PosGatePackage> T parseRequest(byte[] data, T request) throws PosGatePackageException {
        // обработка заголока
        final byte[] packetLength = new byte[4];
        System.arraycopy(data, 0, packetLength, 0, packetLength.length);
        final byte[] tid = new byte[8];
        System.arraycopy(data, 4, tid, 0, tid.length);
        PosGateHeader posGateHeader = new PosGateHeader(Integer.valueOf(new String(packetLength)), new String(tid));
        request.setPosGateHeader(posGateHeader);

        // обработка тела
        final byte[] packet = new byte[data.length - 12];
        System.arraycopy(data, 12, packet, 0, packet.length);
        final AtomicInteger cursor = new AtomicInteger(0);
        Arrays.stream(request.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Visa2Field.class))
                .forEach(field -> {
                    Visa2Field packetField = field.getAnnotation(Visa2Field.class);
                    try {
                        int position = 0;
                        boolean delimiterFound = false;
                        byte[] valueData = new byte[packetField.length()];
                        while (position < valueData.length) {
                            byte val = packet[cursor.get() + position];
                            if (val == packetField.delimiter()) {
                                delimiterFound = true;
                                break;
                            }
                            valueData[position++] = val;
                        }
                        boolean nextIsDelimiter = packet[cursor.get() + position + 1] == packetField.delimiter();
                        cursor.set(cursor.get() + (delimiterFound || nextIsDelimiter ? position + 1 : position));

                        byte[] value = new byte[position];
                        System.arraycopy(valueData, 0, value, 0, value.length);
                        boolean isAccessible = field.isAccessible();
                        field.setAccessible(true);
                        switch (packetField.type()) {
                            case MIX:
                            case STRING:
                                field.set(request, position == 0 ? null : new String(value));
                                break;
                            case INTEGER:
                                field.set(request, position == 0 ? null : Integer.valueOf(new String(value)));
                                break;
                            case LONG:
                                field.set(request, position == 0 ? null : Long.valueOf(new String(value)));
                                break;
                            default:
                                throw new IllegalArgumentException("no field type defined");
                        }
                        field.setAccessible(isAccessible);
                    } catch (Exception e) {
                        throw new PosGatePackageException("Failed parse field (" + packetField.description() + "): " + e.getMessage(), request);
                    }
                });

        return request;
    }

    /**
     * Расчет MAC для HSM
     *
     * @return calculate Mac
     * @throws RuntimeException при невозможности получить поле @Mac
     */
    public static String calculateMac(Object object) throws RuntimeException {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Mac.class))
                .map(field -> {
                    try {
                        boolean isAccessible = field.isAccessible();
                        field.setAccessible(true);
                        Object value = field.get(object);
                        String macFieldValue = value != null ? String.valueOf(value) : "";
                        field.setAccessible(isAccessible);
                        return macFieldValue;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed get MAC field: " + e.getMessage());
                    }
                })
                .collect(Collectors.joining());
    }

    /**
     * Транслитерация сообщения по таблице: {@link #cyrillicToLatin}
     *
     * @param message исходное сообщение
     * @return обработанное сообщение
     */
    public static String transliterateMessage(final String message) {
        StringBuilder builder = new StringBuilder();
        for (char symbol : message.toUpperCase().toCharArray()) {
            if (cyrillicToLatin.containsKey(symbol)) {
                builder.append(String.valueOf(cyrillicToLatin.get(symbol)));
            }
        }
        return builder.toString();
    }

    /**
     * Текущая дата в формате MMDDYY
     *
     * @return текущая дата
     */
    public static String getCurrentDateMMDDYY() {
        return MMDDYY.format(new Date());
    }

    /**
     * Генерация авторизационного кода из шести символов
     *
     * @return AuthorizationCode
     */
    public static String generateAuthorizationCode() {
        String authorizationCode = "000000";
        while (authorizationCode.equals("000000")) {
            authorizationCode = RandomStringUtils.random(6, "ACEFGHJKLMNPQRUVWXY1234567980").toUpperCase();
        }
        return authorizationCode;
    }
}
