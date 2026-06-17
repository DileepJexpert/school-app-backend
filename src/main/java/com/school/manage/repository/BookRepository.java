package com.school.manage.repository;

import com.school.manage.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByActiveTrueOrderByTitleAsc();
    List<Book> findByCategoryAndActiveTrue(String category);
    List<Book> findByTitleContainingIgnoreCaseAndActiveTrue(String title);
    Optional<Book> findByIsbnAndActiveTrue(String isbn);
}
