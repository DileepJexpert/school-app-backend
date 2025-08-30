// File: com/school/manage/controller/ReportController.java
package com.school.manage.controller;

import com.school.manage.service.FeeService;
import com.school.manage.dto.FeeReportResponse;
import com.school.manage.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportSummaryService reportSummaryService;

    /**
     * Fee Collection Report (Hybrid)
     * Returns summary cards, class-wise breakdown, payment mode breakdown,
     * paginated transactions, and filter options in one response.
     */
    @GetMapping("/fees/report-summary")
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
}
