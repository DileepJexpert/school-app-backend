package com.school.manage.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class to generate unique receipt numbers.
 */
public class ReceiptGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Private constructor to prevent instantiation
    private ReceiptGenerator() {}

    /**
     * Generates a receipt number with the format: RCPT-YYYYMMDD-XXXX
     * where XXXX is a random 4-digit number.
     * @return A unique receipt number string.
     */
    public static String generate() {
        String datePart = LocalDate.now().format(DATE_FORMAT);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "RCPT-" + datePart + "-" + randomPart;
    }
}
