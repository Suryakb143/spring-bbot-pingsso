# Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Browser                            │
│                                                                   │
│  ┌──────────────────────────────────────────────────────┐       │
│  │         Angular Single Page Application              │       │
│  │  ┌────────────────────────────────────────────────┐  │       │
│  │  │  Components: Login, Dashboard                  │  │       │
│  │  │  Services: AuthService, HttpInterceptor        │  │       │
│  │  │  Guards: AuthGuard (route protection)          │  │       │
│  │  └────────────────────────────────────────────────┘  │       │
│  │  Local Storage: access_token, current_user          │       │
│  └──────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
                              │
                    HTTP Requests + Auth Header
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    PingSSO OAuth2 Provider                       │
│  ┌────────────────────────────────────────────────────┐        │
│  │  Authorization Endpoint (/as/authorization.oauth2) │        │
│  │  Token Endpoint (/as/token.oauth2)                 │        │
│  │  User Info Endpoint (/idp/userinfo.openid)        │        │
│  └────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
                              │
                    OAuth2 Authorization Code Flow
                              │
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot REST API                            │
│                                                                   │
│  ┌────────────────────────────────────────────────────┐         │
│  │              Controllers                            │         │
│  │  - AuthController (/api/auth/*)                   │         │
│  │  - UserController (/api/users/*)                  │         │
│  └────────────────────────────────────────────────────┘         │
│                           ↓                                       │
│  ┌────────────────────────────────────────────────────┐         │
│  │              Business Logic                        │         │
│  │  - AuthService: OAuth2 token exchange            │         │
│  │  - UserService: User management                  │         │
│  └────────────────────────────────────────────────────┘         │
│                           ↓                                       │
│  ┌────────────────────────────────────────────────────┐         │
│  │            Data Access Layer                       │         │
│  │  - UserRepository (JPA)                           │         │
│  │  - H2 Database (in-memory)                        │         │
│  └────────────────────────────────────────────────────┘         │
│                                                                   │
│  Security Filters: CSRF, CORS, Authentication                   │
│  Logging & Monitoring: Debug, Info, Error Levels               │
└─────────────────────────────────────────────────────────────────┘
```

## Component Interaction Diagram

```
Angular App                 Spring Boot API           PingSSO
    │                            │                      │
    ├─ User clicks Login ─────────────────────────────→ │
    │                            │  ┌─────────────────┐ │
    │                            │  │ AuthController  │ │
    │                            │  │ - Handle login  │ │
    │                            │  └─────────────────┘ │
    │                            │         │            │
    │                            │         └────────────┤
    │←──────── Redirect to Auth Endpoint ──────────────┤
    │                            │         (code flow) │
    │                            │                      │
    ├──────────── User Authenticates ────────────────→ │
    │                            │                      │
    │←─────── Redirect w/ Code ──────────────────────── │
    │                            │                      │
    ├─ Send Code to Backend ────→│                      │
    │                            │                      │
    │                            ├──── Exchange Code ──→│
    │                            │ (Token Request)      │
    │                            │                      │
    │                            │←──── Access Token ───┤
    │                            │                      │
    │←─── Store Token ───────────┤                      │
    │ (localStorage)             │                      │
    │                            │                      │
    ├──── Get User Data ────────→│                      │
    │ (w/ Auth Header)           ├──── Get User Info ──→│
    │                            │                      │
    │                            │←─── User Claims ─────┤
    │←── User + Token ──────────-┤                      │
    │ (Redirect to Dashboard)    │                      │
    │                            │                      │
    ├─ Request Protected API ───→│                      │
    │ (w/ Bearer Token)          ├─ Validate Token     │
    │                            ├─ Return Data        │
    │←─ Protected Resource ──────┤                      │
    │                            │                      │
```

## Authentication Flow Sequence

```
Sequence Diagram: OAuth2 Authorization Code Flow

participant User
participant Angular as "Angular App"
participant Spring as "Spring Boot API"
participant PingSSO as "PingSSO"

User->>Angular: Click "Login with PingSSO"
Angular->>Angular: Generate state + PKCE (optional)
Angular->>PingSSO: Redirect to authorization endpoint\n(client_id, redirect_uri, scope, state)
PingSSO->>User: Display login form
User->>PingSSO: Enter credentials
PingSSO->>User: Authenticate & Authorize
PingSSO->>Angular: Redirect to callback\n(code, state)
Angular->>Angular: Verify state parameter
Angular->>Spring: POST /api/auth/login\n(authorization code)
Spring->>PingSSO: POST /token (token request)\n(code, client_id, client_secret, redirect_uri)
PingSSO->>Spring: Return access_token + id_token
Spring->>PingSSO: GET /userinfo\n(Bearer: access_token)
PingSSO->>Spring: Return user claims
Spring->>Spring: Create/Update user in DB
Spring->>Spring: Save session/token
Spring->>Angular: Return user data + token
Angular->>Angular: Store token in localStorage
Angular->>Angular: Redirect to /dashboard
Angular->>User: Display dashboard
```

## Technology Stack

### Frontend (Angular 18)
- **Framework**: Angular 18
- **Language**: TypeScript 5.4
- **Styling**: SCSS
- **State Management**: RxJS Observables
- **HTTP**: Angular HttpClient
- **Routing**: Angular Router
- **Authentication**: OAuth2 OIDC via Browser APIs
- **Build Tool**: Angular CLI, Webpack

### Backend (Spring Boot 3.1)
- **Framework**: Spring Boot 3.1
- **Language**: Java 17
- **Security**: Spring Security 6
- **OAuth2**: Spring Security OAuth2 Client
- **Data Access**: Spring Data JPA, Hibernate
- **Database**: H2 (in-memory), easily swap to PostgreSQL/MySQL
- **Build Tool**: Maven 3.8
- **Server**: Embedded Tomcat

### Infrastructure
- **Containerization**: Docker, Docker Compose
- **Orchestration**: Kubernetes (optional)
- **Reverse Proxy**: Nginx
- **Web Server**: Tomcat (Spring Boot)
- **Frontend Server**: Nginx
- **Database**: H2 (Dev), PostgreSQL/MySQL (Prod)

## Security Architecture

### Authentication Flow
1. OAuth2 Authorization Code Flow (RFC 6749)
2. OpenID Connect (OIDC) for user information
3. Bearer Token (JWT) for API requests

### Security Layers

```
┌──────────────────────────────────────┐
│  Browser Security                    │
│  - SOP (Same Origin Policy)          │
│  - CSRF Tokens                       │
│  - Secure Headers                    │
└──────────────────────────────────────┘
         │
┌──────────────────────────────────────┐
│  Transport Security                  │
│  - HTTPS/TLS                         │
│  - Certificate Pinning               │
└──────────────────────────────────────┘
         │
┌──────────────────────────────────────┐
│  Application Security                │
│  - CORS                              │
│  - Authentication                    │
│  - Authorization                     │
│  - Input Validation                  │
│  - Output Encoding                   │
└──────────────────────────────────────┘
         │
┌──────────────────────────────────────┐
│  Data Security                       │
│  - Password Encryption               │
│  - Token Signing                     │
│  - Data Encryption at Rest           │
└──────────────────────────────────────┘
```

## Data Flow Architecture

```
Data Entities:

User (Entity)
├── id: Long
├── email: String (unique)
├── name: String
├── picture: String
├── pingSsoId: String
├── roles: Set<String>
├── active: Boolean
├── lastLoginTime: LocalDateTime
├── lastLoginIp: String
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime

AuthToken (DTO)
├── accessToken: String
├── refreshToken: String (optional)
└── expiresIn: Number

User Observable Streams (RxJS):
├── user$: Observable<User | null>
├── token$: Observable<AuthToken | null>
└── authenticated$: Observable<boolean>
```

## Deployment Architecture

### Development
```
localhost:4200 ──→ Angular Dev Server
              ──→ ng serve (live reload)

localhost:8080 ──→ Spring Boot App
               ──→ mvn spring-boot:run
               ──→ File System Storage
```

### Docker Compose
```
Docker Network (bridge)
├── pingsso-api (port 8080)
│   └── Spring Boot Container
│       └── H2 Database
└── pingsso-app (port 4200)
    └── Nginx Container
        └── Angular Build
```

### Kubernetes
```
Kubernetes Cluster
├── Namespace: pingsso
├── Service: pingsso-api (ClusterIP:8080)
│   └── Deployment: pingsso-api (replicas: 2)
├── Service: pingsso-app (LoadBalancer:80)
│   └── Deployment: pingsso-app (replicas: 2)
├── ConfigMap: pingsso-config
├── Secret: pingsso-secrets
└── PersistentVolume: database (optional)
```

### Production Cloud
```
Load Balancer
├── CDN: Static Assets (Angular)
│   └── S3 / Cloud Storage
├── API Gateway
│   └── Spring Boot Instances (auto-scaling)
│       ├── Database (RDS / Cloud SQL)
│       └── Cache (Redis / Memcached)
└── OAuth2 Provider: PingSSO
```

## Configuration Management

```
Environment Hierarchy:

1. Hardcoded Defaults (lowest priority)
   - Default values in code

2. Configuration Files
   - application.yml (Spring Boot)
   - environment.ts (Angular)
   - docker-compose.yml (Docker)

3. Environment Variables (highest priority)
   - PINGSSO_CLIENT_ID
   - PINGSSO_CLIENT_SECRET
   - DATABASE_URL
   - etc.

Principle: Environment variables override file configs
```

## Database Schema

```sql
-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    picture VARCHAR(255),
    pingsso_id VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- User Roles Table (element collection)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_pingsso_id ON users(pingsso_id);
CREATE INDEX idx_created_at ON users(created_at);
```

## API Endpoints Summary

### Authentication
- `POST /api/auth/login` - OAuth2 callback handler
- `POST /api/auth/callback` - Token exchange
- `POST /api/auth/logout` - Logout user

### Users
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users?email=...` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/health` - Health check

## Monitoring & Logging Architecture

```
Application Logs
    ├── Console (stdout/stderr)
    ├── File System (rotating logs)
    └── Log Aggregator (ELK Stack / CloudWatch)
         ├── Elasticsearch: Log storage
         └── Kibana: Log visualization

Metrics
    ├── Application Metrics
    │   ├── Request count/duration
    │   ├── Error rates
    │   └── Active users
    ├── System Metrics
    │   ├── CPU usage
    │   ├── Memory usage
    │   └── Disk I/O
    └── Business Metrics
        ├── Login success/failure
        ├── User registrations
        └── API response times

Monitoring Tools:
    ├── Prometheus: Metrics collection
    ├── Grafana: Visualization
    ├── ELK Stack: Log management
    └── Alerts: PagerDuty, Slack
```

## Performance Considerations

### Frontend
- Lazy loading modules
- OnPush change detection
- Tree-shaking optimization
- Code splitting
- Caching strategies

### Backend
- Database indexing
- Connection pooling
- Query optimization
- Caching (Redis)
- Load balancing

### Infrastructure
- CDN for static assets
- Horizontal scaling
- Auto-scaling groups
- Request compression (gzip)
- Connection keep-alive
