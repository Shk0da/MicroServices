package com.github.shk0da.micro.smartvista.domain.message;

import com.github.shk0da.micro.smartvista.domain.IsoFieldType;
import com.github.shk0da.micro.smartvista.domain.IsoMessageField;
import com.github.shk0da.micro.smartvista.domain.IsoMessageType;
import com.github.shk0da.micro.smartvista.domain.IsoMsg;
import com.github.shk0da.micro.smartvista.util.SmartVistaUtil;

import java.util.Arrays;
import java.util.Date;

@IsoMsg(type = IsoMessageType.AuthorizationRs)
public class AuthorizationRs {

    @IsoMessageField(IsoFieldType.FIELD_2)
    private String primaryAccountNumber;

    @IsoMessageField(IsoFieldType.FIELD_3)
    private String processingCode;

    @IsoMessageField(IsoFieldType.FIELD_7)
    private Date transmissionDate;

    @IsoMessageField(IsoFieldType.FIELD_11)
    private String systemsTraceAuditNumber;

    @IsoMessageField(IsoFieldType.FIELD_12)
    private Date localTransactionDate;

    @IsoMessageField(IsoFieldType.FIELD_14)
    private Date dateExpiration;

    @IsoMessageField(IsoFieldType.FIELD_37)
    private String retrievalReferenceNumber;

    @IsoMessageField(IsoFieldType.FIELD_38)
    private String authorisationIdentificationResponse;

    @IsoMessageField(IsoFieldType.FIELD_39_3)
    private String responseCode;

    @IsoMessageField(IsoFieldType.FIELD_48_29)
    private String cardID;

    @IsoMessageField(IsoFieldType.FIELD_55)
    private byte[] emvData;

    public void setPrimaryAccountNumber(String primaryAccountNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public void setTransmissionDate(Date transmissionDate) {
        this.transmissionDate = transmissionDate;
    }

    public void setSystemsTraceAuditNumber(String systemsTraceAuditNumber) {
        this.systemsTraceAuditNumber = systemsTraceAuditNumber;
    }

    public void setLocalTransactionDate(Date localTransactionDate) {
        this.localTransactionDate = localTransactionDate;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public void setRetrievalReferenceNumber(String retrievalReferenceNumber) {
        this.retrievalReferenceNumber = retrievalReferenceNumber;
    }

    public void setAuthorisationIdentificationResponse(String authorisationIdentificationResponse) {
        this.authorisationIdentificationResponse = authorisationIdentificationResponse;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseCode(SmartVistaUtil.ResponseCode responseCode) {
        this.responseCode = responseCode.getValue();
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setEmvData(byte[] emvData) {
        this.emvData = emvData;
    }

    @Override
    public String toString() {
        return "AuthorizationRs{" +
                "primaryAccountNumber='" + primaryAccountNumber + '\'' +
                ", processingCode='" + processingCode + '\'' +
                ", transmissionDate='" + transmissionDate + '\'' +
                ", systemsTraceAuditNumber='" + systemsTraceAuditNumber + '\'' +
                ", localTransactionDate='" + localTransactionDate + '\'' +
                ", dateExpiration='" + dateExpiration + '\'' +
                ", retrievalReferenceNumber='" + retrievalReferenceNumber + '\'' +
                ", authorisationIdentificationResponse='" + authorisationIdentificationResponse + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", cardID='" + cardID + '\'' +
                ", emvData=" + Arrays.toString(emvData) +
                '}';
    }
}
