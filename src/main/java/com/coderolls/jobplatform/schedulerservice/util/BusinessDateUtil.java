package com.coderolls.jobplatform.schedulerservice.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class BusinessDateUtil {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    private BusinessDateUtil() {}

    public static String getBusinessDate() {
        return todayMinus1();
    }

    /** Business date is always computed from UTC "today", previous business day (T-1, weekends skipped). */
    public static String todayMinus1() {
        return format(previousBusinessDay(LocalDate.now(ZoneOffset.UTC)));
    }

    public static LocalDate previousBusinessDay(LocalDate from) {
        LocalDate date = from.minusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date;
    }

    public static String format(LocalDate date) {
        return date.format(FORMAT);
    }

    public static LocalDate parse(String businessDate) {
        return LocalDate.parse(businessDate, FORMAT);
    }
}