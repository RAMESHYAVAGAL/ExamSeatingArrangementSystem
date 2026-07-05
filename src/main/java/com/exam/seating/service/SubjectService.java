package com.exam.seating.service;

import com.exam.seating.dto.SubjectDTO;
import java.util.List;

public interface SubjectService {

    SubjectDTO saveSubject(SubjectDTO dto);

    SubjectDTO updateSubject(Long id, SubjectDTO dto);

    List<SubjectDTO> getSubjectsByExamId(Long examId);

    SubjectDTO getSubjectById(Long id);

    void deleteSubject(Long id);
}