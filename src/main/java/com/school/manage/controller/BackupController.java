package com.school.manage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.manage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BackupController {

    private final StudentRepository studentRepository;
    private final StudentFeeProfileRepository studentFeeProfileRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ExpenseRepository expenseRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentResultRepository studentResultRepository;
    private final TimetableRepository timetableRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final BusRepository busRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/backup")
    public ResponseEntity<byte[]> downloadBackup() throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            addEntry(zip, "students.json",           studentRepository.findAll());
            addEntry(zip, "fee_profiles.json",       studentFeeProfileRepository.findAll());
            addEntry(zip, "fee_structures.json",     feeStructureRepository.findAll());
            addEntry(zip, "payment_records.json",    paymentRecordRepository.findAll());
            addEntry(zip, "expenses.json",           expenseRepository.findAll());
            addEntry(zip, "attendance.json",         attendanceRepository.findAll());
            addEntry(zip, "results.json",            studentResultRepository.findAll());
            addEntry(zip, "timetable.json",          timetableRepository.findAll());
            addEntry(zip, "transport_routes.json",   transportRouteRepository.findAll());
            addEntry(zip, "buses.json",              busRepository.findAll());
            addEntry(zip, "notifications.json",      notificationRepository.findAll());
        }

        String filename = "school_backup_" + timestamp + ".zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(baos.toByteArray());
    }

    private void addEntry(ZipOutputStream zip, String filename, Object data) throws Exception {
        zip.putNextEntry(new ZipEntry(filename));
        zip.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data));
        zip.closeEntry();
    }
}
