package com.github.shk0da.micro.main.domain.message;

import java.io.Serializable;
import java.util.Date;

public class LimitCheck implements Serializable {

    private final String hpan;
    private final Date dateExpiration;
    private final String amountTransaction;
    private final String amountAccount;
    private final String currencyCodeTransaction;
    private final String currencyCodeAccount;
    private boolean result;

    public LimitCheck(String hpan, Date dateExpiration, String amountTransaction, String amountAccount, String currencyCodeTransaction, String currencyCodeAccount) {
        this.hpan = hpan;
        this.dateExpiration = dateExpiration;
        this.amountTransaction = amountTransaction;
        this.amountAccount = amountAccount;
        this.currencyCodeTransaction = currencyCodeTransaction;
        this.currencyCodeAccount = currencyCodeAccount;
    }

    public String getHpan() {
        return hpan;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public String getAmountTransaction() {
        return amountTransaction;
    }

    public String getAmountAccount() {
        return amountAccount;
    }

    public String getCurrencyCodeTransaction() {
        return currencyCodeTransaction;
    }

    public String getCurrencyCodeAccount() {
        return currencyCodeAccount;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "LimitCheck{" +
                "hpan='" + hpan + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", amountTransaction=" + amountTransaction +
                ", amountAccount=" + amountAccount +
                ", currencyCodeTransaction='" + currencyCodeTransaction + '\'' +
                ", currencyCodeAccount='" + currencyCodeAccount + '\'' +
                ", result=" + result +
                '}';
    }
}
