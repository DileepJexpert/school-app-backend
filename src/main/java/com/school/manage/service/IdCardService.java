package com.school.manage.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.school.manage.model.SchoolWebsite;
import com.school.manage.model.Staff;
import com.school.manage.model.Student;
import com.school.manage.repository.StaffRepository;
import com.school.manage.repository.StudentRepository;
import com.school.manage.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdCardService {

    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final MongoTemplate mongoTemplate;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 51, 102));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
    private static final Font CARD_TITLE_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.DARK_GRAY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font NAME_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
    private static final Font PHOTO_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);

    private static final Color HEADER_BG = new Color(0, 51, 102);
    private static final Color BORDER_COLOR = new Color(0, 51, 102);

    public byte[] generateStudentIdCard(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        SchoolWebsite school = mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(TenantContext.getTenant())),
                SchoolWebsite.class);

        String schoolName = school != null && school.getSchoolName() != null
                ? school.getSchoolName() : "School";
        String schoolAddress = school != null && school.getAddress() != null
                ? school.getAddress() : "";
        String schoolPhone = school != null && school.getPhone() != null
                ? school.getPhone() : "";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addStudentCardContent(doc, student, schoolName, schoolAddress, schoolPhone);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[IdCard] Failed to generate student ID card for '{}': {}", studentId, e.getMessage(), e);
            throw new RuntimeException("ID card generation failed: " + e.getMessage());
        }
    }

    public byte[] generateStaffIdCard(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found: " + staffId));

        SchoolWebsite school = mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(TenantContext.getTenant())),
                SchoolWebsite.class);

        String schoolName = school != null && school.getSchoolName() != null
                ? school.getSchoolName() : "School";
        String schoolAddress = school != null && school.getAddress() != null
                ? school.getAddress() : "";
        String schoolPhone = school != null && school.getPhone() != null
                ? school.getPhone() : "";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addStaffCardContent(doc, staff, schoolName, schoolAddress, schoolPhone);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[IdCard] Failed to generate staff ID card for '{}': {}", staffId, e.getMessage(), e);
            throw new RuntimeException("ID card generation failed: " + e.getMessage());
        }
    }

    public byte[] generateBulkIdCards(List<String> studentIds) {
        SchoolWebsite school = mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(TenantContext.getTenant())),
                SchoolWebsite.class);

        String schoolName = school != null && school.getSchoolName() != null
                ? school.getSchoolName() : "School";
        String schoolAddress = school != null && school.getAddress() != null
                ? school.getAddress() : "";
        String schoolPhone = school != null && school.getPhone() != null
                ? school.getPhone() : "";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            boolean first = true;
            for (String studentId : studentIds) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

                if (!first) {
                    doc.newPage();
                }
                addStudentCardContent(doc, student, schoolName, schoolAddress, schoolPhone);
                first = false;
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[IdCard] Failed to generate bulk ID cards: {}", e.getMessage(), e);
            throw new RuntimeException("Bulk ID card generation failed: " + e.getMessage());
        }
    }

    private void addStudentCardContent(Document doc, Student student,
                                       String schoolName, String schoolAddress, String schoolPhone)
            throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(45);
        card.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell defaultCell = card.getDefaultCell();
        defaultCell.setBorderColor(BORDER_COLOR);
        defaultCell.setBorderWidth(1.5f);

        // School name header
        PdfPCell headerCell = createCell(schoolName.toUpperCase(), TITLE_FONT, Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(Color.WHITE);
        headerCell.setPaddingTop(10);
        headerCell.setPaddingBottom(2);
        headerCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
        headerCell.setBorderColor(BORDER_COLOR);
        headerCell.setBorderWidth(1.5f);
        card.addCell(headerCell);

        // School address
        if (!schoolAddress.isEmpty()) {
            PdfPCell addressCell = createCell(schoolAddress, SUBTITLE_FONT, Element.ALIGN_CENTER);
            addressCell.setPaddingTop(0);
            addressCell.setPaddingBottom(4);
            addressCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            addressCell.setBorderColor(BORDER_COLOR);
            addressCell.setBorderWidth(1.5f);
            card.addCell(addressCell);
        }

        // Separator line
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        separatorCell.setBorderColor(BORDER_COLOR);
        separatorCell.setBorderWidth(1.5f);
        separatorCell.setFixedHeight(3);
        separatorCell.setBorderWidthBottom(0);
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidth(0);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setBorderColorBottom(HEADER_BG);
        lineCell.setFixedHeight(3);
        separatorTable.addCell(lineCell);
        separatorCell.addElement(separatorTable);
        card.addCell(separatorCell);

        // Card title bar
        PdfPCell titleCell = createCell("STUDENT IDENTITY CARD", CARD_TITLE_FONT, Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(HEADER_BG);
        titleCell.setPaddingTop(5);
        titleCell.setPaddingBottom(5);
        titleCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        titleCell.setBorderColor(BORDER_COLOR);
        titleCell.setBorderWidth(1.5f);
        card.addCell(titleCell);

        // Photo and basic info row
        PdfPTable photoInfoTable = new PdfPTable(2);
        photoInfoTable.setWidthPercentage(100);
        photoInfoTable.setWidths(new float[]{1f, 1.5f});

        // Photo placeholder
        PdfPCell photoCell = new PdfPCell();
        photoCell.setFixedHeight(80);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        photoCell.setBorderColor(BORDER_COLOR);
        photoCell.setBorderWidth(1f);
        photoCell.setPadding(4);
        Paragraph photoText = new Paragraph("PHOTO", PHOTO_FONT);
        photoText.setAlignment(Element.ALIGN_CENTER);
        photoCell.addElement(photoText);
        photoInfoTable.addCell(photoCell);

        // Basic info next to photo
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(0);
        infoCell.setPadding(4);
        infoCell.addElement(new Paragraph(student.getFullName() != null ? student.getFullName() : "N/A", NAME_FONT));
        infoCell.addElement(createLabelValueParagraph("Class: ",
                student.getClassForAdmission() != null ? student.getClassForAdmission() : "N/A"));
        infoCell.addElement(createLabelValueParagraph("Roll No: ",
                student.getRollNumber() != null ? student.getRollNumber() : "N/A"));
        infoCell.addElement(createLabelValueParagraph("Blood Group: ",
                student.getBloodGroup() != null ? student.getBloodGroup() : "N/A"));
        photoInfoTable.addCell(infoCell);

        PdfPCell photoInfoCell = new PdfPCell(photoInfoTable);
        photoInfoCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        photoInfoCell.setBorderColor(BORDER_COLOR);
        photoInfoCell.setBorderWidth(1.5f);
        photoInfoCell.setPadding(6);
        card.addCell(photoInfoCell);

        // Detail fields
        addDetailRow(card, "Admission No", student.getAdmissionNumber() != null ? student.getAdmissionNumber() : "N/A");
        addDetailRow(card, "Academic Year", student.getAcademicYear() != null ? student.getAcademicYear() : "N/A");

        String fatherName = student.getParentDetails() != null && student.getParentDetails().getFatherName() != null
                ? student.getParentDetails().getFatherName() : "N/A";
        addDetailRow(card, "Father's Name", fatherName);

        String contact = "N/A";
        if (student.getParentDetails() != null && student.getParentDetails().getFatherMobile() != null) {
            contact = student.getParentDetails().getFatherMobile();
        } else if (student.getContactDetails() != null && student.getContactDetails().getPrimaryContactNumber() != null) {
            contact = student.getContactDetails().getPrimaryContactNumber();
        }
        addDetailRow(card, "Contact", contact);

        // Footer with school phone
        if (!schoolPhone.isEmpty()) {
            PdfPCell footerCell = createCell("Phone: " + schoolPhone, SUBTITLE_FONT, Element.ALIGN_CENTER);
            footerCell.setBackgroundColor(new Color(245, 248, 252));
            footerCell.setPaddingTop(5);
            footerCell.setPaddingBottom(5);
            footerCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            footerCell.setBorderColor(BORDER_COLOR);
            footerCell.setBorderWidth(1.5f);
            card.addCell(footerCell);
        } else {
            PdfPCell emptyFooter = new PdfPCell(new Phrase(""));
            emptyFooter.setFixedHeight(5);
            emptyFooter.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            emptyFooter.setBorderColor(BORDER_COLOR);
            emptyFooter.setBorderWidth(1.5f);
            card.addCell(emptyFooter);
        }

        doc.add(card);
    }

    private void addStaffCardContent(Document doc, Staff staff,
                                     String schoolName, String schoolAddress, String schoolPhone)
            throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(45);
        card.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell defaultCell = card.getDefaultCell();
        defaultCell.setBorderColor(BORDER_COLOR);
        defaultCell.setBorderWidth(1.5f);

        // School name header
        PdfPCell headerCell = createCell(schoolName.toUpperCase(), TITLE_FONT, Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(Color.WHITE);
        headerCell.setPaddingTop(10);
        headerCell.setPaddingBottom(2);
        headerCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
        headerCell.setBorderColor(BORDER_COLOR);
        headerCell.setBorderWidth(1.5f);
        card.addCell(headerCell);

        // School address
        if (!schoolAddress.isEmpty()) {
            PdfPCell addressCell = createCell(schoolAddress, SUBTITLE_FONT, Element.ALIGN_CENTER);
            addressCell.setPaddingTop(0);
            addressCell.setPaddingBottom(4);
            addressCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            addressCell.setBorderColor(BORDER_COLOR);
            addressCell.setBorderWidth(1.5f);
            card.addCell(addressCell);
        }

        // Separator line
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        separatorCell.setBorderColor(BORDER_COLOR);
        separatorCell.setBorderWidth(1.5f);
        separatorCell.setFixedHeight(3);
        separatorCell.setBorderWidthBottom(0);
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidth(0);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setBorderColorBottom(HEADER_BG);
        lineCell.setFixedHeight(3);
        separatorTable.addCell(lineCell);
        separatorCell.addElement(separatorTable);
        card.addCell(separatorCell);

        // Card title bar
        PdfPCell titleCell = createCell("STAFF IDENTITY CARD", CARD_TITLE_FONT, Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(HEADER_BG);
        titleCell.setPaddingTop(5);
        titleCell.setPaddingBottom(5);
        titleCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        titleCell.setBorderColor(BORDER_COLOR);
        titleCell.setBorderWidth(1.5f);
        card.addCell(titleCell);

        // Photo and basic info row
        PdfPTable photoInfoTable = new PdfPTable(2);
        photoInfoTable.setWidthPercentage(100);
        photoInfoTable.setWidths(new float[]{1f, 1.5f});

        // Photo placeholder
        PdfPCell photoCell = new PdfPCell();
        photoCell.setFixedHeight(80);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        photoCell.setBorderColor(BORDER_COLOR);
        photoCell.setBorderWidth(1f);
        photoCell.setPadding(4);
        Paragraph photoText = new Paragraph("PHOTO", PHOTO_FONT);
        photoText.setAlignment(Element.ALIGN_CENTER);
        photoCell.addElement(photoText);
        photoInfoTable.addCell(photoCell);

        // Basic info next to photo
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(0);
        infoCell.setPadding(4);
        infoCell.addElement(new Paragraph(staff.getFullName() != null ? staff.getFullName() : "N/A", NAME_FONT));
        infoCell.addElement(createLabelValueParagraph("Designation: ",
                staff.getDesignation() != null ? staff.getDesignation() : "N/A"));
        infoCell.addElement(createLabelValueParagraph("Department: ",
                staff.getDepartment() != null ? staff.getDepartment() : "N/A"));
        infoCell.addElement(createLabelValueParagraph("Blood Group: ",
                staff.getBloodGroup() != null ? staff.getBloodGroup() : "N/A"));
        photoInfoTable.addCell(infoCell);

        PdfPCell photoInfoCell = new PdfPCell(photoInfoTable);
        photoInfoCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        photoInfoCell.setBorderColor(BORDER_COLOR);
        photoInfoCell.setBorderWidth(1.5f);
        photoInfoCell.setPadding(6);
        card.addCell(photoInfoCell);

        // Detail fields
        addDetailRow(card, "Employee ID", staff.getEmployeeId() != null ? staff.getEmployeeId() : "N/A");
        addDetailRow(card, "Contact", staff.getPhone() != null ? staff.getPhone() : "N/A");

        // Footer with school phone
        if (!schoolPhone.isEmpty()) {
            PdfPCell footerCell = createCell("Phone: " + schoolPhone, SUBTITLE_FONT, Element.ALIGN_CENTER);
            footerCell.setBackgroundColor(new Color(245, 248, 252));
            footerCell.setPaddingTop(5);
            footerCell.setPaddingBottom(5);
            footerCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            footerCell.setBorderColor(BORDER_COLOR);
            footerCell.setBorderWidth(1.5f);
            card.addCell(footerCell);
        } else {
            PdfPCell emptyFooter = new PdfPCell(new Phrase(""));
            emptyFooter.setFixedHeight(5);
            emptyFooter.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            emptyFooter.setBorderColor(BORDER_COLOR);
            emptyFooter.setBorderWidth(1.5f);
            card.addCell(emptyFooter);
        }

        doc.add(card);
    }

    private void addDetailRow(PdfPTable card, String label, String value) {
        PdfPTable rowTable = new PdfPTable(2);
        rowTable.setWidthPercentage(100);
        try {
            rowTable.setWidths(new float[]{1.2f, 2f});
        } catch (DocumentException e) {
            log.error("[IdCard] Failed to set row widths: {}", e.getMessage());
        }

        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(0);
        labelCell.setPaddingLeft(8);
        labelCell.setPaddingBottom(3);
        rowTable.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(0);
        valueCell.setPaddingBottom(3);
        rowTable.addCell(valueCell);

        PdfPCell rowCell = new PdfPCell(rowTable);
        rowCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        rowCell.setBorderColor(BORDER_COLOR);
        rowCell.setBorderWidth(1.5f);
        rowCell.setPadding(0);
        card.addCell(rowCell);
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        PdfPCell cell = new PdfPCell();
        cell.addElement(paragraph);
        cell.setBorder(0);
        cell.setPadding(4);
        return cell;
    }

    private Paragraph createLabelValueParagraph(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label, LABEL_FONT));
        p.add(new Phrase(value, VALUE_FONT));
        p.setSpacingBefore(2);
        return p;
    }
}
