package com.library.library_management_system.dto.request;

import com.library.library_management_system.enums.Genre;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "Invalid ISBN format")
    private String isbn;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @NotNull(message = "Genre is required")
    private Genre genre;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;

    @Size(max = 100, message = "Publisher must be less than 100 characters")
    private String publisher;

    @Size(max = 50, message = "Language must be less than 50 characters")
    @Builder.Default
    private String language = "English";

    @Positive(message = "Pages must be positive")
    private Integer pages;

    @NotNull(message = "Total copies is required")
    @Positive(message = "Total copies must be positive")
    private Integer totalCopies;

    @Size(max = 50, message = "Shelf location must be less than 50 characters")
    private String shelfLocation;

    @Pattern(regexp = "^https?://.*", message = "Cover image URL must be a valid URL")
    private String coverImageUrl;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private Double price;

    @Size(max = 20, message = "Edition must be less than 20 characters")
    private String edition;
}