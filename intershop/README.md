# Intershop - Reactive E-Commerce Platform

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![R2DBC](https://img.shields.io/badge/R2DBC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-blue?style=for-the-badge)

## 📚 Technology Stack

### Backend (Reactive)
- **Java 21** - Core programming language
- **Spring Boot 3.x** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Spring Data R2DBC** - Reactive database access
- **R2DBC H2** - Reactive H2 database driver
- **Project Reactor** - Reactive programming foundation
- **Lombok** - Code reduction boilerplate
- **Maven** - Dependency and build management

### Frontend
- **Thymeleaf** - Server-side templating

## 🚀 Reactive Architecture

This application is built using **reactive programming principles**:

- **Non-blocking I/O** - All database operations and HTTP requests are non-blocking
- **Event-driven** - Uses reactive streams for data processing
- **Scalable** - Designed to handle high concurrency with minimal resource usage
- **Responsive** - Provides better performance under load

## 🔐 OAuth2 Authentication

This application implements **OAuth2 with Keycloak** for secure authentication and authorization:

### Authentication Features
- **OAuth2 Authorization Code Flow** - Industry-standard authentication protocol
- **Keycloak Integration** - Enterprise-grade identity and access management
- **JWT Token Management** - Secure token-based authentication
- **Role-based Access Control** - Different permissions for users and admins
- **Session Management** - Secure user session handling

### Setup
Refer to `KEYCLOAK_SETUP.md` for detailed Keycloak configuration and setup instructions.

## 🛍️ Core Features

### User Functionality
- ✅ Product catalog browsing and search
- ✅ Shopping cart management
- ✅ Order placement and history
- ✅ User account system
- ✅ OAuth2 authentication

### Admin Functionality
- ⚙️ Product management (CRUD)
- 📊 Order management
- 👥 User management
- 🔐 Role-based access control

## 🚀 Getting Started

### Prerequisites
- Java 21 JDK
- Maven
- Docker (for containerized deployment)
- Git (for version control)

### Installation

1. Clone the repository:
```bash
  git clone https://github.com/danjos91/intershop.git
  cd intershop
```

2. **Option A: Run with Docker (Recommended)**
```bash
# Build the Docker image
  docker buildx build --platform linux/amd64 -t intershop .

# Run the container
  docker run -p 8080:8080 intershop
```

3. **Option B: Run Locally**
```bash
# Start redis
  #docker run -d --name intershop-redis -p 6379:6379 redis:7-alpine
  #need to run docker desktop if this is windows
  docker run --name redis-server -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server & sleep 7 && redis-cli"
# Build the executable JAR
  mvn clean package

# Run the application
  mvn spring-boot:run
```

## 🌐 Access the Application

Once running, access the application at:
- **Local Development**: http://localhost:8080
- **Docker Container**: http://localhost:8080

## 🔧 Development

### Key Reactive Components

- **Controllers**: Use `@RestController` with reactive return types (`Mono<T>`, `Flux<T>`)
- **Services**: Implement reactive business logic using Project Reactor
- **Repositories**: Extend `ReactiveCrudRepository` for reactive database operations
- **Database**: R2DBC provides reactive database connectivity

### Database Schema

The application uses an in-memory H2 database with the following tables:
- `users` - User accounts and authentication
- `items` - Product catalog
- `orders` - Order management
- `order_items` - Order line items

## 🧪 Testing

Run the test suite:
```bash
  mvn test
```
