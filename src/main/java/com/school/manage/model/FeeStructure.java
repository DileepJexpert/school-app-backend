package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Represents the master fee template for a specific class and academic year.
 * This is the "master data" used to generate fee profiles for students.
 */
@Data
@Document(collection = "fee_structures")
public class FeeStructure {

    @Id
    private String id;

    private String academicYear;
    private String className;

    /**
     * This field MUST be a List of FeeComponent objects to be iterable.
     * This structure allows each fee component to have its own name, amount, and frequency,
     * which is essential for the FeeProfileService to work correctly.
     */
    private List<FeeComponent> feeComponents;
}
