package com.github.shk0da.micro.visa2.domain;

import com.github.shk0da.micro.visa2.annotation.Mac;
import com.github.shk0da.micro.visa2.annotation.Visa2Field;
import com.github.shk0da.micro.visa2.util.Visa2Util;
import org.springframework.lang.Nullable;

/**
 * Запрос на основные операции:
 * «оплата покупки», «выдача наличных», «возврат покупки», «отмена последней операции», «предавторизация»,
 * «завершение расчета», «запрос остатка на счете», «безналичный перевод»
 */
public class MainOperationRq implements PosGatePackage {

    private PosGateHeader posGateHeader;

    /**
     * “J0.”
     */
    @Visa2Field(length = 3, type = Visa2Field.Type.STRING, description = "Заголовок пакета")
    private String packetHeader;

    /**
     * Не используется. ‘0’
     */
    @Mac
    @Visa2Field(length = 1, type = Visa2Field.Type.INTEGER, description = "Индекс ключа")
    private Integer keyIndex;

    /**
     * ‘00000’ .. ‘99999’
     */
    @Mac
    @Visa2Field(length = 5, type = Visa2Field.Type.INTEGER, description = "Случайное число")
    private Integer randomNumber;

    /**
     * Загружается в настройки терминала
     */
    @Mac
    @Visa2Field(length = 12, description = "Номер мерчанта")
    private String merchantNumber;

    /**
     * Загружается в настройки терминала
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.STRING, description = "Первая часть номера терминала")
    private String storeNumber;

    /**
     * Загружается в настройки терминала
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.STRING, description = "Вторая часть номера терминала")
    private String terminalNumber;

    /**
     * Загружается в настройки терминала
     */
    @Visa2Field(length = 4, type = Visa2Field.Type.INTEGER, description = "Категория торговой точки")
    private Integer outletCategory;

    /**
     * Загружается в настройки терминала (“643”,”840”, “978”)
     */
    @Visa2Field(length = 3, type = Visa2Field.Type.INTEGER, description = "Валюта терминала")
    private Integer currencyTerminal;

    /**
     * ‘H’ – поддерживается многовалютность и информация о лимитах
     * ‘J’ – поддерживается отрицательный баланс в ответе
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Опции, поддерживаемые терминалом")
    private String supportedOptions;

    /**
     * ‘0’ – нет
     * ‘1’ – есть
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Наличие бесконтактного ридера у терминала")
    private String hasContactlessReader;

    /**
     * “123”  - соответствует версии 1.2.3.
     * Первый символ содержит версию, второй – релиз, третий – билд. Символ может превышать значение ‘9’ (т.е. 0х39).
     * Чтобы получить значение версии, релиза или билда, нужно из символа вычесть код 0x30. Например, символ ‘9’
     * в первой позиции означает версию 9.х.х, а символ ‘ ; ‘ означает версию 11.х.х, поскольку он имеет код 0x3B,
     * а 0x3B – 0x30 = 0x0B = 11.
     */
    @Visa2Field(length = 3, type = Visa2Field.Type.STRING, description = "Версия ПО терминала")
    private String version;

    /**
     * Присваивается Сбербанком (“A” – “Z”)
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Модель терминала")
    private String model;

    /**
     * “00”
     */
    @Visa2Field(length = 2, type = Visa2Field.Type.STRING, description = "Резерв 1")
    private String reserve1;

    /**
     * {@link Visa2Util.OperationType}
     */
    @Mac
    @Visa2Field(length = 2, type = Visa2Field.Type.STRING, description = "Тип операции")
    private String typeOfOperation;

    /**
     * Загружается в настройки терминала
     */
    @Mac
    @Visa2Field(length = 8, type = Visa2Field.Type.STRING, description = "Идентификатор терминала")
    private String terminalID;

    /**
     * ‘Y’
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Тип кодировки номера карты")
    private String typeEncodingCardNumber;

    /**
     * “0001” – “9999”
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.INTEGER, description = "Порядковый номер запроса")
    private Integer requestSequenceNumber;

    /**
     * ‘@’ – по подписи
     * ’S’ – по ПИНу
     * ‘N’ – верификация держателя не требуется
     * ‘5’ – держатель верифицирован по 3DSec1
     * ‘6’ – была попытка  3DSec1-верификации, не завершившаяся успехом
     * ‘7’ – интернет-оплата, 3DSec1-верификация не выполнялась
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Способ идентификации держателя карты")
    private String methodIdentificationCardHolder;

    /***
     * ‘D’ – стандартное чтение 2-й дорожки через магнитный ридер
     * ‘F’ – аналогично ‘D’, но предварительно была попытка использовать чип (fallback)
     * ‘M’ – номер карты введен вручную с клавиатуры терминала
     * ‘C’ – прочитан чип
     * ‘R’ – бесконтактная карта в режиме MagStripe
     * ‘E’ – бесконтактная карта в режиме EMV
     * ‘A’ – режим Credential in file
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Способ чтения карты")
    private String howToReadCard;

    /**
     * PAN=YYMMtttt...t     ,
     * где YYMM – срок действия карты
     */
    @Mac
    @Visa2Field(length = 37, type = Visa2Field.Type.MIX, description = "Вторая дорожка карты")
    private String secondTrackCard;

