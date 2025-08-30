// File: com/school/manage/model/FeeReportResponse.java
package com.school.manage.model;

import com.school.manage.enums.PaymentMode;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FeeReportResponse {
    private BigDecimal grandTotalCollected;
    private List<ClassWiseFeeSummary> classSummaries;
}





