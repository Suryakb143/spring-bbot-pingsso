# Spring Boot PingSSO Application

A Spring Boot REST API application with PingSSO OAuth2 authentication integration and sample test data.

## Features

- **Spring Boot 3.1**: Modern Spring framework
- **OAuth2 Integration**: PingSSO authentication support
- **JPA/Hibernate**: ORM for database operations
- **H2 Database**: In-memory database with sample test data
- **REST APIs**: User management and authentication endpoints
- **Security**: Spring Security with CORS support
- **Sample Data**: Pre-loaded test users with various roles

## Project Structure

```
src/
├── main/
│   ├── java/com/pingsso/app/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   ├── entity/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   └── Application.java
│   └── resources/
│       └── application.yml
└── test/
```

## Prerequisites

- Java 17 or later
- Maven 3.6+

## Installation & Setup

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Database Console

Navigate to `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Authentication

- **POST** `/api/auth/login` - User login
  ```json
  {
    "email": "john.doe@example.com",
    "name": "John Doe",
    "picture": "url-to-picture",
    "pingSsoId": "pingsso-001"
  }
  ```

- **POST** `/api/auth/logout` - User logout

- **GET** `/api/auth/user-info?email=john.doe@example.com` - Get user information

### User Management

- **GET** `/api/users` - Get all users
- **GET** `/api/users/{id}` - Get specific user
- **PUT** `/api/users/{id}` - Update user
  ```json
  {
    "name": "Updated Name",
    "picture": "new-picture-url"
  }
  ```
- **DELETE** `/api/users/{id}` - Delete user

- **GET** `/api/users/health` - API health check

## Sample Test Data

The application initializes with 4 sample users:

| Email | Name | Role | PingSSO ID |
|-------|------|------|-----------|
| john.doe@example.com | John Doe | ADMIN, USER | pingsso-001 |
| jane.smith@example.com | Jane Smith | USER | pingsso-002 |
| bob.wilson@example.com | Bob Wilson | MANAGER, USER | pingsso-003 |
| alice.johnson@example.com | Alice Johnson | USER | pingsso-004 |

## PingSSO Configuration

Update PingSSO settings in `src/main/resources/application.yml`:

```yaml
pingsso:
  client-id: your-client-id
  client-secret: your-client-secret
  discovery-url: https://your-pingsso-server/.well-known/openid-configuration
  authorization-uri: https://your-pingsso-server/as/authorization.oauth2
  token-uri: https://your-pingsso-server/as/token.oauth2
  user-info-uri: https://your-pingsso-server/idp/userinfo.openid
```

## Running Tests

```bash
mvn test
```

## Database Schema

### Users Table

| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT | PRIMARY KEY |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| name | VARCHAR(255) | NOT NULL |
| picture | VARCHAR(255) | |
| active | BOOLEAN | NOT NULL |
| pingsso_id | VARCHAR(255) | |
| last_login_ip | VARCHAR(255) | |
| last_login_time | TIMESTAMP | |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

### User Roles Table

| Column | Type | Constraint |
|--------|------|-----------|
| user_id | BIGINT | FOREIGN KEY |
| role | VARCHAR(50) | |

## Technologies Used

- Spring Boot 3.1
- Spring Security
- Spring Data JPA
- H2 Database
- Lombok
- JWT (JJWT)
- Maven

## License

This project is licensed under the MIT License.

## Contributing

Feel free to enhance and contribute to this project.
