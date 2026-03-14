package com.school.manage.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.school.manage.tenant.TenantAwareMongoDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * Multi-tenant MongoDB configuration.
 *
 * Creates:
 *   1. A shared MongoClient (single connection pool for the entire Atlas cluster)
 *   2. A TenantAwareMongoDatabaseFactory — picks the right DB per request thread
 *   3. The primary MongoTemplate — used by all Spring Data repositories (tenant-aware)
 *   4. A separate "platformMongoTemplate" — always points to platform_db (school registry)
 *
 * All existing repositories (StudentRepository, FeeStructureRepository, etc.) continue
 * to work with ZERO code changes — they just now operate on the correct per-tenant DB.
 */
@Configuration
public class MultiTenantMongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * Single MongoClient for the entire application (connection pool shared across tenants).
     * Replaces Spring Boot auto-configured MongoClient.
     */
    @Bean
    @Primary
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * Tenant-aware database factory.
     * Dynamically resolves "springfield" → "springfield_db", "dps" → "dps_db", etc.
     */
    @Bean
    @Primary
    public TenantAwareMongoDatabaseFactory mongoDatabaseFactory() {
        return new TenantAwareMongoDatabaseFactory(mongoClient());
    }

    /**
     * Primary MongoTemplate used by ALL Spring Data repositories.
     * Because it uses TenantAwareMongoDatabaseFactory, every query automatically
     * runs against the correct tenant database.
     *
     * The MappingMongoConverter is auto-wired by Spring and includes
     * the BigDecimal converters defined in MongoConfig.
     */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MappingMongoConverter converter) {
        return new MongoTemplate(mongoDatabaseFactory(), converter);
    }

    /**
     * Platform MongoTemplate — always points to platform_db regardless of tenant.
     * Used exclusively by SchoolOnboardingService to manage the school registry.
     *
     * Inject with: @Qualifier("platformMongoTemplate")
     */
    @Bean("platformMongoTemplate")
    public MongoTemplate platformMongoTemplate(MappingMongoConverter converter) {
        SimpleMongoClientDatabaseFactory platformFactory =
                new SimpleMongoClientDatabaseFactory(mongoClient(), TenantAwareMongoDatabaseFactory.getPlatformDb());
        return new MongoTemplate(platformFactory, converter);
    }
}
