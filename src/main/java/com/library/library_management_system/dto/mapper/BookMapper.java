package com.library.library_management_system.dto.mapper;

import com.library.library_management_system.dto.request.BookRequest;
import com.library.library_management_system.dto.request.BookUpdateRequest;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.entity.Book;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BookMapper {

    public static Book toEntity(BookRequest request) {
        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .genre(request.getGenre())
                .publicationDate(request.getPublicationDate())
                .publisher(request.getPublisher())
                .language(request.getLanguage())
                .pages(request.getPages())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies()) // Initially all available
                .shelfLocation(request.getShelfLocation())
                .coverImageUrl(request.getCoverImageUrl())
                .price(request.getPrice())
                .edition(request.getEdition())
                .isActive(true)
                .build();
    }

    public static BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .genre(book.getGenre())
                .publicationDate(book.getPublicationDate())
                .publisher(book.getPublisher())
                .language(book.getLanguage())
                .pages(book.getPages())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .borrowedCopies(book.getBorrowedCopies())
                .status(book.getStatus())
                .shelfLocation(book.getShelfLocation())
                .coverImageUrl(book.getCoverImageUrl())
                .price(book.getPrice())
                .edition(book.getEdition())
                .isActive(book.getIsActive())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public static BookResponse toResponseWithStats(Book book, Long totalBorrows,
                                                   Long uniqueBorrowers, Double avgDuration, Double popularityScore) {
        BookResponse response = toResponse(book);
        response.setTotalBorrows(totalBorrows);
        response.setUniqueBorrowers(uniqueBorrowers);
        response.setAverageBorrowDuration(avgDuration);
        response.setPopularityScore(popularityScore);
        return response;
    }

    public static void updateEntityFromRequest(Book book, BookUpdateRequest request) {
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        if (request.getPublicationDate() != null) {
            book.setPublicationDate(request.getPublicationDate());
        }
        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher());
        }
        if (request.getLanguage() != null) {
            book.setLanguage(request.getLanguage());
        }
        if (request.getPages() != null) {
            book.setPages(request.getPages());
        }
        if (request.getTotalCopies() != null) {
            book.setTotalCopies(request.getTotalCopies());
        }
        if (request.getAvailableCopies() != null) {
            book.setAvailableCopies(request.getAvailableCopies());
        }
        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }
        if (request.getShelfLocation() != null) {
            book.setShelfLocation(request.getShelfLocation());
        }
        if (request.getCoverImageUrl() != null) {
            book.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }
        if (request.getEdition() != null) {
            book.setEdition(request.getEdition());
        }
        if (request.getIsActive() != null) {
            book.setIsActive(request.getIsActive());
        }
    }
}