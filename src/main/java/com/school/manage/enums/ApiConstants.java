package com.school.manage.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for centralizing API endpoint paths.
 * Prevents "magic strings" scattered across the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiConstants {
    public static final String API_BASE_URL = "/api";
    public static final String FEES_ENDPOINT = API_BASE_URL + "/fees";
    public static final String STUDENT_FEE_PROFILE_ENDPOINT = FEES_ENDPOINT + "/student/{studentId}";
    public static final String COLLECT_FEE_ENDPOINT = FEES_ENDPOINT + "/collect";
    public static final String TRANSACTION_HISTORY_ENDPOINT = FEES_ENDPOINT + "/transactions";
}
