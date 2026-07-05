package com.exam.seating.service;

import com.exam.seating.dto.*;
import com.exam.seating.entity.Student;

import java.util.List;
import java.util.Map;

public interface StudentDashboardService {
	
	Student getCurrentStudent();

    StudentProfileDTO getStudentProfile(Long studentId);

    StudentDashboardStatsDTO getDashboardStats(Long studentId);

    Map<String, Object> getDashboardOverview(Long studentId);

    List<HallTicketDTO> getAllHallTickets(Long studentId);

    HallTicketDTO getHallTicketById(Long hallTicketId);

    HallTicketDTO getHallTicketByExam(Long studentId, Long examId);

    List<HallTicketDTO> getCurrentExams(Long studentId);

    List<HallTicketDTO> getUpcomingExams(Long studentId);

    List<HallTicketDTO> getCompletedExams(Long studentId);

    Map<String, Long> getHallTicketStatusCounts(Long studentId);

    boolean hasExamsToday(Long studentId);

    List<HallTicketDTO> searchHallTickets(Long studentId, String searchTerm);

    List<HallTicketDTO> filterHallTicketsByDate(
            Long studentId,
            String startDate,
            String endDate
    );

    List<HallTicketDTO> filterHallTicketsByStatus(
            Long studentId,
            String status
    );
}