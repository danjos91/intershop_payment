# Multi-Project: Intershop + Payment Service

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)


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

2. **Start all services:**
```bash
  docker-compose up -d
```

### Alternative: Manual Setup

#### Build All Services
```bash
# Build everything from the root directory
mvn clean install -DskipTests
```

#### Run Individual Services

**Redis on Docker:**
```bash
docker run --name redis-server -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server & sleep 7 && redis-cli"
```

**Intershop Service:**
```bash
cd intershop
mvn spring-boot:run
```

**Payment Service:**
```bash
cd payment-service
mvn spring-boot:run
```

#### Run from Multi-Project Root
```bash
# Run both services simultaneously
  mvn -pl intershop spring-boot:run
  mvn -pl payment-service spring-boot:run
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