package com.library.library_management_system.dto.request;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFilterDto {

    private List<String> genres;
    private List<String> authors;
    private List<String> publishers;
    private List<String> languages;
    private List<String> statuses;

    private Integer minYear;
    private Integer maxYear;
    private Integer minPages;
    private Integer maxPages;
    private Double minPrice;
    private Double maxPrice;

    private Boolean hasAvailableCopies;
    private Boolean isPopular; // Has more than average borrows
    private Boolean isNewArrival; // Added in last 30 days

    // Advanced filters
    private Integer minBorrowCount;
    private Integer maxBorrowCount;
    private Double minRating; // If ratings are implemented
    private Double maxRating;
}
