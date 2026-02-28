package com.school.manage.service;

import com.school.manage.model.FeeStructure;
import com.school.manage.model.Student;
import com.school.manage.repository.FeeStructureRepository;
import com.school.manage.repository.StudentFeeProfileRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
    private final StudentRepository studentRepository;
    private final StudentFeeProfileRepository studentFeeProfileRepository;
    private final FeeProfileService feeProfileService;

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

            // Retroactively create fee profiles for students already admitted to this class
            // who were skipped at admission time because the fee structure did not exist yet.
            List<Student> existingStudents = studentRepository
                    .findByClassForAdmissionAndAcademicYear(structure.getClassName(), structure.getAcademicYear());
            int created = 0;
            for (Student student : existingStudents) {
                if (!studentFeeProfileRepository.existsById(student.getId())) {
                    feeProfileService.createFeeProfileForNewStudent(student);
                    created++;
                }
            }
            if (created > 0) {
                log.info("Retroactively created {} fee profile(s) for class '{}' ({})",
                        created, structure.getClassName(), structure.getAcademicYear());
            }
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
