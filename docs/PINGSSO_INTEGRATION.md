# PingSSO Integration Guide

This guide explains how to set up and configure PingSSO OAuth2 authentication for the PingSSO workspace applications.

## What is PingSSO?

PingSSO is a centralized identity and access management (IAM) solution that provides:
- OAuth2 / OpenID Connect (OIDC) support
- Multi-factor authentication (MFA)
- User federation
- Single Sign-On (SSO)
- Role-based access control (RBAC)

## Prerequisites

1. Access to a PingSSO instance or compatible OAuth2 provider
2. Administrator credentials to register applications
3. Understanding of OAuth2/OIDC flow
4. Both Angular and Spring Boot applications deployed

## PingSSO Configuration

### Step 1: Register OAuth2 Applications

#### Register Angular Application

In your PingSSO admin console:

1. Navigate to **Applications > OAuth2 Applications**
2. Click **Create New Application**
3. Configure:
   - **Client Name**: Angular PingSSO App
   - **Client Type**: Public (Native/SPA)
   - **Allowed Redirect URIs**:
     - Development: `http://localhost:4200/callback`
     - Production: `https://app.example.com/callback`
   - **Allowed Origins**: 
     - Development: `http://localhost:4200`
     - Production: `https://app.example.com`
   - **Scopes**: `openid profile email`
   - **Response Type**: `code`
   - **Response Mode**: `query`
4. Copy the **Client ID** (no client secret for public clients)
5. Save the application

#### Register Spring Boot Application

1. Navigate to **Applications > OAuth2 Applications**
2. Click **Create New Application**
3. Configure:
   - **Client Name**: Spring Boot PingSSO API
   - **Client Type**: Confidential (Web Server)
   - **Allowed Redirect URIs**: `http://localhost:8080/callback` (if needed)
   - **Grant Types**: 
     - Authorization Code
     - Refresh Token
   - **Scopes**: `openid profile email`
4. Copy:
   - **Client ID**
   - **Client Secret** (keep secure!)
5. Save the application

### Step 2: Configure Angular Application

Update `angular-pingsso-app/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  pingSsoConfig: {
    clientId: 'YOUR_ANGULAR_CLIENT_ID',           // From Step 1
    clientSecret: '',                               // Empty for public clients
    redirectUri: 'http://localhost:4200/callback',
    authorizationEndpoint: 'https://pingsso.example.com/as/authorization.oauth2',
    tokenEndpoint: 'https://pingsso.example.com/as/token.oauth2',
    userInfoEndpoint: 'https://pingsso.example.com/idp/userinfo.openid',
    scope: 'openid profile email'
  }
};
```

For production, update `environment.prod.ts` with production URLs.

### Step 3: Configure Spring Boot Application

Update `springboot-pingsso-app/src/main/resources/application.yml`:

```yaml
pingsso:
  client-id: YOUR_SPRINGBOOT_CLIENT_ID           # From Step 1
  client-secret: YOUR_SPRINGBOOT_CLIENT_SECRET   # Keep secure!
  discovery-url: https://pingsso.example.com/.well-known/openid-configuration
  authorization-uri: https://pingsso.example.com/as/authorization.oauth2
  token-uri: https://pingsso.example.com/as/token.oauth2
  user-info-uri: https://pingsso.example.com/idp/userinfo.openid
  scopes: openid,profile,email
```

**IMPORTANT**: Use environment variables in production:

```bash
export PINGSSO_CLIENT_ID=your-client-id
export PINGSSO_CLIENT_SECRET=your-client-secret
```

### Step 4: OpenID Connect Discovery

PingSSO supports automatic configuration via OpenID Connect Discovery:

```bash
# Get configuration from PingSSO
curl https://pingsso.example.com/.well-known/openid-configuration
```

This returns endpoints and configuration you can use.

## OAuth2 Authorization Code Flow

The application implements the standard OAuth2 Authorization Code flow:

```
1. User clicks "Login with PingSSO"
   ↓
2. Angular redirects to PingSSO authorization endpoint with:
   - client_id
   - redirect_uri
   - response_type=code
   - scope
   - state (CSRF protection)
   ↓
3. User authenticates on PingSSO
   ↓
4. PingSSO redirects back with authorization code to redirect_uri
   ↓
5. Angular's callback component receives the code
   ↓
6. Angular sends code to Spring Boot backend
   ↓
7. Spring Boot exchanges code for access token:
   - Authorization: Basic [base64(client_id:client_secret)]
   - grant_type=authorization_code
   - code
   - redirect_uri
   ↓
8. Spring Boot receives access token and user info
   ↓
9. Angular stores token and redirects to dashboard
   ↓
10. Token automatically included in all API requests
```

## Security Best Practices

### 1. PKCE (Proof Key for Code Exchange)

For public clients (Angular), consider using PKCE:

```typescript
// In auth.service.ts
private generateCodeChallenge(): string {
  const codeVerifier = this.generateRandomString(128);
  const hash = this.sha256(codeVerifier);
  return this.fixedEncodeURIComponent(this.base64url(hash));
}
```

### 2. State Parameter

