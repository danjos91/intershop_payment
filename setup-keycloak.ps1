# PowerShell script to set up Keycloak for Intershop
Write-Host "🚀 Setting up Keycloak for Intershop..." -ForegroundColor Green

# Wait for Keycloak to be ready
Write-Host "⏳ Waiting for Keycloak to start..." -ForegroundColor Yellow
do {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/realms/master" -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            break
        }
    }
    catch {
        Write-Host "Waiting for Keycloak..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
} while ($true)

Write-Host "✅ Keycloak is ready!" -ForegroundColor Green

# Get admin token
Write-Host "🔑 Getting admin token..." -ForegroundColor Yellow
$tokenBody = @{
    username = "admin"
    password = "admin123"
    grant_type = "password"
    client_id = "admin-cli"
}

$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8082/realms/master/protocol/openid-connect/token" -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"

if ($tokenResponse.access_token) {
    $adminToken = $tokenResponse.access_token
    Write-Host "✅ Admin token obtained" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to get admin token" -ForegroundColor Red
    exit 1
}

# Create realm
Write-Host "🏗️ Creating intershop realm..." -ForegroundColor Yellow
$realmData = Get-Content "keycloak-setup.json" -Raw

$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

try {
    Invoke-RestMethod -Uri "http://localhost:8082/admin/realms" -Method Post -Body $realmData -Headers $headers
    Write-Host "✅ Realm created successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Realm might already exist or there was an error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "✅ Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Keycloak Admin Console: http://localhost:8082/admin" -ForegroundColor Cyan
Write-Host "👤 Admin credentials: admin / admin123" -ForegroundColor Cyan
Write-Host "🔑 Realm: intershop" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Client Information:" -ForegroundColor Cyan
Write-Host "   Intershop App Client ID: intershop-app" -ForegroundColor White
Write-Host "   Intershop App Secret: jxwpCLO21XPJKcf5ahnsBinBIc7guEAJ" -ForegroundColor White
Write-Host "   Payment Service Client ID: payment-service" -ForegroundColor White
Write-Host "   Payment Service Secret: cS02149Gddsnb4t0jl1KbeAyknrMfFYM" -ForegroundColor White
Write-Host ""
Write-Host "🧪 Test User:" -ForegroundColor Cyan
Write-Host "   Username: testuser" -ForegroundColor White
Write-Host "   Password: testpass" -ForegroundColor White
Write-Host ""
Write-Host "🔗 OAuth2 Endpoints:" -ForegroundColor Cyan
Write-Host "   Authorization: http://localhost:8082/realms/intershop/protocol/openid-connect/auth" -ForegroundColor White
Write-Host "   Token: http://localhost:8082/realms/intershop/protocol/openid-connect/token" -ForegroundColor White
Write-Host "   JWKS: http://localhost:8082/realms/intershop/protocol/openid-connect/certs" -ForegroundColor White

