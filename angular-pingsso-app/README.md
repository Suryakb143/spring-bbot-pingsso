# Angular PingSSO Application

A modern Angular application with PingSSO authentication integration.

## Getting Started

### Prerequisites
- Node.js (16 or later)
- npm or yarn

### Installation

```bash
npm install
```

### Development Server

```bash
npm start
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Configuration

Update PingSSO configuration in `src/environments/environment.ts`:

```typescript
pingSsoConfig: {
  clientId: 'your-client-id',
  clientSecret: 'your-client-secret',
  redirectUri: 'http://localhost:4200/callback',
  authorizationEndpoint: 'https://your-pingsso-server/as/authorization.oauth2',
  tokenEndpoint: 'https://your-pingsso-server/as/token.oauth2',
  userInfoEndpoint: 'https://your-pingsso-server/idp/userinfo.openid',
  scope: 'openid profile email'
}
```

### Features

- **PingSSO OAuth2 Integration**: Secure authentication with PingSSO
- **Auth Guard**: Protected routes that require authentication
- **HTTP Interceptor**: Automatically adds auth token to all API requests
- **User Info**: Display authenticated user information
- **Sample Dashboard**: Demo dashboard with sample data

### Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── services/
│   │   │   └── auth.service.ts
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts
│   │   └── guards/
│   │       └── auth.guard.ts
│   ├── features/
│   │   ├── login/
│   │   │   └── login.component.*
│   │   └── dashboard/
│   │       └── dashboard.component.*
│   ├── app.component.ts
│   ├── app.module.ts
│   └── app-routing.module.ts
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
├── main.ts
├── styles.scss
└── index.html
```

### Build

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

### Testing

```bash
npm test
```

## License

This project is licensed under the MIT License.
