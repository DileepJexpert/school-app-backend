package com.school.manage.tenant;

import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * A MongoDB database factory that dynamically picks the database based on the
 * current tenant stored in TenantContext.
 *
 * Tenant "springfield"  → database "springfield_db"
 * Tenant "dps_rohini"   → database "dps_rohini_db"
 * No tenant / default   → database "platform_db"
 *
 * This gives full database-level isolation: each school's data is completely
 * separate — no cross-school data leaks are possible even with bugs.
 */
public class TenantAwareMongoDatabaseFactory extends SimpleMongoClientDatabaseFactory {

    private static final Logger log = LoggerFactory.getLogger(TenantAwareMongoDatabaseFactory.class);
    private static final String PLATFORM_DB = "platform_db";
    private final MongoClient mongoClient;

    public TenantAwareMongoDatabaseFactory(MongoClient mongoClient) {
        // Pass platform_db as the default; getMongoDatabase() overrides this at runtime
        super(mongoClient, PLATFORM_DB);
        this.mongoClient = mongoClient;
    }

    @Override
    public com.mongodb.client.MongoDatabase getMongoDatabase() {
        String tenant = TenantContext.getTenant();
        String dbName = (tenant != null && !tenant.isBlank())
                ? tenant + "_db"
                : PLATFORM_DB;
        log.debug("[TenantFactory] Resolving DB — tenant='{}' → database='{}'", tenant, dbName);
        return mongoClient.getDatabase(dbName);
    }

    @Override
    public com.mongodb.client.MongoDatabase getMongoDatabase(String dbName) {
        return mongoClient.getDatabase(dbName);
    }

    public static String getPlatformDb() {
        return PLATFORM_DB;
    }
}
