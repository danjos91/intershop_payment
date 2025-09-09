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

### Quick Start with Docker Compose

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
