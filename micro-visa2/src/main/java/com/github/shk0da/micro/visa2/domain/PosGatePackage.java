package com.github.shk0da.micro.visa2.domain;

public interface PosGatePackage {
    /**
     * @param posGateHeader Заголовок PosGate
     */
    void setPosGateHeader(PosGateHeader posGateHeader);

    /**
     * @return Заголовок PosGate
     */
    PosGateHeader getPosGateHeader();
}
