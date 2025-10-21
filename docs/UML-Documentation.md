# Spring Boot OAuth2 JWT Application - UML Documentation

This document contains comprehensive UML diagrams for the Spring Boot OAuth2 JWT authentication application.

## 1. Class Diagram

The class diagram shows the structure of all classes and their relationships in the application:

```mermaid
---
title: Spring Boot OAuth2 JWT Application - Class Diagram
---
classDiagram
    %% Main Application Class
    class DemoOauth2HeadersApplication {
        <<SpringBootApplication>>
        +main(String[] args)$ void
    }

    %% Controllers
    class AuthController {
        <<RestController>>
        -JwtService jwtService
        -UserDetailsService userDetailsService
        -PasswordEncoder passwordEncoder
        +AuthController(JwtService, UserDetailsService, PasswordEncoder)
        +login(LoginRequest, HttpServletResponse) ResponseEntity~AuthResponse~
        +refresh(RefreshRequest, HttpServletResponse) ResponseEntity~AuthResponse~
    }

    class ApiController {
        <<RestController>>
        +hello(Authentication) String
    }

    %% Services
    class JwtService {
        <<Service>>
        -SecretKey key
        -int accessTokenMinutes
        -int refreshTokenDays
        +JwtService(String secret, int accessMins, int refreshDays)
        +generateAccessToken(String username) String
        +generateRefreshToken(String username) String
        +validateToken(String token) Claims
        +getUsernameFromToken(String token) String
        +isTokenValid(String token) boolean
        +isAccessToken(String token) boolean
        +isRefreshToken(String token) boolean
    }

    %% Configuration
    class SecurityConfig {
        <<Configuration>>
        -JwtAuthenticationFilter jwtAuthenticationFilter
        +SecurityConfig(JwtAuthenticationFilter)
        +filterChain(HttpSecurity) SecurityFilterChain
        +corsConfigurationSource() CorsConfigurationSource
        +userDetailsService() UserDetailsService
        +passwordEncoder() PasswordEncoder
    }

    %% Filters
    class JwtAuthenticationFilter {
        <<Component>>
        -JwtService jwtService
        +JwtAuthenticationFilter(JwtService)
        +doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void
    }

    %% DTOs
    class LoginRequest {
        -String username
        -String password
        +getUsername() String
        +setUsername(String) void
        +getPassword() String
        +setPassword(String) void
    }

    class RefreshRequest {
        -String refreshToken
        +getRefreshToken() String
        +setRefreshToken(String) void
    }

    class AuthResponse {
        -String message
        +getMessage() String
        +setMessage(String) void
    }

    %% Spring Framework Classes (External)
    class UserDetailsService {
        <<interface>>
        +loadUserByUsername(String) UserDetails
    }

    class PasswordEncoder {
        <<interface>>
        +encode(CharSequence) String
        +matches(CharSequence, String) boolean
    }

    class OncePerRequestFilter {
        <<abstract>>
        +doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void*
    }

    class HttpSecurity {
        <<SpringSecurity>>
    }

    class Authentication {
        <<interface>>
        +getName() String
        +getPrincipal() Object
    }

    %% Relationships
    AuthController --> JwtService : uses
    AuthController --> UserDetailsService : uses
    AuthController --> PasswordEncoder : uses
    AuthController --> LoginRequest : receives
    AuthController --> RefreshRequest : receives
    AuthController --> AuthResponse : returns

    ApiController --> Authentication : uses

    SecurityConfig --> JwtAuthenticationFilter : configures
    SecurityConfig --> UserDetailsService : creates
    SecurityConfig --> PasswordEncoder : creates
    SecurityConfig --> HttpSecurity : configures

    JwtAuthenticationFilter --|> OncePerRequestFilter : extends
    JwtAuthenticationFilter --> JwtService : uses

    %% Package annotations
    note for AuthController "Located in: controller package\nHandles authentication endpoints\n/auth/login and /auth/refresh"
    note for ApiController "Located in: controller package\nHandles protected API endpoints\n/api/hello"
    note for JwtService "Located in: service package\nManages JWT token operations"
    note for SecurityConfig "Located in: config package\nConfigures Spring Security"
    note for JwtAuthenticationFilter "Located in: filter package\nProcesses JWT tokens from requests"

    %% Styling
    classDef controller fill:#e1f5fe,stroke:#0277bd,stroke-width:2px
    classDef service fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef config fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef dto fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef filter fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    classDef spring fill:#f5f5f5,stroke:#616161,stroke-width:1px,stroke-dasharray: 5 5

    class AuthController:::controller
    class ApiController:::controller
    class JwtService:::service
    class SecurityConfig:::config
    class LoginRequest:::dto
    class RefreshRequest:::dto
    class AuthResponse:::dto
    class JwtAuthenticationFilter:::filter
    class UserDetailsService:::spring
    class PasswordEncoder:::spring
    class OncePerRequestFilter:::spring
    class HttpSecurity:::spring
    class Authentication:::spring
```

