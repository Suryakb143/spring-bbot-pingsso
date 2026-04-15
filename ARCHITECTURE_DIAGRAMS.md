# PingSSO Complete Architecture & Flow Documentation

## Overview

This document provides a comprehensive visual guide to the entire PingSSO Spring Boot application architecture, including all files, flows, components, and interactions.

---

## 📊 Diagram Summary

### 1. **Component Architecture Diagram**
**Shows:** All application layers and how components interact
- **Client Layer:** Angular Frontend + Browser
- **API Layer:** REST Controllers
- **Security Layer:** Filters, Token Providers, CORS
- **Service Layer:** Business logic (UserService, SessionService)
- **Entity Layer:** Data models (User, Session)
- **Repository Layer:** JPA Data Access
- **Database Layer:** H2 In-Memory Database
- **Configuration:** application.yml, Security, Swagger configs
- **External:** PingSSO OAuth2 Provider
- **Testing:** Swagger UI, REST Client, Postman, Documentation

### 2. **Login Flow Sequence Diagram**
**Shows:** Step-by-step process of user login
1. Angular app sends login request with user credentials
2. CORS filter validates cross-origin request
3. AuthController receives request
4. UserService creates or updates user in database
5. UserService records login timestamp and IP
6. SessionService creates new session
7. BearerTokenProvider generates JWT token
8. Session cookie is set (HttpOnly, Secure, SameSite=Strict)
9. Response returned with user data, token, and session ID
10. Angular stores bearerToken and sessionId for future requests

**Key Points:**
- Dual authentication: JWT Bearer token + Session cookie
- User data persisted in H2 database
- Token includes userId and roles claims
- Session stored separately for multi-device tracking

### 3. **Authenticated Request Flow Diagram**
**Shows:** How subsequent API calls are authenticated
1. Angular retrieves stored bearerToken
2. Sends request with `Authorization: Bearer {JWT}`
3. BearerTokenFilter intercepts request
   - Validates JWT signature
   - Checks token expiration
   - Extracts userId and email
   - Sets request attributes
4. SessionFilter extracts sessionId from PINGSSO_SESSION cookie
5. AuthController checks that userId is not null
6. If null, returns 401 Unauthorized
7. If valid, proceeds with business logic
8. Queries database for session information
9. Returns response with session data

**Key Points:**
- Multiple security layers ensure proper authentication
- Request attributes passed through filter chain
- Database queries executed within authenticated context
- 401 errors for missing/invalid authentication

### 4. **File Structure & Dependencies Diagram**
**Shows:** Complete project structure with all files and relationships

#### Spring Boot Application (`springboot-pingsso-app`)
```
src/main/
├── java/com/pingsso/app/
│   ├── Application.java (Entry point)
│   ├── config/
│   │   ├── SwaggerConfig.java (OpenAPI 3.0 configuration)
│   │   ├── SecurityConfig.java (Filter chain, CORS)
│   │   ├── CorsConfig.java (Cross-origin settings)
│   │   └── DataInitializer.java (Sample data)
│   ├── controller/
│   │   ├── AuthController.java (9 API endpoints)
│   │   └── UserController.java (User endpoints)
│   ├── entity/
│   │   ├── User.java (@Entity for users table)
│   │   └── Session.java (@Entity for sessions table)
│   ├── repository/
│   │   ├── UserRepository.java (JPA data access)
│   │   └── SessionRepository.java (JPA data access)
│   ├── service/
│   │   ├── UserService.java (User business logic)
│   │   ├── SessionService.java (Session management)
│   │   └── CustomUserDetailsService.java
│   └── security/
│       ├── BearerTokenFilter.java (JWT extraction & validation)
│       ├── BearerTokenProvider.java (Token generation)
│       └── SessionFilter.java (Cookie extraction)
│
└── resources/
    └── application.yml (Configuration)

pom.xml - Maven dependencies
rest-client.http - VS Code REST testing
PingSSO-API.postman_collection.json - Postman collection
API_TESTING_GUIDE.md - Testing documentation
Dockerfile - Container image definition
```

#### Angular Frontend (`angular-pingsso-app`)
```
src/
├── app/
│   ├── app.component.ts
│   ├── app-routing.module.ts
│   ├── core/
│   │   ├── guards/auth.guard.ts
│   │   ├── interceptors/auth.interceptor.ts
│   │   └── services/auth.service.ts
│   ├── features/
│   │   ├── login/
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.scss
│   │   └── dashboard/
│   │       ├── dashboard.component.ts
│   │       ├── dashboard.component.html
│   │       └── dashboard.component.scss
│   ├── shared/components/
│   └── app.module.ts
├── environments/
│   ├── environment.ts (dev)
│   └── environment.prod.ts (production)
└── styles.scss
```

