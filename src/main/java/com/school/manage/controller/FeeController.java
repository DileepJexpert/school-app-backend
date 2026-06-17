package com.school.manage.controller;


import com.school.manage.model.FeeStructure;
import com.school.manage.service.FeeReceiptPdfService;
import com.school.manage.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;
    private final FeeReceiptPdfService feeReceiptPdfService;

    @PostMapping("/structure")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public FeeStructure saveFeeStructure(@RequestBody FeeStructure feeStructure) {
        log.info("[FeeController] POST /api/fees/structure — class='{}', year='{}'",
                feeStructure.getClassName(), feeStructure.getAcademicYear());
      //  return feeService.(feeStructure);
        return feeStructure;
    }

    @GetMapping("/structure/{academicYear}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public List<FeeStructure> getFeeStructureByYear(@PathVariable String academicYear) {
        log.debug("[FeeController] GET /api/fees/structure/{}", academicYear);
      //  return feeService.getFeeStructureByYear(academicYear);
        return List.of();
    }

    /**
     * Download a PDF receipt for a specific fee payment record.
     *
     * GET /api/fees/receipt/{feeRecordId}
     */
    @GetMapping("/receipt/{feeRecordId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String feeRecordId) {
        log.info("[FeeController] GET /api/fees/receipt/{}", feeRecordId);
        byte[] pdf = feeReceiptPdfService.generateReceipt(feeRecordId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt_" + feeRecordId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Download bulk receipts for all paid fees in a class.
     *
     * GET /api/fees/receipts/bulk?className=&academicYear=
     */
    @GetMapping("/receipts/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> downloadBulkReceipts(
            @RequestParam String className,
            @RequestParam String academicYear) {
        log.info("[FeeController] GET /api/fees/receipts/bulk — class='{}', year='{}'",
                className, academicYear);
        byte[] pdf = feeReceiptPdfService.generateBulkReceipts(className, academicYear);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipts_" + className + "_" + academicYear + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}