## 2. Sequence Diagram

The sequence diagram illustrates the authentication flow and interaction between components:

```mermaid
---
title: OAuth2 JWT Authentication Flow - Sequence Diagram
---
sequenceDiagram
    participant Client
    participant AuthController
    participant JwtService
    participant UserDetailsService
    participant PasswordEncoder
    participant JwtFilter
    participant ApiController

    %% Login Flow
    rect rgb(230, 245, 255)
    note over Client, PasswordEncoder: Authentication Phase
    Client->>+AuthController: POST /auth/login<br/>{username, password}
    AuthController->>+UserDetailsService: loadUserByUsername(username)
    UserDetailsService-->>-AuthController: UserDetails
    AuthController->>+PasswordEncoder: matches(password, hashedPassword)
    PasswordEncoder-->>-AuthController: boolean (true/false)
    
    alt Invalid credentials
        AuthController-->>Client: 401 Unauthorized<br/>{"message": "Invalid credentials"}
    else Valid credentials
        AuthController->>+JwtService: generateAccessToken(username)
        JwtService-->>-AuthController: accessToken
        AuthController->>+JwtService: generateRefreshToken(username)
        JwtService-->>-AuthController: refreshToken
        AuthController-->>-Client: 200 OK<br/>Headers: X-Access-Token, X-Refresh-Token<br/>{"message": "login ok"}
    end
    end

    %% Protected Resource Access
    rect rgb(245, 255, 230)
    note over Client, ApiController: Protected Resource Access
    Client->>+JwtFilter: GET /api/hello<br/>Authorization: Bearer {accessToken}
    JwtFilter->>+JwtService: isTokenValid(token)
    JwtService-->>-JwtFilter: boolean
    JwtFilter->>+JwtService: isAccessToken(token)
    JwtService-->>-JwtFilter: boolean
    
    alt Invalid or expired token
        JwtFilter-->>Client: 403 Forbidden
    else Valid token
        JwtFilter->>+JwtService: getUsernameFromToken(token)
        JwtService-->>-JwtFilter: username
        JwtFilter->>JwtFilter: Set SecurityContext
        JwtFilter->>+ApiController: Forward request with Authentication
        ApiController-->>-Client: 200 OK<br/>"Hello, {username}!"
    end
    end

    %% Token Refresh Flow
    rect rgb(255, 245, 230)
    note over Client, JwtService: Token Refresh Phase
    Client->>+AuthController: POST /auth/refresh<br/>{refreshToken}
    AuthController->>+JwtService: isTokenValid(refreshToken)
    JwtService-->>-AuthController: boolean
    AuthController->>+JwtService: isRefreshToken(refreshToken)
    JwtService-->>-AuthController: boolean
    
    alt Invalid refresh token
        AuthController-->>Client: 401 Unauthorized<br/>{"message": "Invalid refresh token"}
    else Valid refresh token
        AuthController->>+JwtService: getUsernameFromToken(refreshToken)
        JwtService-->>-AuthController: username
        AuthController->>+JwtService: generateAccessToken(username)
        JwtService-->>-AuthController: newAccessToken
        AuthController-->>-Client: 200 OK<br/>Header: X-Access-Token<br/>{"message": "refreshed"}
    end
    end

    %% Error Scenarios
    rect rgb(255, 230, 230)
    note over Client, AuthController: Error Scenarios
    Client->>+JwtFilter: GET /api/hello<br/>(No Authorization Header)
    JwtFilter-->>-Client: 403 Forbidden
    
    Client->>+AuthController: POST /auth/login<br/>{invalid JSON}
    AuthController-->>-Client: 400 Bad Request<br/>Validation Error
    end
```

## 3. Package Structure Diagram

The package diagram shows the organization of classes within the application packages:

