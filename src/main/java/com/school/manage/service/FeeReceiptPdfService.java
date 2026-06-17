package com.school.manage.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.PaymentRecord;
import com.school.manage.model.SchoolWebsite;
import com.school.manage.model.Student;
import com.school.manage.model.StudentFeeProfile;
import com.school.manage.repository.PaymentRecordRepository;
import com.school.manage.repository.StudentFeeProfileRepository;
import com.school.manage.repository.StudentRepository;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeeReceiptPdfService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final StudentRepository studentRepository;
    private final StudentFeeProfileRepository studentFeeProfileRepository;
    private final MongoTemplate mongoTemplate;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 102));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font TABLE_HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font TABLE_CELL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);

    private static final Color HEADER_BG = new Color(0, 51, 102);
    private static final Color ROW_ALT_BG = new Color(240, 240, 240);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public FeeReceiptPdfService(
            PaymentRecordRepository paymentRecordRepository,
            StudentRepository studentRepository,
            StudentFeeProfileRepository studentFeeProfileRepository,
            @Qualifier("platformMongoTemplate") MongoTemplate mongoTemplate) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.studentRepository = studentRepository;
        this.studentFeeProfileRepository = studentFeeProfileRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Generates a PDF receipt for a single fee payment record.
     */
    public byte[] generateReceipt(String feeRecordId) {
        PaymentRecord record = paymentRecordRepository.findById(feeRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record not found: " + feeRecordId));

        Student student = studentRepository.findById(record.getStudentId()).orElse(null);
        StudentFeeProfile feeProfile = studentFeeProfileRepository
                .findById(record.getStudentId()).orElse(null);

        SchoolWebsite school = getSchoolInfo();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addReceiptContent(doc, record, student, feeProfile, school);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[FeeReceipt] Failed to generate receipt for '{}': {}",
                    feeRecordId, e.getMessage(), e);
            throw new RuntimeException("Receipt generation failed: " + e.getMessage());
        }
    }

    /**
     * Generates a multi-page PDF containing receipts for all paid fees
     * in a given class and academic year.
     */
    public byte[] generateBulkReceipts(String className, String academicYear) {
        List<StudentFeeProfile> profiles = studentFeeProfileRepository.findByClassName(className);

        if (profiles.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No fee profiles found for class: " + className);
        }

        // Collect student IDs from the profiles
        List<String> studentIds = profiles.stream()
                .map(StudentFeeProfile::getId)
                .collect(Collectors.toList());

        // Get all payment records for these students
        List<PaymentRecord> allRecords = studentIds.stream()
                .flatMap(id -> paymentRecordRepository
                        .findByStudentIdOrderByPaymentDateDesc(id).stream())
                .collect(Collectors.toList());

        if (allRecords.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No payment records found for class: " + className);
        }

        SchoolWebsite school = getSchoolInfo();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            for (int i = 0; i < allRecords.size(); i++) {
                PaymentRecord record = allRecords.get(i);
                Student student = studentRepository.findById(record.getStudentId()).orElse(null);
                StudentFeeProfile feeProfile = studentFeeProfileRepository
                        .findById(record.getStudentId()).orElse(null);

                if (i > 0) {
                    doc.newPage();
                }
                addReceiptContent(doc, record, student, feeProfile, school);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[FeeReceipt] Failed to generate bulk receipts for class '{}': {}",
                    className, e.getMessage(), e);
            throw new RuntimeException("Bulk receipt generation failed: " + e.getMessage());
        }
    }

    private SchoolWebsite getSchoolInfo() {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(TenantContext.getTenant())),
                SchoolWebsite.class);
    }

    private void addReceiptContent(Document doc, PaymentRecord record,
                                   Student student, StudentFeeProfile feeProfile,
                                   SchoolWebsite school) throws Exception {

        String schoolName = school != null && school.getSchoolName() != null
                ? school.getSchoolName() : "School";
        String schoolAddress = school != null && school.getAddress() != null
                ? school.getAddress() : "";
        String schoolPhone = school != null && school.getPhone() != null
                ? school.getPhone() : "";

        // ── School Header ──
        Paragraph title = new Paragraph(schoolName, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        if (!schoolAddress.isEmpty()) {
            Paragraph addr = new Paragraph(schoolAddress, SUBTITLE_FONT);
            addr.setAlignment(Element.ALIGN_CENTER);
            doc.add(addr);
        }
        if (!schoolPhone.isEmpty()) {
            Paragraph phone = new Paragraph("Phone: " + schoolPhone, SUBTITLE_FONT);
            phone.setAlignment(Element.ALIGN_CENTER);
            doc.add(phone);
        }

        doc.add(new Paragraph(" "));

        // ── Receipt Title ──
        Paragraph receiptTitle = new Paragraph("FEE RECEIPT", HEADER_FONT);
        receiptTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(receiptTitle);

        doc.add(new Paragraph(" "));

        // ── Receipt Info Table (Receipt No + Date) ──
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});

        addInfoCell(infoTable, "Receipt No: " + record.getReceiptNumber(), Element.ALIGN_LEFT);
        addInfoCell(infoTable, "Date: " + record.getPaymentDate().format(DATE_FMT),
                Element.ALIGN_RIGHT);
        doc.add(infoTable);

        doc.add(new Paragraph(" "));

        // ── Student Details ──
        PdfPTable studentTable = new PdfPTable(2);
        studentTable.setWidthPercentage(100);
        studentTable.setWidths(new float[]{1, 2});

        String studentName = record.getStudentName() != null
                ? record.getStudentName() : "N/A";
        String studentClass = "";
        String admissionNo = "";
        if (student != null) {
            studentClass = student.getClassForAdmission() != null
                    ? student.getClassForAdmission() : "";
            admissionNo = student.getAdmissionNumber() != null
                    ? student.getAdmissionNumber() : "";
        } else if (feeProfile != null) {
            studentClass = feeProfile.getClassName() != null
                    ? feeProfile.getClassName() : "";
        }

        addLabelValueRow(studentTable, "Student Name", studentName);
        addLabelValueRow(studentTable, "Class", studentClass);
        addLabelValueRow(studentTable, "Admission No", admissionNo);
        doc.add(studentTable);

        doc.add(new Paragraph(" "));

        // ── Fee Breakdown Table ──
        PdfPTable feeTable = new PdfPTable(2);
        feeTable.setWidthPercentage(100);
        feeTable.setWidths(new float[]{3, 1});

        // Header row
        addTableHeaderCell(feeTable, "Fee Component");
        addTableHeaderCell(feeTable, "Amount");

        // Fee items from paidForMonths (installment names)
        List<String> feeItems = record.getPaidForMonths();
        BigDecimal totalGross = BigDecimal.ZERO;

        if (feeItems != null && !feeItems.isEmpty() && feeProfile != null
                && feeProfile.getFeeInstallments() != null) {
            boolean alternate = false;
            for (String item : feeItems) {
                BigDecimal itemAmount = feeProfile.getFeeInstallments().stream()
                        .filter(inst -> item.equals(inst.getInstallmentName()))
                        .map(inst -> inst.getAmountDue() != null
                                ? inst.getAmountDue() : BigDecimal.ZERO)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                totalGross = totalGross.add(itemAmount);

                Color bg = alternate ? ROW_ALT_BG : Color.WHITE;
                addTableCell(feeTable, item, Element.ALIGN_LEFT, bg);
                addTableCell(feeTable, formatAmount(itemAmount), Element.ALIGN_RIGHT, bg);
                alternate = !alternate;
            }
        } else if (feeItems != null) {
            // Fallback: list items without individual amounts
            boolean alternate = false;
            for (String item : feeItems) {
                Color bg = alternate ? ROW_ALT_BG : Color.WHITE;
                addTableCell(feeTable, item, Element.ALIGN_LEFT, bg);
                addTableCell(feeTable, "-", Element.ALIGN_RIGHT, bg);
                alternate = !alternate;
            }
            totalGross = record.getAmountPaid() != null ? record.getAmountPaid() : BigDecimal.ZERO;
            if (record.getDiscount() != null) {
                totalGross = totalGross.add(record.getDiscount());
            }
        }

        doc.add(feeTable);

        doc.add(new Paragraph(" "));

        // ── Totals Section ──
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[]{3, 1});

        BigDecimal discount = record.getDiscount() != null
                ? record.getDiscount() : BigDecimal.ZERO;
        BigDecimal amountPaid = record.getAmountPaid() != null
                ? record.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal balance = totalGross.subtract(discount).subtract(amountPaid);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balance = BigDecimal.ZERO;
        }

        addTotalRow(totalsTable, "Total Amount", formatAmount(totalGross));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "Discount", "- " + formatAmount(discount));
        }
        addTotalRow(totalsTable, "Amount Paid", formatAmount(amountPaid));
        addTotalRow(totalsTable, "Balance", formatAmount(balance));

        doc.add(totalsTable);

        doc.add(new Paragraph(" "));

        // ── Payment Mode ──
        String paymentMode = record.getPaymentMode() != null
                ? record.getPaymentMode().name() : "N/A";
        Paragraph modePara = new Paragraph("Payment Mode: " + paymentMode, LABEL_FONT);
        doc.add(modePara);

        if (record.getTransactionId() != null && !record.getTransactionId().isEmpty()) {
            Paragraph txnPara = new Paragraph(
                    "Transaction ID: " + record.getTransactionId(), VALUE_FONT);
            doc.add(txnPara);
        }

        if (record.getRemarks() != null && !record.getRemarks().isEmpty()) {
            Paragraph remarksPara = new Paragraph(
                    "Remarks: " + record.getRemarks(), VALUE_FONT);
            doc.add(remarksPara);
        }

        // ── Footer ──
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
                "This is a computer-generated receipt and does not require a signature.",
                FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private void addInfoCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addLabelValueRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int alignment, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_CELL_FONT));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, TOTAL_FONT));
        labelCell.setBorder(PdfPCell.TOP);
        labelCell.setPadding(4);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, TOTAL_FONT));
        valueCell.setBorder(PdfPCell.TOP);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
