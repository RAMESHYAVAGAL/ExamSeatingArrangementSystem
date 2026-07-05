package com.exam.seating.controller;

import com.exam.seating.dto.HallTicketDTO;
import com.exam.seating.service.HallTicketService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hall-tickets")
@CrossOrigin(origins = "*")
public class HallTicketController {

    private final HallTicketService hallTicketService;

    public HallTicketController(HallTicketService hallTicketService) {
        this.hallTicketService = hallTicketService;
    }

    @PostMapping("/generate/{examId}")
    public ResponseEntity<?> generateHallTickets(@PathVariable Long examId) {
        try {
            Map<String, Object> result = hallTicketService.generateHallTicketsForExam(examId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", result.get("message"),
                            "generated", result.get("generated")
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHallTicket(@PathVariable Long id) {
        try {
            HallTicketDTO dto = hallTicketService.getHallTicketDetails(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPDF(@PathVariable Long id) {
        try {
            byte[] pdfBytes = hallTicketService.downloadHallTicketPDF(id);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            String filename = "hall-ticket-" + id + ".pdf";

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\""
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<?> getByExam(@PathVariable Long examId) {
        try {
            List<HallTicketDTO> tickets = hallTicketService.getHallTicketsByExam(examId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", tickets
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PutMapping("/{id}/invigilator/{invigilatorId}")
    public ResponseEntity<?> assignInvigilator(
            @PathVariable Long id,
            @PathVariable Long invigilatorId) {
        try {
            Map<String, Object> result = hallTicketService.assignInvigilator(id, invigilatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHallTicket(@PathVariable Long id) {
        try {
            Map<String, Object> result = hallTicketService.deleteHallTicket(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }
}