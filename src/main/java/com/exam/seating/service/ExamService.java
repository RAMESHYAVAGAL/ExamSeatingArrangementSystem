package com.exam.seating.service;

import com.exam.seating.dto.ExamDTO;
import java.util.List;

public interface ExamService {

    ExamDTO saveExam(ExamDTO dto);

    ExamDTO updateExam(Long id, ExamDTO dto);

    List<ExamDTO> getAllExams();

    ExamDTO getExamById(Long id);

    void deleteExam(Long id);

    ExamDTO getExamByCode(String examCode);

    List<ExamDTO> searchExams(String query);

    Long getSubjectCount(Long examId);
}