package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Document(collection = "fee_structures")
public class FeeStructure {
    @Id
    private String id;
    private String academicYear;
    private String className;
    private Map<String, Double> feeComponents;
}