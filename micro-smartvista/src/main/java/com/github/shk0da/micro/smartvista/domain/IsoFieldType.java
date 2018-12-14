package com.github.shk0da.micro.smartvista.domain;

import com.solab.iso8583.IsoType;

/**
 * Типы полей в {@link com.solab.iso8583.IsoMessage}
 */
public enum IsoFieldType {

    /*
        C – Conditional. Field/value is present in the message under certain conditions, which are explained in Data Field Description.
        M – Mandatory. Field/value must be present in the message.
        O – Optional. Field/value presence in the message is up to the message initiator or the responder.

        ----------------------------------------------------------------------------------------------------------------

        A	Alphabetic characters only.
        N	Numeric digits only.
        S	Special characters
        b	Binary representation, Length N means that field contains N bits
        an	Alphabetic and numeric characters only.
        As	Alphabetic and special characters
        ns	Numeric digits and special characters
        ans	Alphabetic, numeric, and special characters.
        4	Fixed length of 4 characters.
        …16	Variable length up to a maximum of 16 characters.  All variable length fields will also contain two or three positions at the beginning of the field to identify the number of positions following to the end of that field
        b	Binary representation of data.
        Bit –Map	8 bytes (64 bits) in binary format.  Each bit signifies the presence (1) or the absence (0) in the message of the data field associated with that particular bit
        x	“C” for credit, “D” for debit and shall always be associated with a numeric amount data field, i.e., x+n 16 in amount, net reconciliation means prefix “C” or “D” and 16 digits of amount, net reconciliation
        z	Tracks 2 and 3 code set as defined in ISO 4909 and ISO 7813
    */

    FIELD_2(IsoType.LLVAR, "2"),              // Primary Account Number
    FIELD_3(IsoType.NUMERIC, "3", 6),         // Processing Code
    FIELD_4(IsoType.NUMERIC, "4", 12),        // Amount, Transaction
    FIELD_5(IsoType.NUMERIC, "5", 12),        // Amount, Account
    FIELD_7(IsoType.DATE10, "7"),             // Date and Time, Transmission
    FIELD_9(IsoType.NUMERIC, "9", 8),         // Conversion Rate, Account
    FIELD_11(IsoType.NUMERIC, "11", 6),       // Systems Trace Audit Number
    FIELD_12(IsoType.DATE12, "12", 12),       // Time, Local Transaction
    FIELD_14(IsoType.DATE_EXP, "14"),         // Date, Expiration
    FIELD_15(IsoType.NUMERIC, "15", 6),       // Settlement Date
    FIELD_18(IsoType.NUMERIC, "18", 4),       // Merchant Type
    FIELD_19(IsoType.NUMERIC, "19", 3),       // Acquirer Country Code
    FIELD_22(IsoType.NUMERIC, "22", 12),      // Point of Service Date Code
    FIELD_25(IsoType.NUMERIC, "25", 2),       // Point-of-Service Condition Code
    FIELD_32(IsoType.LLVAR, "32"),            // Acquiring Institution Identification Code
    FIELD_37(IsoType.ALPHA, "37", 12),        // Retrieval Reference Number
    FIELD_38(IsoType.ALPHA, "38", 6),         // Authorization Identification Response
    FIELD_39_2(IsoType.ALPHA, "39", 2),       // Response Code
    FIELD_39_3(IsoType.ALPHA, "39", 3),       // Response Code
    FIELD_41(IsoType.ALPHA, "41", 8),         // Card Acceptor Terminal Identification
    FIELD_42(IsoType.ALPHA, "42", 15),        // Merchant Identification
    FIELD_43(IsoType.LLVAR, "43", 40),        // Card Acceptor Name and Location

