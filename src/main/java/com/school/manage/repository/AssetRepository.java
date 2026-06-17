package com.school.manage.repository;

import com.school.manage.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends MongoRepository<Asset, String> {
    List<Asset> findByActiveTrueOrderByNameAsc();
    List<Asset> findByCategoryAndActiveTrue(String category);
    List<Asset> findByConditionAndActiveTrue(String condition);
    Optional<Asset> findByAssetCodeAndActiveTrue(String assetCode);
    List<Asset> findByLocationAndActiveTrue(String location);
}
