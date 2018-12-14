package com.github.shk0da.micro.smartvista.domain.message;

import com.github.shk0da.micro.smartvista.domain.IsoFieldType;
import com.github.shk0da.micro.smartvista.domain.IsoMessageField;
import com.github.shk0da.micro.smartvista.domain.IsoMessageType;
import com.github.shk0da.micro.smartvista.domain.IsoMsg;

import java.util.Arrays;
import java.util.Date;

/**
 * Authorization Message
 */
@IsoMsg(type = IsoMessageType.AuthorizationRq)
public class AuthorizationRq {

    @IsoMessageField(IsoFieldType.FIELD_2)
    private String primaryAccountNumber;

    @IsoMessageField(IsoFieldType.FIELD_3)
    private String processingCode;

    @IsoMessageField(IsoFieldType.FIELD_4)
    private String amountTransaction;

    @IsoMessageField(IsoFieldType.FIELD_5)
    private String amountAccount;

    @IsoMessageField(IsoFieldType.FIELD_7)
    private Date transmissionDate;

    @IsoMessageField(IsoFieldType.FIELD_9)
    private String conversionRateAccount;

    @IsoMessageField(IsoFieldType.FIELD_11)
    private String systemsTraceAuditNumber;

    @IsoMessageField(IsoFieldType.FIELD_12)
    private Date localTransactionDate;

    @IsoMessageField(IsoFieldType.FIELD_14)
    private Date dateExpiration;

    @IsoMessageField(IsoFieldType.FIELD_15)
    private String settlementDate;

    @IsoMessageField(IsoFieldType.FIELD_18)
    private String merchantType;

    @IsoMessageField(IsoFieldType.FIELD_19)
    private String acquirerCountryCode;

    @IsoMessageField(IsoFieldType.FIELD_22)
    private String pointOfServiceDateCode;

    @IsoMessageField(IsoFieldType.FIELD_32)
    private String acquiringInstitutionIdentificationCode;

    @IsoMessageField(IsoFieldType.FIELD_37)
    private String retrievalReferenceNumber;

    @IsoMessageField(IsoFieldType.FIELD_43)
    private String cardAcceptorNameAndLocation;

    @IsoMessageField(IsoFieldType.FIELD_48_29)
    private String cardID;

    @IsoMessageField(IsoFieldType.FIELD_48_27)
    private String acquirerNetworkIdentifier;

    @IsoMessageField(IsoFieldType.FIELD_49)
    private String currencyCodeTransaction;

    @IsoMessageField(IsoFieldType.FIELD_50)
    private String currencyCodeAccount;

    @IsoMessageField(IsoFieldType.FIELD_52)
    private byte[] pinData;

    @IsoMessageField(IsoFieldType.FIELD_55)
    private byte[] emvData;

    public String getPrimaryAccountNumber() {
        return primaryAccountNumber;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public String getAmountTransaction() {
        return amountTransaction;
    }

    public String getAmountAccount() {
        return amountAccount;
    }

    public Date getTransmissionDate() {
        return transmissionDate;
    }

    public String getConversionRateAccount() {
        return conversionRateAccount;
    }

    public String getSystemsTraceAuditNumber() {
        return systemsTraceAuditNumber;
    }

    public Date getLocalTransactionDate() {
        return localTransactionDate;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public String getAcquirerCountryCode() {
        return acquirerCountryCode;
    }

    public String getPointOfServiceDateCode() {
        return pointOfServiceDateCode;
    }

    public String getAcquiringInstitutionIdentificationCode() {
        return acquiringInstitutionIdentificationCode;
    }

    public String getRetrievalReferenceNumber() {
        return retrievalReferenceNumber;
    }

    public String getCardAcceptorNameAndLocation() {
        return cardAcceptorNameAndLocation;
    }

    public String getCardID() {
        return cardID;
    }

    public String getAcquirerNetworkIdentifier() {
        return acquirerNetworkIdentifier;
    }

    public String getCurrencyCodeTransaction() {
        return currencyCodeTransaction;
    }

    public String getCurrencyCodeAccount() {
        return currencyCodeAccount;
    }

    public byte[] getPinData() {
        return pinData;
    }

    public byte[] getEmvData() {
        return emvData;
    }

    @Override
    public String toString() {
        return "AuthorizationRq{" +
                "primaryAccountNumber='" + primaryAccountNumber + '\'' +
                ", processingCode='" + processingCode + '\'' +
                ", amountTransaction='" + amountTransaction + '\'' +
                ", amountAccount='" + amountAccount + '\'' +
                ", transmissionDate='" + transmissionDate + '\'' +
                ", conversionRateAccount='" + conversionRateAccount + '\'' +
                ", systemsTraceAuditNumber='" + systemsTraceAuditNumber + '\'' +
                ", localTransactionDate='" + localTransactionDate + '\'' +
                ", dateExpiration='" + dateExpiration + '\'' +
                ", settlementDate='" + settlementDate + '\'' +
                ", merchantType='" + merchantType + '\'' +
                ", acquirerCountryCode='" + acquirerCountryCode + '\'' +
                ", pointOfServiceDateCode='" + pointOfServiceDateCode + '\'' +
                ", acquiringInstitutionIdentificationCode='" + acquiringInstitutionIdentificationCode + '\'' +
                ", retrievalReferenceNumber='" + retrievalReferenceNumber + '\'' +
                ", cardAcceptorNameAndLocation='" + cardAcceptorNameAndLocation + '\'' +
                ", cardID='" + cardID + '\'' +
                ", acquirerNetworkIdentifier='" + acquirerNetworkIdentifier + '\'' +
                ", currencyCodeTransaction='" + currencyCodeTransaction + '\'' +
                ", currencyCodeAccount='" + currencyCodeAccount + '\'' +
                ", pinData=" + Arrays.toString(pinData) +
                ", emvData=" + Arrays.toString(emvData) +
                '}';
    }
}
