package com.github.shk0da.micro.smartvista.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

public final class DateUtil {

    public static final DateTimeFormatter DT_FORMATTER_YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
    public static final DateTimeFormatter DT_FORMATTER_MMYY = new DateTimeFormatterBuilder()
            .appendPattern("MMyy")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1L)
            .toFormatter();

    // ISO-8583 type DATE12
    public static final DateTimeFormatter DATE12_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    // ISO-8583 type DATE10
    public static final DateTimeFormatter DATE10_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("MMddHHmmss")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter();

    // ISO-8583 type DATE_EXP
    public static final DateTimeFormatter EXPIRATION_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyMM")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1L)
            .toFormatter();

    // ISO-8583 type DATE4
    public static final DateTimeFormatter DATE4_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("MMdd")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1L)
            .toFormatter();

    private DateUtil() {
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime dateToLocalDateTime(Date dateTime) {
        return dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static ZonedDateTime dateToZonedDateTime(Date dateTime) {
        return ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

    public static Date localDateToDate(LocalDate localDate) {
        ZonedDateTime zdt = localDateToZonedDateTime(localDate);
        return Date.from(zdt.toInstant());
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    public static ZonedDateTime localDateToZonedDateTime(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault());
    }

    /**
     * int[MMYY] -> LocalDate
     */
    public static LocalDate intMmyyToLocalDate(int mmyy) {
        int month = mmyy / 100;
        int year = 2000 + (mmyy - month * 100);
        return LocalDate.of(year, month, 1);
    }

    /**
     * int[MMYY] -> Date
     */
    public static Date intMmyyToDate(int mmyy) {
        LocalDate localDate = intMmyyToLocalDate(mmyy);
        return localDateToDate(localDate);
    }

    /**
     * LocalDate -> int[MMYY]
     */
    public static int localDateToIntMmyy(LocalDate localDate) {
        int mm = localDate.getMonth().getValue();
        int yy = localDate.getYear() % 100;
        return mm * 100 + yy;
    }

    /**
     * Date -> int[MMYY]
     */
    public static int dateToIntMmyy(Date date) {
        return localDateToIntMmyy(dateToLocalDate(date));
    }

    /**
     * "yyMM" string -> Date
     */
    public static Date expDateStrToDate(String expDate) {
        LocalDate localDate = LocalDate.parse(expDate, EXPIRATION_DATE_FORMATTER);
        return localDateToDate(localDate);
    }

    /**
     * "yyMMddHHmmss" string -> Date
     */
    public static Date date12StrToDate(String date12str) {
        LocalDateTime localDateTime = LocalDateTime.parse(date12str, DATE12_FORMATTER);
        return localDateTimeToDate(localDateTime);
    }

    /**
     * "MMddHHmmss" string -> Date
     */
    public static Date date10StrToDate(String date10str) {
        LocalDateTime localDateTime = LocalDateTime.parse(date10str, DATE10_FORMATTER);
        return localDateTimeToDate(localDateTime);
    }

    /**
     * "MMdd" string -> Date
     */
    public static Date date4StrToDate(String date4str) {
        LocalDate localDate = LocalDate.parse(date4str, DATE4_FORMATTER);
        return localDateToDate(localDate);
    }

    /**
     * LocalDate -> "yyMM" string
     */
    public static String expDateToStr(LocalDate localDate) {
        return EXPIRATION_DATE_FORMATTER.format(localDate);
    }

    /**
     * Date -> "yyMM" string
     */
    public static String expDateToStr(Date date) {
        return expDateToStr(dateToLocalDate(date));
    }

    public static Date getTodayStart() {
        return localDateToDate(LocalDate.now());
    }
}
