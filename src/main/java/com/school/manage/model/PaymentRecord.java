package com.school.manage.model;

import com.school.manage.enums.PaymentMode;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Document(collection = "payment_records")
public class PaymentRecord {

    @Id
    private String id;
    private String studentId;
    private String studentName;
    private String receiptNumber;
    private LocalDate paymentDate;
    private BigDecimal amountPaid; // This is the net amount paid after discount
    
    /** --- NEW FIELD ---
     * Stores the discount amount applied to this specific transaction.
     */
    private BigDecimal discount;

    private PaymentMode paymentMode;
    private List<String> paidForMonths;
    private String remarks;
    private String chequeDetails;
    private String transactionId;
}
