package com.school.manage.controller;

import com.school.manage.model.FeeStructure;
import com.school.manage.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing the master Fee Structures.
 * This controller handles API requests from the "Fee Setup" page in the Flutter app.
 */
@RestController
@RequestMapping("/api/feestructures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For production, specify allowed origins
public class FeeStructureController {

    private final FeeStructureService feeStructureService;

    /**
     * API endpoint to create or update the fee structure for an entire academic year.
     * It receives a list of fee structures (one for each class).
     *
     * @param feeStructures The list of fee structures from the request body.
     * @return A success response with no content.
     */
    @PostMapping
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
    public ResponseEntity<List<FeeStructure>> getFeeStructuresByYear(@RequestParam("year") String year) {
        List<FeeStructure> structures = feeStructureService.getFeeStructuresByYear(year);
        return ResponseEntity.ok(structures);
    }
}
