package com.school.manage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordDto {
    private String receiptNumber;
    private LocalDate paymentDate;
    private double amountPaid;
    private String paymentMode;
    private List<String> paidForMonths;
    private String remarks;
    // You might not always want to send cheque/transaction details back in a list,
    // but include if needed for display.
    private String chequeDetails;
    private String transactionId;
}