#!/bin/bash

echo "🚀 Setting up Keycloak for Intershop..."

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to start..."
until curl -f http://localhost:8082/realms/master > /dev/null 2>&1; do
    echo "Waiting for Keycloak..."
    sleep 5
done

echo "✅ Keycloak is ready!"

# Get admin token
echo "🔑 Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8082/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Failed to get admin token"
    exit 1
fi

echo "✅ Admin token obtained"

# Create realm
echo "🏗️ Creating intershop realm..."
curl -s -X POST http://localhost:8082/admin/realms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @keycloak-setup.json

echo "✅ Realm created"

# Get realm token for further operations
echo "🔑 Getting realm admin token..."
REALM_TOKEN=$(curl -s -X POST http://localhost:8082/realms/intershop/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

echo "✅ Setup complete!"
echo ""
echo "🌐 Keycloak Admin Console: http://localhost:8082/admin"
echo "👤 Admin credentials: admin / admin123"
echo "🔑 Realm: intershop"
echo ""
echo "📋 Client Information:"
echo "   Intershop App Client ID: intershop-app"
echo "   Intershop App Secret: jxwpCLO21XPJKcf5ahnsBinBIc7guEAJ"
echo "   Payment Service Client ID: payment-service"
echo "   Payment Service Secret: cS02149Gddsnb4t0jl1KbeAyknrMfFYM"
echo ""
echo "🧪 Test User:"
echo "   Username: testuser"
echo "   Password: testpass"
echo ""
echo "🔗 OAuth2 Endpoints:"
echo "   Authorization: http://localhost:8082/realms/intershop/protocol/openid-connect/auth"
echo "   Token: http://localhost:8082/realms/intershop/protocol/openid-connect/token"
echo "   JWKS: http://localhost:8082/realms/intershop/protocol/openid-connect/certs"

