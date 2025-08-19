package com.library.library_management_system.controller;

import com.library.library_management_system.dto.request.BookRequest;
import com.library.library_management_system.dto.request.BookSearchRequest;
import com.library.library_management_system.dto.request.BookUpdateRequest;
import com.library.library_management_system.dto.response.ApiResponse;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import com.library.library_management_system.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Book Management Controller
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Management", description = "Book management and catalog endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BookController {

    private final BookService bookService;

    // ============= Book CRUD Operations =============

    @Operation(summary = "Add a new book", description = "Add a new book to the library catalog (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book added successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid book data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Book with ISBN already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> addBook(
            @Parameter(description = "Book details", required = true)
            @Valid @RequestBody BookRequest request) {

        log.info("Add book request: {}", request.getTitle());

        BookResponse book = bookService.addBook(request);

        return ResponseEntity.status(201)
                .body(ApiResponse.success(book, "Book added successfully"));
    }

    @Operation(summary = "Get all books", description = "Get all books with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getAllBooks(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Get all books request - page: {}, size: {}, sortBy: {}", page, size, sortBy);

        PagedResponse<BookResponse> books = bookService.getAllBooks(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Search books", description = "Search books with advanced filters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @PostMapping("/search")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> searchBooks(
            @Parameter(description = "Search criteria", required = true)
            @Valid @RequestBody BookSearchRequest request) {

        log.debug("Book search request: {}", request.getSearchTerm());

        PagedResponse<BookResponse> books = bookService.searchBooks(request);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get book by ID", description = "Get book details by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {

        log.debug("Get book by ID: {}", id);

        BookResponse book = bookService.getBookById(id);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @Operation(summary = "Get book by ISBN", description = "Get book details by ISBN")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found")
    })
    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> getBookByIsbn(
            @Parameter(description = "Book ISBN", required = true)
            @PathVariable String isbn) {

        log.debug("Get book by ISBN: {}", isbn);

        BookResponse book = bookService.getBookByIsbn(isbn);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @Operation(summary = "Update book", description = "Update book details (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "ISBN already exists")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Book update details", required = true)
            @Valid @RequestBody BookUpdateRequest request) {

        log.info("Update book request for ID: {}", id);

        BookResponse book = bookService.updateBook(id, request);

        return ResponseEntity.ok(ApiResponse.success(book, "Book updated successfully"));
    }

    @Operation(summary = "Delete book", description = "Soft delete book (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete book with active borrowings")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {

        log.info("Delete book request for ID: {}", id);

        bookService.deleteBook(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }

    @Operation(summary = "Toggle book status", description = "Activate/deactivate book (Admin only)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleBookStatus(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "New active status", required = true)
            @RequestParam boolean isActive) {

        log.info("Toggle book status for ID: {} to {}", id, isActive);

        bookService.toggleBookStatus(id, isActive);

        return ResponseEntity.ok(ApiResponse.success(null,
                String.format("Book %s successfully", isActive ? "activated" : "deactivated")));
    }

    // ============= Book Catalog and Search =============

    @Operation(summary = "Get books by genre", description = "Get all books of a specific genre")
    @GetMapping("/genre/{genre}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByGenre(
            @Parameter(description = "Book genre", required = true)
            @PathVariable Genre genre) {

        log.debug("Get books by genre: {}", genre);

        List<BookResponse> books = bookService.getBooksByGenre(genre);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get books by author", description = "Get books by author with pagination")
    @GetMapping("/author/{author}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBooksByAuthor(
            @Parameter(description = "Author name", required = true)
            @PathVariable String author,
            @Parameter(description = "Page number")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Get books by author: {}", author);

        PagedResponse<BookResponse> books = bookService.getBooksByAuthor(author, page, size);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get available books", description = "Get all currently available books")
    @GetMapping("/available")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAvailableBooks() {

        log.debug("Get available books request");

        List<BookResponse> books = bookService.getAvailableBooks();

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get books by status", description = "Get books filtered by status (Admin only)")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByStatus(
            @Parameter(description = "Book status", required = true)
            @PathVariable BookStatus status) {

        log.debug("Get books by status: {}", status);

        List<BookResponse> books = bookService.getBooksByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    // ============= Book Statistics and Reports =============

    @Operation(summary = "Get most borrowed books", description = "Get most popular books by borrowing frequency")
    @GetMapping("/most-borrowed")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getMostBorrowedBooks(
            @Parameter(description = "Number of books to return")
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get most borrowed books request - limit: {}", limit);

        List<BookResponse> books = bookService.getMostBorrowedBooks(limit);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get books with low availability", description = "Get books with limited copies available (Admin only)")
    @GetMapping("/low-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksWithLowAvailability(
            @Parameter(description = "Availability threshold")
            @RequestParam(defaultValue = "2") int threshold) {

        log.debug("Get books with low availability - threshold: {}", threshold);

        List<BookResponse> books = bookService.getBooksWithLowAvailability(threshold);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get recently added books", description = "Get books added since a specific date")
    @GetMapping("/recent")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getRecentlyAddedBooks(
            @Parameter(description = "Since date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate) {

        log.debug("Get recently added books since: {}", sinceDate);

        List<BookResponse> books = bookService.getRecentlyAddedBooks(sinceDate);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get books needing attention", description = "Get books that are damaged, lost, or need maintenance (Admin only)")
    @GetMapping("/needs-attention")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksNeedingAttention() {

        log.debug("Get books needing attention request");

        List<BookResponse> books = bookService.getBooksNeedingAttention();

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get book with statistics", description = "Get book details with borrowing statistics")
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> getBookWithStats(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {

        log.debug("Get book with stats by ID: {}", id);

        BookResponse book = bookService.getBookWithStats(id);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    // ============= Book Availability Management =============

    @Operation(summary = "Check book availability", description = "Check if book is available for borrowing")
    @GetMapping("/{id}/availability")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkBookAvailability(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {

        log.debug("Check book availability for ID: {}", id);

        boolean isAvailable = bookService.isBookAvailable(id);

        return ResponseEntity.ok(ApiResponse.success(isAvailable,
                isAvailable ? "Book is available" : "Book is not available"));
    }

    @Operation(summary = "Update book availability", description = "Update book copies (Admin only)")
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateBookAvailability(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Total copies", required = true)
            @RequestParam int totalCopies,
            @Parameter(description = "Available copies", required = true)
            @RequestParam int availableCopies) {

        log.info("Update book availability for ID: {} - total: {}, available: {}",
                id, totalCopies, availableCopies);

        bookService.updateBookAvailability(id, totalCopies, availableCopies);

        return ResponseEntity.ok(ApiResponse.success(null, "Book availability updated successfully"));
    }

    // ============= Book Statistics =============

    @Operation(summary = "Get book availability statistics", description = "Get overall book availability statistics (Admin only)")
    @GetMapping("/stats/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object[]>> getBookAvailabilityStats() {

        log.debug("Get book availability statistics request");

        Object[] stats = bookService.getBookAvailabilityStats();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get popular genre statistics", description = "Get borrowing statistics by genre")
    @GetMapping("/stats/popular-genres")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getPopularGenreStats() {

        log.debug("Get popular genre statistics request");

        List<Object[]> stats = bookService.getPopularGenreStats();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get book count by genre", description = "Get total count of books by genre")
    @GetMapping("/count/genre/{genre}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getBookCountByGenre(
            @Parameter(description = "Book genre", required = true)
            @PathVariable Genre genre) {

        log.debug("Get book count by genre: {}", genre);

        long count = bookService.countBooksByGenre(genre);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "Get available book count", description = "Get total count of available books")
    @GetMapping("/count/available")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getAvailableBookCount() {

        log.debug("Get available book count request");

        long count = bookService.countAvailableBooks();

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}