# library-management-system
 A comprehensive library management system built with Spring Boot, MySQL &amp; GraphQL


 
Nalanda Library Management System

 Complete Features Implemented:
- User management with JWT authentication
- Book catalog with advanced search
- Borrowing system with overdue tracking
- REST API with 50+ endpoints  
- GraphQL API for flexible querying
- MySQL database with JPA/Hibernate
- Comprehensive exception handling
- Swagger/OpenAPI documentation
- Role-based security (Admin/Member)
- Real-time analytics and reporting

 Technical Stack:
- Spring Boot 3.2.0
- Java 17
- MySQL 8.0
- JWT Security
- GraphQL
- Maven
- Lombok

 Project Statistics:
- 15+ packages
- 100+ Java files
- 5 main entities (User, Book, BorrowingRecord)
- 5 controllers with full CRUD
- 5 service interfaces with implementations
- Custom exception handling
- Native SQL queries for performance



using springboot and mysqllibrary-management-system/
├── src/
│   └── main/
│       ├── java/com/library/management/
│       │   ├── LibraryManagementSystemApplication.java 
│       │   │
│       │   ├── entity/                    # JPA Entities (Database Models)
│       │   │   ├── User.java             # User with ADMIN/MEMBER roles
│       │   │   ├── Book.java             # Book details + availability
│       │   │   ├── BorrowingRecord.java  # Borrowing transactions
│       │   │   └── BaseEntity.java       # Common fields (id, timestamps)
│       │   │
│       │   ├── enums/                     # Enumerations
│       │   │   ├── UserRole.java         # ADMIN, MEMBER
│       │   │   ├── BookStatus.java       # AVAILABLE, BORROWED, RESERVED
│       │   │   └── BorrowStatus.java     # BORROWED, RETURNED, OVERDUE
│       │   │
│       │   ├── repository/               # Data Access Layer
│       │   │   ├── UserRepository.java
│       │   │   ├── BookRepository.java
│       │   │   └── BorrowingRecordRepository.java
│       │   │
│       │   ├── dto/                      # Data Transfer Objects
│       │   │   ├── request/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── BookRequest.java
│       │   │   │   └── BorrowBookRequest.java
│       │   │   └── response/
│       │   │       ├── AuthResponse.java
│       │   │       ├── BookResponse.java
│       │   │       ├── UserResponse.java
│       │   │       └── BorrowingHistoryResponse.java
│       │   │
│       │   ├── service/                  # Business Logic Layer
│       │   │   ├── AuthService.java      # Authentication & JWT
│       │   │   ├── UserService.java      # User management
│       │   │   ├── BookService.java      # Book CRUD + availability
│       │   │   ├── BorrowingService.java # Borrow/return logic
│       │   │   └── ReportService.java    # Aggregation reports
│       │   │
│       │   ├── controller/               # REST API Controllers
│       │   │   ├── AuthController.java   # /api/auth/*
│       │   │   ├── BookController.java   # /api/books/*
│       │   │   ├── UserController.java   # /api/users/*
│       │   │   ├── BorrowingController.java # /api/borrowings/*
│       │   │   └── ReportController.java # /api/reports/*
│       │   │
│       │   ├── graphql/                  # GraphQL Implementation
│       │   │   ├── resolver/
│       │   │   │   ├── BookResolver.java
│       │   │   │   ├── UserResolver.java
│       │   │   │   └── BorrowingResolver.java
│       │   │   └── scalar/
│       │   │       └── DateTimeScalar.java
│       │   │
│       │   ├── security/                 # Security & JWT
│       │   │   ├── JwtAuthenticationEntryPoint.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── JwtTokenProvider.java
│       │   │   ├── UserPrincipal.java
│       │   │   └── SecurityConfig.java
│       │   │
│       │   ├── config/                   # Configuration Classes
│       │   │   ├── DatabaseConfig.java
│       │   │   └── GraphQLConfig.java
│       │   │
│       │   └── exception/                # Global Exception Handling
│       │       ├── GlobalExceptionHandler.java
│       │       ├── ResourceNotFoundException.java
│       │       ├── BadRequestException.java
│       │       └── UnauthorizedException.java
│       │
│       └── resources/
│           ├── application.properties 
│           ├── data.sql                  # Sample data
│           └── graphql/
│               └── schema.graphqls       # GraphQL schema
│
├── pom.xml 
└── README.md
