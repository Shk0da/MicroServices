package com.github.shk0da.micro.visa2.domain;

/**
 * PosGate package header
 */
public class PosGateHeader {

    /**
     * Длина пакета
     */
    private final int length;

    /**
     * Идентификатор устройства (TID): От ‘00000001’ до ‘ZZZZZZZZ’
     */
    private final String tid;


    public PosGateHeader(int length, String tid) {
        this.length = length;
        this.tid = tid;
    }

    public int getLength() {
        return length;
    }

    public String getTid() {
        return tid;
    }

    @Override
    public String toString() {
        return "PosGateHeader{" +
                "length=" + length +
                ", tid='" + tid + '\'' +
                '}';
    }
}
