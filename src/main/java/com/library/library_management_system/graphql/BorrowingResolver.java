package com.library.library_management_system.graphql;

import com.library.library_management_system.dto.request.BorrowBookRequest;
import com.library.library_management_system.dto.request.ReturnBookRequest;
import com.library.library_management_system.dto.response.BorrowingHistoryResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.enums.BorrowStatus;
import com.library.library_management_system.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BorrowingResolver {

    private final BorrowingService borrowingService;

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public List<BorrowingHistoryResponse> myCurrentBooks() {
        log.debug("GraphQL: Get current user's borrowed books");
        return borrowingService.getCurrentUserBorrowedBooks();
    }

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public Map<String, Object> myBorrowingHistory(@Argument Map<String, Object> pagination) {
        log.debug("GraphQL: Get current user borrowing history");

        int page = pagination != null ? (Integer) pagination.getOrDefault("page", 0) : 0;
        int size = pagination != null ? (Integer) pagination.getOrDefault("size", 10) : 10;

        PagedResponse<BorrowingHistoryResponse> result =
                borrowingService.getCurrentUserBorrowingHistory(page, size);

        return Map.of(
                "content", result.getContent(),
                "pageInfo", Map.of(
                        "page", result.getPage(),
                        "size", result.getSize(),
                        "totalElements", result.getTotalElements(),
                        "totalPages", result.getTotalPages(),
                        "hasNext", result.getHasNext(),
                        "hasPrevious", result.getHasPrevious()
                )
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BorrowingHistoryResponse> overdueBooks() {
        log.debug("GraphQL: Get overdue books");
        return borrowingService.getOverdueBooks();
    }

    @MutationMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public BorrowingHistoryResponse borrowBook(@Argument Map<String, Object> input) {
        log.info("GraphQL: Borrow book");

        BorrowBookRequest request = BorrowBookRequest.builder()
                .bookId(((Number) input.get("bookId")).longValue())
                .borrowingPeriodDays(input.containsKey("borrowingPeriodDays") ?
                        (Integer) input.get("borrowingPeriodDays") : 14)
                .notes((String) input.get("notes"))
                .build();

        return borrowingService.borrowBook(request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public BorrowingHistoryResponse returnBook(@Argument Map<String, Object> input) {
        log.info("GraphQL: Return book");

        ReturnBookRequest request = ReturnBookRequest.builder()
                .borrowingRecordId(((Number) input.get("borrowingRecordId")).longValue())
                .notes((String) input.get("notes"))
                .isLost(input.containsKey("isLost") ? (Boolean) input.get("isLost") : false)
                .build();

        return borrowingService.returnBook(request);
    }

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public Boolean canUserBorrowBook(@Argument Long userId, @Argument Long bookId) {
        log.debug("GraphQL: Check if user {} can borrow book {}", userId, bookId);
        return borrowingService.canUserBorrowBook(userId, bookId);
    }
}
