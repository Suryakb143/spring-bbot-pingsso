export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  pingSsoConfig: {
    clientId: 'angular-app-client',
    clientSecret: 'your-client-secret',
    redirectUri: 'http://localhost:4200/callback',
    authorizationEndpoint: 'https://pingsso.example.com/as/authorization.oauth2',
    tokenEndpoint: 'https://pingsso.example.com/as/token.oauth2',
    userInfoEndpoint: 'https://pingsso.example.com/idp/userinfo.openid',
    scope: 'openid profile email'
  }
};
