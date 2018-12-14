package com.github.shk0da.micro.smartvista.domain.message;


import com.github.shk0da.micro.smartvista.domain.IsoFieldType;
import com.github.shk0da.micro.smartvista.domain.IsoMessageField;
import com.github.shk0da.micro.smartvista.domain.IsoMessageType;
import com.github.shk0da.micro.smartvista.domain.IsoMsg;
import com.github.shk0da.micro.smartvista.util.SmartVistaUtil;

import java.util.Date;

@IsoMsg(type = IsoMessageType.NetworkManagementRs)
public class NetworkManagementRs {

    @IsoMessageField(IsoFieldType.FIELD_7)
    private Date transmissionDateTime;

    @IsoMessageField(IsoFieldType.FIELD_11)
    private String systemsTraceAuditNumber;

    @IsoMessageField(IsoFieldType.FIELD_39_2)
    private String responseCode;

    @IsoMessageField(IsoFieldType.FIELD_70)
    private String networkManagementCode;

    public Date getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public String getSystemsTraceAuditNumber() {
        return systemsTraceAuditNumber;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getNetworkManagementCode() {
        return networkManagementCode;
    }

    public void setTransmissionDateTime(Date transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public void setSystemsTraceAuditNumber(String systemsTraceAuditNumber) {
        this.systemsTraceAuditNumber = systemsTraceAuditNumber;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseCode(SmartVistaUtil.ResponseCode responseCode) {
        this.responseCode = responseCode.getValue();
    }

    public void setNetworkManagementCode(String networkManagementCode) {
        this.networkManagementCode = networkManagementCode;
    }

    @Override
    public String toString() {
        return "NetworkManagementMessageRs{" +
                "transmissionDateTime='" + transmissionDateTime + '\'' +
                ", systemsTraceAuditNumber='" + systemsTraceAuditNumber + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", networkManagementCode='" + networkManagementCode + '\'' +
                '}';
    }
}
