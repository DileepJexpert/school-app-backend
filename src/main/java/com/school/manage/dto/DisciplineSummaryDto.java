package com.school.manage.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DisciplineSummaryDto {

    private long totalIncidents;
    private long resolvedIncidents;
    private long unresolvedIncidents;

    /** Severity -> count: WARNING=5, MINOR=3, etc. */
    private Map<String, Long> bySeverity;

    /** Category -> count */
    private Map<String, Long> byCategory;

    /** Class -> count */
    private Map<String, Long> byClass;
}
