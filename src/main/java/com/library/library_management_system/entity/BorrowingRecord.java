package com.library.library_management_system.entity;

import com.library.library_management_system.enums.BorrowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * BorrowingRecord entity representing book borrowing transactions
 */
@Entity
@Table(name = "borrowing_records",
        indexes = {
                @Index(name = "idx_borrowing_user", columnList = "user_id"),
                @Index(name = "idx_borrowing_book", columnList = "book_id"),
                @Index(name = "idx_borrowing_status", columnList = "status"),
                @Index(name = "idx_borrowing_borrow_date", columnList = "borrow_date"),
                @Index(name = "idx_borrowing_due_date", columnList = "due_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "book"})
@EqualsAndHashCode(callSuper = true)
public class BorrowingRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BorrowStatus status = BorrowStatus.BORROWED;

    @Column(name = "fine_amount")
    @Builder.Default
    private Double fineAmount = 0.0;

    @Column(name = "renewal_count")
    @Builder.Default
    private Integer renewalCount = 0;

    @Column(name = "max_renewals_allowed")
    @Builder.Default
    private Integer maxRenewalsAllowed = 2;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "issued_by")
    private String issuedBy; // Admin who issued the book

    @Column(name = "returned_to")
    private String returnedTo; // Admin who processed the return

    // Custom constructors
    public BorrowingRecord(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusWeeks(2); // 2 weeks borrowing period
        this.status = BorrowStatus.BORROWED;
        this.fineAmount = 0.0;
        this.renewalCount = 0;
        this.maxRenewalsAllowed = 2;
    }

    public BorrowingRecord(User user, Book book, int borrowingPeriodDays) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(borrowingPeriodDays);
        this.status = BorrowStatus.BORROWED;
        this.fineAmount = 0.0;
        this.renewalCount = 0;
        this.maxRenewalsAllowed = 2;
    }

    // Helper methods
    public boolean isOverdue() {
        if (status == BorrowStatus.RETURNED) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
    }

    public boolean canRenew() {
        return renewalCount < maxRenewalsAllowed &&
                status == BorrowStatus.BORROWED &&
                !isOverdue();
    }

    public void renew(int additionalDays) {
        if (canRenew()) {
            renewalCount++;
            dueDate = dueDate.plusDays(additionalDays);
            status = BorrowStatus.RENEWED;
        }
    }

    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.status = BorrowStatus.RETURNED;

        // Calculate fine if overdue
        if (isOverdue()) {
            long overdueDays = getDaysOverdue();
            this.fineAmount = overdueDays * 1.0; // $1 per day fine
        }
    }

    public void markAsLost() {
        this.status = BorrowStatus.LOST;
        this.returnDate = LocalDate.now();
        // Set fine amount to book price or a fixed amount
        if (book != null && book.getPrice() != null) {
            this.fineAmount = book.getPrice();
        } else {
            this.fineAmount = 50.0; // Default fine for lost book
        }
    }

    public long getBorrowingDuration() {
        LocalDate endDate = returnDate != null ? returnDate : LocalDate.now();
        return endDate.toEpochDay() - borrowDate.toEpochDay();
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (borrowDate == null) {
            borrowDate = LocalDate.now();
        }
        if (dueDate == null) {
            dueDate = LocalDate.now().plusWeeks(2); // Default 2 weeks
        }
        if (status == null) {
            status = BorrowStatus.BORROWED;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
        if (renewalCount == null) {
            renewalCount = 0;
        }
        if (maxRenewalsAllowed == null) {
            maxRenewalsAllowed = 2;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();

        // Auto-update status if overdue
        if (status == BorrowStatus.BORROWED && isOverdue()) {
            status = BorrowStatus.OVERDUE;
        }
    }
}