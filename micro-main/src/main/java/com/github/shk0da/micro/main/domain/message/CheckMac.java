package com.github.shk0da.micro.main.domain.message;

import java.io.Serializable;

public class CheckMac implements Serializable {

    private final String mac; // HEX
    private final String message; // HEX
    private boolean result;

    public CheckMac(String mac, String message) {
        this.mac = mac;
        this.message = message;
    }

    public String getMac() {
        return mac;
    }

    public String getMessage() {
        return message;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CheckMac{" +
                "mac='" + mac + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
