package com.library.library_management_system.service;

import com.library.library_management_system.dto.request.BookRequest;
import com.library.library_management_system.dto.request.BookSearchRequest;
import com.library.library_management_system.dto.request.BookUpdateRequest;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;

import java.time.LocalDate;
import java.util.List;

/**
 * Book Service Interface
 */
public interface BookService {

    /**
     * Add a new book
     */
    BookResponse addBook(BookRequest request);

    /**
     * Get book by ID
     */
    BookResponse getBookById(Long id);

    /**
     * Get book by ISBN
     */
    BookResponse getBookByIsbn(String isbn);

    /**
     * Update book details
     */
    BookResponse updateBook(Long id, BookUpdateRequest request);

    /**
     * Delete book (soft delete)
     */
    void deleteBook(Long id);

    /**
     * Toggle book active status
     */
    void toggleBookStatus(Long id, boolean isActive);

    /**
     * Get all books with pagination
     */
    PagedResponse<BookResponse> getAllBooks(int page, int size, String sortBy, String sortDirection);

    /**
     * Search books with filters
     */
    PagedResponse<BookResponse> searchBooks(BookSearchRequest request);

    /**
     * Get books by genre
     */
    List<BookResponse> getBooksByGenre(Genre genre);

    /**
     * Get books by author
     */
    PagedResponse<BookResponse> getBooksByAuthor(String author, int page, int size);

    /**
     * Get available books
     */
    List<BookResponse> getAvailableBooks();

    /**
     * Get books by status
     */
    List<BookResponse> getBooksByStatus(BookStatus status);

    /**
     * Get most borrowed books
     */
    List<BookResponse> getMostBorrowedBooks(int limit);

    /**
     * Get books with low availability
     */
    List<BookResponse> getBooksWithLowAvailability(int threshold);

    /**
     * Get recently added books
     */
    List<BookResponse> getRecentlyAddedBooks(LocalDate sinceDate);

    /**
     * Get books needing attention (damaged, lost, etc.)
     */
    List<BookResponse> getBooksNeedingAttention();

    /**
     * Get book with statistics
     */
    BookResponse getBookWithStats(Long bookId);

    /**
     * Get book availability statistics
     */
    Object[] getBookAvailabilityStats();

    /**
     * Get popular genres statistics
     */
    List<Object[]> getPopularGenreStats();

    /**
     * Count books by genre
     */
    long countBooksByGenre(Genre genre);

    /**
     * Count available books
     */
    long countAvailableBooks();

    /**
     * Check if book exists
     */
    boolean existsById(Long id);

    /**
     * Check if ISBN already exists
     */
    boolean existsByIsbn(String isbn);

    /**
     * Check if book is available for borrowing
     */
    boolean isBookAvailable(Long bookId);

    /**
     * Reserve copies when borrowing
     */
    boolean reserveCopy(Long bookId);

    /**
     * Release copy when returning
     */
    boolean releaseCopy(Long bookId);

    /**
     * Update book availability
     */
    void updateBookAvailability(Long bookId, int totalCopies, int availableCopies);
}