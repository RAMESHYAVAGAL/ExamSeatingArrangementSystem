package com.exam.seating.service.impl;

import com.exam.seating.dto.ExamDTO;
import com.exam.seating.entity.Exam;
import com.exam.seating.repository.ExamRepository;
import com.exam.seating.repository.SubjectRepository;
import com.exam.seating.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public ExamDTO saveExam(ExamDTO dto) {
        if (examRepository.existsByExamCodeAndDeletedFalse(dto.getExamCode())) {
            throw new RuntimeException("Exam code already exists");
        }

        Exam exam = mapToEntity(dto);
        exam = examRepository.save(exam);

        return mapToDTO(exam);
    }

    @Override
    public ExamDTO updateExam(Long id, ExamDTO dto) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (!exam.getExamCode().equals(dto.getExamCode()) &&
                examRepository.existsByExamCodeAndDeletedFalseAndIdNot(dto.getExamCode(), id)) {
            throw new RuntimeException("Exam code already exists");
        }

        exam.setExamName(dto.getExamName());
        exam.setExamCode(dto.getExamCode());
        exam.setStartDate(dto.getStartDate());  
        exam.setEndDate(dto.getEndDate());      
        exam.setYear(dto.getYear());
        exam.setSemester(dto.getSemester());

        return mapToDTO(examRepository.save(exam));
    }

    @Override
    public List<ExamDTO> getAllExams() {
        return examRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToDTOWithSubjectCount)
                .toList();
    }

    @Override
    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        return mapToDTOWithSubjectCount(exam);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        exam.setDeleted(true);
        examRepository.save(exam);
    }

    @Override
    public ExamDTO getExamByCode(String examCode) {
        Exam exam = examRepository.findByExamCodeAndDeletedFalse(examCode)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        return mapToDTOWithSubjectCount(exam);
    }

    @Override
    public List<ExamDTO> searchExams(String query) {
        return examRepository.searchExams(query)
                .stream()
                .map(this::mapToDTOWithSubjectCount)
                .toList();
    }

    @Override
    public Long getSubjectCount(Long examId) {
        Long count = subjectRepository.countByExam_IdAndDeletedFalse(examId);
        return count == null ? 0L : count;
    }

    private Exam mapToEntity(ExamDTO dto) {
        return Exam.builder()
                .examName(dto.getExamName())
                .examCode(dto.getExamCode())
                .startDate(dto.getStartDate())  
                .endDate(dto.getEndDate())     
                .year(dto.getYear())
                .semester(dto.getSemester())
                .build();
    }

    private ExamDTO mapToDTO(Exam exam) {
        return ExamDTO.builder()
                .id(exam.getId())
                .examName(exam.getExamName())
                .examCode(exam.getExamCode())
                .startDate(exam.getStartDate())  
                .endDate(exam.getEndDate())      
                .year(exam.getYear())
                .semester(exam.getSemester())
                .build();
    }

    private ExamDTO mapToDTOWithSubjectCount(Exam exam) {
        ExamDTO dto = mapToDTO(exam);

        Long count = subjectRepository.countByExam_IdAndDeletedFalse(exam.getId());
        dto.setSubjectCount(count == null ? 0 : count.intValue());

        return dto;
    }
}