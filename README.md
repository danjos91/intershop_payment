# Multi-Project: Intershop + Payment Service

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=oauth&logoColor=white)


## 🚀 Technology Stack

### Backend Services
- **Java 21** - Core programming languages (different versions per service)
- **Spring Boot 3.x** - Modern application framework
- **Spring WebFlux** - Reactive web framework for high performance
- **Spring Data R2DBC** - Reactive database access (Intershop)
- **Project Reactor** - Reactive programming foundation
- **Lombok** - Code reduction and boilerplate elimination
- **Maven** - Centralized dependency and build management

### Infrastructure & Data
- **Redis** - High-performance caching and session storage
- **H2 Database** - Lightweight, embedded database for development
- **Docker** - Containerized deployment and development
- **Docker Compose** - Multi-service orchestration

### API & Integration
- **RESTful APIs** - HTTP-based service communication
- **OpenAPI 3.0** - API specification and documentation (Payment Service)
- **Thymeleaf** - Server-side templating (Intershop)

## 🚀 Getting Started

### Prerequisites
- **Java 21** - Core programming language for both services
- **Maven 3.6+** for build management
- **Docker & Docker Compose** for containerized deployment
- **Git** for version control

### Quick Start with Docker Compose (Recommended)

1. **Clone the repository:**
```bash
  git clone https://github.com/danjos91/multiproject.git
  cd multiproject
```

2. **Start keycloak and redis:**
```bash
  docker-compose up -d
```

### Local Development Setup

#### Prerequisites for Local Development
- **Java 21** JDK
- **Maven 3.6+**
- **Docker & Docker Compose** (for Keycloak and Redis)
- **Git**

#### Step 1: Start Infrastructure Services
```bash
# Start Keycloak and Redis services
docker-compose up -d

# Verify Keycloak is running
curl http://localhost:8082/realms/master
```

#### Step 2: Configure Keycloak OAuth2
1. **Access Keycloak Admin Console:**
   - Open http://localhost:8082/admin
   - Login with `admin` / `admin123`

2. **Follow Keycloak Setup:**
   - Refer to `KEYCLOAK_SETUP.md` for detailed configuration
   - Create the `intershop` realm
   - Configure OAuth2 clients and users
   - Set up required scopes

#### Step 3: Build All Services
```bash
# Build everything from the root directory
mvn clean install -DskipTests
```

#### Step 4: Run Services Locally

**Option A: Run Individual Services**

**Intershop Service (Port 8080):**
```bash
cd intershop
mvn spring-boot:run
```

**Payment Service (Port 8081):**
```bash
cd payment-service
mvn spring-boot:run
```

**Option B: Run from Multi-Project Root**
```bash
# Run both services simultaneously (in separate terminals)
mvn -pl intershop spring-boot:run
mvn -pl payment-service spring-boot:run
```

#### Step 5: Access the Application
- **Intershop**: http://localhost:8080 (redirects to Keycloak for authentication)
- **Payment Service API**: http://localhost:8081
- **Keycloak Admin**: http://localhost:8082/admin

### Alternative: Manual Setup (Without OAuth2)

If you want to run without OAuth2 authentication:

**Redis on Docker:**
```bash
docker run --name redis-server -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server & sleep 7 && redis-cli"
```

**Run Services:**
```bash
# Intershop Service
cd intershop && mvn spring-boot:run

# Payment Service  
cd payment-service && mvn spring-boot:run
```

## 🧪 Testing

### Run All Tests
```bash
# Test all services
mvn test
```

## 🐳 Docker Support

### Individual Service Builds
```bash
# Build Intershop
cd intershop && docker build -t intershop .

# Build Payment Service
cd payment-service && docker build -t payment-service .
```