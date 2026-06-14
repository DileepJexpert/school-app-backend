package com.school.manage.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.school.manage.dto.StudentReportCardDto;
import com.school.manage.model.Attendance;
import com.school.manage.model.CoscholasticAssessment;
import com.school.manage.model.SchoolWebsite;
import com.school.manage.model.Student;
import com.school.manage.repository.AttendanceRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCardPdfService {

    private final ResultService resultService;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MongoTemplate mongoTemplate;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 102));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(0, 51, 102));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font CELL_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.DARK_GRAY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font GRADE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 102, 51));
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

    private static final Color HEADER_BG = new Color(0, 51, 102);
    private static final Color ALT_ROW = new Color(245, 248, 252);
    private static final Color BORDER_COLOR = new Color(200, 210, 220);

    public byte[] generateReportCardPdf(String studentId, String academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        StudentReportCardDto reportCard = resultService.getStudentReportCard(studentId, academicYear);

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

            addSchoolHeader(doc, schoolName, schoolAddress, schoolPhone);
            addReportCardTitle(doc, academicYear);
            addStudentInfo(doc, student, reportCard);
            addAttendanceSummary(doc, studentId, academicYear);

            if (reportCard.getSubjects() != null && !reportCard.getSubjects().isEmpty()) {
                addResultsTable(doc, reportCard);
            }

            addOverallPerformance(doc, reportCard);
            addCoscholastic(doc, reportCard);
            addGradingScale(doc);
            addSignatureSection(doc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[ReportCardPdf] Failed to generate PDF for student '{}': {}", studentId, e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }

    private void addSchoolHeader(Document doc, String schoolName, String address, String phone)
            throws DocumentException {
        Paragraph name = new Paragraph(schoolName.toUpperCase(), TITLE_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        doc.add(name);

        if (!address.isBlank()) {
            Paragraph addr = new Paragraph(address, SUBTITLE_FONT);
            addr.setAlignment(Element.ALIGN_CENTER);
            doc.add(addr);
        }

        if (!phone.isBlank()) {
            Paragraph ph = new Paragraph("Phone: " + phone, SUBTITLE_FONT);
            ph.setAlignment(Element.ALIGN_CENTER);
            doc.add(ph);
        }

        doc.add(Chunk.NEWLINE);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidth(0);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setBorderColorBottom(HEADER_BG);
        lineCell.setFixedHeight(3);
        line.addCell(lineCell);
        doc.add(line);
        doc.add(Chunk.NEWLINE);
    }

    private void addReportCardTitle(Document doc, String academicYear) throws DocumentException {
        Paragraph title = new Paragraph("PROGRESS REPORT — " + academicYear, SECTION_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        doc.add(title);
    }

    private void addStudentInfo(Document doc, Student student, StudentReportCardDto reportCard)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2f, 1.2f, 2f});
        table.setSpacingAfter(12);

        addInfoRow(table, "Student Name", student.getFullName(),
                "Class", student.getClassForAdmission());
        addInfoRow(table, "Roll Number", reportCard.getRollNumber() != null ? reportCard.getRollNumber() : "—",
                "Academic Year", reportCard.getAcademicYear());
        addInfoRow(table, "Date of Birth", student.getDateOfBirth() != null
                        ? student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "—",
                "Class Rank", String.valueOf(reportCard.getClassRank()));

        String fatherName = student.getParentDetails() != null
                ? student.getParentDetails().getFatherName() : null;
        String motherName = student.getParentDetails() != null
                ? student.getParentDetails().getMotherName() : null;
        addInfoRow(table, "Father's Name", fatherName != null ? fatherName : "—",
                "Mother's Name", motherName != null ? motherName : "—");

        doc.add(table);
    }

    private void addInfoRow(PdfPTable table, String label1, String value1,
                            String label2, String value2) {
        PdfPCell l1 = new PdfPCell(new Phrase(label1, LABEL_FONT));
        l1.setBorder(0);
        l1.setPaddingBottom(4);
        l1.setBackgroundColor(ALT_ROW);

        PdfPCell v1 = new PdfPCell(new Phrase(value1, VALUE_FONT));
        v1.setBorder(0);
        v1.setPaddingBottom(4);

        PdfPCell l2 = new PdfPCell(new Phrase(label2, LABEL_FONT));
        l2.setBorder(0);
        l2.setPaddingBottom(4);
        l2.setBackgroundColor(ALT_ROW);

        PdfPCell v2 = new PdfPCell(new Phrase(value2, VALUE_FONT));
        v2.setBorder(0);
        v2.setPaddingBottom(4);

        table.addCell(l1);
        table.addCell(v1);
        table.addCell(l2);
        table.addCell(v2);
    }

    private void addAttendanceSummary(Document doc, String studentId, String academicYear)
            throws DocumentException {
        List<Attendance> records = attendanceRepository.findByStudentIdAndAcademicYear(studentId, academicYear);

        if (records.isEmpty()) return;

        long total = records.size();
        long present = records.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long absent = records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        long late = records.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        double pct = total > 0 ? Math.round((double) (present + late) / total * 1000.0) / 10.0 : 0;

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);

        Paragraph heading = new Paragraph("ATTENDANCE", SECTION_FONT);
        heading.setSpacingAfter(4);
        doc.add(heading);

        String[] headers = {"Total Days", "Present", "Absent", "Late", "Attendance %"};
        String[] values = {String.valueOf(total), String.valueOf(present),
                String.valueOf(absent), String.valueOf(late), pct + "%"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, CELL_BOLD));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        doc.add(table);
    }

    private void addResultsTable(Document doc, StudentReportCardDto reportCard) throws DocumentException {
        Paragraph heading = new Paragraph("SCHOLASTIC PERFORMANCE", SECTION_FONT);
        heading.setSpacingAfter(6);
        doc.add(heading);

        Set<String> examTypes = new LinkedHashSet<>();
        for (StudentReportCardDto.SubjectSummary ss : reportCard.getSubjects()) {
            if (ss.getExamResults() != null) {
                examTypes.addAll(ss.getExamResults().keySet());
            }
        }

        List<String> orderedExams = new ArrayList<>(examTypes);
        int numExams = orderedExams.size();
        int numCols = 1 + numExams * 3 + 2;

        PdfPTable table = new PdfPTable(numCols);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);

        float[] widths = new float[numCols];
        widths[0] = 2.2f;
        for (int i = 1; i <= numExams * 3; i++) widths[i] = 1f;
        widths[numCols - 2] = 1.2f;
        widths[numCols - 1] = 1f;
        table.setWidths(widths);

        addHeaderCell(table, "Subject");
        for (String exam : orderedExams) {
            String display = exam.replace("_", " ");
            PdfPCell cell = new PdfPCell(new Phrase(display, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            cell.setColspan(3);
            table.addCell(cell);
        }
        addHeaderCell(table, "Weighted %");
        addHeaderCell(table, "Grade");

        addSubHeaderCell(table, "");
        for (int i = 0; i < numExams; i++) {
            addSubHeaderCell(table, "Marks");
            addSubHeaderCell(table, "%");
            addSubHeaderCell(table, "Grade");
        }
        addSubHeaderCell(table, "");
        addSubHeaderCell(table, "");

        boolean alternate = false;
        for (StudentReportCardDto.SubjectSummary ss : reportCard.getSubjects()) {
            Color bg = alternate ? ALT_ROW : Color.WHITE;
            alternate = !alternate;

            addDataCell(table, ss.getSubject(), CELL_BOLD, bg, Element.ALIGN_LEFT);

            for (String exam : orderedExams) {
                StudentReportCardDto.ExamResult er = ss.getExamResults() != null
                        ? ss.getExamResults().get(exam) : null;
                if (er != null) {
                    addDataCell(table, String.format("%.0f/%.0f", er.getMarksObtained(), er.getMaxMarks()),
                            CELL_FONT, bg, Element.ALIGN_CENTER);
                    addDataCell(table, String.format("%.1f", er.getPercentage()),
                            CELL_FONT, bg, Element.ALIGN_CENTER);
                    addDataCell(table, er.getGrade(), CELL_BOLD, bg, Element.ALIGN_CENTER);
                } else {
                    addDataCell(table, "—", CELL_FONT, bg, Element.ALIGN_CENTER);
                    addDataCell(table, "—", CELL_FONT, bg, Element.ALIGN_CENTER);
                    addDataCell(table, "—", CELL_FONT, bg, Element.ALIGN_CENTER);
                }
            }

            addDataCell(table, String.format("%.1f%%", ss.getWeightedPercentage()),
                    CELL_BOLD, bg, Element.ALIGN_CENTER);
            addDataCell(table, ss.getPredictedGrade(), CELL_BOLD, bg, Element.ALIGN_CENTER);
        }

        doc.add(table);
    }

    private void addOverallPerformance(Document doc, StudentReportCardDto reportCard)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        table.setWidths(new float[]{1.5f, 1f, 1f, 1f});

        PdfPCell titleCell = new PdfPCell(new Phrase("OVERALL PERFORMANCE", SECTION_FONT));
        titleCell.setColspan(4);
        titleCell.setBorder(0);
        titleCell.setPaddingBottom(8);
        table.addCell(titleCell);

        addInfoCell(table, "Cumulative %", String.format("%.1f%%", reportCard.getCumulativePercentage()));
        addInfoCell(table, "Overall Grade", reportCard.getOverallGrade() != null ? reportCard.getOverallGrade() : "—");
        addInfoCell(table, "Grade Point", String.format("%.1f", reportCard.getOverallGradePoint()));
        addInfoCell(table, "Class Rank", String.valueOf(reportCard.getClassRank()));

        doc.add(table);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(6);
        cell.setBackgroundColor(ALT_ROW);
        cell.addElement(new Phrase(label, LABEL_FONT));
        cell.addElement(new Phrase(value, GRADE_FONT));
        table.addCell(cell);
    }

    private void addCoscholastic(Document doc, StudentReportCardDto reportCard) throws DocumentException {
        CoscholasticAssessment term1 = reportCard.getCoscholasticTerm1();
        CoscholasticAssessment term2 = reportCard.getCoscholasticTerm2();

        if (term1 == null && term2 == null) return;

        Paragraph heading = new Paragraph("CO-SCHOLASTIC ASSESSMENT", SECTION_FONT);
        heading.setSpacingBefore(4);
        heading.setSpacingAfter(6);
        doc.add(heading);

        Set<String> areas = new LinkedHashSet<>();
        if (term1 != null && term1.getAreas() != null) {
            term1.getAreas().forEach(a -> areas.add(a.getName()));
        }
        if (term2 != null && term2.getAreas() != null) {
            term2.getAreas().forEach(a -> areas.add(a.getName()));
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1f, 1f});
        table.setSpacingAfter(12);

        addHeaderCell(table, "Activity / Area");
        addHeaderCell(table, "Term 1");
        addHeaderCell(table, "Term 2");

        Map<String, String> t1Map = new HashMap<>();
        Map<String, String> t2Map = new HashMap<>();
        if (term1 != null && term1.getAreas() != null) {
            term1.getAreas().forEach(a -> t1Map.put(a.getName(), a.getGrade()));
        }
        if (term2 != null && term2.getAreas() != null) {
            term2.getAreas().forEach(a -> t2Map.put(a.getName(), a.getGrade()));
        }

        boolean alt = false;
        for (String area : areas) {
            Color bg = alt ? ALT_ROW : Color.WHITE;
            alt = !alt;
            addDataCell(table, area, CELL_FONT, bg, Element.ALIGN_LEFT);
            addDataCell(table, t1Map.getOrDefault(area, "—"), CELL_BOLD, bg, Element.ALIGN_CENTER);
            addDataCell(table, t2Map.getOrDefault(area, "—"), CELL_BOLD, bg, Element.ALIGN_CENTER);
        }

        doc.add(table);
    }

    private void addGradingScale(Document doc) throws DocumentException {
        Paragraph heading = new Paragraph("GRADING SCALE (CBSE)", SMALL_FONT);
        heading.setSpacingBefore(4);
        heading.setSpacingAfter(2);
        doc.add(heading);

        String scale = "A1 (91-100) | A2 (81-90) | B1 (71-80) | B2 (61-70) | " +
                "C1 (51-60) | C2 (41-50) | D (33-40) | E (Below 33)";
        Paragraph p = new Paragraph(scale, SMALL_FONT);
        p.setSpacingAfter(16);
        doc.add(p);
    }

    private void addSignatureSection(Document doc) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(30);

        String[] labels = {"Class Teacher", "Examination In-charge", "Principal"};
        for (String label : labels) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(0);
            cell.setPaddingTop(30);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph line = new Paragraph("________________________", CELL_FONT);
            line.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(line);

            Paragraph lbl = new Paragraph(label, LABEL_FONT);
            lbl.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(lbl);

            table.addCell(cell);
        }

        doc.add(table);

        Paragraph date = new Paragraph(
                "Date of Issue: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                SMALL_FONT);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingBefore(8);
        doc.add(date);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addSubHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 7, Font.BOLD, Color.DARK_GRAY)));
        cell.setBackgroundColor(new Color(230, 236, 245));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }
}
