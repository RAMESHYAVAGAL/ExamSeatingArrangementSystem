package com.exam.seating.controller;

import com.exam.seating.dto.BulkUploadResponseDTO;
import com.exam.seating.service.BulkUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/bulk-upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    @PostMapping("/students")
    public ResponseEntity<BulkUploadResponseDTO> uploadStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "year", required = false) Integer year) {
        
        BulkUploadResponseDTO response = bulkUploadService.uploadStudents(file, department, year);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms")
    public ResponseEntity<BulkUploadResponseDTO> uploadRooms(@RequestParam("file") MultipartFile file) {
        BulkUploadResponseDTO response = bulkUploadService.uploadRooms(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invigilators")
    public ResponseEntity<BulkUploadResponseDTO> uploadInvigilators(@RequestParam("file") MultipartFile file) {
        BulkUploadResponseDTO response = bulkUploadService.uploadInvigilators(file);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/exams")
//    public ResponseEntity<BulkUploadResponseDTO> uploadExams(@RequestParam("file") MultipartFile file) {
//        BulkUploadResponseDTO response = bulkUploadService.uploadExams(file);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/subjects")
//    public ResponseEntity<BulkUploadResponseDTO> uploadSubjects(@RequestParam("file") MultipartFile file) {
//        BulkUploadResponseDTO response = bulkUploadService.uploadSubjects(file);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/templates/{type}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String type) {
        byte[] template = bulkUploadService.generateTemplate(type);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + type + "_template.xlsx")
                .body(template);
    }
}