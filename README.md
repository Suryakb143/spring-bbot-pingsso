# PingSSO Workspace

A complete monorepo containing separate Angular and Spring Boot applications configured for PingSSO OAuth2 authentication.

## Project Structure

```
pingsso/
├── angular-pingsso-app/       # Angular frontend application
├── springboot-pingsso-app/    # Spring Boot backend API
├── docker-compose.yml          # Docker orchestration
└── docs/                        # Documentation
```

## Quick Start

### Prerequisites

- **Node.js** 18+ (for Angular development)
- **Java** 17+ (for Spring Boot)
- **Maven** 3.8+ (for building Spring Boot)
- **Docker & Docker Compose** (optional, for containerized deployment)
- **PingSSO Instance** or OAuth2-compatible identity provider

### 1. Angular Application

```bash
cd angular-pingsso-app

# Install dependencies
npm install

# Development server
npm start
# Navigate to http://localhost:4200

# Build for production
npm run build
```

**Configuration:**
- Development: `src/environments/environment.ts`
- Production: `src/environments/environment.prod.ts`

Update `pingSsoConfig` with your PingSSO credentials:
```typescript
pingSsoConfig: {
  clientId: 'your-client-id',
  clientSecret: 'your-client-secret',
  redirectUri: 'http://localhost:4200/callback',
  authorizationEndpoint: 'https://your-pingsso/as/authorization.oauth2',
  tokenEndpoint: 'https://your-pingsso/as/token.oauth2',
  userInfoEndpoint: 'https://your-pingsso/idp/userinfo.openid',
  scope: 'openid profile email'
}
```

### 2. Spring Boot Application

```bash
cd springboot-pingsso-app

# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run
# API available at http://localhost:8080

# Run with JAR
java -jar target/pingsso-springboot-app-1.0.0.jar
```

**Configuration:**
- `src/main/resources/application.yml`

Update PingSSO configuration:
```yaml
pingsso:
  client-id: your-client-id
  client-secret: your-client-secret
  discovery-url: https://your-pingsso/.well-known/openid-configuration
  token-uri: https://your-pingsso/as/token.oauth2
  user-info-uri: https://your-pingsso/idp/userinfo.openid
  scopes: openid,profile,email
```

## Docker Deployment

Run both applications in containers:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

Services will be available at:
- Angular App: `http://localhost:4200`
- Spring Boot API: `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login with PingSSO code
- `POST /api/auth/callback` - Handle OAuth2 callback
- `POST /api/auth/logout` - Logout user

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users?email=...` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/health` - Health check

## Features

### Angular Frontend
- **PingSSO OAuth2 Integration**: Complete authentication flow
- **Token Management**: Automatic token refresh and expiration handling
- **Auth Guard**: Route protection for authenticated pages
- **Auth Interceptor**: Automatic Bearer token injection
- **Responsive UI**: Bootstrap-based components
- **Demo Login**: Fallback demo authentication for testing

### Spring Boot Backend
- **OAuth2 Resource Server**: Token validation and verification
- **User Management**: CRUD operations for users
- **CORS Configuration**: Cross-origin request handling
- **H2 Database**: In-memory database with sample data initialization
- **Security**: Spring Security with OAuth2 client support
- **JWT Support**: Token parsing and validation

## Authentication Flow

1. User initiates login on Angular app
2. Angular redirects to PingSSO authorization endpoint
3. User authenticates on PingSSO
4. PingSSO redirects back to Angular with authorization code
5. Angular exchanges code for access token via Spring Boot callback
6. Token stored in localStorage for future API calls
7. Auth interceptor automatically includes token in all requests
8. Spring Boot validates token and serves protected resources

## Environment Configuration

### Development vs Production

**Development (localhost):**
- Angular: `http://localhost:4200`
- Spring Boot: `http://localhost:8080`
- In-memory H2 database
- Debug logging enabled

**Production:**
- Update environment files with production URLs
- Enable HTTPS endpoints
- Configure persistent database
- Update CORS allowed origins
- Set environment variables for secrets

## Testing

### Angular
```bash
cd angular-pingsso-app
npm test                    # Run unit tests
npm run lint               # Run linter
```

### Spring Boot
```bash
cd springboot-pingsso-app
mvn test                   # Run unit tests
mvn clean verify          # Full build verification
```

## Security Considerations

1. **Client Secret**: Never commit client secrets to version control
2. **HTTPS**: Always use HTTPS in production
3. **Redirect URI**: Whitelist only trusted redirect URIs in PingSSO
4. **CORS**: Restrict CORS origins to known domains
5. **Token Storage**: Consider secure alternatives to localStorage
6. **HTTPS Redirect**: Force HTTPS in production

## Troubleshooting

### CORS Errors
- Ensure Spring Boot CORS configuration includes Angular origin
- Update `CorsConfig.java` with your production domain

### Token Expiration
- Token is automatically refreshed before expiration
- If expired, user is redirected to login
- Check token expiration logic in `AuthService`

### PingSSO Connection Issues
- Verify PingSSO URLs are correct and accessible
- Check firewall/network connectivity
- Enable debug logging in application.yml

### Database Issues
- H2 console available at `http://localhost:8080/h2-console`
- Default credentials: username `sa`, password empty
- Use `jdbc:h2:mem:testdb` as connection URL

## Documentation

- [Angular Documentation](./angular-pingsso-app/README.md)
- [Spring Boot Documentation](./springboot-pingsso-app/README.md)
- [PingSSO Integration Guide](./docs/PINGSSO_INTEGRATION.md)
- [Deployment Guide](./docs/DEPLOYMENT.md)

## CI/CD

Example GitHub Actions workflows are available in `.github/workflows/`.

## License

MIT License

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review PingSSO documentation
3. Check application logs
4. Create an issue in the repository
