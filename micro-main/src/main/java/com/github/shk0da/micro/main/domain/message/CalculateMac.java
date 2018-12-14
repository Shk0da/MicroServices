package com.github.shk0da.micro.main.domain.message;

import java.io.Serializable;

public class CalculateMac implements Serializable {

    private final String data; // HEX
    private String mac; // HEX

    public CalculateMac(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "CalculateMac{" +
                "data=" + data +
                ", mac='" + mac + '\'' +
                '}';
    }
}