    /**
     * ПИН-блок в кодировке ASCII. Может отсутствовать.
     */
    @Nullable
    @Visa2Field(length = 16, type = Visa2Field.Type.MIX, description = "ПИН-блок")
    private String pinBlock;

    /**
     * Сумма в копейках или центах, без точки
     */
    @Mac
    @Visa2Field(length = 12, type = Visa2Field.Type.LONG, description = "Сумма операции")
    private Long amountTransaction;

    /**
     * Закодированный по системе Base64 блок данных в формате TLV). Может отсутствовать.
     * Преобразование двоичного блока данных в формат Base64 (RFC 1113) осуществляется следующим образом.
     * Блок данных рассматривается как непрерывная последовательность бит.
     * Каждые 6 бит преобразуются в 1 печатный символ по таблице:
     * Значение шести бит	00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
     * Печатный символ	    A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P
     * Значение шести бит	10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F
     * Печатный символ	    Q  R  S  T  U  V  W  X  Y  Z  a  b  c  d  e  f
     * Значение шести бит	20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F
     * Печатный символ	    g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v
     * Значение шести бит	30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
     * Печатный символ	    w  x  y  z  0  1  2  3  4  5  6  7  8  9  +  /
     */
    @Nullable
    @Visa2Field(length = 256, type = Visa2Field.Type.MIX, description = "Дополнительный блок")
    private String additionalBlock;

    /**
     * Присутствует в запросах «завершение расчета», «добавочная предавторизация»,  «подтверждение взноса наличных»
     * и «списание арестованных средств».
     */
    @Mac
    @Visa2Field(length = 12, type = Visa2Field.Type.LONG, description = "Номер ссылки")
    private Long referenceNumber;

    /**
     * “000”
     */
    @Visa2Field(length = 3, type = Visa2Field.Type.INTEGER, description = "Резерв 2")
    private Integer reserve2;

    /**
     * MAC-код в кодировке ASCII. Может отсутствовать.
     */
    @Nullable
    @Visa2Field(length = 16, type = Visa2Field.Type.MIX, description = "MAC")
    private String mac;

    @Override
    public void setPosGateHeader(PosGateHeader posGateHeader) {
        this.posGateHeader = posGateHeader;
    }

    @Override
    public PosGateHeader getPosGateHeader() {
        return posGateHeader;
    }

    public String getPacketHeader() {
        return packetHeader;
    }

    public Integer getKeyIndex() {
        return keyIndex;
    }

    public Integer getRandomNumber() {
        return randomNumber;
    }

    public String getMerchantNumber() {
        return merchantNumber;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public Integer getOutletCategory() {
        return outletCategory;
    }

    public Integer getCurrencyTerminal() {
        return currencyTerminal;
    }

    public String getSupportedOptions() {
        return supportedOptions;
    }

    public String getHasContactlessReader() {
        return hasContactlessReader;
    }

    public String getVersion() {
        return version;
    }

    public String getModel() {
        return model;
    }

    public String getReserve1() {
        return reserve1;
    }

    public String getTypeOfOperation() {
        return typeOfOperation;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public String getTypeEncodingCardNumber() {
        return typeEncodingCardNumber;
    }

    public Integer getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public String getMethodIdentificationCardHolder() {
        return methodIdentificationCardHolder;
    }

    public String getHowToReadCard() {
        return howToReadCard;
    }

    public String getSecondTrackCard() {
        return secondTrackCard;
    }

    public String getPinBlock() {
        return pinBlock;
    }

    public Long getAmountTransaction() {
        return amountTransaction;
    }

    public String getAdditionalBlock() {
        return additionalBlock;
    }

    public Long getReferenceNumber() {
        return referenceNumber;
    }

    public Integer getReserve2() {
        return reserve2;
    }

    public String getMac() {
        return mac;
    }

    @Override
    public String toString() {
        return "MainOperationRq{" +
                "posGateHeader=" + posGateHeader +
                ", packetHeader='" + packetHeader + '\'' +
                ", keyIndex=" + keyIndex +
                ", randomNumber=" + randomNumber +
                ", merchantNumber='" + merchantNumber + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", terminalNumber='" + terminalNumber + '\'' +
                ", outletCategory=" + outletCategory +
                ", currencyTerminal=" + currencyTerminal +
                ", supportedOptions='" + supportedOptions + '\'' +
                ", hasContactlessReader='" + hasContactlessReader + '\'' +
                ", version='" + version + '\'' +
                ", model='" + model + '\'' +
                ", reserve1='" + reserve1 + '\'' +
                ", typeOfOperation='" + typeOfOperation + '\'' +
                ", terminalID='" + terminalID + '\'' +
                ", typeEncodingCardNumber='" + typeEncodingCardNumber + '\'' +
                ", requestSequenceNumber=" + requestSequenceNumber +
                ", methodIdentificationCardHolder='" + methodIdentificationCardHolder + '\'' +
                ", howToReadCard='" + howToReadCard + '\'' +
                ", secondTrackCard='" + secondTrackCard + '\'' +
                ", pinBlock='" + pinBlock + '\'' +
                ", amountTransaction=" + amountTransaction +
                ", additionalBlock='" + additionalBlock + '\'' +
                ", referenceNumber=" + referenceNumber +
                ", reserve2=" + reserve2 +
                ", mac='" + mac + '\'' +
                '}';
    }
}
