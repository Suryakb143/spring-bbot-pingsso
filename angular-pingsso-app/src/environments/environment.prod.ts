export const environment = {
  production: true,
  apiUrl: 'https://api.example.com/api',
  pingSsoConfig: {
    clientId: 'angular-app-client-prod',
    clientSecret: 'your-client-secret-prod',
    redirectUri: 'https://app.example.com/callback',
    authorizationEndpoint: 'https://pingsso.example.com/as/authorization.oauth2',
    tokenEndpoint: 'https://pingsso.example.com/as/token.oauth2',
    userInfoEndpoint: 'https://pingsso.example.com/idp/userinfo.openid',
    scope: 'openid profile email'
  }
};
