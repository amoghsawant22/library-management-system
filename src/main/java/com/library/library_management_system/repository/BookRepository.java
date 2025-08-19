package com.library.library_management_system.repository;

import com.library.library_management_system.entity.Book;
import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Book entity with custom queries and native SQL
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ============= Basic CRUD Operations =============

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByGenre(Genre genre);

    List<Book> findByIsActiveTrue();

    // ============= Native SQL Search Queries =============

    /**
     * Search books by title, author, or ISBN using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true 
        AND (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY b.title
        """, nativeQuery = true)
    Page<Book> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find books with advanced filtering using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true
        AND (:genre IS NULL OR b.genre = :genre)
        AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
        AND (:minYear IS NULL OR YEAR(b.publication_date) >= :minYear)
        AND (:maxYear IS NULL OR YEAR(b.publication_date) <= :maxYear)
        AND (:availableOnly = false OR b.available_copies > 0)
        ORDER BY b.title
        """, nativeQuery = true)
    Page<Book> findBooksWithFilters(
            @Param("genre") String genre,
            @Param("author") String author,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("availableOnly") boolean availableOnly,
            Pageable pageable
    );

    /**
     * Get most borrowed books using native SQL
     */
    @Query(value = """
        SELECT 
            b.*,
            COUNT(br.id) as borrow_count
        FROM books b 
        INNER JOIN borrowing_records br ON b.id = br.book_id
        WHERE b.is_active = true
        GROUP BY b.id
        ORDER BY borrow_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Book> findMostBorrowedBooks(@Param("limit") int limit);

    /**
     * Get books with low availability using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true 
        AND b.available_copies <= :threshold
        AND b.total_copies > 0
        ORDER BY b.available_copies, b.title
        """, nativeQuery = true)
    List<Book> findBooksWithLowAvailability(@Param("threshold") int threshold);

    /**
     * Get book availability statistics using native SQL
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_books,
            SUM(b.total_copies) as total_copies,
            SUM(b.available_copies) as available_copies,
            SUM(b.total_copies - b.available_copies) as borrowed_copies,
            COUNT(CASE WHEN b.available_copies = 0 THEN 1 END) as out_of_stock_books
        FROM books b 
        WHERE b.is_active = true
        """, nativeQuery = true)
    Object[] getBookAvailabilityStats();

    /**
     * Find books by genre with availability info using native SQL
     */
    @Query(value = """
        SELECT 
            b.*,
            COUNT(br.id) as total_borrows,
            COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) as current_borrows
        FROM books b 
        LEFT JOIN borrowing_records br ON b.id = br.book_id
        WHERE b.is_active = true 
        AND b.genre = :genre
        GROUP BY b.id
        ORDER BY total_borrows DESC
        """, nativeQuery = true)
    List<Object[]> findBooksByGenreWithStats(@Param("genre") String genre);

    /**
     * Get recently added books using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true 
        AND b.created_at >= :sinceDate
        ORDER BY b.created_at DESC
        """, nativeQuery = true)
    List<Book> findRecentlyAddedBooks(@Param("sinceDate") LocalDate sinceDate);

    /**
     * Update book availability using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE books SET 
            available_copies = available_copies - 1,
            status = CASE 
                WHEN available_copies - 1 = 0 THEN 'BORROWED'
                ELSE status 
            END,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = :updatedBy
        WHERE id = :bookId 
        AND available_copies > 0
        """, nativeQuery = true)
    int decrementAvailableCopies(@Param("bookId") Long bookId, @Param("updatedBy") String updatedBy);

    /**
     * Return book (increment availability) using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE books SET 
            available_copies = available_copies + 1,
            status = CASE 
                WHEN available_copies + 1 > 0 AND status = 'BORROWED' THEN 'AVAILABLE'
                ELSE status 
            END,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = :updatedBy
        WHERE id = :bookId 
        AND available_copies < total_copies
        """, nativeQuery = true)
    int incrementAvailableCopies(@Param("bookId") Long bookId, @Param("updatedBy") String updatedBy);

    /**
     * Find books by author with pagination using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true 
        AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))
        ORDER BY b.title
        """, nativeQuery = true)
    Page<Book> findByAuthorContaining(@Param("author") String author, Pageable pageable);

    /**
     * Get books needing attention (damaged, lost, etc.) using native SQL
     */
    @Query(value = """
        SELECT * FROM books b 
        WHERE b.is_active = true 
        AND b.status IN ('DAMAGED', 'LOST', 'MAINTENANCE')
        ORDER BY b.updated_at DESC
        """, nativeQuery = true)
    List<Book> findBooksNeedingAttention();

    /**
     * Get popular books by genre using native SQL
     */
    @Query(value = """
        SELECT 
            b.genre,
            COUNT(br.id) as total_borrows,
            COUNT(DISTINCT b.id) as unique_books
        FROM books b 
        INNER JOIN borrowing_records br ON b.id = br.book_id
        WHERE b.is_active = true
        GROUP BY b.genre
        ORDER BY total_borrows DESC
        """, nativeQuery = true)
    List<Object[]> getPopularGenreStats();

    // ============= JPA Query Methods =============

    List<Book> findByStatus(BookStatus status);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.availableCopies > 0")
    List<Book> findAvailableBooks();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.genre = :genre AND b.isActive = true")
    long countBooksByGenre(@Param("genre") Genre genre);

    @Query("SELECT b FROM Book b WHERE b.publicationDate >= :date AND b.isActive = true ORDER BY b.publicationDate DESC")
    List<Book> findBooksByPublicationDateAfter(@Param("date") LocalDate date);
}