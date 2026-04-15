#!/bin/bash

# PingSSO Workspace Setup Script
# This script sets up the entire workspace for development

set -e  # Exit on error

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║      PingSSO Workspace Setup Script                          ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check prerequisites
echo "Checking prerequisites..."
echo ""

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    print_success "Node.js installed: $NODE_VERSION"
else
    print_error "Node.js is not installed. Please install Node.js 18+"
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm -v)
    print_success "npm installed: $NPM_VERSION"
else
    print_error "npm is not installed"
    exit 1
fi

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    print_success "Java installed: $JAVA_VERSION"
else
    print_error "Java is not installed. Please install Java 17+"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -v | head -1)
    print_success "Maven installed: $MVN_VERSION"
else
    print_warning "Maven is not installed. Some commands may not work."
fi

# Check Git
if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version)
    print_success "Git installed: $GIT_VERSION"
else
    print_warning "Git is not installed"
fi

echo ""
echo "Setting up Angular application..."
echo ""

# Setup Angular
cd angular-pingsso-app

if [ ! -d "node_modules" ]; then
    print_info "Installing Angular dependencies..."
    npm install
    print_success "Angular dependencies installed"
else
    print_info "Angular dependencies already installed"
fi

# Update environment files
print_info "Angular configuration files:"
echo "  - src/environments/environment.ts"
echo "  - src/environments/environment.prod.ts"
print_info "Please update PingSSO configuration before running"

cd ..

echo ""
echo "Setting up Spring Boot application..."
echo ""

# Setup Spring Boot
cd springboot-pingsso-app

if [ ! -d "target" ]; then
    print_info "Building Spring Boot application..."
    mvn clean install
    print_success "Spring Boot application built"
else
    print_info "Spring Boot application already built"
fi

# Update configuration
print_info "Spring Boot configuration files:"
echo "  - src/main/resources/application.yml"
print_info "Please update PingSSO configuration before running"

cd ..

echo ""
echo "Creating .env file..."
echo ""

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    cat > .env << 'EOF'
# PingSSO Configuration
PINGSSO_CLIENT_ID=your-client-id
PINGSSO_CLIENT_SECRET=your-client-secret
PINGSSO_DISCOVERY_URL=https://pingsso.example.com/.well-known/openid-configuration
PINGSSO_AUTHORIZATION_URI=https://pingsso.example.com/as/authorization.oauth2
PINGSSO_TOKEN_URI=https://pingsso.example.com/as/token.oauth2
PINGSSO_USER_INFO_URI=https://pingsso.example.com/idp/userinfo.openid

# URLs
PINGSSO_REDIRECT_URI=http://localhost:4200/callback
PINGSSO_AUTH_ENDPOINT=https://pingsso.example.com/as/authorization.oauth2
PINGSSO_TOKEN_ENDPOINT=https://pingsso.example.com/as/token.oauth2
PINGSSO_USERINFO_ENDPOINT=https://pingsso.example.com/idp/userinfo.openid
API_URL=http://localhost:8080/api
EOF
    print_success ".env file created"
else
    print_info ".env file already exists"
fi

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║             Setup Complete!                                   ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "Next steps:"
echo ""
echo "1. Update environment configuration:"
echo "   - Edit .env file with your PingSSO credentials"
echo "   - Update angular-pingsso-app/src/environments/environment.ts"
echo "   - Update springboot-pingsso-app/src/main/resources/application.yml"
echo ""
echo "2. Run Angular development server:"
echo "   cd angular-pingsso-app"
echo "   npm start"
echo ""
echo "3. Run Spring Boot application (in a new terminal):"
echo "   cd springboot-pingsso-app"
echo "   mvn spring-boot:run"
echo ""
echo "4. Open browser:"
echo "   http://localhost:4200"
echo ""
echo "For Docker setup:"
echo "   docker-compose up"
echo ""
echo "For more information:"
echo "   - See README.md"
echo "   - See docs/PINGSSO_INTEGRATION.md"
echo "   - See docs/DEPLOYMENT.md"
echo ""
