package com.github.shk0da.micro.smartvista.domain;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import org.apache.commons.lang3.StringUtils;
import com.github.shk0da.micro.smartvista.util.DateUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Field48 implements CustomField<Field48> {

    public static final int FIELD_NUMBER = 48;

    private static final int SUB_FIELD_SIZE = 3;

    private final Map<IsoFieldType, Object> valuesMap = new HashMap<>();

    public Object getValue(IsoFieldType key) {
        return valuesMap.getOrDefault(key, null);
    }

    public void setValue(IsoFieldType fieldType, Object value) {
        valuesMap.put(fieldType, value);
    }

    public Map<IsoFieldType, Object> getValuesMap() {
        return valuesMap;
    }

    @Override
    public Field48 decodeField(String value) {
        if (value != null) {
            int pos = 0;
            while (pos < value.length()) {
                int subField = Integer.parseInt(value.substring(pos, pos + SUB_FIELD_SIZE));
                IsoFieldType fieldType = IsoFieldType.find(FIELD_NUMBER, subField);
                pos += SUB_FIELD_SIZE;

                int length = Integer.parseInt(value.substring(pos, pos + SUB_FIELD_SIZE));
                int expectedLen = fieldType.getLength();
                if (expectedLen > 0 && expectedLen != length) {
                    throw new IllegalArgumentException(
                            fieldType + " expected data size " + expectedLen + " but in input field size = " + length
                    );
                }
                pos += SUB_FIELD_SIZE;

                String data = value.substring(pos, pos + length);
                pos += length;

                Object decodedValue = decode(data, fieldType.getType());
                setValue(fieldType, decodedValue);
            }
        }
        return this;
    }

    private static Object decode(String data, IsoType type) {
        switch (type) {
            case DATE10:
                return DateUtil.date10StrToDate(data);
            case DATE4:
                return DateUtil.date4StrToDate(data);
            case DATE_EXP:
                return DateUtil.expDateStrToDate(data);
            case NUMERIC:
            case AMOUNT:
                if (!StringUtils.isNumeric(data)) {
                    throw new IllegalArgumentException(type + " contain non-digit chars: \"" + data + "\"");
                }
                return data;
            case BINARY:
            case LLBIN:
            case LLLBIN:
                byte[] bin = new byte[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    bin[i] = (byte) (data.charAt(i) & 0xFF);
                }
                return bin;
            case DATE12:
                return DateUtil.date12StrToDate(data);
            default:
                return data;
        }
    }

    @Override
    public String encodeField(Field48 field48) {
        StringBuilder sb = new StringBuilder();
        Map<IsoFieldType, Object> map = field48.getValuesMap();
        for (Map.Entry<IsoFieldType, Object> entry : map.entrySet()) {
            Object data = entry.getValue();
            if (data == null) {
                continue;
            }

            IsoFieldType field = entry.getKey();
            String encodedValue = encode(data, field.getType());

            int size = encodedValue.length();
            if (size > 0) {
                if (field.getLength() > 0 && field.getLength() != size) {
                    throw new IllegalArgumentException(
                            field + " expected data size " + field.getLength() + " but in input field size = " + size
                    );
                }
                sb.append(StringUtils.leftPad(Integer.toString(field.getSubField()), SUB_FIELD_SIZE, "0"));
                sb.append(StringUtils.leftPad(Integer.toString(size), SUB_FIELD_SIZE, "0"));
                sb.append(encodedValue);
            }
        }
        return sb.toString();
    }

    private static String encode(Object data, IsoType type) {
        switch (type) {
            case DATE10:
                return DateUtil.DATE10_FORMATTER.format(DateUtil.dateToZonedDateTime((Date) data));
            case DATE4:
                return DateUtil.DATE4_FORMATTER.format(DateUtil.dateToLocalDate((Date) data));
            case DATE_EXP:
                return DateUtil.expDateToStr((Date) data);
            case NUMERIC:
            case AMOUNT:
                if (!StringUtils.isNumeric((String) data)) {
                    throw new IllegalArgumentException(type + " contain non-digit chars: \"" + data + "\"");
                }
                return (String) data;
            case BINARY:
            case LLBIN:
            case LLLBIN:
                return new String((byte[]) data, StandardCharsets.ISO_8859_1);
            case DATE12:
                return DateUtil.DATE12_FORMATTER.format(DateUtil.dateToZonedDateTime((Date) data));
            default:
                return (String) data;
        }
    }
}
