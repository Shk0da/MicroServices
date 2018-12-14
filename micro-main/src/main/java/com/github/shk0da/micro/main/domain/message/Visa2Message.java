package com.github.shk0da.micro.main.domain.message;

import java.io.Serializable;

public class Visa2Message implements Serializable {

    private final Integer currencyTerminal;
    private final String terminalID;
    private final String typeOfOperation;
    private final Long amountTransaction;
    private final String secondTrackCard;
    private final CheckMac checkMac;

    public Visa2Message(Integer currencyTerminal, String terminalID, String typeOfOperation, Long amountTransaction, String secondTrackCard, CheckMac checkMac) {
        this.currencyTerminal = currencyTerminal;
        this.terminalID = terminalID;
        this.typeOfOperation = typeOfOperation;
        this.amountTransaction = amountTransaction;
        this.secondTrackCard = secondTrackCard;
        this.checkMac = checkMac;
    }

    public Integer getCurrencyTerminal() {
        return currencyTerminal;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public String getTypeOfOperation() {
        return typeOfOperation;
    }

    public Long getAmountTransaction() {
        return amountTransaction;
    }

    public String getSecondTrackCard() {
        return secondTrackCard;
    }

    public CheckMac getCheckMac() {
        return checkMac;
    }

    @Override
    public String toString() {
        return "Visa2{" +
                "currencyTerminal='" + currencyTerminal + '\'' +
                ", terminalID='" + terminalID + '\'' +
                ", typeOfOperation='" + typeOfOperation + '\'' +
                ", amountTransaction=" + amountTransaction +
                ", secondTrackCard='" + secondTrackCard + '\'' +
                ", checkMac=" + checkMac +
                '}';
    }
}
