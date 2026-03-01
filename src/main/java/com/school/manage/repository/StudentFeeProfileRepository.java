package com.school.manage.repository;

import com.school.manage.model.StudentFeeProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

/**
 * Spring Data MongoDB repository for the StudentFeeProfile collection.
 */
public interface StudentFeeProfileRepository extends MongoRepository<StudentFeeProfile, String> {

    /**
     * Finds student fee profiles where the 'name' field contains the given search string,
     * ignoring case. This method is used by the FeeService for searching students.
     *
     * @param name The partial or full name to search for.
     * @return A list of matching student fee profiles.
     */
    List<StudentFeeProfile> findByNameContainingIgnoreCase(String name);

    /**
     * Finds all student fee profiles for a specific class.
     *
     * @param className The exact class name to filter by.
     * @return A list of matching student fee profiles.
     */
    List<StudentFeeProfile> findByClassName(String className);

    /**
     * Returns only profiles with an outstanding balance (dueFees > 0).
     * Uses a server-side MongoDB filter — avoids loading the entire collection
     * into memory, which is critical for schools with 500+ students.
     */
    @Query("{ 'dueFees': { $gt: 0 } }")
    List<StudentFeeProfile> findAllWithOutstandingDues(Sort sort);
}
