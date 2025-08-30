package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class FeeReportResponse {
    private Summary summary;
    private List<ClassWiseFeeSummary> classSummaries;     // ✅ dto version
    private List<PaymentModeSummary> paymentModeSummary;  // ✅ dto version
    private Page<TransactionResponse> transactionsPage;
    private Filters filters;
}
