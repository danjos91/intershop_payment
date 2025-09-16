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


### Manual Setup

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
   - Copy the Secret: `payment-secret-123`

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

3. **Add scopes to clients:**
   - Go to "intershop-app" client → "Client scope" -> "Add client scope"
   - Select `payment:read` and `payment:write`
   - Go to "payment-service" client → "Client scope" -> "Add client scope"
   - Select `payment:read` and `payment:write`

## Step 5: Test the Setup

### Test 1: Get Access Token (Client Credentials Flow)
```bash
curl -X POST http://localhost:8082/realms/intershop/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=payment-service" \
  -d "client_secret=use actual secret here"
```

### Test 2: Get Access Token (Password Flow)
```bash
curl -X POST http://localhost:8082/realms/intershop/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=intershop-app" \
  -d "client_secret=use actual secret here" \
  -d "username=testuser" \
  -d "password=testpass"
```
