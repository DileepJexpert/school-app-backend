/*
// src/main/java/com/school/manage/config/DataInitializer.java
// Note: Ensure your package structure matches this path if you use it.
package com.school.manage.config; // Using package from user's error message

import com.school.manage.model.FeeInstallment; // Assuming models are in com.school.manage.model
import com.school.manage.model.Student;
import com.school.manage.model.PaymentRecord;
import com.school.manage.repository.FeeInstallmentRepository; // Assuming repositories are in com.school.manage.repository
import com.school.manage.repository.PaymentRecordRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional; // Optional for MongoDB

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final FeeInstallmentRepository feeInstallmentRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Override
    public void run(String... args) throws Exception {
        // Optional: Clean up existing data for a fresh start during development
        // studentRepository.deleteAll();
        // feeInstallmentRepository.deleteAll();
        // paymentRecordRepository.deleteAll();

        if (studentRepository.count() == 0) {
            System.out.println("---- No existing student data found, initializing sample data for MongoDB ----");
            createSampleStudents();
        } else {
            System.out.println("---- Student data already exists, skipping initialization ----");
        }
    }

    private void createSampleStudents() {
        // --- Student 1: Rohan Sharma ---
        // This constructor call assumes your Student class (in com.school.manage.model.Student)
        // has a constructor that accepts (String id, String name, String className, String rollNumber, String parentName)
        // This is typically generated by Lombok's @AllArgsConstructor if these are all the fields.
        Student rohan = new Student(
                "S1001",            // id
                "Rohan Sharma",     // name
                "Class 10 A",       // className
                "15",               // rollNumber
                "Mr. Anil Sharma"   // parentName
        );
        studentRepository.save(rohan); // Save student first

        // Rohan's Fee Installments
        // Assuming FeeInstallment constructor is (String installmentId, String studentId, String monthYear, double tuitionFee, double transportFee, double otherCharges, double lateFineApplied, boolean isPaid, String paymentRecordId)
        FeeInstallment rohanApr = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "April 2024", 2500, 800, 0, 0, true, null);
        FeeInstallment rohanMay = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "May 2024", 2500, 800, 0, 0, true, null);
        FeeInstallment rohanJun = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "June 2024", 2500, 800, 0, 0, false, null);
        FeeInstallment rohanJul = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "July 2024", 2500, 800, 0, 0, false, null);
        FeeInstallment rohanAug = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "August 2024", 2500, 800, 0, 100, false, null);
        FeeInstallment rohanSep = new FeeInstallment(UUID.randomUUID().toString(), rohan.getId(), "September 2024", 2500, 800, 0, 0, false, null);

        List<FeeInstallment> rohanInstallments = new ArrayList<>(Arrays.asList(rohanApr, rohanMay, rohanJun, rohanJul, rohanAug, rohanSep));
        feeInstallmentRepository.saveAll(rohanInstallments);

        // Rohan's Last Payment (mock)
        // Assuming PaymentRecord constructor is (String receiptNumber, String studentId, LocalDate paymentDate, double amountPaid, String paymentMode, String remarks, String chequeDetails, String transactionId, List<String> paidForMonths)
        PaymentRecord rohanLastPayment = new PaymentRecord(
                "RCPT12345",
                rohan.getId(),
                LocalDate.of(2024, 5, 10),
                6600.00,
                "Digital Payment",
                "Paid for Apr, May",
                null,
                "TXN_ROHAN_MAY",
                Arrays.asList("April 2024", "May 2024")
        );
        paymentRecordRepository.save(rohanLastPayment);

        rohanApr.setPaymentRecordId(rohanLastPayment.getReceiptNumber());
        rohanMay.setPaymentRecordId(rohanLastPayment.getReceiptNumber());
        feeInstallmentRepository.saveAll(Arrays.asList(rohanApr, rohanMay));


        // --- Student 2: Priya Singh ---
        Student priya = new Student(
                "S1002",            // id
                "Priya Singh",      // name
                "Class 10 A",       // className
                "22",               // rollNumber
                "Mr. Vijay Singh"   // parentName
        );
        studentRepository.save(priya);

        FeeInstallment priyaApr = new FeeInstallment(UUID.randomUUID().toString(), priya.getId(), "April 2024", 2600, 0, 0, 0, true, null);
        FeeInstallment priyaMay = new FeeInstallment(UUID.randomUUID().toString(), priya.getId(), "May 2024", 2600, 0, 0, 0, false, null);
        FeeInstallment priyaJun = new FeeInstallment(UUID.randomUUID().toString(), priya.getId(), "June 2024", 2600, 0, 0, 0, false, null);

        List<FeeInstallment> priyaInstallments = new ArrayList<>(Arrays.asList(priyaApr, priyaMay, priyaJun));
        feeInstallmentRepository.saveAll(priyaInstallments);

        PaymentRecord priyaLastPayment = new PaymentRecord(
                "RCPT12346",
                priya.getId(),
                LocalDate.of(2024, 4, 15),
                2600.00,
                "Cash",
                "Paid for April",
                null, null,
                List.of("April 2024")
        );
        paymentRecordRepository.save(priyaLastPayment);

        priyaApr.setPaymentRecordId(priyaLastPayment.getReceiptNumber());
        feeInstallmentRepository.save(priyaApr);

        System.out.println("---- Sample Students and Fee Data Initialized for MongoDB ----");
    }
}
*/
