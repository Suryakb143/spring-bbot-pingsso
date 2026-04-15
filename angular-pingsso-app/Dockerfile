# Multi-stage build for Angular application
FROM node:18-alpine AS builder

WORKDIR /build

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build application with environment variables
ARG PINGSSO_CLIENT_ID=angular-app-client
ARG PINGSSO_REDIRECT_URI=http://localhost:4200/callback
ARG PINGSSO_AUTH_ENDPOINT=https://pingsso.example.com/as/authorization.oauth2
ARG PINGSSO_TOKEN_ENDPOINT=https://pingsso.example.com/as/token.oauth2
ARG PINGSSO_USERINFO_ENDPOINT=https://pingsso.example.com/idp/userinfo.openid
ARG API_URL=http://localhost:8080/api

ENV PINGSSO_CLIENT_ID=$PINGSSO_CLIENT_ID
ENV PINGSSO_REDIRECT_URI=$PINGSSO_REDIRECT_URI
ENV PINGSSO_AUTH_ENDPOINT=$PINGSSO_AUTH_ENDPOINT
ENV PINGSSO_TOKEN_ENDPOINT=$PINGSSO_TOKEN_ENDPOINT
ENV PINGSSO_USERINFO_ENDPOINT=$PINGSSO_USERINFO_ENDPOINT
ENV API_URL=$API_URL

# Build Angular app
RUN npm run build -- --configuration production

# Nginx runtime stage
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built application from builder
COPY --from=builder /build/dist/angular-pingsso-app /usr/share/nginx/html

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
