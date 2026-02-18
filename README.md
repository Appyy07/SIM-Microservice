# SIM Microservice

A Spring Boot microservice that provides seamless integration between REST and SOAP protocols for SIM card management, with configuration-driven backend routing and data minimization.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)


## Overview

This microservice solves the integration challenge between modern REST-based clients and legacy SOAP-based backend systems. It acts as a protocol translation layer that accepts requests in either REST or SOAP format, stores complete records in a database, and forwards minimal data to configured backend systems using the appropriate protocol.


## Features

### Protocol Support
- REST to REST: Client sends JSON, backend receives JSON
- REST to SOAP: Client sends JSON, backend receives SOAP XML
- SOAP to REST: Client sends SOAP XML, backend receives JSON
- SOAP to SOAP: Client sends SOAP XML, backend receives SOAP XML

### API Types
- RESTful HTTP APIs with JSON
- SOAP Web Services with auto-generated WSDL
- Swagger UI for REST API documentation
- H2 Console for database inspection

### Security and Compliance
- Input validation for all requests
- Unique constraint enforcement on SIM IDs
- Complete audit trail with timestamps
- Minimal data exposure to backends

---

## Architecture

### High-Level Flow

```
Client Layer (REST or SOAP)
          |
          v
Northbound Controllers
  - SimRestController
  - SimSoapEndpoint
          |
          v
Service Layer
  - SimService (Validation, Business Logic)
          |
          v
    +-----+-----+
    |     |     |
    v     v     v
Database  Protocol  Southbound
Layer     Converter Service
(H2/      (REST     (Routing)
Postgres) <-> SOAP)
                |
                v
        Backend Systems
```

### Request Processing Flow

```
1. Client sends request (REST or SOAP)
2. Controller receives and extracts parameters
3. Service validates input data
4. Service checks for duplicate SIM ID
5. Service saves complete record to database
6. Service creates minimal payload (simId + plan only)
7. Southbound service looks up endpoint configuration
8. Protocol converter transforms format if needed
9. HTTP client calls backend system
10. Response returned to client
```

---

## Technology Stack

### Core Framework
- Java 17
- Spring Boot 3.3.5
- Maven 3.8+

### REST Implementation
- Spring Web MVC
- Jackson for JSON serialization
- Springdoc OpenAPI for Swagger documentation

### SOAP Implementation
- Spring Web Services
- WSDL4J for WSDL generation
- XSD-based contract-first design

### Data Layer
- Spring Data JPA
- Hibernate ORM
- H2 Database (development)

### Additional Components
- Spring WebFlux (reactive HTTP client)
- Lombok (code generation)
- SLF4J with Logback (logging)
- Spring Boot Actuator (health checks)

---

## Getting Started

### Prerequisites

```
Java 17 or higher
Maven 3.8 or higher
```

### Build and Run

```bash
# Clone the repository
git clone https://github.com/Appyy07/SIM-Microservice.git
cd sim-microservice

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

```

### Access Points

```
Swagger UI:     http://localhost:8080/swagger-ui.html
H2 Console:     http://localhost:8080/h2-console
                (URL: jdbc:h2:mem:simdb, User: sa, Password: empty)
WSDL:           http://localhost:8080/ws/sim.wsdl
Health Check:   http://localhost:8080/actuator/health
```

---

## API Endpoints

### REST Endpoints

| Endpoint | Method | Description | Query Parameter |
|----------|--------|-------------|-----------------|
| `/api/v1/sim/rest-to-rest` | POST | Create SIM (JSON in/out) | endpointId |
| `/api/v1/sim/rest-to-soap` | POST | Create SIM (JSON in, XML out) | endpointId |
| `/api/v1/sim/soap-to-rest` | POST | Create SIM (XML in, JSON out) | endpointId |
| `/api/v1/sim/{simId}` | GET | Retrieve SIM by ID | - |
| `/api/v1/sim/all` | GET | Retrieve all SIMs | - |
| `/api/v1/sim/{simId}` | DELETE | Delete SIM by ID | - |
| `/api/v1/sim/health` | GET | Health check | - |

### SOAP Operations

| Operation | Namespace | Description |
|-----------|-----------|-------------|
| SimActivation | http://example.com/sim | Activate a SIM card |
| GetSim | http://example.com/sim | Retrieve SIM details |

### Request Example (REST to REST)

```bash
POST /api/v1/sim/rest-to-rest?endpointId=backend1
Content-Type: application/json

{
  "msisdn": "919876543210",
  "simId": "SIM001",
  "endpoint": "backend1",
  "plan": "PREPAID_UNLIMITED",
  "operator": "Airtel",
  "allowances": {
    "dataAllowance": "50GB",
    "smsAllowance": "100/day",
    "voiceAllowance": "Unlimited"
  }
}
```

### Response Example

