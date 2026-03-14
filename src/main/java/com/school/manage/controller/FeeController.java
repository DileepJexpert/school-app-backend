package com.school.manage.controller;


import com.school.manage.model.FeeStructure;
import com.school.manage.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;

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
}