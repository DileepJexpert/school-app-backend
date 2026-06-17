package com.school.manage.controller;

import com.school.manage.model.Asset;
import com.school.manage.model.MaintenanceRecord;
import com.school.manage.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Asset> addAsset(@RequestBody Asset asset) {
        log.info("[AssetController] POST /api/assets — name='{}'", asset.getName());
        Asset saved = assetService.addAsset(asset);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Asset>> getAllAssets() {
        log.debug("[AssetController] GET /api/assets");
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Asset> getAssetById(@PathVariable String id) {
        log.debug("[AssetController] GET /api/assets/{}", id);
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Asset> updateAsset(@PathVariable String id, @RequestBody Asset asset) {
        log.info("[AssetController] PUT /api/assets/{}", id);
        return ResponseEntity.ok(assetService.updateAsset(id, asset));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable String id) {
        log.info("[AssetController] DELETE /api/assets/{}", id);
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Asset>> getAssetsByCategory(@PathVariable String category) {
        log.debug("[AssetController] GET /api/assets/category/{}", category);
        return ResponseEntity.ok(assetService.getAssetsByCategory(category));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getAssetStats() {
        log.debug("[AssetController] GET /api/assets/stats");
        return ResponseEntity.ok(assetService.getAssetStats());
    }

    // --- Maintenance endpoints ---

    @PostMapping("/maintenance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<MaintenanceRecord> addMaintenanceRecord(@RequestBody MaintenanceRecord record) {
        log.info("[AssetController] POST /api/assets/maintenance — assetId='{}'", record.getAssetId());
        MaintenanceRecord saved = assetService.addMaintenanceRecord(record);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/maintenance/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<MaintenanceRecord> updateMaintenanceRecord(
            @PathVariable String id, @RequestBody MaintenanceRecord record) {
        log.info("[AssetController] PUT /api/assets/maintenance/{}", id);
        return ResponseEntity.ok(assetService.updateMaintenanceRecord(id, record));
    }

    @PutMapping("/maintenance/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<MaintenanceRecord> completeMaintenanceRecord(@PathVariable String id) {
        log.info("[AssetController] PUT /api/assets/maintenance/{}/complete", id);
        return ResponseEntity.ok(assetService.completeMaintenanceRecord(id));
    }

    @GetMapping("/maintenance/asset/{assetId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceHistory(@PathVariable String assetId) {
        log.debug("[AssetController] GET /api/assets/maintenance/asset/{}", assetId);
        return ResponseEntity.ok(assetService.getMaintenanceHistory(assetId));
    }

    @GetMapping("/maintenance/upcoming")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<MaintenanceRecord>> getUpcomingMaintenance() {
        log.debug("[AssetController] GET /api/assets/maintenance/upcoming");
        return ResponseEntity.ok(assetService.getUpcomingMaintenance());
    }
}
