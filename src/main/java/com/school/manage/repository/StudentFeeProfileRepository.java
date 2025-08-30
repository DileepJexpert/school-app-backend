package com.school.manage.repository;

import com.school.manage.model.StudentFeeProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
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
}
