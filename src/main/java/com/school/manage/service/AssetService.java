package com.school.manage.service;

import com.school.manage.model.Asset;
import com.school.manage.model.MaintenanceRecord;
import com.school.manage.repository.AssetRepository;
import com.school.manage.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final MongoTemplate mongoTemplate;

    public Asset addAsset(Asset asset) {
        // Auto-generate asset code
        long count = assetRepository.count();
        asset.setAssetCode(String.format("AST-%03d", count + 1));
        asset.setCreatedAt(LocalDateTime.now());
        asset.setActive(true);
        log.info("[AssetService] Adding asset: name='{}', code='{}'", asset.getName(), asset.getAssetCode());
        return assetRepository.save(asset);
    }

    public Asset updateAsset(String id, Asset asset) {
        Asset existing = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
        // Preserve immutable fields
        asset.setId(existing.getId());
        asset.setAssetCode(existing.getAssetCode());
        asset.setCreatedAt(existing.getCreatedAt());
        asset.setUpdatedAt(LocalDateTime.now());
        log.info("[AssetService] Updating asset: id='{}', name='{}'", id, asset.getName());
        return assetRepository.save(asset);
    }

    public void deleteAsset(String id) {
        Asset existing = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
        existing.setActive(false);
        existing.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(existing);
        log.info("[AssetService] Soft-deleted asset: id='{}'", id);
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findByActiveTrueOrderByNameAsc();
    }

    public Asset getAssetById(String id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
    }

    public List<Asset> getAssetsByCategory(String category) {
        return assetRepository.findByCategoryAndActiveTrue(category);
    }

    public List<Asset> getAssetsByCondition(String condition) {
        return assetRepository.findByConditionAndActiveTrue(condition);
    }

    public List<Asset> searchAssets(String queryStr) {
        Query query = new Query(
                Criteria.where("active").is(true)
                        .and("name").regex(queryStr, "i")
        );
        return mongoTemplate.find(query, Asset.class);
    }

    public Map<String, Object> getAssetStats() {
        List<Asset> activeAssets = assetRepository.findByActiveTrueOrderByNameAsc();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAssets", activeAssets.size());

        // Total value: sum of purchasePrice * quantity
        double totalValue = activeAssets.stream()
                .mapToDouble(a -> a.getPurchasePrice() * a.getQuantity())
                .sum();
        stats.put("totalValue", totalValue);

        // Category breakdown: count per category
        Map<String, Long> categoryBreakdown = activeAssets.stream()
                .filter(a -> a.getCategory() != null)
                .collect(Collectors.groupingBy(Asset::getCategory, Collectors.counting()));
        stats.put("categoryBreakdown", categoryBreakdown);

        // Condition breakdown: count per condition
        Map<String, Long> conditionBreakdown = activeAssets.stream()
                .filter(a -> a.getCondition() != null)
                .collect(Collectors.groupingBy(Asset::getCondition, Collectors.counting()));
        stats.put("conditionBreakdown", conditionBreakdown);

        return stats;
    }

    // --- Maintenance Record methods ---

    public MaintenanceRecord addMaintenanceRecord(MaintenanceRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        log.info("[AssetService] Adding maintenance record for asset: '{}'", record.getAssetId());
        return maintenanceRecordRepository.save(record);
    }

    public MaintenanceRecord updateMaintenanceRecord(String id, MaintenanceRecord record) {
        MaintenanceRecord existing = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found: " + id));
        record.setId(existing.getId());
        record.setCreatedAt(existing.getCreatedAt());
        log.info("[AssetService] Updating maintenance record: id='{}'", id);
        return maintenanceRecordRepository.save(record);
    }

    public List<MaintenanceRecord> getMaintenanceHistory(String assetId) {
        return maintenanceRecordRepository.findByAssetIdOrderByScheduledDateDesc(assetId);
    }

    public List<MaintenanceRecord> getUpcomingMaintenance() {
        return maintenanceRecordRepository.findByStatus("SCHEDULED").stream()
                .filter(r -> r.getScheduledDate() != null && !r.getScheduledDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public MaintenanceRecord completeMaintenanceRecord(String id) {
        MaintenanceRecord existing = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found: " + id));
        existing.setStatus("COMPLETED");
        existing.setCompletedDate(LocalDate.now());
        log.info("[AssetService] Completed maintenance record: id='{}'", id);
        return maintenanceRecordRepository.save(existing);
    }
}
