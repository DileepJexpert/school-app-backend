package com.school.manage.repository;

import com.school.manage.model.Gallery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GalleryRepository extends MongoRepository<Gallery, String> {

    List<Gallery> findByPublishedTrueOrderByDateDesc();

    List<Gallery> findByCategory(String category);

    List<Gallery> findByCategoryAndPublishedTrue(String category);

    List<Gallery> findByAcademicYear(String academicYear);

    List<Gallery> findAllByOrderByDateDesc();
}
