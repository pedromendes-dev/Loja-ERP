package com.example.erp_.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter SQLITE_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQLITE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDateTime parseFlexible(String s) {
        if (s == null) return null;
        s = s.trim();
        try {
            if (s.contains("T")) {
                return LocalDateTime.parse(s, ISO);
            } else if (s.length() == 10) {
                // date only
                LocalDate d = LocalDate.parse(s, SQLITE_DATE);
                return d.atStartOfDay();
            } else {
                return LocalDateTime.parse(s, SQLITE_DATETIME);
            }
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s, ISO);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}

