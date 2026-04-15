@echo off
REM PingSSO Workspace Setup Script for Windows
REM This script sets up the entire workspace for development

setlocal enabledelayedexpansion

echo.
echo ============================================================
echo      PingSSO Workspace Setup Script
echo ============================================================
echo.

REM Check prerequisites
echo Checking prerequisites...
echo.

REM Check Node.js
where node >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('node -v') do set NODE_VERSION=%%i
    echo [OK] Node.js installed: !NODE_VERSION!
) else (
    echo [ERROR] Node.js is not installed. Please install Node.js 18+
    exit /b 1
)

REM Check npm
where npm >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('npm -v') do set NPM_VERSION=%%i
    echo [OK] npm installed: !NPM_VERSION!
) else (
    echo [ERROR] npm is not installed
    exit /b 1
)

REM Check Java
where java >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /R "version"') do set JAVA_VERSION=%%i
    echo [OK] Java installed: !JAVA_VERSION!
) else (
    echo [ERROR] Java is not installed. Please install Java 17+
    exit /b 1
)

REM Check Maven
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr /R "Apache"') do set MVN_VERSION=%%i
    echo [OK] Maven installed: !MVN_VERSION!
) else (
    echo [WARNING] Maven is not installed. Some commands may not work.
)

echo.
echo Setting up Angular application...
echo.

cd angular-pingsso-app

if not exist "node_modules" (
    echo Installing Angular dependencies...
    call npm install
    echo [OK] Angular dependencies installed
) else (
    echo [INFO] Angular dependencies already installed
)

echo [INFO] Angular configuration files:
echo.       - src/environments/environment.ts
echo.       - src/environments/environment.prod.ts
echo [INFO] Please update PingSSO configuration before running
echo.

cd ..

echo.
echo Setting up Spring Boot application...
echo.

cd springboot-pingsso-app

if not exist "target" (
    echo Building Spring Boot application...
    call mvn clean install
    echo [OK] Spring Boot application built
) else (
    echo [INFO] Spring Boot application already built
)

echo [INFO] Spring Boot configuration files:
echo.       - src/main/resources/application.yml
echo [INFO] Please update PingSSO configuration before running
echo.

cd ..

echo.
echo Creating .env file...
echo.

if not exist ".env" (
    (
        echo # PingSSO Configuration
        echo PINGSSO_CLIENT_ID=your-client-id
        echo PINGSSO_CLIENT_SECRET=your-client-secret
        echo PINGSSO_DISCOVERY_URL=https://pingsso.example.com/.well-known/openid-configuration
        echo PINGSSO_AUTHORIZATION_URI=https://pingsso.example.com/as/authorization.oauth2
        echo PINGSSO_TOKEN_URI=https://pingsso.example.com/as/token.oauth2
        echo PINGSSO_USER_INFO_URI=https://pingsso.example.com/idp/userinfo.openid
        echo.
        echo # URLs
        echo PINGSSO_REDIRECT_URI=http://localhost:4200/callback
        echo PINGSSO_AUTH_ENDPOINT=https://pingsso.example.com/as/authorization.oauth2
        echo PINGSSO_TOKEN_ENDPOINT=https://pingsso.example.com/as/token.oauth2
        echo PINGSSO_USERINFO_ENDPOINT=https://pingsso.example.com/idp/userinfo.openid
        echo API_URL=http://localhost:8080/api
    ) > .env
    echo [OK] .env file created
) else (
    echo [INFO] .env file already exists
)

echo.
echo ============================================================
echo             Setup Complete!
echo ============================================================
echo.
echo Next steps:
echo.
echo 1. Update environment configuration:
echo    - Edit .env file with your PingSSO credentials
echo    - Update angular-pingsso-app/src/environments/environment.ts
echo    - Update springboot-pingsso-app/src/main/resources/application.yml
echo.
echo 2. Run Angular development server:
echo    cd angular-pingsso-app
echo    npm start
echo.
echo 3. Run Spring Boot application ^(in a new terminal^):
echo    cd springboot-pingsso-app
echo    mvn spring-boot:run
echo.
echo 4. Open browser:
echo    http://localhost:4200
echo.
echo For Docker setup:
echo    docker-compose up
echo.
echo For more information:
echo    - See README.md
echo    - See docs/PINGSSO_INTEGRATION.md
echo    - See docs/DEPLOYMENT.md
echo.

pause
