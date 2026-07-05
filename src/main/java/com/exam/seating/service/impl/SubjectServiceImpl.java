package com.exam.seating.service.impl;

import com.exam.seating.dto.SubjectDTO;
import com.exam.seating.entity.Exam;
import com.exam.seating.entity.Subject;
import com.exam.seating.exception.ResourceNotFoundException;
import com.exam.seating.repository.ExamRepository;
import com.exam.seating.repository.SubjectRepository;
import com.exam.seating.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;

    public SubjectServiceImpl(
            SubjectRepository subjectRepository,
            ExamRepository examRepository
    ) {
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
    }

    @Override
    public SubjectDTO saveSubject(SubjectDTO dto) {
        Exam exam = examRepository.findByIdAndDeletedFalse(dto.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + dto.getExamId()));

        validateSubjectDate(dto.getExamDate(), exam);

        if (subjectRepository.existsBySubjectCodeAndExam_IdAndDeletedFalse(
                dto.getSubjectCode(),
                dto.getExamId())) {
            throw new RuntimeException("Subject code '" + dto.getSubjectCode() + "' already exists for this exam. Please use a different subject code.");
        }

        Subject subject = mapToEntity(dto);
        subject.setExam(exam);

        subject = subjectRepository.save(subject);
        return mapToDTO(subject);
    }

    @Override
    public SubjectDTO updateSubject(Long id, SubjectDTO dto) {
        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        Exam exam = examRepository.findByIdAndDeletedFalse(dto.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + dto.getExamId()));

        validateSubjectDate(dto.getExamDate(), exam);

        if (!subject.getSubjectCode().equals(dto.getSubjectCode())
                && subjectRepository.existsBySubjectCodeAndExam_IdAndDeletedFalseAndIdNot(
                        dto.getSubjectCode(),
                        exam.getId(),
                        id
                )) {
            throw new RuntimeException("Subject code '" + dto.getSubjectCode() + "' already exists for this exam. Please use a different subject code.");
        }

        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setExamDate(dto.getExamDate());  
        subject.setStartTime(dto.getStartTime());
        subject.setEndTime(dto.getEndTime());
        subject.setDuration(dto.getDuration());
        subject.setTotalMarks(dto.getTotalMarks());
        
        if (!subject.getExam().getId().equals(dto.getExamId())) {
            subject.setExam(exam);
        }

        subject = subjectRepository.save(subject);
        return mapToDTO(subject);
    }

    @Override
    public List<SubjectDTO> getSubjectsByExamId(Long examId) {
        return subjectRepository.findByExam_IdAndDeletedFalse(examId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectDTO getSubjectById(Long id) {
        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        return mapToDTO(subject);
    }

    @Override
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        subject.setDeleted(true);
        subjectRepository.save(subject);
    }
    
    private void validateSubjectDate(LocalDate subjectDate, Exam exam) {
        if (subjectDate == null) {
            throw new RuntimeException("Subject exam date is required.");
        }
        
        LocalDate examStartDate = exam.getStartDate();
        LocalDate examEndDate = exam.getEndDate();
        
        if (subjectDate.isBefore(examStartDate) || subjectDate.isAfter(examEndDate)) {
            throw new RuntimeException(
                "Subject exam date must be between " + examStartDate + " and " + examEndDate
            );
        }
    }

    private Subject mapToEntity(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setExamDate(dto.getExamDate()); 
        subject.setStartTime(dto.getStartTime());
        subject.setEndTime(dto.getEndTime());
        subject.setDuration(dto.getDuration());
        subject.setTotalMarks(dto.getTotalMarks());
        subject.setDeleted(false);
        return subject;
    }

    private SubjectDTO mapToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setSubjectCode(subject.getSubjectCode());
        dto.setExamDate(subject.getExamDate());  
        dto.setStartTime(subject.getStartTime());
        dto.setEndTime(subject.getEndTime());
        dto.setDuration(subject.getDuration());
        dto.setTotalMarks(subject.getTotalMarks());

        if (subject.getExam() != null) {
            dto.setExamId(subject.getExam().getId());
        }

        return dto;
    }
}