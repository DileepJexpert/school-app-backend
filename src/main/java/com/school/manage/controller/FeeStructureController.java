package com.school.manage.controller;

import com.school.manage.model.FeeStructure;
import com.school.manage.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feestructures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeeStructureController {

    private final FeeStructureService feeStructureService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Void> createOrUpdateFeeStructures(@RequestBody List<FeeStructure> feeStructures) {
        feeStructureService.saveFeeStructures(feeStructures);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * API endpoint to fetch all fee structures for a specific academic year.
     * This is used by the "Fee Setup" page to load existing data for editing.
     *
     * @param year The academic year to search for (e.g., "2024-2025").
     * @return A list of fee structures for the given year.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<FeeStructure>> getFeeStructuresByYear(@RequestParam("year") String year) {
        List<FeeStructure> structures = feeStructureService.getFeeStructuresByYear(year);
        return ResponseEntity.ok(structures);
    }

    /**
     * Deletes a single fee structure by its MongoDB ID.
     * Previously missing — the Flutter UI was calling DELETE /api/feestructures/{id}
     * which returned 404 every time.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Void> deleteFeeStructure(@PathVariable String id) {
        feeStructureService.deleteFeeStructure(id);
        return ResponseEntity.noContent().build();
    }
}
