package com.library.library_management_system.service.impl;

import com.library.library_management_system.dto.mapper.BorrowingMapper;
import com.library.library_management_system.dto.request.BorrowBookRequest;
import com.library.library_management_system.dto.request.BorrowingSearchRequest;
import com.library.library_management_system.dto.request.ReturnBookRequest;
import com.library.library_management_system.dto.response.BorrowingHistoryResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.entity.Book;
import com.library.library_management_system.entity.BorrowingRecord;
import com.library.library_management_system.entity.User;
import com.library.library_management_system.enums.BorrowStatus;
import com.library.library_management_system.exception.BadRequestException;
import com.library.library_management_system.exception.ResourceNotFoundException;
import com.library.library_management_system.exception.UnauthorizedException;
import com.library.library_management_system.repository.BookRepository;
import com.library.library_management_system.repository.BorrowingRecordRepository;
import com.library.library_management_system.repository.UserRepository;
import com.library.library_management_system.security.UserPrincipal;
import com.library.library_management_system.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Borrowing Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private static final double DEFAULT_FINE_PER_DAY = 1.0;

    @Override
    @Transactional
    public BorrowingHistoryResponse borrowBook(BorrowBookRequest request) {
        log.info("Processing book borrow request for book ID: {}", request.getBookId());

        UserPrincipal currentUser = getCurrentUser();

        // Get user and book entities
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Validate borrowing eligibility
        validateBorrowingEligibility(user, book);

        // Create borrowing record
        BorrowingRecord borrowingRecord = new BorrowingRecord(user, book, request.getBorrowingPeriodDays());
        borrowingRecord.setNotes(request.getNotes());
        borrowingRecord.setIssuedBy(currentUser.getUsername());

        // Reserve book copy
        if (!book.isAvailable()) {
            throw new BadRequestException("Book is not available for borrowing");
        }

        book.borrowCopy();
        bookRepository.save(book);

        // Save borrowing record
        BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

        log.info("Book borrowed successfully: {} by user: {}", book.getTitle(), user.getUsername());
        return BorrowingMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional
    public BorrowingHistoryResponse returnBook(ReturnBookRequest request) {
        log.info("Processing book return for borrowing record ID: {}", request.getBorrowingRecordId());

        UserPrincipal currentUser = getCurrentUser();

        BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(request.getBorrowingRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found"));

        // Validate return eligibility
        validateReturnEligibility(borrowingRecord, currentUser);

        // Process return
        if (request.getIsLost()) {
            borrowingRecord.markAsLost();
        } else {
            borrowingRecord.returnBook();
        }

        borrowingRecord.setReturnedTo(currentUser.getUsername());
        borrowingRecord.setNotes(request.getNotes());

        // Update book availability
        Book book = borrowingRecord.getBook();
        if (!request.getIsLost()) {
            book.returnCopy();
            bookRepository.save(book);
        }

        BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

        log.info("Book returned successfully: {} by user: {}",
                book.getTitle(), borrowingRecord.getUser().getUsername());

        return BorrowingMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional
    public BorrowingHistoryResponse renewBook(Long borrowingRecordId, int additionalDays) {
        log.info("Renewing book for borrowing record ID: {}", borrowingRecordId);

        UserPrincipal currentUser = getCurrentUser();

        BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(borrowingRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found"));

        // Validate renewal eligibility
        validateRenewalEligibility(borrowingRecord, currentUser);

        if (!borrowingRecord.canRenew()) {
            throw new BadRequestException("Book cannot be renewed. Maximum renewals reached or book is overdue.");
        }

        borrowingRecord.renew(additionalDays);
        BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

        log.info("Book renewed successfully: {} for user: {}",
                borrowingRecord.getBook().getTitle(), borrowingRecord.getUser().getUsername());

        return BorrowingMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional
    public BorrowingHistoryResponse markBookAsLost(Long borrowingRecordId) {
        log.info("Marking book as lost for borrowing record ID: {}", borrowingRecordId);

        UserPrincipal currentUser = getCurrentUser();

        BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(borrowingRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found"));

        // Validate user permissions
        if (!currentUser.isAdmin() && !borrowingRecord.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to mark this book as lost");
        }

        borrowingRecord.markAsLost();
        BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

        log.info("Book marked as lost: {} by user: {}",
                borrowingRecord.getBook().getTitle(), borrowingRecord.getUser().getUsername());

        return BorrowingMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowingHistoryResponse getBorrowingRecordById(Long id) {
        log.debug("Getting borrowing record by ID: {}", id);

        BorrowingRecord record = borrowingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found"));

        UserPrincipal currentUser = getCurrentUser();

        // Check permissions
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to view this borrowing record");
        }

        return BorrowingMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingHistoryResponse> getUserBorrowingHistory(Long userId, int page, int size) {
        log.debug("Getting borrowing history for user ID: {}", userId);

        UserPrincipal currentUser = getCurrentUser();

        // Check permissions
        if (!currentUser.isAdmin() && !userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to view this user's borrowing history");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));
        Page<Object[]> historyPage = borrowingRecordRepository.findUserBorrowingHistory(userId, pageable);

        // Convert Object[] results to BorrowingHistoryResponse
        List<BorrowingHistoryResponse> content = historyPage.getContent().stream()
                .map(this::mapObjectArrayToResponse)
                .toList();

        return PagedResponse.of(content, page, size, historyPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingHistoryResponse> getCurrentUserBorrowingHistory(int page, int size) {
        UserPrincipal currentUser = getCurrentUser();
        return getUserBorrowingHistory(currentUser.getId(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistoryResponse> getUserCurrentBorrowedBooks(Long userId) {
        log.debug("Getting current borrowed books for user ID: {}", userId);

        UserPrincipal currentUser = getCurrentUser();

        // Check permissions
        if (!currentUser.isAdmin() && !userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to view this user's borrowed books");
        }

        List<Object[]> currentBooks = borrowingRecordRepository.findUserCurrentBorrowedBooks(userId);

        return currentBooks.stream()
                .map(this::mapObjectArrayToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistoryResponse> getCurrentUserBorrowedBooks() {
        UserPrincipal currentUser = getCurrentUser();
        return getUserCurrentBorrowedBooks(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingHistoryResponse> getAllBorrowingRecords(int page, int size, String sortBy, String sortDirection) {
        log.debug("Getting all borrowing records - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BorrowingRecord> recordPage = borrowingRecordRepository.findAll(pageable);

        List<BorrowingHistoryResponse> content = recordPage.getContent().stream()
                .map(BorrowingMapper::toResponse)
                .toList();

        return PagedResponse.of(content, page, size, recordPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingHistoryResponse> searchBorrowingRecords(BorrowingSearchRequest request) {
        log.debug("Searching borrowing records with filters: {}", request);

        // Implementation would use the repository methods with dynamic queries
        // For now, return basic pagination
        return getAllBorrowingRecords(request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDirection());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistoryResponse> getOverdueBooks() {
        log.debug("Getting overdue books");

        List<Object[]> overdueBooks = borrowingRecordRepository.findOverdueBooks();

        return overdueBooks.stream()
                .map(this::mapObjectArrayToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistoryResponse> getBorrowingRecordsByStatus(BorrowStatus status) {
        log.debug("Getting borrowing records by status: {}", status);

        List<BorrowingRecord> records = borrowingRecordRepository.findByStatus(status);

        return records.stream()
                .map(BorrowingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingHistoryResponse> getBookBorrowingHistory(Long bookId, int page, int size) {
        log.debug("Getting borrowing history for book ID: {}", bookId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));
        List<BorrowingRecord> records = borrowingRecordRepository.findByBookId(bookId);

        List<BorrowingHistoryResponse> content = records.stream()
                .map(BorrowingMapper::toResponse)
                .toList();

        return PagedResponse.of(content, page, size, records.size());
    }

    @Override
    @Transactional
    public int updateOverdueRecords() {
        log.info("Updating overdue records");
        return borrowingRecordRepository.markOverdueRecords();
    }

    @Override
    @Transactional
    public int updateOverdueFines(double finePerDay) {
        log.info("Updating overdue fines with rate: {} per day", finePerDay);
        return borrowingRecordRepository.updateOverdueFines(finePerDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getUsersWithOutstandingFines() {
        log.debug("Getting users with outstanding fines");
        return borrowingRecordRepository.getUsersWithOutstandingFines();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getLibraryStatistics() {
        log.debug("Getting library statistics");
        return borrowingRecordRepository.getLibraryStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getOverdueStatistics() {
        log.debug("Getting overdue statistics");
        return borrowingRecordRepository.getOverdueStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getBorrowingTrendsByMonth(LocalDate startDate) {
        log.debug("Getting borrowing trends since: {}", startDate);
        return borrowingRecordRepository.getBorrowingTrendsByMonth(startDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserBorrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        try {
            validateBorrowingEligibility(user, book);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReturnBook(Long borrowingRecordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .orElse(null);

        return record != null &&
                (record.getStatus() == BorrowStatus.BORROWED || record.getStatus() == BorrowStatus.OVERDUE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canRenewBook(Long borrowingRecordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .orElse(null);

        return record != null && record.canRenew();
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getUserBorrowingCapacity(Long userId) {
        return borrowingRecordRepository.getUserBorrowingCapacity(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUserBorrowings(Long userId) {
        return borrowingRecordRepository.countActiveUserBorrowings(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOverdueBooks() {
        List<BorrowingRecord> overdueRecords = borrowingRecordRepository.findOverdueRecords(LocalDate.now());
        return overdueRecords.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countBorrowingsByStatus(BorrowStatus status) {
        List<BorrowingRecord> records = borrowingRecordRepository.findByStatus(status);
        return records.size();
    }

    // Private helper methods

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private void validateBorrowingEligibility(User user, Book book) {
        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadRequestException("User account is not active");
        }

        // Check if book is active and available
        if (!book.getIsActive()) {
            throw new BadRequestException("Book is not active");
        }

        if (!book.isAvailable()) {
            throw new BadRequestException("Book is not available for borrowing");
        }

        // Check user's borrowing limit
        long currentBorrowings = borrowingRecordRepository.countActiveUserBorrowings(user.getId());
        if (currentBorrowings >= user.getMaxBooksAllowed()) {
            throw new BadRequestException("User has reached maximum borrowing limit");
        }

        // Check for outstanding fines (optional rule)
        List<Object[]> fines = borrowingRecordRepository.getUsersWithOutstandingFines();
        boolean hasOutstandingFines = fines.stream()
                .anyMatch(fine -> ((Number) fine[0]).longValue() == user.getId());

        if (hasOutstandingFines) {
            throw new BadRequestException("User has outstanding fines. Please clear fines before borrowing.");
        }
    }

    private void validateReturnEligibility(BorrowingRecord record, UserPrincipal currentUser) {
        // Check if user is authorized to return this book
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to return this book");
        }

        // Check if book can be returned
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("Book has already been returned");
        }

        if (record.getStatus() == BorrowStatus.LOST) {
            throw new BadRequestException("Book is marked as lost");
        }
    }

    private void validateRenewalEligibility(BorrowingRecord record, UserPrincipal currentUser) {
        // Check if user is authorized to renew this book
        if (!currentUser.isAdmin() && !record.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to renew this book");
        }

        // Check if book can be renewed
        if (record.getStatus() != BorrowStatus.BORROWED) {
            throw new BadRequestException("Only borrowed books can be renewed");
        }
    }

    private BorrowingHistoryResponse mapObjectArrayToResponse(Object[] result) {
        // This would map the Object[] from native queries to BorrowingHistoryResponse
        // Implementation would depend on the exact query structure
        // For now, return a basic response (this should be properly implemented)
        return BorrowingHistoryResponse.builder().build();
    }
}