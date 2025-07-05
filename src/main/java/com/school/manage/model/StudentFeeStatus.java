package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "student_fee_statuses")
public class StudentFeeStatus {

    @Id
    private String id;
    private String studentId;
    private String academicYear;
    private String className;

    private List<FeeInstallment> installments; // A list of all monthly installments

    private double totalFees;
    private double totalPaid;
    private double totalDue;
}