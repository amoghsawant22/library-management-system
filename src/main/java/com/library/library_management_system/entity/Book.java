package com.library.library_management_system.entity;

import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Book entity representing books in the library
 */
@Entity
@Table(name = "books",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "isbn")
        },
        indexes = {
                @Index(name = "idx_book_title", columnList = "title"),
                @Index(name = "idx_book_author", columnList = "author"),
                @Index(name = "idx_book_genre", columnList = "genre"),
                @Index(name = "idx_book_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"borrowingRecords"})
@EqualsAndHashCode(callSuper = true, exclude = {"borrowingRecords"})
public class Book extends BaseEntity {

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @NotBlank
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "Invalid ISBN format")
    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private Genre genre;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "language", length = 50)
    @Builder.Default
    private String language = "English";

    @Column(name = "pages")
    private Integer pages;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @NotNull
    @PositiveOrZero
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(name = "shelf_location", length = 50)
    private String shelfLocation;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "price")
    private Double price;

    @Column(name = "edition", length = 20)
    private String edition;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // One-to-many relationship with BorrowingRecord
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BorrowingRecord> borrowingRecords = new HashSet<>();

    // Custom constructors
    public Book(String title, String author, String isbn, Genre genre) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.status = BookStatus.AVAILABLE;
        this.language = "English";
        this.isActive = true;
        this.borrowingRecords = new HashSet<>();
    }

    public Book(String title, String author, String isbn, Genre genre, Integer totalCopies) {
        this(title, author, isbn, genre);
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    // Custom setter for available copies with status update
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
        updateBookStatus();
    }

    // Helper methods
    public boolean isAvailable() {
        return availableCopies > 0 && status == BookStatus.AVAILABLE && isActive;
    }

    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            updateBookStatus();
        }
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            updateBookStatus();
        }
    }

    public Integer getBorrowedCopies() {
        return totalCopies - availableCopies;
    }

    private void updateBookStatus() {
        if (availableCopies == 0) {
            this.status = BookStatus.BORROWED;
        } else if (availableCopies > 0 && this.status == BookStatus.BORROWED) {
            this.status = BookStatus.AVAILABLE;
        }
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (status == null) {
            status = BookStatus.AVAILABLE;
        }
        if (language == null) {
            language = "English";
        }
        if (isActive == null) {
            isActive = true;
        }
        if (availableCopies == null && totalCopies != null) {
            availableCopies = totalCopies;
        }
    }
}