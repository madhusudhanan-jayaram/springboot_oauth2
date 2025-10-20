# Demo OAuth2 Headers# Spring Boot OAuth2 Resource Server (Simple Demo)



A minimal Spring Boot 3.3 application demonstrating JWT-based authentication with custom headers.This is a minimal Spring Boot app configured as an OAuth2 Resource Server that accepts HS256-signed JWTs.



## FeaturesEndpoints:

- GET /api/public - public endpoint

- **Spring Boot 3.3** with Java 17- GET /api/private - secured, requires a valid JWT in Authorization: Bearer <token>

- **JWT Authentication** using JJWT library

- **Stateless Security** configurationHow to build and run (Windows PowerShell):

- **CORS Support** with exposed custom headers

- **In-memory User Store** (BCrypt encrypted)1. Build

- **Custom Headers**: `X-Access-Token`, `X-Refresh-Token`

   mvn -q -DskipTests package

## Project Structure

2. Run

```

src/   mvn -q spring-boot:run

├── main/

│   ├── java/com/example/demooauth2headers/3. Generate a test token from the project (in Java) or use the JwtTokenUtil class manually.

│   │   ├── DemoOauth2HeadersApplication.java      # Main Spring Boot application

│   │   ├── config/Example usage (PowerShell):

│   │   │   └── SecurityConfig.java                # Security & CORS configuration

│   │   ├── controller/# Generate a token using the helper Java class

│   │   │   ├── AuthController.java                # Authentication endpoints# You can also write a small main or use a unit test to call JwtTokenUtil.createToken("user1", 3600)

│   │   │   └── ApiController.java                 # Protected API endpoints

│   │   ├── dto/# Call secured endpoint

│   │   │   ├── AuthResponse.java                  # Response DTOscurl -H "Authorization: Bearer <token>" http://localhost:8080/api/private

│   │   │   ├── LoginRequest.java                  # Login request DTO

│   │   │   └── RefreshRequest.java                # Refresh request DTONotes:

│   │   ├── filter/- This is intended for local testing only. The secret is hard-coded in the sample; do not use in production.

│   │   │   └── JwtAuthenticationFilter.java       # JWT authentication filter
│   │   └── service/
│   │       └── JwtService.java                    # JWT utilities
│   └── resources/
│       └── application.yml                        # Application configuration
└── test/
    └── java/com/example/demooauth2headers/
        └── DemoOauth2HeadersApplicationTests.java # Integration tests
```

## Configuration

The application is configured in `application.yml`:

```yaml
server:
  port: 8080

app:
  jwt:
    secret: mySecretKey123456789012345678901234567890
    access-mins: 15    # Access token validity in minutes
    refresh-days: 7    # Refresh token validity in days
```

## Default Credentials

- **Username**: `user`
- **Password**: `password`

## API Endpoints

### Authentication Endpoints (Public)

#### POST /auth/login
Authenticate user and receive JWT tokens.

**Request:**
```json
{
  "username": "user",
  "password": "password"
}
```

**Response Headers:**
- `X-Access-Token`: JWT access token (15 minutes validity)
- `X-Refresh-Token`: JWT refresh token (7 days validity)

**Response Body:**
```json
{
  "message": "login ok"
}
```

#### POST /auth/refresh
Refresh access token using valid refresh token.

**Request:**
```json
{
  "refreshToken": "your-refresh-token-here"
}
```

**Response Headers:**
- `X-Access-Token`: New JWT access token

**Response Body:**
```json
{
  "message": "refreshed"
}
```

### Protected Endpoints

#### GET /api/hello
Returns personalized greeting for authenticated users.

**Headers:**
```
Authorization: Bearer your-access-token-here
```

**Response:**
```
Hello, user!
```

## Usage Examples

### 1. Login and Get Tokens

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}' \
  -v
```

The response headers will contain `X-Access-Token` and `X-Refresh-Token`.

### 2. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/hello \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### 3. Refresh Access Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}' \
  -v
```

## Building and Running

### Prerequisites
- Java 17+
- Maven 3.6+

### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Security Features

- **Stateless Authentication**: No server-side sessions
- **CORS Enabled**: Cross-origin requests supported
- **JWT Validation**: Access tokens validated on each request
- **Token Types**: Separate access and refresh tokens
- **BCrypt Encryption**: Passwords stored securely
- **Custom Headers**: Tokens delivered via `X-Access-Token` and `X-Refresh-Token` headers

## Testing

The project includes comprehensive integration tests covering:
- Application context loading
- Successful login with token generation
- Protected endpoint access with valid token
- Unauthenticated request rejection

Run tests with:
```bash
mvn test
```

## JWT Token Structure

### Access Token Claims:
```json
{
  "sub": "user",
  "type": "access",
  "iat": 1634567890,
  "exp": 1634568790
}
```

### Refresh Token Claims:
```json
{
  "sub": "user", 
  "type": "refresh",
  "iat": 1634567890,
  "exp": 1635172690
}
```

## Error Responses

- **401 Unauthorized**: Invalid credentials or expired/invalid tokens
- **403 Forbidden**: Unauthenticated access to protected endpoints
- **400 Bad Request**: Invalid request format or missing required fields

## Dependencies

Key dependencies used in this project:

- `spring-boot-starter-web`: Web framework
- `spring-boot-starter-security`: Security framework
- `spring-boot-starter-validation`: Bean validation
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`: JWT implementation

## License

This is a demo project for educational purposes.