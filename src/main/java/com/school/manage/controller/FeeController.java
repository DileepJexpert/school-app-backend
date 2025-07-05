package com.school.manage.controller;


import com.school.manage.model.FeeStructure;
import com.school.manage.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;

    @PostMapping("/structure")
    public FeeStructure saveFeeStructure(@RequestBody FeeStructure feeStructure) {
        return feeService.saveFeeStructure(feeStructure);
    }

    @GetMapping("/structure/{academicYear}")
    public List<FeeStructure> getFeeStructureByYear(@PathVariable String academicYear) {
        return feeService.getFeeStructureByYear(academicYear);
    }
}