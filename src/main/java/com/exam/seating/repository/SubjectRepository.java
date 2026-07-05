package com.exam.seating.repository;

import com.exam.seating.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByIdAndDeletedFalse(Long id);

    Optional<Subject> findBySubjectCode(String subjectCode);
    
    Optional<Subject> findBySubjectCodeAndDeletedFalse(String subjectCode);

    List<Subject> findByExamId(Long examId);
    
    List<Subject> findByExam_IdAndDeletedFalse(Long examId);
    
    List<Subject> findByExamIdAndDeletedFalse(Long examId);

    boolean existsBySubjectCode(String subjectCode);
    
    boolean existsBySubjectCodeAndDeletedFalse(String subjectCode);
    
    boolean existsBySubjectCodeAndExam_IdAndDeletedFalse(
            String subjectCode,
            Long examId
    );

    boolean existsBySubjectCodeAndExam_IdAndDeletedFalseAndIdNot(
            String subjectCode,
            Long examId,
            Long id
    );

    Long countByExam_IdAndDeletedFalse(Long examId);
    
    Long countByExamIdAndDeletedFalse(Long examId);

    List<Subject> findBySubjectNameContainingIgnoreCase(String subjectName);
    
    List<Subject> findByExamIdAndSubjectNameContainingIgnoreCase(Long examId, String subjectName);
  
    Optional<Subject> findByExamIdAndSubjectCode(Long examId, String subjectCode);
}