    // FIELD 48
    FIELD_48_01(IsoType.NUMERIC, "48.01", 1), // Settlement type
    FIELD_48_02(IsoType.NUMERIC, "48.02", 3), // SVFE Transaction Type
    FIELD_48_04(IsoType.ALPHA, "48.04", 40),  // Full Name
    FIELD_48_05(IsoType.NUMERIC, "48.05", 3), // Credit Type
    FIELD_48_06(IsoType.NUMERIC, "48.06", 6), // Credit Issue Date
    FIELD_48_07(IsoType.NUMERIC, "48.07", 6), // Credit Issue Interval
    FIELD_48_08(IsoType.NUMERIC, "48.08", 6), // Credit End Date
    FIELD_48_09(IsoType.NUMERIC, "48.09", 6), // Credit Enable Date
    FIELD_48_10(IsoType.ALPHA, "48.10", 8),   // Service Identifier
    FIELD_48_11(IsoType.ALPHA, "48.11", 32),  // Service Data
    FIELD_48_12(IsoType.ALPHA, "48.12", 28),  // Prepaid Card Type
    FIELD_48_13(IsoType.ALPHA, "48.13", 6),   // Contract ID
    FIELD_48_14(IsoType.ALPHA, "48.14", 30),  // Address Verification Data
    FIELD_48_15(IsoType.ALPHA, "48.15", 9),   // Postal Code Data
    FIELD_48_16(IsoType.NUMERIC, "48.16", 3), // Installment Payment Quantity
    FIELD_48_17(IsoType.ALPHA, "48.17", 99),  // Text Information Data
    FIELD_48_18(IsoType.NUMERIC, "48.18", 2), // Card status
    FIELD_48_19(IsoType.NUMERIC, "48.19", 4), // Account status
    FIELD_48_20(IsoType.ALPHA, "48.20", 25),  // External code for operation

    // ...

    FIELD_48_27(IsoType.NUMERIC, "48.27", 4), // Acquirer Network Identifier
    FIELD_48_29(IsoType.NUMERIC, "48.29", 12),// Card ID

    // ...

    FIELD_48_36(IsoType.LLVAR, "48.36"),
    FIELD_48_56(IsoType.LLLVAR, "48.56"),
    FIELD_48_76(IsoType.LLLVAR, "48.76"),
    FIELD_48_86(IsoType.NUMERIC, "48.86"),
    FIELD_48_87(IsoType.NUMERIC, "48.87"),
    FIELD_48_88(IsoType.DATE4, "48.88"),
    FIELD_48_89(IsoType.ALPHA, "48.89", 2),
    FIELD_48_92(IsoType.NUMERIC, "48.92", 3),
    FIELD_48_95(IsoType.LLLVAR, "48.95", 2),
    FIELD_48_96(IsoType.LLVAR, "48.96"),
    FIELD_48_97(IsoType.LLVAR, "48.97"),

    FIELD_49(IsoType.NUMERIC, "49", 3),       // Currency Code, Transaction
    FIELD_50(IsoType.NUMERIC, "50", 3),       // Currency Code, Account
    FIELD_52(IsoType.LLBIN, "52", 8),         // Personal Identification Data
    FIELD_55(IsoType.LLLBIN, "55"),           // EMV data
    FIELD_70(IsoType.NUMERIC, "70", 3),       // Network Management Code
    FIELD_102(IsoType.LLVAR, "102");          // Account Identification

    private IsoType type;
    private int field;
    private int subField;
    private int length;

    IsoFieldType(IsoType type, String field) {
        this.type = type;
        parseField(field);
        this.length = 0;
    }

    IsoFieldType(IsoType type, String field, int length) {
        this.type = type;
        parseField(field);
        this.length = length;
    }

    public void parseField(String field) {
        if (field.contains(".")) {
            this.field = new Integer(field.split("\\.")[0]);
            this.subField = new Integer(field.split("\\.")[1]);
        } else {
            this.field = new Integer(field);
            this.subField = 0;
        }
    }

    public IsoType getType() {
        return type;
    }

    public int getField() {
        return field;
    }

    public int getSubField() {
        return subField;
    }

    public int getLength() {
        return length;
    }

    public static IsoFieldType find(int field, int subField) {
        for (IsoFieldType fieldType : IsoFieldType.values()) {
            if (fieldType.field == field && fieldType.subField == subField) {
                return fieldType;
            }
        }

        throw new IllegalArgumentException("Unknown ISO-8583 field \"" + field + (subField > 0 ? "." + Integer.toString(subField) : "") + "\"");
    }
}
