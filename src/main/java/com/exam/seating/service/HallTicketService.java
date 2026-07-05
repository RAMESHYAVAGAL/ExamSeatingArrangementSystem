package com.exam.seating.service;

import com.exam.seating.dto.HallTicketDTO;
import java.util.List;
import java.util.Map;

public interface HallTicketService {

    Map<String, Object> generateHallTicketsForExam(Long examId);

    Map<String, Object> generateHallTicketForStudent(Long examId, Long studentId);

    HallTicketDTO getHallTicketDetails(Long hallTicketId);

    byte[] downloadHallTicketPDF(Long hallTicketId);

    List<HallTicketDTO> getHallTicketsByExam(Long examId);

    Map<String, Object> deleteHallTicket(Long hallTicketId);

    Map<String, Object> assignInvigilator(Long hallTicketId, Long invigilatorId);

    Map<String, Object> assignInvigilatorToMultipleTickets(
            List<Long> hallTicketIds,
            Long invigilatorId
    );

    Map<String, Object> getHallTicketsWithoutInvigilator(Long examId);

    Map<String, Object> updateInvigilatorsFromAssignments(Long examId);
}