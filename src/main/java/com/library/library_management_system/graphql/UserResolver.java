package com.library.library_management_system.graphql;

import com.library.library_management_system.dto.request.UserSearchRequest;
import com.library.library_management_system.dto.request.UserUpdateRequest;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.enums.UserRole;
import com.library.library_management_system.service.UserService;
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
public class UserResolver {

    private final UserService userService;

    @QueryMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public UserResponse me() {
        log.debug("GraphQL: Get current user");
        return userService.getUserWithStats(getCurrentUserId());
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> users(@Argument Map<String, Object> filter,
                                     @Argument Map<String, Object> pagination) {
        log.debug("GraphQL: Get users with filter: {}", filter);

        UserSearchRequest searchRequest = buildUserSearchRequest(filter, pagination);
        PagedResponse<UserResponse> result = userService.searchUsers(searchRequest);

        return Map.of(
                "content", result.getContent(),
                "pageInfo", Map.of(
                        "page", result.getPage(),
                        "size", result.getSize(),
                        "totalElements", result.getTotalElements(),
                        "totalPages", result.getTotalPages(),
                        "hasNext", result.getHasNext(),
                        "hasPrevious", result.getHasPrevious(),
                        "first", result.getFirst(),
                        "last", result.getLast()
                )
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse user(@Argument Long id) {
        log.debug("GraphQL: Get user by ID: {}", id);
        return userService.getUserById(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@Argument Long id, @Argument Map<String, Object> input) {
        log.info("GraphQL: Update user ID: {}", id);

        UserUpdateRequest request = buildUserUpdateRequest(input);
        return userService.updateUser(id, request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument Long id) {
        log.info("GraphQL: Delete user ID: {}", id);
        userService.deleteUser(id);
        return true;
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> mostActiveUsers(@Argument(name = "limit") Integer limit) {
        log.debug("GraphQL: Get most active users, limit: {}", limit);
        return userService.getMostActiveUsers(limit != null ? limit : 10);
    }

    private UserSearchRequest buildUserSearchRequest(Map<String, Object> filter, Map<String, Object> pagination) {
        UserSearchRequest.UserSearchRequestBuilder builder = UserSearchRequest.builder();

        if (filter != null) {
            if (filter.containsKey("searchTerm")) builder.searchTerm((String) filter.get("searchTerm"));
            if (filter.containsKey("role")) builder.role(UserRole.valueOf((String) filter.get("role")));
        }

        if (pagination != null) {
            if (pagination.containsKey("page")) builder.page((Integer) pagination.get("page"));
            if (pagination.containsKey("size")) builder.size((Integer) pagination.get("size"));
        }

        return builder.build();
    }

    private UserUpdateRequest buildUserUpdateRequest(Map<String, Object> input) {
        UserUpdateRequest.UserUpdateRequestBuilder builder = UserUpdateRequest.builder();

        if (input.containsKey("fullName")) builder.fullName((String) input.get("fullName"));
        if (input.containsKey("email")) builder.email((String) input.get("email"));
        if (input.containsKey("phoneNumber")) builder.phoneNumber((String) input.get("phoneNumber"));
        if (input.containsKey("role")) builder.role(UserRole.valueOf((String) input.get("role")));

        return builder.build();
    }

    private Long getCurrentUserId() {
        return 1L; // Placeholder - extract from security context
    }
}