### 5. **API Endpoints & Security Diagram**
**Shows:** All 9 API endpoints with security requirements
#### Public Endpoints (No auth needed)
- `POST /api/auth/login` - Initial user login
- `POST /api/auth/callback` - OAuth callback handler
- `GET /api/auth/user-info` - Get user by email

#### Protected Endpoints (Bearer token required)
- `GET /api/auth/sessions` - List active sessions
- `GET /api/auth/session-info` - Get current session details
- `POST /api/auth/token` - Generate new token
- `POST /api/auth/logout` - Logout single session
- `POST /api/auth/logout-all` - Logout all sessions
- `POST /api/auth/sessions/{sessionId}/revoke` - Revoke specific session

#### Security Filters Applied
1. CORS Filter - Validates origin (localhost:4200)
2. SessionFilter - Extracts sessionId from cookie
3. BearerTokenFilter - Validates JWT token
4. Spring Security Filter - Additional checks

#### Response Types
- **200 OK** - Successful request
- **400 Bad Request** - Invalid input
- **401 Unauthorized** - Missing/invalid authentication
- **403 Forbidden** - Access denied
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

### 6. **Application Startup & Initialization Diagram**
**Shows:** Step-by-step startup process
1. **Load Properties** - Read application.yml
2. **Spring Boot Init** - Start Tomcat on port 8080
3. **Auto Configure** - Set up beans and dependencies
4. **Config Classes** - Load custom configurations
   - SwaggerConfig → Initialize OpenAPI/Swagger
   - SecurityConfig → Set up filter chain
   - CorsConfig → Configure CORS rules
   - DataInitializer → Create sample data
5. **Register Repositories** - Enable JPA repositories
6. **Register Services** - Initialize service beans
7. **Register Controllers** - Map REST endpoints
8. **Initialize Filters** - Set up security filter chain
9. **Database Ready** - H2 database initialized with tables
10. **Routes Ready** - API endpoints and Swagger UI accessible

---

## 🔄 Complete Request-Response Cycle

### Login Cycle
```
1. User enters credentials in Angular login form
2. POST /api/auth/login request sent
3. CORS validation ✓
4. User created/updated in database
5. Session created and stored
6. JWT token generated
7. Session cookie set (HttpOnly)
8. Response with token + sessionId returned
9. Token stored in Angular localStorage
10. SessionId stored in automatic cookie
11. User redirected to dashboard
```

### Authenticated Request Cycle
```
1. User clicks action requiring authentication
2. Angular retrieves token from localStorage
3. Adds Authorization header with Bearer token
4. Request sent to API
5. BearerTokenFilter validates JWT
   - Signature checked
   - Expiration checked
   - Claims extracted (userId)
6. SessionFilter extracts session cookie
7. Controller checks userId exists
8. Business logic executed
9. Database queries performed
10. Response returned
11. Angular processes response
12. UI updated with data
```

---

## 📦 Key Dependencies

### Spring Boot Stack
- **spring-boot-starter-web** - REST API support
- **spring-boot-starter-data-jpa** - Database ORM
- **spring-boot-starter-security** - Security framework
- **spring-security-oauth2-client** - OAuth2 support
- **spring-security-oauth2-resource-server** - Token validation

### JWT & Tokens
- **jjwt-api (0.13.0)** - JWT creation and parsing
- **jjwt-impl (0.13.0)** - JWT implementation
- **jjwt-jackson (0.13.0)** - JSON serialization

### API Documentation
- **springdoc-openapi-starter-webmvc-ui (2.0.4)** - Swagger UI and OpenAPI

### Database
- **h2** - In-memory relational database

### Utilities
- **lombok** - Reduce boilerplate (getters, setters, etc.)

---

## 🔐 Security Features

### Token Security
- JWT signed with HS512 algorithm
- 24-hour token expiration
- Claims include userId and roles
- Token validation on every request

### Cookie Security
- HTTPOnly flag prevents JavaScript access
- Secure flag - only sent over HTTPS
- SameSite=Strict prevents CSRF attacks
- Automatic path /

