package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "books")
public class Book {
    @Id
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String category; // TEXTBOOK, REFERENCE, FICTION, NON_FICTION, MAGAZINE, OTHER
    private String publisher;
    private int publishYear;
    private String location; // shelf/section identifier
    private int totalCopies;
    private int availableCopies;
    private String language;
    private String description;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
