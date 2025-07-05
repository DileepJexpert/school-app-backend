package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "payment_records") // Marks this class as a MongoDB collection
public class PaymentRecord {

    @Id
    private String id; // A unique ID for each payment record

    private String studentId; // Links the payment to a student

    private String receiptNumber;
    private LocalDate paymentDate;
    private double amountPaid;
    private String paymentMode; // e.g., "Cash", "Online", "Cheque"
    private List<String> paidForMonths; // e.g., ["April 2024", "May 2024"]
    private String remarks;
    private String chequeDetails; // (Optional) Cheque number, bank, etc.
    private String transactionId; // (Optional) For online payments
}