package com.exam.seating.service;

import com.exam.seating.dto.HallTicketDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class HallTicketPdfService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    public ByteArrayInputStream generateHallTicketPdf(HallTicketDTO hallTicket) {
        Document document = new Document(PageSize.A4, 40, 40, 60, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            writer.setPageEvent(new PdfPageEvent());
            
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(15, 76, 117));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            
            Paragraph universityHeader = new Paragraph("UNIVERSITY EXAMINATION SYSTEM", titleFont);
            universityHeader.setAlignment(Element.ALIGN_CENTER);
            universityHeader.setSpacingAfter(10);
            document.add(universityHeader);
            
            Paragraph hallTicketTitle = new Paragraph("EXAMINATION HALL TICKET", subHeaderFont);
            hallTicketTitle.setAlignment(Element.ALIGN_CENTER);
            hallTicketTitle.setSpacingAfter(5);
            document.add(hallTicketTitle);
            
            Paragraph academicYear = new Paragraph("Academic Year: " + getCurrentAcademicYear(), normalFont);
            academicYear.setAlignment(Element.ALIGN_CENTER);
            academicYear.setSpacingAfter(20);
            document.add(academicYear);
            
            Paragraph line = new Paragraph();
            line.add(new Chunk("\n"));
            document.add(line);
            
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(10);
            mainTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            
            PdfPTable studentTable = createStudentTable(hallTicket, headerFont, boldFont, normalFont);
            PdfPCell studentCell = new PdfPCell(studentTable);
            studentCell.setBorder(Rectangle.NO_BORDER);
            studentCell.setPadding(10);
            studentCell.setBackgroundColor(new BaseColor(248, 249, 250));
            studentCell.setBorderWidthTop(1);
            studentCell.setBorderColorTop(BaseColor.LIGHT_GRAY);
            mainTable.addCell(studentCell);
            
            PdfPTable examTable = createExamTable(hallTicket, headerFont, boldFont, normalFont);
            PdfPCell examCell = new PdfPCell(examTable);
            examCell.setBorder(Rectangle.NO_BORDER);
            examCell.setPadding(10);
            examCell.setBackgroundColor(new BaseColor(255, 255, 255));
            examCell.setBorderWidthTop(1);
            examCell.setBorderColorTop(BaseColor.LIGHT_GRAY);
            mainTable.addCell(examCell);
            
            PdfPTable seatTable = createSeatTable(hallTicket, headerFont, boldFont, normalFont);
            PdfPCell seatCell = new PdfPCell(seatTable);
            seatCell.setBorder(Rectangle.NO_BORDER);
            seatCell.setPadding(10);
            seatCell.setBackgroundColor(new BaseColor(248, 249, 250));
            seatCell.setBorderWidthTop(1);
            seatCell.setBorderColorTop(BaseColor.LIGHT_GRAY);
            mainTable.addCell(seatCell);
            
            document.add(mainTable);
            
            addQrCodePlaceholder(document, hallTicket, headerFont, normalFont);
            
            addInstructions(document, headerFont, normalFont, smallFont);
            
            Paragraph footer = new Paragraph("\n\nThis is a system generated hall ticket. No signature required.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);
            
            Paragraph generatedDate = new Paragraph("Generated on: " + 
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), 
                    FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
            generatedDate.setAlignment(Element.ALIGN_CENTER);
            document.add(generatedDate);
            
            document.close();
            writer.close();
            
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        
        return new ByteArrayInputStream(out.toByteArray());
    }
    
    private PdfPTable createStudentTable(HallTicketDTO hallTicket, Font headerFont, Font boldFont, Font normalFont) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        
        PdfPCell headerCell = new PdfPCell(new Phrase("STUDENT INFORMATION", headerFont));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(new BaseColor(15, 76, 117));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(8);
        headerCell.setPaddingBottom(10);
        table.addCell(headerCell);
        
        addTableRow(table, "Student Name:", hallTicket.getStudentName(), boldFont, normalFont);
        addTableRow(table, "Roll Number:", hallTicket.getRollNo(), boldFont, normalFont);
        addTableRow(table, "Hall Ticket No:", hallTicket.getHallTicketNo(), boldFont, normalFont);
        addTableRow(table, "Department:", hallTicket.getDepartment(), boldFont, normalFont);
        addTableRow(table, "Year:", String.valueOf(hallTicket.getYear()), boldFont, normalFont);
        addTableRow(table, "Semester:", hallTicket.getSemester() != null ? String.valueOf(hallTicket.getSemester()) : "N/A", 
                    boldFont, normalFont);
        addTableRow(table, "Email:", hallTicket.getStudentEmail(), boldFont, normalFont);
        addTableRow(table, "Phone:", hallTicket.getStudentPhone(), boldFont, normalFont);
        
        return table;
    }
    
    private PdfPTable createExamTable(HallTicketDTO hallTicket, Font headerFont, Font boldFont, Font normalFont) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        
        PdfPCell headerCell = new PdfPCell(new Phrase("EXAMINATION DETAILS", headerFont));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(new BaseColor(15, 76, 117));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(8);
        headerCell.setPaddingBottom(10);
        table.addCell(headerCell);
        
        String examDateDisplay = getExamDateDisplay(hallTicket);
        
        addTableRow(table, "Exam Name:", hallTicket.getExamName(), boldFont, normalFont);
        addTableRow(table, "Subject Code:", hallTicket.getSubjectCode(), boldFont, normalFont);
        addTableRow(table, "Exam Date:", examDateDisplay, boldFont, normalFont);
        
        String startTime = hallTicket.getFormattedStartTime() != null ? 
                          hallTicket.getFormattedStartTime() : "N/A";
        String endTime = hallTicket.getFormattedEndTime() != null ? 
                        hallTicket.getFormattedEndTime() : "N/A";
        addTableRow(table, "Exam Time:", startTime + " to " + endTime, boldFont, normalFont);
        
        addTableRow(table, "Duration:", getFormattedDuration(hallTicket.getDuration()), boldFont, normalFont);
        
        if (hallTicket.getSubjectExamDate() != null) {
            addTableRow(table, "Subject Exam Date:", 
                       hallTicket.getSubjectExamDate().format(dateFormatter), 
                       boldFont, normalFont);
        }
        
        return table;
    }
    
    private PdfPTable createSeatTable(HallTicketDTO hallTicket, Font headerFont, Font boldFont, Font normalFont) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        
        PdfPCell headerCell = new PdfPCell(new Phrase("EXAMINATION HALL DETAILS", headerFont));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(new BaseColor(15, 76, 117));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(8);
        headerCell.setPaddingBottom(10);
        table.addCell(headerCell);
        
        addTableRow(table, "Room:", 
                   (hallTicket.getRoomName() != null ? hallTicket.getRoomName() + " " : "") + 
                   "(" + (hallTicket.getRoomCode() != null ? hallTicket.getRoomCode() : "N/A") + ")", boldFont, normalFont);
        addTableRow(table, "Seat Number:", String.valueOf(hallTicket.getSeatNumber()), boldFont, normalFont);
        addTableRow(table, "Row/Column:", 
                   (hallTicket.getRowNo() != null ? hallTicket.getRowNo() : "N/A") + "/" + 
                   (hallTicket.getColNo() != null ? hallTicket.getColNo() : "N/A"), 
                   boldFont, normalFont);
        addTableRow(table, "Location:", hallTicket.getLocation() != null ? hallTicket.getLocation() : "N/A", boldFont, normalFont);
        addTableRow(table, "Invigilator:", hallTicket.getInvigilatorName() != null ? hallTicket.getInvigilatorName() : "Not Assigned", boldFont, normalFont);
        addTableRow(table, "Invigilator ID:", hallTicket.getInvigilatorEmployeeId() != null ? hallTicket.getInvigilatorEmployeeId() : "N/A", boldFont, normalFont);
        addTableRow(table, "Invigilator Phone:", hallTicket.getInvigilatorPhone() != null ? hallTicket.getInvigilatorPhone() : "N/A", boldFont, normalFont);
        addTableRow(table, "Reporting Time:", "30 minutes before exam start", boldFont, normalFont);
        
        return table;
    }
    
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setPaddingLeft(10);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setPaddingRight(10);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private String getExamDateDisplay(HallTicketDTO hallTicket) {
        LocalDate startDate = hallTicket.getExamStartDate();
        LocalDate endDate = hallTicket.getExamEndDate();
        
        if (startDate == null) return "N/A";
        
        if (endDate == null || startDate.equals(endDate)) {
            return startDate.format(dateFormatter);
        } else {
            return startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter);
        }
    }
    
    private String getFormattedDuration(Integer duration) {
        if (duration == null) return "N/A";
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }
    
    private void addQrCodePlaceholder(Document document, HallTicketDTO hallTicket, Font headerFont, Font normalFont) 
            throws DocumentException {
        Paragraph qrHeader = new Paragraph("EXAMINATION PASS", headerFont);
        qrHeader.setAlignment(Element.ALIGN_CENTER);
        qrHeader.setSpacingBefore(20);
        qrHeader.setSpacingAfter(10);
        document.add(qrHeader);
        
        PdfPTable qrTable = new PdfPTable(1);
        qrTable.setWidthPercentage(30);
        qrTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        PdfPCell qrCell = new PdfPCell();
        qrCell.setFixedHeight(150);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        qrCell.setBackgroundColor(new BaseColor(240, 240, 240));
        qrCell.setBorder(Rectangle.BOX);
        qrCell.setBorderColor(BaseColor.LIGHT_GRAY);
        
        Paragraph qrContent = new Paragraph();
        qrContent.setAlignment(Element.ALIGN_CENTER);
        
        qrContent.add(new Chunk("SCAN AT\n", normalFont));
        qrContent.add(new Chunk("EXAM CENTER\n\n", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY)));
        qrContent.add(new Chunk(hallTicket.getRollNo() + "\n", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(15, 76, 117))));
        qrContent.add(new Chunk(hallTicket.getStudentName(), 
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY)));
        
        qrCell.setPhrase(qrContent);
        qrTable.addCell(qrCell);
        
        document.add(qrTable);
        
        Paragraph scanText = new Paragraph("Present this QR code at the examination hall for verification", normalFont);
        scanText.setAlignment(Element.ALIGN_CENTER);
        scanText.setSpacingBefore(10);
        document.add(scanText);
    }
    
    private void addInstructions(Document document, Font headerFont, Font normalFont, Font smallFont) 
            throws DocumentException {
        Paragraph instructionsHeader = new Paragraph("IMPORTANT INSTRUCTIONS", headerFont);
        instructionsHeader.setAlignment(Element.ALIGN_LEFT);
        instructionsHeader.setSpacingBefore(30);
        instructionsHeader.setSpacingAfter(10);
        document.add(instructionsHeader);
        
        String[] instructions = {
            "1. Carry this hall ticket along with your valid University ID card.",
            "2. Report to the examination hall 30 minutes before the scheduled time.",
            "3. No electronic devices (mobile phones, smart watches, etc.) are allowed.",
            "4. Bring your own writing materials (pen, pencil, ruler, calculator if permitted).",
            "5. Follow all instructions given by the invigilator without fail.",
            "6. Maintain complete silence in the examination hall at all times.",
            "7. Cheating or malpractice will lead to serious consequences including expulsion.",
            "8. Do not leave the examination hall without the invigilator's permission.",
            "9. Submit your answer sheet to the invigilator before leaving the hall.",
            "10. Keep this hall ticket safe for future reference and verification."
        };
        
        for (String instruction : instructions) {
            Paragraph p = new Paragraph(instruction, smallFont);
            p.setSpacingAfter(3);
            document.add(p);
        }
    }
    
    private String getCurrentAcademicYear() {
        int currentYear = java.time.LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        return currentYear + "-" + nextYear;
    }
    
    class PdfPageEvent extends PdfPageEventHelper {
        private PdfTemplate total;
        private BaseFont baseFont;
        
        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                
                String text = "Page " + writer.getPageNumber();
                float textSize = baseFont.getWidthPoint(text, 8);
                float textBase = document.bottom() - 20;
                
                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.setTextMatrix(document.right() - textSize - 20, textBase);
                cb.showText(text);
                cb.endText();
                
                cb.restoreState();
                
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}