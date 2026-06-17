package com.school.manage.service;

import com.school.manage.enums.UserRole;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Payroll;
import com.school.manage.model.StaffAttendance;
import com.school.manage.model.User;
import com.school.manage.repository.PayrollRepository;
import com.school.manage.repository.StaffAttendanceRepository;
import com.school.manage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;

    public List<Payroll> generatePayroll(String month, int year) {
        List<User> staffUsers = userRepository.findByRoleIn(
                Arrays.asList(UserRole.TEACHER, UserRole.SCHOOL_ADMIN));

        // Determine working days in the month
        Month monthEnum = Month.valueOf(month.toUpperCase());
        YearMonth yearMonth = YearMonth.of(year, monthEnum);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // Get the display name for the month (e.g. "June")
        String monthDisplayName = monthEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<Payroll> payrollList = new ArrayList<>();
        for (User user : staffUsers) {
            Payroll payroll = new Payroll();
            payroll.setUserId(user.getId());
            payroll.setStaffName(user.getFullName());
            payroll.setMonth(monthDisplayName);
            payroll.setYear(year);
            payroll.setStatus("DRAFT");
            payroll.setCreatedAt(LocalDateTime.now());

            // Calculate attendance-based fields
            List<StaffAttendance> attendanceRecords =
                    staffAttendanceRepository.findByUserIdAndDateBetween(user.getId(), monthStart, monthEnd);

            int workingDays = attendanceRecords.size();
            int presentDays = (int) attendanceRecords.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                    .count();
            int leaveDays = (int) attendanceRecords.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()) || "ON_LEAVE".equals(a.getStatus()))
                    .count();

            payroll.setWorkingDays(workingDays);
            payroll.setPresentDays(presentDays);
            payroll.setLeaveDays(leaveDays);

            // Compute salary totals
            double gross = payroll.getBasicSalary() + payroll.getHra() + payroll.getDa()
                    + payroll.getTa() + payroll.getOtherAllowances();
            payroll.setGrossSalary(gross);

            double net = gross - payroll.getPf() - payroll.getTax() - payroll.getOtherDeductions();
            payroll.setNetSalary(net);

            payrollList.add(payroll);
        }

        return payrollRepository.saveAll(payrollList);
    }

    public Payroll updatePayroll(String id, Payroll payroll) {
        Payroll existing = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
        payroll.setId(existing.getId());
        payroll.setCreatedAt(existing.getCreatedAt());
        payroll.setUpdatedAt(LocalDateTime.now());

        // Recompute salary totals
        double gross = payroll.getBasicSalary() + payroll.getHra() + payroll.getDa()
                + payroll.getTa() + payroll.getOtherAllowances();
        payroll.setGrossSalary(gross);

        double net = gross - payroll.getPf() - payroll.getTax() - payroll.getOtherDeductions();
        payroll.setNetSalary(net);

        return payrollRepository.save(payroll);
    }

    public Payroll processPayroll(String id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
        payroll.setStatus("PROCESSED");
        payroll.setUpdatedAt(LocalDateTime.now());
        return payrollRepository.save(payroll);
    }

    public Payroll markAsPaid(String id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
        payroll.setStatus("PAID");
        payroll.setPaymentDate(LocalDate.now());
        payroll.setUpdatedAt(LocalDateTime.now());
        return payrollRepository.save(payroll);
    }

    public List<Payroll> getByMonth(String month, int year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }

    public List<Payroll> getByStaff(String userId) {
        return payrollRepository.findByUserId(userId);
    }

    public List<Payroll> getByStaffAndYear(String userId, int year) {
        return payrollRepository.findByUserIdAndYear(userId, year);
    }

    public Payroll getById(String id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
    }

    public Map<String, Object> getStats(String month, int year) {
        List<Payroll> records = payrollRepository.findByMonthAndYear(month, year);

        double totalGrossSalary = records.stream().mapToDouble(Payroll::getGrossSalary).sum();
        double totalNetSalary = records.stream().mapToDouble(Payroll::getNetSalary).sum();
        long paidCount = records.stream().filter(p -> "PAID".equals(p.getStatus())).count();
        long pendingCount = records.stream().filter(p -> !"PAID".equals(p.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStaff", records.size());
        stats.put("totalGrossSalary", totalGrossSalary);
        stats.put("totalNetSalary", totalNetSalary);
        stats.put("paidCount", paidCount);
        stats.put("pendingCount", pendingCount);
        return stats;
    }
}