```json
{
  "status": "success",
  "message": "SIM activated successfully",
  "data": {
    "id": 1,
    "msisdn": "919876543210",
    "simId": "SIM001",
    "endpoint": "backend1",
    "plan": "PREPAID_UNLIMITED",
    "operator": "Airtel",
    "status": "ACTIVE",
    "allowances": {
      "dataAllowance": "50GB",
      "smsAllowance": "100/day",
      "voiceAllowance": "Unlimited"
    },
    "createdAt": "2026-02-17T12:00:00",
    "updatedAt": "2026-02-17T12:00:00"
  },
  "timestamp": "2026-02-17T12:00:00"
}
```

### Error Response Example

```json
{
  "status": 400,
  "message": "Invalid Request",
  "details": "SIM ID already exists: SIM001",
  "timestamp": "2026-02-17T12:00:00",
  "path": "/api/v1/sim/rest-to-rest"
}
```

---

## Configuration

### Southbound Backend Configuration

All backend endpoints are configured in `application.yml`:

```yaml
southbound:
  endpoints:
    backend1:
      url: ${BACKEND1_URL:http://httpbin.org/post}
      protocol: ${BACKEND1_PROTOCOL:REST}
      timeout: ${BACKEND1_TIMEOUT:5000}
      enabled: ${BACKEND1_ENABLED:true}
    
    backend2:
      url: ${BACKEND2_URL:http://httpbin.org/post}
      protocol: ${BACKEND2_PROTOCOL:SOAP}
      timeout: ${BACKEND2_TIMEOUT:5000}
      enabled: ${BACKEND2_ENABLED:true}
```

### Environment Variable Override

All configuration values can be overridden using environment variables:

```bash
export BACKEND1_URL=https://production-billing.company.com/api
export BACKEND1_PROTOCOL=REST
export BACKEND2_URL=https://production-crm.company.com/soap
export BACKEND2_PROTOCOL=SOAP

mvn spring-boot:run
```

### Database Configuration

Development (H2 in-memory):
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:simdb
    username: sa
    password:
```

Production wil use PostgreSQL

---

## Database Schema

### sim_records Table

```sql
CREATE TABLE sim_records (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    msisdn            VARCHAR(15) NOT NULL UNIQUE,
    sim_id            VARCHAR(50) NOT NULL UNIQUE,
    endpoint          VARCHAR(50),
    plan              VARCHAR(100) NOT NULL,
    operator          VARCHAR(50),
    status            VARCHAR(20),
    data_allowance    VARCHAR(50),
    sms_allowance     VARCHAR(50),
    voice_allowance   VARCHAR(50),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    
    INDEX idx_sim_id (sim_id),
    INDEX idx_msisdn (msisdn)
);
```

## Project Structure

```
src/main/java/com/example/sim/
├── SimMicroserviceApplication.java          Main application class
├── config/
│   ├── SouthboundConfig.java               Backend endpoint configuration
│   ├── WebClientConfig.java                HTTP client configuration
│   └── SoapConfig.java                     SOAP web service configuration
├── controller/
│   ├── SimRestController.java              REST API endpoints
│   └── GlobalExceptionHandler.java         Centralized error handling
├── dto/
│   ├── ApiResponse.java                    Success response wrapper
│   └── ErrorResponse.java                  Error response format
├── model/
│   ├── SimRecord.java                      Database entity
│   └── SouthboundRequest.java              Minimal backend payload DTO
├── repository/
│   └── SimRepository.java                  Database access layer
├── service/
│   ├── SimService.java                     Business logic
│   └── SouthboundService.java              Backend communication
├── soap/
│   ├── SimSoapEndpoint.java                SOAP operation handler
│   └── generated/                          Auto-generated from XSD
│       ├── SimActivationRequest.java
│       ├── SimActivationResponse.java
│       ├── GetSimRequest.java
│       ├── GetSimResponse.java
│       └── Allowances.java
└── util/
    └── ProtocolConverter.java              REST/SOAP format converter

src/main/resources/
├── application.yml                         Main configuration
├── application-dev.yml                     Development profile
├── application-prod.yml                    Production profile
└── sim.xsd                                 SOAP contract definition
```

---

## Key Design Patterns

### Separation of Concerns
- **Controllers**: Handle HTTP/SOAP requests and responses only
- **Services**: Contain business logic and orchestration
- **Repositories**: Manage database operations
- **Utilities**: Provide cross-cutting functionality

### Exception Handling
Global exception handler converts all exceptions to appropriate HTTP status codes with standardized error response format.

### Data Transfer Objects
- `SimRecord`: Complete entity for database and client
- `SouthboundRequest`: Minimal DTO with only simId and plan for backend

---

---

## Logging

Application logs are available at:
- Console output (default)

Log levels can be adjusted in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.example.sim: DEBUG
    com.example.sim.service.SouthboundService: TRACE
```


---

## References

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Web Services: https://spring.io/projects/spring-ws
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Swagger/OpenAPI: https://springdoc.org/