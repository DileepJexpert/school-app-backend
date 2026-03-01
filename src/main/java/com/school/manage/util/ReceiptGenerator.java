package com.school.manage.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generates receipt numbers that are guaranteed unique for any realistic
 * school transaction volume.
 *
 * Format: RCPT-YYYYMMDD-XXXXXXXX
 *   YYYYMMDD   — calendar date (groups receipts by day for easy lookup)
 *   XXXXXXXX   — first 8 hex chars of a random UUID (~4 billion combinations)
 *
 * The previous implementation used ThreadLocalRandom.nextInt(1000, 9999)
 * which gave only 8 999 combinations per day — a statistically meaningful
 * collision risk for schools processing 100+ payments a day.
 */
public class ReceiptGenerator {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private ReceiptGenerator() {}

    public static String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String unique = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "RCPT-" + date + "-" + unique;
    }
}
