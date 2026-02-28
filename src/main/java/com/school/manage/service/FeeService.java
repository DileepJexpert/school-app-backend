package com.school.manage.service;

import com.school.manage.enums.PaymentMode;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.*;
import com.school.manage.repository.PaymentRecordRepository;
import com.school.manage.repository.StudentFeeProfileRepository;
import com.school.manage.util.ReceiptGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Pipeline;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {

    private final StudentFeeProfileRepository studentFeeProfileRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional
    public PaymentRecordResponse collectFee(FeePaymentRequest request) {
        log.info("Processing fee collection for student ID: {}", request.getStudentId());
        StudentFeeProfile profile = studentFeeProfileRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));
        BigDecimal grossAmountSelected = profile.getFeeInstallments().stream()
                .filter(inst -> request.getInstallmentNames().contains(inst.getInstallmentName()))
                .map(FeeInstallment::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        profile.getFeeInstallments().stream()
                .filter(installment -> request.getInstallmentNames().contains(installment.getInstallmentName()))
                .forEach(installment -> {
                    installment.setStatus("PAID");
                    installment.setAmountPaid(installment.getAmountDue());
                });
        profile.setPaidFees(profile.getPaidFees().add(request.getAmount()));
        profile.setDueFees(profile.getDueFees().subtract(grossAmountSelected));
        profile.setTotalDiscountGiven(profile.getTotalDiscountGiven().add(request.getDiscount()));
        PaymentRecord paymentRecord = PaymentRecord.builder()
                .studentId(profile.getId())
                .studentName(profile.getName())
                .receiptNumber(ReceiptGenerator.generate())
                .paymentDate(LocalDate.now())
                .amountPaid(request.getAmount())
                .discount(request.getDiscount())
                .paymentMode(request.getPaymentMode())
                .paidForMonths(request.getInstallmentNames())
                .remarks(request.getRemarks())
                .chequeDetails(request.getChequeDetails())
                .transactionId(request.getTransactionId())
                .build();
        PaymentRecord savedRecord = paymentRecordRepository.save(paymentRecord);
        profile.setLastPayment(savedRecord);
        studentFeeProfileRepository.save(profile);
        return mapToPaymentRecordResponse(savedRecord);
    }

    public StudentFeeProfileResponse getStudentFeeProfile(String studentId) {
        log.info("Fetching fee profile for student ID: {}", studentId);
        StudentFeeProfile profile = studentFeeProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        log.info("Found profile for student: {}", profile.getName());
        return mapToStudentFeeProfileResponse(profile);
    }

    /**
     * Searches for students across TWO collections:
     *
     * 1. student_fee_profiles — students who already have a fee profile (happy path).
     * 2. students             — students admitted before a fee structure was set up,
     *                           so no profile was auto-created for them. Returned with
     *                           zero fee amounts so the admin can still select them.
     *
     * This ensures students are always findable regardless of whether the admin
     * configured the fee structure before or after the student's admission.
     */
    /**
     * Returns all student fee profiles that have an outstanding due amount greater than zero,
     * sorted by due amount descending (highest dues first).
     */
    public List<StudentFeeProfileResponse> getOutstandingDues() {
        return studentFeeProfileRepository.findAll()
                .stream()
                .filter(p -> p.getDueFees() != null && p.getDueFees().compareTo(java.math.BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(StudentFeeProfile::getDueFees).reversed())
                .map(this::mapToStudentFeeProfileResponse)
                .collect(Collectors.toList());
    }

    public List<StudentFeeProfileResponse> searchStudents(String name, String className, String rollNumber) {
        log.info("Searching for students with name: [{}], class: [{}], roll: [{}]", name, className, rollNumber);

        // ── 1. Search student_fee_profiles ───────────────────────────────────
        final Query profileQuery = new Query();
        final List<Criteria> profileCriteria = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            profileCriteria.add(Criteria.where("name").regex(name, "i"));
        }
        if (className != null && !className.isEmpty()) {
            profileCriteria.add(Criteria.where("className").is(className));
        }
        if (rollNumber != null && !rollNumber.isEmpty()) {
            profileCriteria.add(Criteria.where("rollNumber").is(rollNumber));
        }
        if (!profileCriteria.isEmpty()) {
            profileQuery.addCriteria(new Criteria().andOperator(profileCriteria.toArray(new Criteria[0])));
        }
        List<StudentFeeProfile> profileStudents = mongoTemplate.find(profileQuery, StudentFeeProfile.class);

        // Collect IDs already covered by profiles so we don't duplicate them below
        Set<String> profileIds = profileStudents.stream()
                .map(StudentFeeProfile::getId)
                .collect(Collectors.toSet());

        // ── 2. Search students collection for those WITHOUT a fee profile ─────
        final Query studentQuery = new Query();
        final List<Criteria> studentCriteria = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            studentCriteria.add(Criteria.where("fullName").regex(name, "i"));
        }
        if (className != null && !className.isEmpty()) {
            studentCriteria.add(Criteria.where("classForAdmission").is(className));
        }
        if (rollNumber != null && !rollNumber.isEmpty()) {
            studentCriteria.add(Criteria.where("rollNumber").is(rollNumber));
        }
        if (!studentCriteria.isEmpty()) {
            studentQuery.addCriteria(new Criteria().andOperator(studentCriteria.toArray(new Criteria[0])));
        }
        List<Student> rawStudents = mongoTemplate.find(studentQuery, Student.class);
        List<Student> studentsWithoutProfile = rawStudents.stream()
                .filter(s -> !profileIds.contains(s.getId()))
                .collect(Collectors.toList());

        // ── 3. Merge and return ───────────────────────────────────────────────
        List<StudentFeeProfileResponse> result = new ArrayList<>();
        result.addAll(profileStudents.stream()
                .map(this::mapToStudentFeeProfileResponse)
                .collect(Collectors.toList()));
        result.addAll(studentsWithoutProfile.stream()
                .map(this::mapStudentToMinimalFeeProfileResponse)
                .collect(Collectors.toList()));

        log.info("Found {} student(s) — {} with fee profile, {} without.",
                result.size(), profileStudents.size(), studentsWithoutProfile.size());
        return result;
    }

    /** Minimal fee profile response for students who have no StudentFeeProfile yet. */
    private StudentFeeProfileResponse mapStudentToMinimalFeeProfileResponse(Student student) {
        String parentName = (student.getParentDetails() != null)
                ? student.getParentDetails().getFatherName() : "";
        String rollNumber = (student.getRollNumber() != null) ? student.getRollNumber() : "";
        return StudentFeeProfileResponse.builder()
                .id(student.getId())
                .name(student.getFullName())
                .className(student.getClassForAdmission())
                .rollNumber(rollNumber)
                .parentName(parentName)
                .totalFees(BigDecimal.ZERO)
                .paidFees(BigDecimal.ZERO)
                .dueFees(BigDecimal.ZERO)
                .totalDiscountGiven(BigDecimal.ZERO)
                .feeInstallments(Collections.emptyList())
                .lastPayment(null)
                .build();
    }

    public FeeReportResponse generateCollectionReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating fee collection report from {} to {}", startDate, endDate);

        // 1. Match stage: Filters payments by date if provided
        List<Criteria> criteriaList = new ArrayList<>();
        if (startDate != null) {
            criteriaList.add(Criteria.where("paymentDate").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("paymentDate").lte(endDate));
        }
        Criteria criteria = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList);
        MatchOperation matchStage = match(criteria);

        // 2. AddFields: convert amountPaid and discount (stored as String) into Decimal
        AggregationOperation addFieldsOperation = context -> new Document("$addFields",
                new Document("amountPaid", new Document("$toDecimal", "$amountPaid"))
                        .append("discount", new Document("$toDecimal", "$discount"))
        );

        // 3. Lookup students collection with ObjectId conversion
        AggregationOperation lookupOperation = context -> {
            Document lookupPipeline = new Document("$lookup",
                    new Document("from", "students")
                            .append("let", new Document("studentIdStr", "$studentId"))
                            .append("pipeline", Arrays.asList(
                                    new Document("$match",
                                            new Document("$expr",
                                                    new Document("$eq", Arrays.asList(
                                                            "$_id",
                                                            new Document("$toObjectId", "$$studentIdStr")
                                                    ))
                                            )
                                    )
                            ))
                            .append("as", "studentProfile")
            );
            return lookupPipeline;
        };

        // 4. Unwind the results from the lookup
        UnwindOperation unwindOperation = unwind("$studentProfile");

        // 5. Group by classForAdmission and payment mode to sum amounts
        GroupOperation groupByClassAndMode = group(
                Fields.fields()
                        .and("classForAdmission", "$studentProfile.classForAdmission")
                        .and("paymentMode", "$paymentMode")
        ).sum("amountPaid").as("totalAmount");

        // 6. Group again by just classForAdmission
        GroupOperation groupByClass = group("$_id.classForAdmission")
                .sum("totalAmount").as("totalCollectedInClass")
                .push(new Document("paymentMode", "$_id.paymentMode")
                        .append("totalAmount", "$totalAmount"))
                .as("paymentModeBreakdown");

        // 7. Project the final fields
        ProjectionOperation projectStage = project("totalCollectedInClass", "paymentModeBreakdown")
                .and("_id").as("classForAdmission")
                .andExclude("_id");

        // 8. Build the final aggregation
        Aggregation aggregation = newAggregation(
                matchStage,
                addFieldsOperation,
                lookupOperation,
                unwindOperation,
                groupByClassAndMode,
                groupByClass,
                projectStage
        );

        // 9. Execute the aggregation
        AggregationResults<ClassWiseFeeSummary> results = mongoTemplate.aggregate(
                aggregation, "payment_records", ClassWiseFeeSummary.class
        );

        List<ClassWiseFeeSummary> classSummaries = results.getMappedResults();
        BigDecimal grandTotal = classSummaries.stream()
                .map(ClassWiseFeeSummary::getTotalCollectedInClass)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FeeReportResponse report = new FeeReportResponse();
        report.setGrandTotalCollected(grandTotal);
        report.setClassSummaries(classSummaries);

        log.info("Report generation complete. Grand total: {}", grandTotal);
        return report;
    }


    // --- Helper Methods for Mapping ---
    private PaymentRecordResponse mapToPaymentRecordResponse(PaymentRecord record) {
        if (record == null) return null;
        return PaymentRecordResponse.builder().transactionId(record.getId()).studentId(record.getStudentId()).studentName(record.getStudentName()).receiptNumber(record.getReceiptNumber()).paymentDate(record.getPaymentDate()).amountPaid(record.getAmountPaid()).discount(record.getDiscount()).paymentMode(record.getPaymentMode().toString()).paidForInstallments(record.getPaidForMonths()).remarks(record.getRemarks()).build();
    }
    private StudentFeeProfileResponse mapToStudentFeeProfileResponse(StudentFeeProfile profile) {
        if (profile == null) return null;
        List<FeeInstallmentResponse> installmentResponses = (profile.getFeeInstallments() != null) ? profile.getFeeInstallments().stream().map(this::mapToFeeInstallmentResponse).collect(Collectors.toList()) : Collections.emptyList();
        PaymentRecordResponse lastPaymentResponse = mapToPaymentRecordResponse(profile.getLastPayment());
        return StudentFeeProfileResponse.builder().id(profile.getId()).name(profile.getName()).className(profile.getClassName()).rollNumber(profile.getRollNumber()).parentName(profile.getParentName()).totalFees(profile.getTotalFees()).paidFees(profile.getPaidFees()).dueFees(profile.getDueFees()).totalDiscountGiven(profile.getTotalDiscountGiven()).feeInstallments(installmentResponses).lastPayment(lastPaymentResponse).build();
    }
    private FeeInstallmentResponse mapToFeeInstallmentResponse(FeeInstallment installment) {
        return FeeInstallmentResponse.builder().installmentName(installment.getInstallmentName()).amountDue(installment.getAmountDue()).status(installment.getStatus()).build();
    }
}
