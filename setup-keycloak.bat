@echo off
echo 🚀 Setting up Keycloak for Intershop...

REM Wait for Keycloak to be ready
echo ⏳ Waiting for Keycloak to start...
:wait_loop
curl -f http://localhost:8082/realms/master >nul 2>&1
if %errorlevel% neq 0 (
    echo Waiting for Keycloak...
    timeout /t 5 /nobreak >nul
    goto wait_loop
)

echo ✅ Keycloak is ready!

REM Get admin token
echo 🔑 Getting admin token...
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8082/realms/master/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=admin123" -d "grant_type=password" -d "client_id=admin-cli"') do set response=%%i

REM Extract token using PowerShell
for /f "tokens=*" %%i in ('powershell -command "('%response%' | ConvertFrom-Json).access_token"') do set ADMIN_TOKEN=%%i

if "%ADMIN_TOKEN%"=="" (
    echo ❌ Failed to get admin token
    pause
    exit /b 1
)

echo ✅ Admin token obtained

REM Create realm
echo 🏗️ Creating intershop realm...
curl -s -X POST http://localhost:8082/admin/realms -H "Authorization: Bearer %ADMIN_TOKEN%" -H "Content-Type: application/json" -d @keycloak-setup.json

echo ✅ Realm created

echo ✅ Setup complete!
echo.
echo 🌐 Keycloak Admin Console: http://localhost:8082/admin
echo 👤 Admin credentials: admin / admin123
echo 🔑 Realm: intershop
echo.
echo 📋 Client Information:
echo    Intershop App Client ID: intershop-app
echo    Intershop App Secret: jxwpCLO21XPJKcf5ahnsBinBIc7guEAJ
echo    Payment Service Client ID: payment-service
echo    Payment Service Secret: cS02149Gddsnb4t0jl1KbeAyknrMfFYM
echo.
echo 🧪 Test User:
echo    Username: testuser
echo    Password: testpass
echo.
echo 🔗 OAuth2 Endpoints:
echo    Authorization: http://localhost:8082/realms/intershop/protocol/openid-connect/auth
echo    Token: http://localhost:8082/realms/intershop/protocol/openid-connect/token
echo    JWKS: http://localhost:8082/realms/intershop/protocol/openid-connect/certs
echo.
pause

