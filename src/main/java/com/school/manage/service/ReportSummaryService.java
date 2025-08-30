package com.school.manage.service;

import com.mongodb.BasicDBObject;
import com.school.manage.enums.PaymentMode;
import com.school.manage.model.PaymentRecord;
import com.school.manage.model.StudentFeeProfile;
import com.school.manage.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSummaryService {

    private final MongoTemplate mongoTemplate;

    public FeeReportResponse generateCollectionReport(
            LocalDate startDate,
            LocalDate endDate,
            String className,
            String paymentMode,
            String search,
            int page,
            int size
    ) {
        // ------------------ 1. Build Filters ------------------
        List<Criteria> filters = new ArrayList<>();

        // 🔒 Temporarily disable date filters until PaymentRecord.paymentDate = LocalDateTime
        /*
        if (startDate != null) {
            filters.add(Criteria.where("paymentDate").gte(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            filters.add(Criteria.where("paymentDate").lte(endDate.atTime(23, 59, 59)));
        }
        */

        if (paymentMode != null && !"All Modes".equalsIgnoreCase(paymentMode)) {
            filters.add(Criteria.where("paymentMode").is(paymentMode.toUpperCase())); // normalize
        }
        if (search != null && !search.isBlank()) {
            filters.add(new Criteria().orOperator(
                    Criteria.where("studentName").regex(search, "i"),
                    Criteria.where("receiptNumber").regex(search, "i")
            ));
        }

        Criteria criteria = filters.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(filters.toArray(new Criteria[0]));

        MatchOperation matchStage = match(criteria);

        // Cast string amounts to Decimal
        AggregationOperation addFields = ctx -> new Document("$addFields",
                new Document("amountPaid", new Document("$toDecimal", "$amountPaid"))
                        .append("discount", new Document("$toDecimal", "$discount"))
        );

        log.info("Filters applied: startDate={}, endDate={}, className={}, paymentMode={}, search={}",
                startDate, endDate, className, paymentMode, search);

        // ------------------ 2. Summary Aggregation ------------------
        GroupOperation groupSummary = group()
                .sum("amountPaid").as("totalCollected")
                .sum("discount").as("totalDiscountGiven")
                .count().as("totalTransactions");

        Aggregation summaryAgg = newAggregation(matchStage, addFields, groupSummary);

        Document summaryDoc = mongoTemplate.aggregate(summaryAgg, "payment_records", Document.class)
                .getUniqueMappedResult();

        log.info("Summary aggregation result: {}", summaryDoc);

        Summary summary = Summary.builder()
                .totalCollected(toBigDecimal(summaryDoc != null ? summaryDoc.get("totalCollected") : null))
                .totalDiscountGiven(toBigDecimal(summaryDoc != null ? summaryDoc.get("totalDiscountGiven") : null))
                .totalTransactions(summaryDoc != null ? summaryDoc.getInteger("totalTransactions", 0) : 0)
                .build();

        // totalDue from student_fee_profiles
        Document dueDoc = mongoTemplate.aggregate(
                newAggregation(group().sum("dueFees").as("totalDue")),
                "student_fee_profiles",
                Document.class
        ).getUniqueMappedResult();

        BigDecimal totalDue = toBigDecimal(dueDoc != null ? dueDoc.get("totalDue") : null);
        summary.setTotalDue(totalDue);

        // ------------------ 3. Class-wise Breakdown ------------------

        // ✅ Lookup with ObjectId conversion
        AggregationOperation lookupWithCast = ctx -> new Document("$lookup",
                new Document("from", "student_fee_profiles")
                        .append("let", new Document("studentIdStr", "$studentId"))
                        .append("pipeline", Arrays.asList(
                                new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq", Arrays.asList(
                                                        "$_id", new Document("$toObjectId", "$$studentIdStr")
                                                ))
                                        )
                                )
                        ))
                        .append("as", "studentProfile")
        );

        GroupOperation groupByClassAndMode = group(
                Fields.fields()
                        .and("className", "$studentProfile.className")
                        .and("paymentMode", "$paymentMode")
        )
                .sum("amountPaid").as("totalAmount")
                .sum("discount").as("totalDiscount")
                .count().as("transactionCount");

        GroupOperation groupByClass = group("$_id.className")
                .sum("totalAmount").as("totalCollectedInClass")
                .sum("totalDiscount").as("totalDiscountInClass")
                .sum("transactionCount").as("transactionCountInClass")
                .push(new BasicDBObject("paymentMode", "$_id.paymentMode")
                        .append("totalAmount", "$totalAmount")
                        .append("totalDiscount", "$totalDiscount")
                        .append("transactionCount", "$transactionCount"))
                .as("paymentModeBreakdown");

        ProjectionOperation projectStage = project("totalCollectedInClass", "totalDiscountInClass",
                "transactionCountInClass", "paymentModeBreakdown")
                .and("_id").as("classForAdmission")
                .andExclude("_id");

        Aggregation classAgg = newAggregation(
                matchStage,
                addFields,
                lookupWithCast,
                unwind("studentProfile", true),
                groupByClassAndMode,
                groupByClass,
                projectStage
        );

        List<ClassWiseFeeSummary> classSummaries = mongoTemplate.aggregate(classAgg, "payment_records", Document.class)
                .getMappedResults()
                .stream()
                .map(doc -> {
                    log.debug("Class summary raw: {}", doc);
                    List<Document> pmBreakdown = (List<Document>) doc.get("paymentModeBreakdown");
                    List<PaymentModeBreakdown> breakdowns = pmBreakdown != null
                            ? pmBreakdown.stream().map(pm -> PaymentModeBreakdown.builder()
                            .paymentMode(pm.getString("paymentMode"))
                            .totalAmount(toBigDecimal(pm.get("totalAmount")))
                            .totalDiscount(toBigDecimal(pm.get("totalDiscount")))
                            .transactionCount(pm.getInteger("transactionCount", 0))
                            .build()).collect(Collectors.toList())
                            : Collections.emptyList();

                    return ClassWiseFeeSummary.builder()
                            .classForAdmission(doc.getString("classForAdmission"))
                            .totalCollectedInClass(toBigDecimal(doc.get("totalCollectedInClass")))
                            .totalDiscountInClass(toBigDecimal(doc.get("totalDiscountInClass")))
                            .transactionCountInClass(doc.getInteger("transactionCountInClass", 0))
                            .paymentModeBreakdown(breakdowns)
                            .build();
                }).collect(Collectors.toList());

        // ------------------ 4. Payment Mode Breakdown ------------------
        GroupOperation groupByMode = group("paymentMode")
                .sum("amountPaid").as("totalAmount");

        ProjectionOperation modeProject = project("totalAmount")
                .and("_id").as("paymentMode")
                .andExclude("_id");

        Aggregation modeAgg = newAggregation(matchStage, addFields, groupByMode, modeProject);

        List<PaymentModeSummary> paymentModeSummary = mongoTemplate.aggregate(modeAgg, "payment_records", Document.class)
                .getMappedResults()
                .stream()
                .map(doc -> {
                    log.debug("Payment mode summary raw: {}", doc);
                    return PaymentModeSummary.builder()
                            .paymentMode(doc.getString("paymentMode"))
                            .totalAmount(toBigDecimal(doc.get("totalAmount")))
                            .build();
                })
                .collect(Collectors.toList());

        // ------------------ 5. Transactions (Paginated) ------------------
        Query query = new Query(criteria).with(PageRequest.of(page, size));
        List<PaymentRecord> records = mongoTemplate.find(query, PaymentRecord.class, "payment_records");

        log.info("Fetched {} transactions from DB", records.size());

        List<TransactionResponse> transactionResponses = records.stream().map(record -> {
            StudentFeeProfile profile = mongoTemplate.findById(record.getStudentId(), StudentFeeProfile.class);
            return TransactionResponse.builder()
                    .id(record.getId())
                    .studentId(record.getStudentId())
                    .studentName(record.getStudentName())
                    .receiptNumber(record.getReceiptNumber())
                    .paymentDate(record.getPaymentDate())
                    .amountPaid(toBigDecimal(record.getAmountPaid()))
                    .discount(toBigDecimal(record.getDiscount()))
                    .paymentMode(record.getPaymentMode() != null ? record.getPaymentMode().name() : "UNKNOWN")
                    .paidForMonths(record.getPaidForMonths())
                    .remarks(record.getRemarks())
                    .className(profile != null ? profile.getClassName() : null)
                    .rollNumber(profile != null ? profile.getRollNumber() : null)
                    .collectedBy("Admin")
                    .build();
        }).toList();

        long totalElements = mongoTemplate.count(new Query(criteria), PaymentRecord.class, "payment_records");
        Page<TransactionResponse> pageResult = new PageImpl<>(transactionResponses, PageRequest.of(page, size), totalElements);

        // ------------------ 6. Filters ------------------
        List<String> classes = mongoTemplate.query(StudentFeeProfile.class)
                .distinct("className").as(String.class).all();
        List<String> modes = Arrays.stream(PaymentMode.values())
                .map(Enum::name).toList();

        Filters filtersResponse = Filters.builder()
                .classes(classes)
                .paymentModes(modes)
                .build();

        // ------------------ 7. Build Response ------------------
        return FeeReportResponse.builder()
                .summary(summary)
                .classSummaries(classSummaries)
                .paymentModeSummary(paymentModeSummary)
                .transactionsPage(pageResult)
                .filters(filtersResponse)
                .build();
    }

    // 🔧 Helper to safely convert any Mongo value → BigDecimal
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        if (value instanceof org.bson.types.Decimal128 dec) return dec.bigDecimalValue();
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            log.warn("Failed to convert value={} to BigDecimal, returning ZERO", value);
            return BigDecimal.ZERO;
        }
    }
}
