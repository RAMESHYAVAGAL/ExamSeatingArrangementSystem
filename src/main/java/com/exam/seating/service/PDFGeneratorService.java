package com.exam.seating.service;

import com.exam.seating.dto.HallTicketDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PDFGeneratorService {
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    
    public byte[] generateHallTicketPDF(HallTicketDTO hallTicket) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph title = new Paragraph("HALL TICKET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph header = new Paragraph("UNIVERSITY/COLLEGE NAME", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(30);
        document.add(header);
        
        PdfPTable studentTable = new PdfPTable(2);
        studentTable.setWidthPercentage(100);
        studentTable.setSpacingAfter(20);
        
        addTableCell(studentTable, "Student Name:", hallTicket.getStudentName() != null ? hallTicket.getStudentName() : "N/A", true);
        addTableCell(studentTable, "Roll Number:", hallTicket.getRollNo() != null ? hallTicket.getRollNo() : "N/A", false);
        addTableCell(studentTable, "Hall Ticket No:", hallTicket.getHallTicketNo() != null ? hallTicket.getHallTicketNo() : "N/A", true);
        addTableCell(studentTable, "Department:", hallTicket.getDepartment() != null ? hallTicket.getDepartment() : "N/A", false);
        addTableCell(studentTable, "Year:", hallTicket.getYear() != null ? hallTicket.getYear().toString() : "N/A", true);
        addTableCell(studentTable, "Semester:", hallTicket.getSemester() != null ? hallTicket.getSemester().toString() : "N/A", false);
        addTableCell(studentTable, "Email:", hallTicket.getStudentEmail() != null ? hallTicket.getStudentEmail() : "N/A", true);
        addTableCell(studentTable, "Phone:", hallTicket.getStudentPhone() != null ? hallTicket.getStudentPhone() : "N/A", false);
        
        document.add(studentTable);
        
        PdfPTable examTable = new PdfPTable(2);
        examTable.setWidthPercentage(100);
        examTable.setSpacingAfter(20);
        
        String examDateDisplay = getExamDateDisplay(hallTicket);
        
        addTableCell(examTable, "Exam Name:", hallTicket.getExamName() != null ? hallTicket.getExamName() : "N/A", true);
        addTableCell(examTable, "Subject Code:", hallTicket.getSubjectCode() != null ? hallTicket.getSubjectCode() : "N/A", false);
        addTableCell(examTable, "Exam Date:", examDateDisplay, true);
        
        String startTime = hallTicket.getStartTime() != null ? 
                          hallTicket.getStartTime().format(timeFormatter) : "N/A";
        String endTime = hallTicket.getEndTime() != null ? 
                        hallTicket.getEndTime().format(timeFormatter) : "N/A";
        addTableCell(examTable, "Time:", startTime + " to " + endTime, false);
        
        addTableCell(examTable, "Duration:", getFormattedDuration(hallTicket.getDuration()), true);
        
        if (hallTicket.getSubjectExamDate() != null) {
            addTableCell(examTable, "Subject Exam Date:", 
                       hallTicket.getSubjectExamDate().format(dateFormatter), false);
        }
        
        document.add(examTable);
        
        PdfPTable seatTable = new PdfPTable(2);
        seatTable.setWidthPercentage(100);
        seatTable.setSpacingAfter(30);
        
        addTableCell(seatTable, "Room Code:", hallTicket.getRoomCode() != null ? hallTicket.getRoomCode() : "N/A", true);
        addTableCell(seatTable, "Room Name:", hallTicket.getRoomName() != null ? hallTicket.getRoomName() : "N/A", false);
        addTableCell(seatTable, "Location:", hallTicket.getLocation() != null ? hallTicket.getLocation() : "N/A", true);
        addTableCell(seatTable, "Row Number:", hallTicket.getRowNo() != null ? hallTicket.getRowNo().toString() : "N/A", false);
        addTableCell(seatTable, "Column Number:", hallTicket.getColNo() != null ? hallTicket.getColNo().toString() : "N/A", true);
        addTableCell(seatTable, "Seat Number:", hallTicket.getSeatNumber() != null ? hallTicket.getSeatNumber().toString() : "N/A", false);
        
        if (hallTicket.getInvigilatorName() != null) {
            addTableCell(seatTable, "Invigilator:", hallTicket.getInvigilatorName(), true);
            addTableCell(seatTable, "Invigilator ID:", hallTicket.getInvigilatorEmployeeId() != null ? 
                        hallTicket.getInvigilatorEmployeeId() : "N/A", false);
            addTableCell(seatTable, "Invigilator Phone:", hallTicket.getInvigilatorPhone() != null ? 
                        hallTicket.getInvigilatorPhone() : "N/A", true);
            addTableCell(seatTable, "Invigilator Dept:", hallTicket.getInvigilatorDepartment() != null ? 
                        hallTicket.getInvigilatorDepartment() : "N/A", false);
        }
        
        document.add(seatTable);
        
        Font instructionFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph instructions = new Paragraph("IMPORTANT INSTRUCTIONS:", instructionFont);
        instructions.setSpacingAfter(10);
        document.add(instructions);
        
        String[] instructionPoints = {
            "1. Bring this hall ticket and college ID card to the exam center.",
            "2. Arrive at least 30 minutes before the exam starts.",
            "3. No electronic devices are allowed in the exam hall.",
            "4. Follow all COVID-19 safety protocols if applicable.",
            "5. No re-entry is allowed once you leave the exam hall.",
            "6. Keep this hall ticket safe for future reference.",
            "7. Report any discrepancies to the examination office immediately."
        };
        
        for (String point : instructionPoints) {
            Paragraph pointPara = new Paragraph(point, instructionFont);
            pointPara.setSpacingAfter(5);
            document.add(pointPara);
        }
        
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph footer = new Paragraph("\n\nThis is a system generated hall ticket. No signature required.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        Paragraph generatedDate = new Paragraph(
            "Generated on: " + java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            ), 
            FontFactory.getFont(FontFactory.HELVETICA, 8)
        );
        generatedDate.setAlignment(Element.ALIGN_CENTER);
        document.add(generatedDate);
        
        document.close();
        return outputStream.toByteArray();
    }
    
    private void addTableCell(PdfPTable table, String label, String value, boolean isBoldLabel) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, isBoldLabel ? labelFont : valueFont));
        labelCell.setBorderWidth(1);
        labelCell.setPadding(8);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorderWidth(1);
        valueCell.setPadding(8);
        
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
}