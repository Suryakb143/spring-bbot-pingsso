# PingSSO API Testing Guide

This guide covers three methods to test the PingSSO Spring Boot API endpoints:
1. **Swagger UI** - Interactive API documentation (Built-in)
2. **REST Client** - VS Code extension for quick testing
3. **Postman** - Full-featured API testing tool

## Quick Start

### 1. Start the Application

```bash
cd springboot-pingsso-app
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Method 1: Swagger UI (Interactive Documentation)

### Accessing Swagger UI

Once the application is running, open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

### Features
- ✅ View all available endpoints with documentation
- ✅ See request/response models
- ✅ Try endpoints directly from the UI
- ✅ View HTTP status codes and error responses
- ✅ Bearer token authentication support

### How to Use
1. Navigate to **http://localhost:8080/swagger-ui.html**
2. Click on any endpoint to expand it
3. Click **"Try it out"** button
4. Fill in required parameters
5. Click **"Execute"** to send the request
6. View the response

### Endpoints Available in Swagger

#### Authentication
- **POST /api/auth/login** - Login user with PingSSO credentials
- **POST /api/auth/callback** - Handle OAuth callback

#### Sessions
- **GET /api/auth/sessions** - List active sessions (requires auth)
- **GET /api/auth/session-info** - Get current session info (requires auth)
- **POST /api/auth/sessions/{sessionId}/revoke** - Revoke a session (requires auth)

#### User
- **GET /api/auth/user-info** - Get user by email

#### Token
- **POST /api/auth/token** - Generate new bearer token (requires auth)

#### Logout
- **POST /api/auth/logout** - Logout current session (requires auth)
- **POST /api/auth/logout-all** - Logout all sessions (requires auth)

---

## Method 2: REST Client (VS Code Extension)

### Installation
1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Search for "REST Client"
4. Install the extension by Huachao Mou

### Using the REST Client

1. Open the file: `springboot-pingsso-app/rest-client.http`
2. You'll see all the API requests organized by category
3. Update the variables at the top:
   ```http
   @baseUrl = http://localhost:8080
   @sessionId = <your-session-id>
   @bearerToken = <your-bearer-token>
   @email = <your-email>
   ```

4. Click **"Send Request"** above any request to execute it
5. Results appear in a side panel

### Example: Login Flow

1. **Execute Login request** to get sessionId and bearerToken
2. **Copy** the bearerToken from the response
3. **Paste** it into the `@bearerToken` variable
4. **Copy** the sessionId from the response
5. **Paste** it into the `@sessionId` variable
6. Now all authenticated requests will work

### Tips
- Variables in `{{ }}` are automatically replaced
- Use `@name` comments to organize requests
- Right-click a request to view more options
- Responses are cached and can be viewed later

---

## Method 3: Postman

### Installation
1. Download Postman from https://www.postman.com/downloads/
2. Install and launch the application

### Importing the Collection

1. Open Postman
2. Click **Import** (top-left)
3. Select the file: `springboot-pingsso-app/PingSSO-API.postman_collection.json`
4. Collection is now imported

### Setting Up Environment Variables

1. Click **Environments** (left sidebar)
2. Click **Create New Environment** or select existing
3. Set variables:
   - `baseUrl`: http://localhost:8080
   - `email`: test@example.com
   - `sessionId`: (will be filled after login)
   - `bearerToken`: (will be filled after login)

### Making Requests

1. Select the collection from the left sidebar
2. Expand folders to see requests
3. Click a request to open it
4. Click **Send** button
5. View response in the lower panel

### Example: Complete Login Flow

1. **Login request**
   - POST /api/auth/login
   - Click Send
   - Copy `sessionId` from response
   - Copy `bearerToken` from response

2. **Set variables**
   - Click Environment settings
   - Update `sessionId` and `bearerToken` variables

3. **Test authenticated endpoints**
   - GET /api/auth/sessions
   - GET /api/auth/session-info
   - POST /api/auth/logout

---

## Testing Different Scenarios

### 1. Basic Login

**Request:**
```json
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "pingSsoId": "pingsso-123",
  "name": "John Doe",
  "picture": "https://example.com/avatar.jpg"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://example.com/avatar.jpg",
    "roles": [],
    "active": true
  },
  "sessionId": "sess_abc123xyz",
  "expiresAt": "2026-04-15T21:16:34",
  "bearerToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 2. OAuth Callback

**Request:**
```json
POST http://localhost:8080/api/auth/callback
Content-Type: application/json

{
  "userInfo": {
    "sub": "pingsso-oauth-123",
    "email": "oauth@example.com",
    "name": "OAuth User",
    "picture": "https://example.com/oauth-avatar.jpg"
  }
}
```

### 3. Get Active Sessions (Requires Auth)

**Request:**
```
GET http://localhost:8080/api/auth/sessions
Authorization: Bearer <your-bearer-token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "sessions": [
    {
      "id": 1,
      "sessionId": "sess_abc123xyz",
      "userId": 1,
      "email": "user@example.com",
      "ipAddress": "127.0.0.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2026-04-14T21:16:34",
      "lastAccessedAt": "2026-04-14T21:16:34",
      "expiresAt": "2026-04-15T21:16:34",
      "active": true,
      "deviceName": "Chrome Desktop"
    }
  ],
  "count": 1
}
```

### 4. Logout Current Session

**Request:**
```
POST http://localhost:8080/api/auth/logout
Authorization: Bearer <your-bearer-token>
Cookie: PINGSSO_SESSION=<your-session-id>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

### 5. Logout All Sessions

**Request:**
```
POST http://localhost:8080/api/auth/logout-all
Authorization: Bearer <your-bearer-token>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out from all sessions"
}
```

---

## Common Response Codes

| Code | Scenario |
|------|----------|
| **200** | Request successful |
| **400** | Invalid request body or parameters |
| **401** | Unauthorized - missing or invalid bearer token |
| **403** | Forbidden - user lacks permission (e.g., trying to revoke others' sessions) |
| **404** | Resource not found (e.g., user or session not found) |
| **500** | Internal server error |

---

## Security Headers

The API includes CORS headers for browser requests:
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Credentials: true
```

Session cookies are set with:
- `HttpOnly`: true (cannot be accessed via JavaScript)
- `Secure`: true (only sent over HTTPS)
- `SameSite`: Strict (prevents CSRF attacks)

---

## Additional Resources

- **OpenAPI Specification**: http://localhost:8080/api-docs
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Database Console**: http://localhost:8080/h2-console

---

## Troubleshooting

### Bearer token not working
- Ensure the token is copied exactly (no spaces)
- Tokens expire after 24 hours by default
- Use `/api/auth/token` endpoint to generate a new token

### CORS errors in browser
- Ensure the Angular app is running on http://localhost:4200
- Check CORS configuration in AuthController

### Session not found
- Sessions are stored in H2 database (in-memory)
- Sessions are cleared when application restarts
- Re-login to create a new session

### 401 Unauthorized on authenticated endpoints
- Check if Bearer token is included in Authorization header
- Format must be: `Authorization: Bearer <token>`
- Verify token has not expired
