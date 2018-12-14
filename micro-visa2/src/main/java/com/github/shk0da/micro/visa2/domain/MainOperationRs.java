package com.github.shk0da.micro.visa2.domain;

import com.github.shk0da.micro.visa2.annotation.Mac;
import com.github.shk0da.micro.visa2.annotation.Visa2Delimiter;
import com.github.shk0da.micro.visa2.annotation.Visa2Field;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import com.github.shk0da.micro.visa2.util.Visa2Util;

/**
 * Ответ для {@link MainOperationRq}
 */
public class MainOperationRs implements PosGatePackage {

    private PosGateHeader posGateHeader;

    /**
     * Не используется. Всегда “Y”
     */
    @Visa2Field(length = 1, type = Visa2Field.Type.STRING, description = "Тип хоста")
    private String hostType;

    /**
     * Возвращается из запроса неизменным
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.STRING, description = "Первая часть номера терминала")
    private String storeNumber;

    /**
     * Возвращается из запроса неизменным
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.STRING, description = "Вторая часть номера терминала")
    private String terminalNumber;

    /**
     * Не используется. Всегда “5”
     */
    @Mac
    @Visa2Field(length = 1, type = Visa2Field.Type.INTEGER, description = "Вид авторизации")
    private Integer authorizationType;

    /**
     * Возвращается из запроса неизменным
     */
    @Mac
    @Visa2Field(length = 4, type = Visa2Field.Type.INTEGER, description = "Порядковый номер запроса")
    private Integer requestSequenceNumber;

    /**
     * «00» – успешно, другие коды - ошибка
     * {@link Visa2Util.Code#code}
     */
    @Mac
    @Visa2Field(length = 2, type = Visa2Field.Type.INTEGER, description = "Код ответа")
    private Integer responseCode;

    /**
     * Если код ответа не равен «00», это поле имеет значение «000000».
     * Для запроса остатка на счете при успешном выполнении первые 3 позиции содержат код валюты клиентского счета (‘810’,’840’,’978’).
     */
    @Mac
    @Visa2Field(length = 6, type = Visa2Field.Type.MIX, description = "Код авторизации")
    private String authorizationCode;

    /**
     * Формат: ММДДГГ
     */
    @Mac
    @Visa2Field(length = 6, type = Visa2Field.Type.STRING, description = "Дата операции")
    private String dateOfOperation;

    /**
     * Представлено в специальной кодировке латиницей. {@link Visa2Util#cyrillicToLatin}
     * Для запроса остатка на счете при успешном выполнении содержит состояние клиентского счета.
     */
    @Mac
    @Visa2Field(length = 16, type = Visa2Field.Type.STRING, description = "Сообщение оператору")
    private String messageToOperator;

    /**
     * “0”
     * Вниание! @Mac обрабатывается при условии, если присутвует следующее поле: Номер ссылки (RRN) {@link #referenceNumber}!
     */
    @Mac
    @Visa2Field(length = 1, type = Visa2Field.Type.INTEGER, description = "Резерв 1")
    private Integer reserve1;

    /**
     * Возвращается хостом для операций «предавторизация», «арест средств», «взнос наличных», «оплата покупки»,
     * «выдача наличных», «перевод с карты на карту», «безналичный перевод» и любых других финансовых операций
     * изменяющих итоги терминала (кроме «отмены операции»)
     * Для других типов операции отсутствует.
     */
    @Mac
    @Nullable
    @Visa2Delimiter
    @Visa2Field(length = 12, type = Visa2Field.Type.LONG, description = "Номер ссылки")
    private Long referenceNumber;

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
    @Visa2Delimiter
    @Visa2Field(length = 1024, type = Visa2Field.Type.MIX, description = "Дополнительный блок ")
    private String additionalBlock;

    /**
     * MAC-код в кодировке ASCII. Может отсутствовать
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

    public void setMac(@Nullable String mac) {
        if (mac != null && mac.length() > 16) {
            throw new IllegalArgumentException("Wrong MAC! Length: " + mac.length() + ". MAC: " + mac);
        }
        this.mac = mac;
    }

    public static Builder builder() {
        return new MainOperationRs().new Builder();
    }

    public class Builder {

        private Builder() {
            // private constructor
        }

        public Builder posGateHeader(PosGateHeader posGateHeader) {
            MainOperationRs.this.posGateHeader = posGateHeader;
            return this;
        }

        public Builder hostType(String hostType) {
            MainOperationRs.this.hostType = hostType;
            return this;
        }

        public Builder storeNumber(String storeNumber) {
            MainOperationRs.this.storeNumber = storeNumber;
            return this;
        }

        public Builder terminalNumber(String terminalNumber) {
            MainOperationRs.this.terminalNumber = terminalNumber;
            return this;
        }

        public Builder authorizationType(Integer authorizationType) {
            MainOperationRs.this.authorizationType = authorizationType;
            return this;
        }

        public Builder requestSequenceNumber(Integer requestSequenceNumber) {
            MainOperationRs.this.requestSequenceNumber = requestSequenceNumber;
            return this;
        }

        public Builder responseCode(Integer responseCode) {
            MainOperationRs.this.responseCode = responseCode;
            return this;
        }

        public Builder authorizationCode(String authorizationCode) {
            MainOperationRs.this.authorizationCode = authorizationCode;
            return this;
        }

        public Builder dateOfOperation(String dateOfOperation) {
            MainOperationRs.this.dateOfOperation = dateOfOperation;
            return this;
        }

        public Builder messageToOperator(String messageToOperator) {
            final String message = messageToOperator.length() > 16
                    ? messageToOperator.substring(0, 16)
                    : messageToOperator;
            MainOperationRs.this.messageToOperator = StringUtils.rightPad(Visa2Util.transliterateMessage(message), 16, "");
            return this;
        }

        public Builder reserve1(Integer reserve1) {
            MainOperationRs.this.reserve1 = reserve1;
            return this;
        }

        public Builder referenceNumber(Long referenceNumber) {
            MainOperationRs.this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder additionalBlock(String additionalBlock) {
            MainOperationRs.this.additionalBlock = additionalBlock;
            return this;
        }

        public Builder mac(String mac) {
            MainOperationRs.this.mac = mac;
            return this;
        }

        public MainOperationRs build() {
            return MainOperationRs.this;
        }
    }

    @Override
    public String toString() {
        return "MainOperationRs{" +
                "posGateHeader=" + posGateHeader +
                ", hostType='" + hostType + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", terminalNumber='" + terminalNumber + '\'' +
                ", authorizationType=" + authorizationType +
                ", requestSequenceNumber=" + requestSequenceNumber +
                ", responseCode=" + responseCode +
                ", authorizationCode='" + authorizationCode + '\'' +
                ", dateOfOperation=" + dateOfOperation +
                ", messageToOperator='" + messageToOperator + '\'' +
                ", reserve1=" + reserve1 +
                ", referenceNumber=" + referenceNumber +
                ", additionalBlock='" + additionalBlock + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
