package com.school.manage.repository;

import com.school.manage.model.CertificateRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CertificateRepository extends MongoRepository<CertificateRecord, String> {

    List<CertificateRecord> findByStudentId(String studentId);

    List<CertificateRecord> findByCertificateType(String certificateType);

    List<CertificateRecord> findByClassNameAndAcademicYear(String className, String academicYear);
}