```mermaid
---
title: Spring Boot OAuth2 Project - Package Structure
---
flowchart TD
    subgraph "com.example.demooauth2headers"
        
        subgraph "Main Application"
            Main["🚀 DemoOauth2HeadersApplication<br/>@SpringBootApplication"]
        end
        
        subgraph "controller"
            AuthCtrl["🔐 AuthController<br/>@RestController<br/>/auth/**"]
            ApiCtrl["🛡️ ApiController<br/>@RestController<br/>/api/**"]
        end
        
        subgraph "service"
            JwtSrv["🎫 JwtService<br/>@Service<br/>JWT Operations"]
        end
        
        subgraph "config"
            SecConfig["🔧 SecurityConfig<br/>@Configuration<br/>@EnableWebSecurity"]
        end
        
        subgraph "filter"
            JwtFilter["🔍 JwtAuthenticationFilter<br/>@Component<br/>extends OncePerRequestFilter"]
        end
        
        subgraph "dto"
            LoginReq["📝 LoginRequest<br/>@Valid"]
            RefreshReq["📝 RefreshRequest<br/>@Valid"]
            AuthResp["📄 AuthResponse"]
        end
    end
    
    subgraph "Spring Framework Dependencies"
        SpringSec["🛡️ Spring Security"]
        SpringWeb["🌐 Spring Web"]
        SpringBoot["⚡ Spring Boot"]
        JWT["🎫 JJWT Library"]
    end
    
    subgraph "External Resources"
        AppYml["📋 application.yml<br/>JWT Configuration"]
        PomXml["📦 pom.xml<br/>Dependencies"]
        Tests["🧪 Tests<br/>Integration Tests"]
    end
    
    %% Relationships
    Main --> AuthCtrl
    Main --> ApiCtrl
    Main --> JwtSrv
    Main --> SecConfig
    Main --> JwtFilter
    
    AuthCtrl --> JwtSrv
    AuthCtrl --> LoginReq
    AuthCtrl --> RefreshReq
    AuthCtrl --> AuthResp
    
    JwtFilter --> JwtSrv
    SecConfig --> JwtFilter
    
    %% Dependencies
    Main -.-> SpringBoot
    AuthCtrl -.-> SpringWeb
    SecConfig -.-> SpringSec
    JwtSrv -.-> JWT
    
    Main -.-> AppYml
    Main -.-> PomXml
    Tests -.-> Main
    
    %% Styling
    classDef main fill:#FFE082,stroke:#F57F17,stroke-width:3px
    classDef controller fill:#E1F5FE,stroke:#0277BD,stroke-width:2px
    classDef service fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
    classDef config fill:#FFF3E0,stroke:#EF6C00,stroke-width:2px
    classDef filter fill:#FCE4EC,stroke:#C2185B,stroke-width:2px
    classDef dto fill:#E8F5E8,stroke:#388E3C,stroke-width:2px
    classDef spring fill:#F5F5F5,stroke:#616161,stroke-width:1px,stroke-dasharray: 5 5
    classDef resource fill:#FFF8E1,stroke:#F9A825,stroke-width:2px
    
    class Main main
    class AuthCtrl,ApiCtrl controller
    class JwtSrv service
    class SecConfig config
    class JwtFilter filter
    class LoginReq,RefreshReq,AuthResp dto
    class SpringSec,SpringWeb,SpringBoot,JWT spring
    class AppYml,PomXml,Tests resource
```

## 4. Component Descriptions

### Controllers
- **AuthController**: Handles authentication endpoints (`/auth/login`, `/auth/refresh`)
- **ApiController**: Manages protected API endpoints (`/api/hello`)

### Services
- **JwtService**: Core JWT operations (generation, validation, parsing)

### Configuration
- **SecurityConfig**: Spring Security configuration with CORS and stateless session management

### Filters
- **JwtAuthenticationFilter**: Processes JWT tokens from incoming requests

### DTOs
- **LoginRequest**: Request payload for login endpoint
- **RefreshRequest**: Request payload for refresh endpoint  
- **AuthResponse**: Standard response format for auth operations

### Key Features Illustrated

1. **Dependency Injection**: Spring's IoC container manages all component relationships
2. **Security Filter Chain**: JWT filter intercepts requests before reaching controllers
3. **Stateless Authentication**: No server-side session storage
4. **Token-based Authorization**: Access and refresh token pattern
5. **CORS Support**: Cross-origin resource sharing enabled
6. **Validation**: Input validation using Bean Validation annotations

### Security Flow Summary

1. **Authentication**: Client sends credentials → generates JWT tokens
2. **Authorization**: Client includes JWT in header → validates and processes request  
3. **Token Refresh**: Client sends refresh token → generates new access token
4. **Error Handling**: Invalid requests return appropriate HTTP status codes

This UML documentation provides a complete architectural overview of the Spring Boot OAuth2 JWT application.