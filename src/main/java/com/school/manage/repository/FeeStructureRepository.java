package com.school.manage.repository;

import com.school.manage.model.FeeStructure;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends MongoRepository<FeeStructure, String> {

    Optional<FeeStructure> findByClassNameAndAcademicYear(String className, String academicYear);

    List<FeeStructure> findByAcademicYear(String academicYear);

    /**
     * --- NEW METHOD ---
     * Deletes all fee structure documents that match the given academic year.
     * This is used by the FeeStructureService to ensure a clean save.
     *
     * @param academicYear The academic year for which to delete structures.
     */
    void deleteByAcademicYear(String academicYear);
}
