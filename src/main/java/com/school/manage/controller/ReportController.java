// File: com/school/manage/controller/ReportController.java
package com.school.manage.controller;

import com.school.manage.dto.FeeReportResponse;
import com.school.manage.dto.SchoolSummaryResponse;
import com.school.manage.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportSummaryService reportSummaryService;

    @GetMapping("/fees/report-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<FeeReportResponse> getCollectionReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String className,
            @RequestParam(required = false) String paymentMode,
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FeeReportResponse report = reportSummaryService.generateCollectionReport(
                startDate, endDate, className, paymentMode, search, page, size
        );
        return ResponseEntity.ok(report);
    }

    /**
     * Returns a consolidated school-wide summary for the Reports dashboard:
     * total students, class-wise enrollment, fee totals, monthly fee collections,
     * and payment mode breakdown.
     */
    @GetMapping("/school-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<SchoolSummaryResponse> getSchoolSummary() {
        return ResponseEntity.ok(reportSummaryService.getSchoolSummary());
    }
}
