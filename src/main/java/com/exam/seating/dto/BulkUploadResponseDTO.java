package com.exam.seating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponseDTO {
    private boolean success;
    private String message;
    private int totalRecords;
    private int successCount;
    private int failureCount;
    private List<UploadError> errors = new ArrayList<>();
    private List<UploadSummary> summaries = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadError {
        private int rowNumber;
        private String field;
        private String value;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadSummary {
        private String entityType;
        private int total;
        private int created;
        private int updated;
        private int skipped;
    }
}