### Request Security
- CORS restricted to localhost:4200
- CORS credentials allowed
- All requests filtered through security chain
- Multiple validation layers

### Data Security
- Passwords not stored (OAuth provider handles)
- SQL injection prevented via JPA
- CSRF protection via Spring Security
- Input validation on all endpoints

---

## 🗄️ Database Schema

### `users` Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    picture TEXT,
    pingsso_id VARCHAR(255),
    active BOOLEAN DEFAULT true,
    last_login_ip VARCHAR(45),
    last_login_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### `sessions` Table
```sql
CREATE TABLE sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    email VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_name VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    active BOOLEAN DEFAULT true,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🧪 Testing Resources

### Swagger UI
- **URL:** http://localhost:8080/swagger-ui.html
- **Type:** Interactive API documentation
- **Features:** Try endpoints, view schemas, Bearer token support

### REST Client
- **File:** rest-client.http
- **Tool:** Install "REST Client" VS Code extension
- **Features:** VS Code integrated testing, variables support

### Postman Collection
- **File:** PingSSO-API.postman_collection.json
- **Type:** Complete API collection
- **Features:** Environment variables, test automation, sharing

### Documentation
- **File:** API_TESTING_GUIDE.md
- **Content:** Complete testing instructions with examples

---

## 🚀 Running the Application

### Start Spring Boot
```bash
cd springboot-pingsso-app
mvn spring-boot:run
```

Server runs on: **http://localhost:8080**

### Start Angular Frontend
```bash
cd angular-pingsso-app
npm install
ng serve
```

Frontend runs on: **http://localhost:4200**

### Using Docker Compose
```bash
docker-compose up
```

Both services start automatically.

---

## 📝 File Relationships

```
application.yml
├── Configures Spring Boot
├── Sets database connection
├── Defines JWT secret
├── Sets session timeout
└── Enables Swagger

SecurityConfig.java
├── Creates SessionFilter
├── Creates BearerTokenFilter
├── Sets filter chain order
└── Configures CORS

AuthController.java
├── Uses UserService
├── Uses SessionService
├── Uses BearerTokenProvider
└── Exposes 9 REST endpoints
    ├── Calls UserService methods
    ├── Calls SessionService methods
    ├── Calls BearerTokenProvider methods
    └── Returns response objects

UserService.java
├── Uses UserRepository
├── Creates/updates User entities
└── Manages user data

SessionService.java
├── Uses SessionRepository
├── Creates Session entities
└── Manages session lifecycle

UserRepository.java & SessionRepository.java
├── Extend JpaRepository
├── Query database tables
└── Return entity objects

BearerTokenProvider.java
├── Generates JWT tokens
├── Signs with secret key
└── Encodes userId claims

SecurityFilters
├── BearerTokenFilter
│   ├── Validates JWT
│   └── Sets userId attribute
└── SessionFilter
    ├── Extracts sessionId
    └── Sets sessionId attribute
```

---

## 🎯 Key Concepts

### Dual Authentication
- **Bearer Token (JWT):** Used for stateless API authentication
- **Session Cookie:** Used for browser-based sessions and CSRF protection

### Filter Chain Order
1. CORS validation
2. SessionFilter (extract cookie)
3. BearerTokenFilter (validate JWT)
4. Spring Security filters
5. Application servlet

### Request Attributes
- `userId` - Set by BearerTokenFilter from JWT
- `userEmail` - Set by BearerTokenFilter from JWT
- `sessionId` - Set by SessionFilter from cookie

### Response Pattern
All responses follow a consistent structure:
```json
{
  "success": true/false,
  "message": "Optional message",
  "data": {...}
}
```

---

## 📌 Summary Table

| Component | Purpose | Technology |
|-----------|---------|-----------|
| AuthController | REST API endpoints | Spring Web |
| UserService | User management | Spring Service |
| SessionService | Session management | Spring Service |
| BearerTokenProvider | JWT token generation | JJWT |
| User Entity | User data model | JPA |
| Session Entity | Session data model | JPA |
| H2 Database | Data persistence | In-Memory DB |
| Swagger | API documentation | springdoc-openapi |
| Angular | Frontend UI | TypeScript/Angular |
| Docker | Containerization | Docker/Compose |

---

## 🔗 Related Files

- [API Testing Guide](API_TESTING_GUIDE.md)
- [REST Client](rest-client.http)
- [Postman Collection](PingSSO-API.postman_collection.json)
- [Configuration](CONFIG.md)
- [README](README.md)
