package com.library.library_management_system.dto.response;

import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private Genre genre;
    private LocalDate publicationDate;
    private String publisher;
    private String language;
    private Integer pages;
    private Integer totalCopies;
    private Integer availableCopies;
    private Integer borrowedCopies;
    private BookStatus status;
    private String shelfLocation;
    private String coverImageUrl;
    private Double price;
    private String edition;
    private Boolean isActive;

    // Statistics
    private Long totalBorrows;
    private Long uniqueBorrowers;
    private Double averageBorrowDuration;
    private Double popularityScore;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
