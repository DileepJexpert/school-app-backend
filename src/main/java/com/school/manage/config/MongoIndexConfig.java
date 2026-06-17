package com.school.manage.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.school.manage.model.School;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Creates MongoDB indexes that cannot be expressed via Spring Data annotations.
 *
 * Currently maintains:
 *   - A partial unique index on {@code users.email} that ignores null and empty
 *     values, allowing multiple users to have no email while preventing
 *     duplicate non-empty emails within the same database.
 *
 * The index is created on platform_db and on every active tenant database.
 */
@Slf4j
@Component
public class MongoIndexConfig {

    private final MongoClient mongoClient;
    private final MongoTemplate platformMongoTemplate;

    public MongoIndexConfig(MongoClient mongoClient,
                            @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.mongoClient = mongoClient;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndexes() {
        // 1. Platform DB
        try {
            createEmailIndex(platformMongoTemplate.getCollection("users"));
            log.info("Ensured partial unique index on platform_db.users.email");
        } catch (Exception e) {
            log.warn("Could not create partial unique index on platform_db users.email: {}", e.getMessage());
        }

        // 2. Every active tenant DB
        List<School> schools = platformMongoTemplate.find(
                Query.query(Criteria.where("active").is(true)), School.class);

        for (School school : schools) {
            try {
                String dbName = school.getTenantId() + "_db";
                MongoCollection<Document> collection =
                        mongoClient.getDatabase(dbName).getCollection("users");
                createEmailIndex(collection);
                log.info("Ensured partial unique index on {}.users.email", dbName);
            } catch (Exception e) {
                log.warn("Could not create partial unique index on {}_db users.email: {}",
                        school.getTenantId(), e.getMessage());
            }
        }
    }

    /**
     * Creates a partial unique index on the {@code email} field.
     *
     * The partial filter expression restricts the index to documents where
     * {@code email} is a non-empty string, so null / missing / empty-string
     * values are excluded from the uniqueness constraint.
     */
    private void createEmailIndex(MongoCollection<Document> collection) {
        Document filter = new Document("$and", Arrays.asList(
                new Document("email", new Document("$type", "string")),
                new Document("email", new Document("$gt", ""))
        ));

        collection.createIndex(
                Indexes.ascending("email"),
                new IndexOptions()
                        .unique(true)
                        .partialFilterExpression(filter)
                        .name("email_unique_partial")
                        .background(true)
        );
    }
}
