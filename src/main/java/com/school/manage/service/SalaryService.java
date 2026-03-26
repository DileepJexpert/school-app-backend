package com.school.manage.service;

import com.school.manage.model.SalaryRecord;
import com.school.manage.model.Staff;
import com.school.manage.repository.SalaryRecordRepository;
import com.school.manage.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryRecordRepository salaryRecordRepository;
    private final StaffRepository staffRepository;

    /**
     * Generate salary records for all active staff for a given month/year.
     * Skips staff who already have a record for that month.
     */
    public List<SalaryRecord> generateMonthlySalary(int month, int year, String generatedBy) {
        log.info("[SalaryService] Generating salary for month={}, year={}", month, year);
        List<Staff> activeStaff = staffRepository.findByStatus("ACTIVE");
        List<SalaryRecord> generated = new ArrayList<>();

        for (Staff staff : activeStaff) {
            // Skip if already generated
            if (salaryRecordRepository.findByStaffIdAndMonthAndYear(staff.getId(), month, year).isPresent()) {
                log.debug("Salary already exists for staff '{}' for {}/{}", staff.getEmployeeId(), month, year);
                continue;
            }

            SalaryRecord record = new SalaryRecord();
            record.setStaffId(staff.getId());
            record.setStaffName(staff.getFullName());
            record.setDepartment(staff.getDepartment());
            record.setDesignation(staff.getDesignation());
            record.setMonth(month);
            record.setYear(year);

            // Calculate salary components (basic formula, can be customized)
            double basic = staff.getBasicSalary();
            record.setBasicPay(basic);
            record.setHra(Math.round(basic * 0.20 * 100.0) / 100.0);  // 20% HRA
            record.setDa(Math.round(basic * 0.10 * 100.0) / 100.0);   // 10% DA
            record.setTa(Math.round(basic * 0.05 * 100.0) / 100.0);   // 5% TA
            record.setOtherAllowances(0);

            double gross = basic + record.getHra() + record.getDa() + record.getTa();
            record.setGrossSalary(Math.round(gross * 100.0) / 100.0);

            // Deductions
            record.setPf(Math.round(basic * 0.12 * 100.0) / 100.0);   // 12% PF
            record.setTax(0); // TDS calculation would go here
            record.setOtherDeductions(0);

            double deductions = record.getPf() + record.getTax() + record.getOtherDeductions();
            record.setTotalDeductions(Math.round(deductions * 100.0) / 100.0);
            record.setNetSalary(Math.round((gross - deductions) * 100.0) / 100.0);

            record.setStatus("GENERATED");
            record.setGeneratedBy(generatedBy);
            record.setGeneratedAt(LocalDateTime.now());

            generated.add(salaryRecordRepository.save(record));
        }

        log.info("[SalaryService] Generated {} salary records for {}/{}", generated.size(), month, year);
        return generated;
    }

    public List<SalaryRecord> getSalaryByMonth(int month, int year) {
        return salaryRecordRepository.findByMonthAndYear(month, year);
    }

    public List<SalaryRecord> getSalaryByStaff(String staffId) {
        return salaryRecordRepository.findByStaffId(staffId);
    }

    public SalaryRecord markAsPaid(String recordId, String paymentMode, String transactionRef) {
        SalaryRecord record = salaryRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Salary record not found: " + recordId));
        record.setStatus("PAID");
        record.setPaymentMode(paymentMode);
        record.setTransactionRef(transactionRef);
        record.setPaidAt(LocalDateTime.now());
        return salaryRecordRepository.save(record);
    }
}
