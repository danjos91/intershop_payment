# Keycloak OAuth2 Authorization Server Setup

This guide will help you set up Keycloak as the OAuth2 authorization server for the Intershop project.

## Prerequisites

- Docker and Docker Compose installed
- curl command available
- jq command available (for JSON parsing)

## Step 1: Start Keycloak

1. **Start the services:**
   ```bash
   docker-compose up -d
   ```

2. **Wait for Keycloak to be ready:**
   ```bash
   # Check if Keycloak is running
   curl http://localhost:8082/realms/master
   ```

## Step 2: Access Keycloak Admin Console

1. **Open browser and go to:** http://localhost:8082/admin
2. **Login with:**
   - Username: `admin`
   - Password: `admin123`

## Step 3: Create the Intershop Realm

### Option A: Automatic Setup (Recommended)

Run the setup script:
```bash
# On Linux/Mac
chmod +x setup-keycloak.sh
./setup-keycloak.sh

# On Windows
setup-keycloak.bat
```

### Option B: Manual Setup

1. **Create Realm:**
   - Click "Create Realm" button
   - Name: `intershop`
   - Click "Create"

2. **Create Intershop App Client:**
   - Go to "Clients" → "Create"
   - Client ID: `intershop-app`
   - Client Protocol: `openid-connect`
   - Click "Save"
   - Set Client authentication: `ON`
   - Set Authorization: `OFF`
   - Set Valid redirect URIs: `http://localhost:8080/*`
   - Set Web origins: `http://localhost:8080`
   - Go to "Credentials" tab
   - Copy the Secret: `intershop-secret-123`

3. **Create Payment Service Client:**
   - Go to "Clients" → "Create"
   - Client ID: `payment-service`
   - Client Protocol: `openid-connect`
   - Click "Save"
   - Set Client authentication: `ON`
   - Set Authorization: `OFF`
   - Set Service accounts roles: `ON`
   - Go to "Credentials" tab
   - Copy the Secret: `cS02149Gddsnb4t0jl1KbeAyknrMfFYM`

4. **Create Test User:**
   - Go to "Users" → "Create new user"
   - Username: `testuser`
   - Email: `testuser@example.com`
   - First name: `Test`
   - Last name: `User`
   - Set "Email verified": `ON`
   - Click "Save"
   - Go to "Credentials" tab
   - Set password: `testpass`
   - Set "Temporary": `OFF`

## Step 4: Configure Client Scopes

1. **Create Payment Scopes:**
   - Go to "Client scopes" → "Create"
   - Name: `payment:read`
   - Description: `Read access to payment service`
   - Protocol: `openid-connect`
   - Click "Save"

2. **Create Payment Write Scope:**
   - Go to "Client scopes" → "Create"
   - Name: `payment:write`
   - Description: `Write access to payment service`
   - Protocol: `openid-connect`
   - Click "Save"

## Step 5: Test the Setup

### Test 1: Check Keycloak is Running
```bash
curl http://localhost:8082/realms/intershop/.well-known/openid_configuration
```

### Test 2: Get Access Token (Client Credentials Flow)
```bash
curl -X POST http://localhost:8082/realms/intershop/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=payment-service" \
  -d "client_secret=cS02149Gddsnb4t0jl1KbeAyknrMfFYM"
```

### Test 3: Get Access Token (Password Flow)
```bash
curl -X POST http://localhost:8082/realms/intershop/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=intershop-app" \
  -d "client_secret=jxwpCLO21XPJKcf5ahnsBinBIc7guEAJ" \
  -d "username=testuser" \
  -d "password=testpass"
```

## Important URLs

- **Keycloak Admin Console:** http://localhost:8082/admin
- **Realm:** `intershop`
- **Authorization Endpoint:** http://localhost:8082/realms/intershop/protocol/openid-connect/auth
- **Token Endpoint:** http://localhost:8082/realms/intershop/protocol/openid-connect/token
- **JWKS Endpoint:** http://localhost:8082/realms/intershop/protocol/openid-connect/certs

## Client Configuration Summary

| Service | Client ID | Secret | Flow | Purpose |
|---------|-----------|--------|------|---------|
| Intershop App | `intershop-app` | `jxwpCLO21XPJKcf5ahnsBinBIc7guEAJ` | Authorization Code + Client Credentials | Web application authentication |
| Payment Service | `cS02149Gddsnb4t0jl1KbeAyknrMfFYM` | Client Credentials | Resource server authentication |

## Troubleshooting

### Keycloak Won't Start
- Check if port 8082 is available
- Check Docker logs: `docker-compose logs keycloak`

### Can't Access Admin Console
- Wait a few minutes for Keycloak to fully initialize
- Check if the container is running: `docker ps`

### Token Requests Fail
- Verify client credentials are correct
- Check if the realm exists
- Ensure the client is enabled

## Next Steps

Once Keycloak is set up and tested, you can:
1. Configure the Intershop application to use OAuth2 client
2. Configure the Payment service to validate JWT tokens
3. Test the complete OAuth2 flow between services

