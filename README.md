# Multi-Project: Intershop + Payment Service

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=oauth2&logoColor=white)

## 📋 Project Overview

This is a **multimodule Maven project** containing two microservices:

1. **Intershop** - Main e-commerce application with Redis caching
2. **Payment Service** - RESTful payment processing service

Both services are built with **Spring Boot 3.x** and **Spring WebFlux** for reactive programming, providing high performance and scalability.

## 🚀 Technology Stack

### Backend Services
- **Java 21** - Core programming language
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
- **OAuth2** - Secure service-to-service authentication

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

2. **Start all services:**
```bash
docker-compose up -d
```

3. **Access the applications:**
   - **Intershop**: http://localhost:8080
   - **Payment Service**: http://localhost:8081
   - **Redis**: localhost:6379

### Alternative: Manual Setup

#### Build All Services
```bash
# Build everything from the root directory
mvn clean install -DskipTests
```

#### Run Individual Services

**1. Start Redis:**
```bash
# Option A: Using Docker
docker run --name redis-server -d -p 6379:6379 redis:7.4.2-bookworm

# Option B: Using Docker Compose (Redis only)
docker-compose up redis -d
```

**2. Start Payment Service:**
```bash
cd payment-service
mvn spring-boot:run
```

**3. Start Intershop Service:**
```bash
cd intershop
mvn spring-boot:run
```

#### Run from Multi-Project Root
```bash
# Run both services simultaneously
mvn -pl payment-service spring-boot:run &
mvn -pl intershop spring-boot:run
```

## 🏗️ Architecture

### Service Communication
```
┌─────────────────┐    HTTP/REST    ┌─────────────────┐
│   Intershop     │◄──────────────►│  Payment Service│
│   (Port 8080)   │   OAuth2 JWT    │   (Port 8081)   │
└─────────────────┘                 └─────────────────┘
         │
         │ Redis Cache
         ▼
┌─────────────────┐
│      Redis      │
│   (Port 6379)   │
└─────────────────┘
```

### Key Features

#### Intershop Application
- **Product Catalog** with search, filtering, and pagination
- **Shopping Cart** with real-time updates
- **Order Management** with payment integration
- **Redis Caching** for improved performance
- **User Authentication** and session management
- **Responsive Web UI** with Thymeleaf templates

#### Payment Service
- **Balance Management** with real-time balance checking
- **Payment Processing** with transaction validation
- **OAuth2 Security** for secure API access
- **OpenAPI Documentation** for easy integration
- **Reactive Architecture** for high performance

## 🧪 Testing

### Run All Tests
```bash
# Test all services
mvn test
```

### Run Specific Service Tests
```bash
# Test only Intershop
mvn -pl intershop test

# Test only Payment Service
mvn -pl payment-service test
```

### Test Coverage
- **Unit Tests** for all service layers
- **Integration Tests** with Testcontainers
- **WebFlux Tests** for reactive endpoints
- **Payment Integration Tests** with WireMock

## 🐳 Docker Support

### Individual Service Builds
```bash
# Build Intershop
cd intershop && docker build -t intershop .

# Build Payment Service
cd payment-service && docker build -t payment-service .
```

### Docker Compose Services
- **redis**: Redis cache server
- **payment-service**: Payment processing service
- **intershop**: Main e-commerce application

### Environment Variables
```bash
# Redis Configuration
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# Payment Service Configuration
PAYMENT_SERVICE_URL=http://payment-service:8081
PAYMENT_INITIAL_BALANCE=1000.00
PAYMENT_CURRENCY=RUB
```

## 🔧 Configuration

### Application Profiles
- **default**: Local development configuration
- **docker**: Docker container configuration
- **test**: Test environment configuration

### Redis Configuration
- **Cache TTL**: 5 minutes for product data
- **Connection Pool**: 8 max connections
- **Serialization**: JSON with Jackson

### Payment Service Configuration
- **Initial Balance**: 1000.00 RUB
- **OAuth2 Scopes**: payment:read, payment:write
- **API Documentation**: Available at /swagger-ui.html

## 🚀 Deployment

### Production Deployment
1. **Build Docker images:**
```bash
docker-compose build
```

2. **Deploy with Docker Compose:**
```bash
docker-compose up -d
```

3. **Monitor services:**
```bash
docker-compose logs -f
```

### Health Checks
- **Intershop**: http://localhost:8080/actuator/health
- **Payment Service**: http://localhost:8081/actuator/health

## 📚 API Documentation

### Payment Service API
- **GET /api/payment/balance** - Get current balance
- **POST /api/payment/process** - Process payment

### OpenAPI Specification
Available at: http://localhost:8081/swagger-ui.html

## 🔒 Security

### OAuth2 Configuration
- **Client Credentials Flow** for service-to-service communication
- **JWT Tokens** for secure API access
- **Scope-based Authorization** for fine-grained access control

### Security Headers
- **CORS** configured for cross-origin requests
- **CSRF Protection** disabled for API endpoints
- **Content Security Policy** headers included

## 🐛 Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Ensure Redis is running on port 6379
   - Check Redis configuration in application.yaml

2. **Payment Service Unavailable**
   - Verify payment service is running on port 8081
   - Check OAuth2 configuration

3. **Docker Compose Issues**
   - Ensure Docker and Docker Compose are installed
   - Check port conflicts (8080, 8081, 6379)

### Logs
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs intershop
docker-compose logs payment-service
docker-compose logs redis
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.