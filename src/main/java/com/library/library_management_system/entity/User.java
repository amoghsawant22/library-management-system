package com.library.library_management_system.entity;


import com.library.library_management_system.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing library users (Admin/Member)
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "borrowingRecords"})
@EqualsAndHashCode(callSuper = true, exclude = {"borrowingRecords"})
public class User extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 120)
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "membership_date")
    private LocalDate membershipDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "max_books_allowed")
    @Builder.Default
    private Integer maxBooksAllowed = 5; // Default limit for members

    // One-to-many relationship with BorrowingRecord
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BorrowingRecord> borrowingRecords = new HashSet<>();

    // Custom constructor for basic user creation
    public User(String username, String fullName, String email, String password) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.membershipDate = LocalDate.now();
        this.role = UserRole.MEMBER;
        this.isActive = true;
        this.maxBooksAllowed = 5;
        this.borrowingRecords = new HashSet<>();
    }

    // Custom constructor with role
    public User(String username, String fullName, String email, String password, UserRole role) {
        this(username, fullName, email, password);
        this.role = role;
        setMaxBooksBasedOnRole();
    }

    // Helper methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isMember() {
        return role == UserRole.MEMBER;
    }

    // Custom setter for role to automatically adjust max books
    public void setRole(UserRole role) {
        this.role = role;
        setMaxBooksBasedOnRole();
    }

    private void setMaxBooksBasedOnRole() {
        if (role == UserRole.ADMIN) {
            this.maxBooksAllowed = 20; // Admins can borrow more books
        } else {
            this.maxBooksAllowed = 5; // Default for members
        }
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (membershipDate == null) {
            membershipDate = LocalDate.now();
        }
        if (role == null) {
            role = UserRole.MEMBER;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (maxBooksAllowed == null) {
            setMaxBooksBasedOnRole();
        }
    }
}