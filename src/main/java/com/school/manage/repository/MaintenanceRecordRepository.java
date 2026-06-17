package com.school.manage.repository;

import com.school.manage.model.MaintenanceRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceRecordRepository extends MongoRepository<MaintenanceRecord, String> {
    List<MaintenanceRecord> findByAssetIdOrderByScheduledDateDesc(String assetId);
    List<MaintenanceRecord> findByStatus(String status);
    List<MaintenanceRecord> findByScheduledDateBetween(LocalDate from, LocalDate to);
}
