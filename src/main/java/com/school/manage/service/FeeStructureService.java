package com.school.manage.service;

import com.school.manage.model.FeeStructure;
import com.school.manage.repository.FeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class containing the business logic for managing master Fee Structures.
 */
@Service
@RequiredArgsConstructor
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;

    /**
     * Saves a list of fee structures for an academic year.
     * To prevent duplicates, it first deletes any existing structures for that year
     * before saving the new ones.
     *
     * @param feeStructures The list of new fee structures to save.
     */
    @Transactional
    public void saveFeeStructures(List<FeeStructure> feeStructures) {
        if (feeStructures == null || feeStructures.isEmpty()) {
            return; // Do nothing if the list is empty
        }

        // Get the academic year from the first item (assuming all are for the same year)
        String academicYear = feeStructures.get(0).getAcademicYear();

        // Delete all existing fee structures for this academic year to ensure a clean update
        feeStructureRepository.deleteByAcademicYear(academicYear);

        // Save the new fee structures
        feeStructureRepository.saveAll(feeStructures);
    }

    /**
     * Retrieves all fee structures for a given academic year.
     *
     * @param academicYear The year to search for.
     * @return A list of fee structures.
     */
    public List<FeeStructure> getFeeStructuresByYear(String academicYear) {
        return feeStructureRepository.findByAcademicYear(academicYear);
    }
}
