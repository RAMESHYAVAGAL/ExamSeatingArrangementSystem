package com.exam.seating.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallTicketDTO {

    private Long examId;

    private String studentName;
    private String rollNo;
    private String hallTicketNo;
    private String department;
    private String studentEmail;
    private String studentPhone;
    private Integer year;
    private Integer semester;

    private String examName;
    private String subjectCode;
    private LocalDate examStartDate;
    private LocalDate examEndDate;
    private LocalDate subjectExamDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;

    private String roomCode;
    private String roomName;
    private Integer rowNo;
    private Integer colNo;
    private Integer seatNumber;
    private String location;

    private String invigilatorName;
    private String invigilatorEmployeeId;
    private String invigilatorPhone;
    private String invigilatorDepartment;

    private String status;
    private boolean hallTicketsGenerated;
    
    public String getFormattedExamStartDate() {
        return examStartDate != null
                ? examStartDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : null;
    }

    public String getFormattedExamEndDate() {
        return examEndDate != null
                ? examEndDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : null;
    }

    public String getFormattedSubjectExamDate() {
        return subjectExamDate != null
                ? subjectExamDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : null;
    }

    @Deprecated
    public String getFormattedExamDate() {
        return examStartDate != null
                ? examStartDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : null;
    }

    public String getFormattedStartTime() {
        return startTime != null
                ? startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                : null;
    }

    public String getFormattedEndTime() {
        return endTime != null
                ? endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                : null;
    }

    public String getExamDateRange() {
        if (examStartDate != null && examEndDate != null) {
            if (examStartDate.equals(examEndDate)) {
                return examStartDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } else {
                return examStartDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) 
                       + " to " 
                       + examEndDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
        }
        return null;
    }

    public String getFormattedDuration() {
        if (duration != null) {
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
        return null;
    }
}