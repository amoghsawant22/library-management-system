package com.library.library_management_system.graphql;

import com.library.library_management_system.dto.request.BookRequest;
import com.library.library_management_system.dto.request.BookSearchRequest;
import com.library.library_management_system.dto.request.BookUpdateRequest;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.enums.BookStatus;
import com.library.library_management_system.enums.Genre;
import com.library.library_management_system.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookResolver {

    private final BookService bookService;

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public Map<String, Object> books(@Argument Map<String, Object> filter,
                                     @Argument Map<String, Object> pagination) {
        log.debug("GraphQL: Get books with filter: {}", filter);

        BookSearchRequest searchRequest = buildBookSearchRequest(filter, pagination);
        PagedResponse<BookResponse> result = bookService.searchBooks(searchRequest);

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
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public BookResponse book(@Argument Long id) {
        log.debug("GraphQL: Get book by ID: {}", id);
        return bookService.getBookById(id);
    }

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public BookResponse bookByIsbn(@Argument String isbn) {
        log.debug("GraphQL: Get book by ISBN: {}", isbn);
        return bookService.getBookByIsbn(isbn);
    }

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public List<BookResponse> availableBooks() {
        log.debug("GraphQL: Get available books");
        return bookService.getAvailableBooks();
    }

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public List<BookResponse> mostBorrowedBooks(@Argument(name = "limit") Integer limit) {
        log.debug("GraphQL: Get most borrowed books, limit: {}", limit);
        return bookService.getMostBorrowedBooks(limit != null ? limit : 10);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse addBook(@Argument Map<String, Object> input) {
        log.info("GraphQL: Add book");

        BookRequest request = buildBookRequest(input);
        return bookService.addBook(request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse updateBook(@Argument Long id, @Argument Map<String, Object> input) {
        log.info("GraphQL: Update book ID: {}", id);

        BookUpdateRequest request = buildBookUpdateRequest(input);
        return bookService.updateBook(id, request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteBook(@Argument Long id) {
        log.info("GraphQL: Delete book ID: {}", id);
        bookService.deleteBook(id);
        return true;
    }

    private BookSearchRequest buildBookSearchRequest(Map<String, Object> filter, Map<String, Object> pagination) {
        BookSearchRequest.BookSearchRequestBuilder builder = BookSearchRequest.builder();

        if (filter != null) {
            if (filter.containsKey("searchTerm")) builder.searchTerm((String) filter.get("searchTerm"));
            if (filter.containsKey("genre")) builder.genre(Genre.valueOf((String) filter.get("genre")));
            if (filter.containsKey("availableOnly")) builder.availableOnly((Boolean) filter.get("availableOnly"));
        }

        if (pagination != null) {
            if (pagination.containsKey("page")) builder.page((Integer) pagination.get("page"));
            if (pagination.containsKey("size")) builder.size((Integer) pagination.get("size"));
        }

        return builder.build();
    }

    private BookRequest buildBookRequest(Map<String, Object> input) {
        return BookRequest.builder()
                .title((String) input.get("title"))
                .author((String) input.get("author"))
                .isbn((String) input.get("isbn"))
                .genre(Genre.valueOf((String) input.get("genre")))
                .totalCopies((Integer) input.get("totalCopies"))
                .description((String) input.get("description"))
                .publisher((String) input.get("publisher"))
                .build();
    }

    private BookUpdateRequest buildBookUpdateRequest(Map<String, Object> input) {
        BookUpdateRequest.BookUpdateRequestBuilder builder = BookUpdateRequest.builder();

        if (input.containsKey("title")) builder.title((String) input.get("title"));
        if (input.containsKey("author")) builder.author((String) input.get("author"));
        if (input.containsKey("description")) builder.description((String) input.get("description"));
        if (input.containsKey("totalCopies")) builder.totalCopies((Integer) input.get("totalCopies"));

        return builder.build();
    }
}