The application includes state parameter for CSRF protection:

```typescript
// In auth.service.ts
state: this.generateState()  // Random value
```

Verify state matches on callback.

### 3. Token Storage

Current: localStorage (suitable for SPAs, consider alternatives)

Alternatives:
- **httpOnly cookies**: Most secure, but requires backend cookie management
- **sessionStorage**: Only for current tab
- **IndexedDB**: More storage capacity

### 4. HTTPS Requirement

Always use HTTPS in production:

```bash
# Generate self-signed certificate for testing
openssl req -x509 -newkey rsa:4096 -nodes -out cert.pem -keyout key.pem -days 365
```

### 5. CORS Configuration

Restrict to known domains only:

```java
// In CorsConfig.java
.allowedOrigins("https://app.example.com")
```

### 6. Secret Management

Never commit secrets:

```bash
# Use environment variables
export PINGSSO_CLIENT_SECRET=$(cat /run/secrets/pingsso_secret)

# Or use a secrets management system
# - AWS Secrets Manager
# - HashiCorp Vault
# - Kubernetes Secrets
```

## Troubleshooting

### Common Issues

#### 1. "Invalid Redirect URI"
- **Cause**: Redirect URI doesn't match registered in PingSSO
- **Solution**: 
  ```bash
  # Check registered URIs
  curl https://pingsso.example.com/oauth/applications/{client_id}
  
  # Update environment files
  cat angular-pingsso-app/src/environments/environment.ts
  ```

#### 2. "CORS Error"
- **Cause**: Browser blocks cross-origin request
- **Solution**:
  ```yaml
  # In Spring Boot application.yml
  cors:
    allowed-origins: http://localhost:4200
    allowed-methods: GET,POST,PUT,DELETE
  ```

#### 3. "Token Verification Failed"
- **Cause**: Spring Boot cannot verify token signature
- **Solution**:
  ```bash
  # Ensure token endpoint URL is correct
  openssl s_client -connect pingsso.example.com:443
  
  # Check certificate chain
  # Add PingSSO's CA certificate if needed
  ```

#### 4. "Access Token Expired"
- **Cause**: Token TTL exceeded
- **Solution**: Token is automatically refreshed 1 minute before expiration in `AuthService`

#### 5. "Cannot Find User Info"
- **Cause**: userInfo endpoint returns different claim names
- **Solution**: check PingSSO documentation for claim names and update `AuthService.getUserInfo()`

### Debug Logging

Enable debug logging for troubleshooting:

**Angular:**
```typescript
// In auth.service.ts
console.log('Token:', token);
console.log('User:', user);
```

**Spring Boot:**
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
    com.pingsso.app: DEBUG
```

## Advanced Configuration

### Multi-Tenant Setup

For multiple PingSSO instances:

```typescript
// In auth.service.ts
private getPingSsoConfig(): PingSsoConfig {
  const tenantId = this.getTenantIdFromUrl();
  return this.getTenantConfig(tenantId);
}
```

### Custom Claims

Map custom claims to user roles:

```java
// In UserService.java
private Set<String> mapClaimsToRoles(Map<String, Object> claims) {
  Set<String> roles = new HashSet<>();
  // Map PingSSO roles to application roles
  List<String> pingRoles = (List<String>) claims.get("roles");
  for (String role : pingRoles) {
    roles.add("ROLE_" + role.toUpperCase());
  }
  return roles;
}
```

### Attribute Mapping

Map PingSSO attributes to User entity:

```java
// In UserService.java
private User mapAttributesToUser(Map<String, Object> attributes) {
  User user = new User();
  user.setEmail((String) attributes.get("email"));
  user.setName((String) attributes.get("name"));
  user.setPicture((String) attributes.get("picture"));
  // Map additional attributes as needed
  return user;
}
```

## Testing

### Integration Testing

```java
// In AuthControllerTest.java
@Test
public void testOAuthCallback() {
  Map<String, Object> tokenData = new LinkedHashMap<>();
  Map<String, Object> userInfo = new LinkedHashMap<>();
  userInfo.put("sub", "user-123");
  userInfo.put("email", "test@example.com");
  userInfo.put("name", "Test User");
  
  ResponseEntity<?> response = authController.handleCallback(tokenData);
  assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
}
```

### Manual Testing Checklist

- [ ] Login with PingSSO succeeds
- [ ] Token is stored in localStorage
- [ ] User info displayed correctly
- [ ] API calls include Authorization header
- [ ] Token refresh works before expiration
- [ ] Logout clears tokens
- [ ] Dashboard is protected (requires auth)
- [ ] Invalid redirects go to login
- [ ] State parameter prevents CSRF attacks

## Support

For issues:
1. Check PingSSO documentation: `https://docs.pingsso.example.com`
2. Enable debug logging
3. Check browser console for errors
4. Verify network requests in browser DevTools
5. Check application logs for backend errors

## Related Documentation

- [OAuth2 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html)
- [Spring Security OAuth2 Docs](https://spring.io/projects/spring-security-oauth)
- [Angular HttpClient Interceptors](https://angular.io/guide/http#intercepting-requests-and-responses)
