package com.github.shk0da.micro.smartvista.domain;

import java.util.Arrays;
import java.util.Objects;

/**
 * Типы {@link com.solab.iso8583.IsoMessage}
 */
public enum IsoMessageType {

    AuthorizationRq(0x1100, 0),
    AuthorizationRs(0x1110, 0, 0x1100),
    AuthorizationAdviceRq(0x1120, 0),
    AuthorizationAdviceRs(0x1130, 0, 0x1120),
    CardManagementRq(0x1344, 910000),
    CardManagementRs(0x1354, 910000, 0x1344),
    NetworkManagementRq(0x1804, 0),
    NetworkManagementRs(0x1814, 0, 0x1804);

    private final int type;
    private final String code;
    private final String typeRequest;

    IsoMessageType(int type, int code) {
        this.type = type;
        this.code = (code == 0) ? "" : code + "0000";
        this.typeRequest = Integer.toString(type);
    }

    IsoMessageType(int type, int code, int typeRequest) {
        this.type = type;
        this.code = (code == 0) ? "" : code + "0000";
        this.typeRequest = Integer.toString(typeRequest);
    }

    public int getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getTypeRequest() {
        return typeRequest;
    }

    public static IsoMessageType find(int type, String code) {
        for (IsoMessageType item : IsoMessageType.values()) {
            if (item.getType() == type && Objects.equals(item.getCode(), code)) {
                return item;
            }
        }

        throw new IllegalArgumentException("Unknown ISO-8583 message type " + type + " with code \"" + code + "\"");
    }

    public static IsoMessageType fromType(int type) {
        return Arrays.stream(IsoMessageType.values())
                .parallel()
                .filter(isoMessageType -> isoMessageType.getType() == type)
                .findFirst()
                .orElse(null);
    }
}
