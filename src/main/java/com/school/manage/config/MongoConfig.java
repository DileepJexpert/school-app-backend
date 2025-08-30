package com.school.manage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Configuration class to register custom converters for MongoDB.
 * This resolves the InaccessibleObjectException for BigDecimal on Java 17+.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new BigDecimalToStringConverter(),
            new StringToBigDecimalConverter()
        ));
    }

    /**
     * Converts a BigDecimal from the Java model to a String for storing in MongoDB.
     */
    private static class BigDecimalToStringConverter implements Converter<BigDecimal, String> {
        @Override
        public String convert(@NonNull BigDecimal source) {
            return source.toString();
        }
    }

    /**
     * Converts a String from the MongoDB document back to a BigDecimal in the Java model.
     */
    private static class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
        @Override
        public BigDecimal convert(@NonNull String source) {
            return new BigDecimal(source);
        }
    }
}
