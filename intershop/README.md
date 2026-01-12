# Intershop - Reactive E-Commerce Platform

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![R2DBC](https://img.shields.io/badge/R2DBC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-blue?style=for-the-badge)
![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=oauth&logoColor=white)

## 📖 About

Intershop is a modern, reactive e-commerce platform built with Spring WebFlux that provides a scalable solution for online shopping. The application features a complete product catalog, shopping cart functionality, order management, and user authentication through OAuth2 with Keycloak. Built on reactive programming principles, Intershop delivers high performance and efficient resource utilization, making it ideal for handling concurrent user requests and high-traffic scenarios.

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

**Prerequisites:**
- Docker and Docker Compose installed
- Java 21 JDK
- Maven

**Step 1: Start Keycloak and Redis**
```bash
# Start Keycloak and Redis services
  docker-compose up -d

# Wait for Keycloak to be ready (optional check)
  curl http://localhost:8082/realms/master
```

**Step 2: Configure Keycloak**
1. Open http://localhost:8082/admin in your browser
2. Login with admin/admin123
3. Follow the detailed setup in `KEYCLOAK_SETUP.md` to:
   - Create the `intershop` realm
   - Configure clients and users
   - Set up OAuth2 scopes

**Step 3: Start Redis (if not using docker-compose)**
```bash
# Alternative: Start Redis separately
  docker run --name redis-server -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server & sleep 7 && redis-cli"
```

**Step 4: Build and Run the Application**
```bash
# Build the executable JAR
  mvn clean package

# Run the application
  mvn spring-boot:run
```

**Note:** The application will be available at http://localhost:8080 and will redirect to Keycloak for authentication.

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
