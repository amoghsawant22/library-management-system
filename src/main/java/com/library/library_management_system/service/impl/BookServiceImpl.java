package com.library.library_management_system.service.impl;

import com.library.library_management_system.dto.mapper.BookMapper;
import com.library.library_management_system.dto.mapper.PageMapper;
import com.library.library_management_system.dto.request.BookRequest;
import com.library.library_management_system.dto.request.BookSearchRequest;
import com.library.library_management_system.dto.request.BookUpdateRequest;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.entity.Book;
import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import com.library.library_management_system.exception.BadRequestException;
import com.library.library_management_system.exception.ResourceNotFoundException;
import com.library.library_management_system.repository.BookRepository;
import com.library.library_management_system.repository.BorrowingRecordRepository;
import com.library.library_management_system.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Book Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Override
    @CacheEvict(value = {"books", "bookStats"}, allEntries = true)
    @Transactional
    public BookResponse addBook(BookRequest request) {
        log.info("Adding new book: {}", request.getTitle());

        // Check if ISBN already exists
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = BookMapper.toEntity(request);
        Book savedBook = bookRepository.save(book);

        log.info("Book added successfully: {} (ID: {})", savedBook.getTitle(), savedBook.getId());
        return BookMapper.toResponse(savedBook);
    }

    @Override
    @Cacheable(value = "books", key = "#id")
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        log.debug("Getting book by ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        return BookMapper.toResponse(book);
    }

    @Override
    @Cacheable(value = "books", key = "#isbn")
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        log.debug("Getting book by ISBN: {}", isbn);

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));

        return BookMapper.toResponse(book);
    }

    @Override
    @CacheEvict(value = {"books", "bookStats"}, key = "#id")
    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        log.info("Updating book with ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        // ✅ REMOVED ISBN CHECK - ISBN should not be updated after creation
        // ISBN is a unique identifier and should remain constant

        // Validate availability logic
        if (request.getAvailableCopies() != null && request.getTotalCopies() != null) {
            if (request.getAvailableCopies() > request.getTotalCopies()) {
                throw new BadRequestException("Available copies cannot exceed total copies");
            }
        }

        // Additional validation for available copies
        if (request.getAvailableCopies() != null && request.getTotalCopies() == null) {
            if (request.getAvailableCopies() > book.getTotalCopies()) {
                throw new BadRequestException("Available copies cannot exceed total copies");
            }
        }

        BookMapper.updateEntityFromRequest(book, request);
        Book updatedBook = bookRepository.save(book);

        log.info("Book updated successfully: {} (ID: {})", updatedBook.getTitle(), updatedBook.getId());
        return BookMapper.toResponse(updatedBook);
    }

    @Override
    @CacheEvict(value = {"books", "bookStats"}, key = "#id")
    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        // Check if book has active borrowings
        long activeBorrowings = borrowingRecordRepository.countActiveUserBorrowings(id);
        if (activeBorrowings > 0) {
            throw new BadRequestException("Cannot delete book with active borrowings");
        }

        // Soft delete by deactivating
        book.setIsActive(false);
        bookRepository.save(book);

        log.info("Book soft deleted: {} (ID: {})", book.getTitle(), book.getId());
    }

    @Override
    @CacheEvict(value = {"books", "bookStats"}, key = "#id")
    @Transactional
    public void toggleBookStatus(Long id, boolean isActive) {
        log.info("Toggling book status for ID: {} to {}", id, isActive);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        book.setIsActive(isActive);
        bookRepository.save(book);

        log.info("Book status updated successfully for: {} (ID: {})", book.getTitle(), id);
    }

    @Override
    @Cacheable(value = "books")
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getAllBooks(int page, int size, String sortBy, String sortDirection) {
        log.debug("Getting all books - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Book> bookPage = bookRepository.findAll(pageable);

        return PageMapper.toPagedResponse(bookPage, BookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> searchBooks(BookSearchRequest request) {
        log.debug("Searching books with filters: {}", request);

        Sort.Direction direction = request.getSortDirection().equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(direction, request.getSortBy()));

        Page<Book> bookPage;

        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            bookPage = bookRepository.searchBooks(request.getSearchTerm(), pageable);
        } else {
            // Use advanced filters
            bookPage = bookRepository.findBooksWithFilters(
                    request.getGenre() != null ? request.getGenre().name() : null,
                    request.getAuthor(),
                    request.getMinYear(),
                    request.getMaxYear(),
                    request.getAvailableOnly(),
                    pageable
            );
        }

        return PageMapper.toPagedResponse(bookPage, BookMapper::toResponse);
    }

    @Override
    @Cacheable(value = "books", key = "#genre.name()")
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByGenre(Genre genre) {
        log.debug("Getting books by genre: {}", genre);

        List<Book> books = bookRepository.findByGenre(genre);
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getBooksByAuthor(String author, int page, int size) {
        log.debug("Getting books by author: {}", author);

        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        Page<Book> bookPage = bookRepository.findByAuthorContaining(author, pageable);

        return PageMapper.toPagedResponse(bookPage, BookMapper::toResponse);
    }

    @Override
    @Cacheable(value = "books", key = "'available'")
    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks() {
        log.debug("Getting available books");

        List<Book> books = bookRepository.findAvailableBooks();
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByStatus(BookStatus status) {
        log.debug("Getting books by status: {}", status);

        List<Book> books = bookRepository.findByStatus(status);
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "bookStats", key = "'mostBorrowed:' + #limit")
    @Transactional(readOnly = true)
    public List<BookResponse> getMostBorrowedBooks(int limit) {
        log.debug("Getting most borrowed books, limit: {}", limit);

        List<Book> books = bookRepository.findMostBorrowedBooks(limit);
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "bookStats", key = "'lowAvailability:' + #threshold")
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksWithLowAvailability(int threshold) {
        log.debug("Getting books with low availability, threshold: {}", threshold);

        List<Book> books = bookRepository.findBooksWithLowAvailability(threshold);
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getRecentlyAddedBooks(LocalDate sinceDate) {
        log.debug("Getting recently added books since: {}", sinceDate);

        List<Book> books = bookRepository.findRecentlyAddedBooks(sinceDate);
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "bookStats", key = "'needingAttention'")
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksNeedingAttention() {
        log.debug("Getting books needing attention");

        List<Book> books = bookRepository.findBooksNeedingAttention();
        return books.stream().map(BookMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "bookStats", key = "#bookId")
    @Transactional(readOnly = true)
    public BookResponse getBookWithStats(Long bookId) {
        log.debug("Getting book with stats for ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Get borrowing statistics (this would need to be implemented in repository)
        // For now, return basic response
        return BookMapper.toResponse(book);
    }

    @Override
    @Cacheable(value = "bookStats", key = "'availability'")
    @Transactional(readOnly = true)
    public Object[] getBookAvailabilityStats() {
        log.debug("Getting book availability statistics");
        return bookRepository.getBookAvailabilityStats();
    }

    @Override
    @Cacheable(value = "bookStats", key = "'genreStats'")
    @Transactional(readOnly = true)
    public List<Object[]> getPopularGenreStats() {
        log.debug("Getting popular genre statistics");
        return bookRepository.getPopularGenreStats();
    }

    @Override
    @Transactional(readOnly = true)
    public long countBooksByGenre(Genre genre) {
        return bookRepository.countBooksByGenre(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableBooks() {
        return bookRepository.findAvailableBooks().size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        return book.isAvailable();
    }

    @Override
    @Transactional
    public boolean reserveCopy(Long bookId) {
        log.debug("Reserving copy for book ID: {}", bookId);

        String currentUser = getCurrentUsername();
        int updated = bookRepository.decrementAvailableCopies(bookId, currentUser);

        return updated > 0;
    }

    @Override
    @Transactional
    public boolean releaseCopy(Long bookId) {
        log.debug("Releasing copy for book ID: {}", bookId);

        String currentUser = getCurrentUsername();
        int updated = bookRepository.incrementAvailableCopies(bookId, currentUser);

        return updated > 0;
    }

    @Override
    @CacheEvict(value = {"books", "bookStats"}, key = "#bookId")
    @Transactional
    public void updateBookAvailability(Long bookId, int totalCopies, int availableCopies) {
        log.info("Updating book availability for ID: {}, total: {}, available: {}",
                bookId, totalCopies, availableCopies);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (availableCopies > totalCopies) {
            throw new BadRequestException("Available copies cannot exceed total copies");
        }

        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        bookRepository.save(book);

        log.info("Book availability updated successfully for: {} (ID: {})", book.getTitle(), bookId);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}