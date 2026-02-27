package com.school.manage.service;

import com.school.manage.model.FeeStructure;
import com.school.manage.repository.FeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;

    /**
     * Upserts a list of fee structures.
     *
     * BUG FIX: The previous implementation called deleteByAcademicYear() before saving,
     * which wiped ALL structures for the year every time a single structure was added.
     * Now uses per-item upsert: if a structure for the same class + year already exists,
     * its _id is reused so MongoDB updates it in-place. New class entries are inserted.
     */
    @Transactional
    public void saveFeeStructures(List<FeeStructure> feeStructures) {
        if (feeStructures == null || feeStructures.isEmpty()) {
            return;
        }
        for (FeeStructure structure : feeStructures) {
            // Reuse the existing _id if this class+year combo already has a record
            feeStructureRepository
                    .findByClassNameAndAcademicYear(structure.getClassName(), structure.getAcademicYear())
                    .ifPresent(existing -> structure.setId(existing.getId()));
            feeStructureRepository.save(structure);
        }
    }

    /**
     * Deletes a single fee structure by its ID.
     */
    public void deleteFeeStructure(String id) {
        feeStructureRepository.deleteById(id);
    }

    /**
     * Retrieves all fee structures for a given academic year.
     */
    public List<FeeStructure> getFeeStructuresByYear(String academicYear) {
        return feeStructureRepository.findByAcademicYear(academicYear);
    }
}
