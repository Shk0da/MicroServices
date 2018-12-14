package com.github.shk0da.micro.smartvista.domain.message;

import com.github.shk0da.micro.smartvista.domain.IsoFieldType;
import com.github.shk0da.micro.smartvista.domain.IsoMessageField;
import com.github.shk0da.micro.smartvista.domain.IsoMessageType;
import com.github.shk0da.micro.smartvista.domain.IsoMsg;

import java.util.Date;

/**
 * Echo Test / Sign-on / Sign-off
 */
@IsoMsg(type = IsoMessageType.NetworkManagementRq)
public class NetworkManagementRq {

    @IsoMessageField(IsoFieldType.FIELD_7)
    private Date transmissionDateTime;

    @IsoMessageField(IsoFieldType.FIELD_11)
    private String systemsTraceAuditNumber;

    @IsoMessageField(IsoFieldType.FIELD_70)
    private String networkManagementCode;

    public void setNetworkManagementCode(String networkManagementCode) {
        this.networkManagementCode = networkManagementCode;
    }

    public void setTransmissionDateTime(Date transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public void setSystemsTraceAuditNumber(String systemsTraceAuditNumber) {
        this.systemsTraceAuditNumber = systemsTraceAuditNumber;
    }

    public Date getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public String getSystemsTraceAuditNumber() {
        return systemsTraceAuditNumber;
    }

    public String getNetworkManagementCode() {
        return networkManagementCode;
    }

    @Override
    public String toString() {
        return "NetworkManagementMessageRq{" +
                "transmissionDateTime='" + transmissionDateTime + '\'' +
                ", systemsTraceAuditNumber='" + systemsTraceAuditNumber + '\'' +
                ", networkManagementCode='" + networkManagementCode + '\'' +
                '}';
    }
}
