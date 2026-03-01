package com.school.manage.service;

import com.school.manage.model.FeeComponent;
import com.school.manage.model.FeeInstallment;
import com.school.manage.model.FeeStructure;
import com.school.manage.model.Student;
import com.school.manage.model.StudentFeeProfile;
import com.school.manage.repository.FeeStructureRepository;
import com.school.manage.repository.StudentFeeProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This service is responsible for automatically generating a detailed fee profile
 * for a student upon their admission.
 */
@Service
public class FeeProfileService {

    private static final Logger logger = LoggerFactory.getLogger(FeeProfileService.class);

    private final FeeStructureRepository feeStructureRepository;
    private final StudentFeeProfileRepository studentFeeProfileRepository;

    @Autowired
    public FeeProfileService(FeeStructureRepository feeStructureRepository, StudentFeeProfileRepository studentFeeProfileRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.studentFeeProfileRepository = studentFeeProfileRepository;
    }

    /**
     * Creates and saves a complete fee profile for a newly admitted student.
     *
     * @param student The newly admitted student object, which must have an ID.
     */
    public void createFeeProfileForNewStudent(Student student) {
        logger.info("Generating fee profile for new student: {} (ID: {})", student.getFullName(), student.getId());

        FeeStructure feeStructure = feeStructureRepository.findByClassNameAndAcademicYear(
                student.getClassForAdmission(),
                student.getAcademicYear()
        ).orElse(null);

        if (feeStructure == null || feeStructure.getFeeComponents() == null || feeStructure.getFeeComponents().isEmpty()) {
            logger.warn("No fee structure found for class '{}' and year '{}'. No fee profile will be created.",
                    student.getClassForAdmission(), student.getAcademicYear());
            return;
        }

        // --- CORRECTED LOGIC ---

        // 1. Calculate the total for each frequency type first.
        BigDecimal totalMonthlyAmount = feeStructure.getFeeComponents().stream()
                .filter(c -> "MONTHLY".equalsIgnoreCase(c.getFrequency()))
                .map(FeeComponent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNonMonthlyAmount = feeStructure.getFeeComponents().stream()
                .filter(c -> "YEARLY".equalsIgnoreCase(c.getFrequency()) || "ONE_TIME".equalsIgnoreCase(c.getFrequency()))
                .map(FeeComponent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAnnualFee = totalMonthlyAmount.multiply(new BigDecimal(12)).add(totalNonMonthlyAmount);

        // 2. Now, build the list of installments.
        List<FeeInstallment> allInstallments = new ArrayList<>();

        // Create 12 installments for the combined monthly fees.
        if (totalMonthlyAmount.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate admissionDate = student.getDateOfAdmission() != null
                    ? student.getDateOfAdmission() : LocalDate.now();
            for (int i = 0; i < 12; i++) {
                Month month = Month.APRIL.plus(i);
                int year = admissionDate.getYear();
                if (month.getValue() < Month.APRIL.getValue()) {
                    year += 1;
                }
                FeeInstallment installment = new FeeInstallment();
                installment.setInstallmentId(UUID.randomUUID().toString());
                installment.setInstallmentName("Monthly Fee - " + month.name());
                installment.setAmountDue(totalMonthlyAmount);
                installment.setDueDate(LocalDate.of(year, month, 10));
                installment.setStatus("PENDING");
                allInstallments.add(installment);
            }
        }

        // Create a single installment for each yearly or one-time fee.
        // This forEach loop now only adds to a list, it does not modify external variables, which fixes the error.
        feeStructure.getFeeComponents().stream()
                .filter(component -> "YEARLY".equalsIgnoreCase(component.getFrequency()) || "ONE_TIME".equalsIgnoreCase(component.getFrequency()))
                .forEach(component -> {
                    FeeInstallment installment = new FeeInstallment();
                    installment.setInstallmentId(UUID.randomUUID().toString());
                    installment.setInstallmentName(component.getFeeName());
                    installment.setAmountDue(component.getAmount());
                    installment.setDueDate((student.getDateOfAdmission() != null
                            ? student.getDateOfAdmission() : LocalDate.now()).plusDays(30));
                    installment.setStatus("PENDING");
                    allInstallments.add(installment);
                });


        // 3. Assemble and save the final StudentFeeProfile.
        StudentFeeProfile profile = new StudentFeeProfile();
        profile.setId(student.getId());
        profile.setName(student.getFullName());
        profile.setClassName(student.getClassForAdmission());
        profile.setAcademicYear(student.getAcademicYear());
        profile.setRollNumber(student.getRollNumber());
        profile.setParentName(student.getParentDetails() != null ? student.getParentDetails().getFatherName() : null);
        profile.setTotalFees(totalAnnualFee);
        profile.setPaidFees(BigDecimal.ZERO);
        profile.setDueFees(totalAnnualFee);
        profile.setFeeInstallments(allInstallments);
        profile.setLastPayment(null);

        studentFeeProfileRepository.save(profile);
        logger.info("Successfully created fee profile for student ID: {}", student.getId());
    }
}
