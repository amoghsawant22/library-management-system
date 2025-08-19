package com.library.library_management_system.dto.request;

import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookUpdateRequest {

    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private Genre genre;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;

    @Size(max = 100, message = "Publisher must be less than 100 characters")
    private String publisher;

    @Size(max = 50, message = "Language must be less than 50 characters")
    private String language;

    @Positive(message = "Pages must be positive")
    private Integer pages;

    @PositiveOrZero(message = "Total copies must be positive or zero")
    private Integer totalCopies;

    @PositiveOrZero(message = "Available copies must be positive or zero")
    private Integer availableCopies;

    private BookStatus status;

    @Size(max = 50, message = "Shelf location must be less than 50 characters")
    private String shelfLocation;

    @Pattern(regexp = "^https?://.*", message = "Cover image URL must be a valid URL")
    private String coverImageUrl;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private Double price;

    @Size(max = 20, message = "Edition must be less than 20 characters")
    private String edition;

    private Boolean isActive;
}