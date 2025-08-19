package com.library.library_management_system.dto.request;

import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchRequest {

    private String searchTerm; // Search in title, author, ISBN
    private String title;
    private String author;
    private String isbn;
    private Genre genre;
    private BookStatus status;
    private String publisher;
    private String language;

    private Integer minYear;
    private Integer maxYear;

    @Min(value = 1, message = "Minimum pages must be at least 1")
    private Integer minPages;

    @Min(value = 1, message = "Maximum pages must be at least 1")
    private Integer maxPages;

    private Double minPrice;
    private Double maxPrice;

    private Boolean availableOnly = false;
    private Boolean isActive = true;

    // Sorting options
    @Pattern(regexp = "^(title|author|publicationDate|genre|createdAt|totalBorrows)$",
            message = "Invalid sort field")
    @Builder.Default
    private String sortBy = "title";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String sortDirection = "asc";

    // Pagination
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private Integer size = 10;
}
