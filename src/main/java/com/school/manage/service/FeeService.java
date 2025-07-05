package com.school.manage.service;


import com.school.manage.model.FeeStructure;
import com.school.manage.repository.FeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {
    private final FeeStructureRepository feeStructureRepository;

    public FeeStructure saveFeeStructure(FeeStructure feeStructure) {
        return feeStructureRepository.save(feeStructure);
    }

    public List<FeeStructure> getFeeStructureByYear(String academicYear) {
        return feeStructureRepository.findByAcademicYear(academicYear);
    }
}