# PingSSO Configuration Files

## Overview

This directory contains configuration templates and environment-specific settings for the PingSSO workspace applications.

## Files

### `.env` - Environment Variables

Main environment configuration file (not committed to git).

```bash
# Copy from template
cp env.example .env

# Edit with your values
nano .env
```

**Key Variables:**
- `PINGSSO_CLIENT_ID` - OAuth2 client ID
- `PINGSSO_CLIENT_SECRET` - OAuth2 client secret (keep secure!)
- `PINGSSO_*_URI` - OAuth2 endpoint URLs
- `PINGSSO_*_ENDPOINT` - Endpoint URLs
- `API_URL` - Backend API base URL

### `.env.example` - Environment Template

Template for `.env` file with example values and documentation.

**Never commit actual secrets!**

## Angular Environment Files

Located in `angular-pingsso-app/src/environments/`

### `environment.ts` - Development

Used during `npm start` and `ng serve`.

Configuration:
- `production: false`
- `apiUrl: http://localhost:8080/api`
- `pingSsoConfig` with development endpoints

### `environment.prod.ts` - Production

Used during `npm run build -- --configuration production`.

Configuration:
- `production: true`
- `apiUrl: https://api.example.com/api`
- `pingSsoConfig` with production endpoints

## Spring Boot Configuration

Located in `springboot-pingsso-app/src/main/resources/`

### `application.yml` - Default Configuration

Main Spring Boot configuration file.

**Key Sections:**
- `spring.application.name` - Application name
- `spring.datasource` - Database configuration
- `pingsso` - OAuth2 and PingSSO settings
- `logging` - Logging levels

### `application-dev.yml` - Development Profile

Active when running with `spring.profiles.active=dev`.

**Overrides:**
- Debug logging enabled
- H2 database console enabled
- DDL: `create-drop` (rebuild on startup)

### `application-prod.yml` - Production Profile

Active when running with `spring.profiles.active=prod`.

**Overrides:**
- Normal logging levels
- PostgreSQL/MySQL database
- DDL: `validate` (no schema changes)
- Secrets from environment

## Environment-Specific Configuration

### Development (localhost)

**Angular:**
```typescript
// src/environments/environment.ts
apiUrl: 'http://localhost:8080/api',
pingSsoConfig: {
  clientId: 'angular-app-client', // Demo client
  redirectUri: 'http://localhost:4200/callback',
  // ... pingsso endpoints
}
```

**Spring Boot:**
```yaml
spring:
  application:
    name: pingsso-springboot-app
  datasource:
    url: jdbc:h2:mem:testdb
server:
  port: 8080
```

### Production (cloud deployment)

**Angular:**
```typescript
// src/environments/environment.prod.ts
apiUrl: 'https://api.mycompany.com/api',
pingSsoConfig: {
  clientId: 'production-client-id',
  redirectUri: 'https://myapp.mycompany.com/callback',
  // ... production pingsso endpoints
}
```

**Spring Boot:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.mycompany.com:5432/pingsso
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
server:
  port: 8080
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE}
```

## Secrets Management

### Development (Insecure - Development Only)

```bash
# .env file (DO NOT COMMIT)
PINGSSO_CLIENT_SECRET=demo-secret-12345
```

### Production (Secure Methods)

#### 1. Kubernetes Secrets

```bash
kubectl create secret generic pingsso-secrets \
  --from-literal=PINGSSO_CLIENT_SECRET="prod-secret-xxxxx"
```

#### 2. AWS Secrets Manager

```bash
aws secretsmanager create-secret \
  --name /pingsso/prod/client-secret \
  --secret-string "prod-secret-xxxxx"
```

#### 3. HashiCorp Vault

```bash
vault kv put secret/pingsso/prod \
  client_secret="prod-secret-xxxxx"
```

#### 4. Environment Variables (Docker/Kubernetes)

```bash
# In container runtime
export PINGSSO_CLIENT_SECRET="prod-secret-xxxxx"
```

## Configuration Loading Order

### Spring Boot

1. **Defaults**: Hardcoded in code
2. **`application.yml`**: Default properties
3. **`application-{profile}.yml`**: Profile-specific
4. **Environment Variables**: Override all above
5. **System Properties**: Highest priority

### Angular

1. **Defaults**: Hardcoded in code
2. **`environment.ts` or `environment.prod.ts`**: Build-time selection
3. **Runtime Configuration**: Window object (if implemented)

## Docker Environment Configuration

### docker-compose.yml

Environment variables passed to containers:

```yaml
services:
  springboot-api:
    environment:
      - PINGSSO_CLIENT_ID=${PINGSSO_CLIENT_ID}
      - PINGSSO_CLIENT_SECRET=${PINGSSO_CLIENT_SECRET}
      # ...
```

### Dockerfile

Build-time arguments:

```dockerfile
ARG PINGSSO_CLIENT_ID=angular-app-client
ENV PINGSSO_CLIENT_ID=$PINGSSO_CLIENT_ID
```

## Configuration Best Practices

### DO

✅ Use environment variables for secrets and sensitive data
✅ Keep separate configurations for dev/prod
✅ Document all configuration options
✅ Validate configuration on startup
✅ Use strong secrets (30+ characters)
✅ Rotate secrets regularly
✅ Audit configuration changes

### DON'T

❌ Commit secrets to git repository
❌ Use same secrets for dev and prod
❌ Store secrets in application properties files
❌ Hardcode credentials in source code
❌ Share secrets in logs or error messages
❌ Use weak secrets
❌ Store plain-text passwords

## Configuration Validation

### Spring Boot Startup Validation

Add to `Application.java`:

```java
@Component
public class ConfigurationValidator implements InitializingBean {
    
    @Value("${pingsso.client-id}")
    private String clientId;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException(
                "PingSSO client ID not configured");
        }
    }
}
```

## Common Configuration Issues

### Issue: "Property not found"

**Solution:** Check environment variable name matches configuration key

### Issue: "Invalid endpoint URL"

**Solution:** Verify PingSSO URL is correct and accessible

### Issue: "Connection refused"

**Solution:** Check if services are running and network connectivity

## Reference

- [Spring Boot Configuration](https://spring.io/guides/topicals/spring-boot-docker/)
- [Angular Environment Configuration](https://angular.io/guide/build)
- [12 Factor App Configuration](https://12factor.net/config)
- [OAuth2 Configuration Best Practices](https://tools.ietf.org/html/rfc6749)
