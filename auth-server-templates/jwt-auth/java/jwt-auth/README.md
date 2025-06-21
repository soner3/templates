# 🔐 JWT Auth Server Template for Single Page Applications

This project is a **starter template** for building secure authentication backends for **Single Page Applications (SPAs)** using **Spring Boot** and **Spring Security**. It is **not production-ready** out of the box — key management and database configuration must be adapted before production use.

---

## 🚀 Overview

- ⚠️ **Not production-ready** – intended as a foundation for custom implementations
- 🔐 Based on **OAuth2 Resource Server** using **custom JWTs**
- 📄 Includes **OpenAPI documentation** for all REST endpoints
- 🛢️ Uses **Spring Data JPA** and supports **pluggable relational databases**
- 💾 Comes with **H2** for development and testing convenience
- 🧪 Integration tests run with **PostgreSQL** (DB can be swapped easily)
- 📊 Integrated with **Spring Boot Actuator** and **Prometheus** for monitoring

---

## 🧱 Tech Stack

| Component           | Description                                      |
|---------------------|--------------------------------------------------|
| **Spring Boot**     | Application framework                            |
| **Spring Security** | Authentication, CSRF, JWT, OAuth2                |
| **JWT**             | Custom token generation and validation           |
| **Spring Data JPA** | Database abstraction                             |
| **H2 Database**     | In-memory DB for local development               |
| **PostgreSQL**      | Used in tests; production DB can be configured   |
| **OpenAPI**         | Swagger UI documentation                         |
| **Actuator**        | Health & metrics endpoints                       |
| **Prometheus**      | Metrics scraping (optional)                      |

---

## ❗ Important Notes

> This project is a **template**, not a production-ready service.

- ❌ No key management (e.g., keystore, rotation) is included
- ❌ No production-grade DB tuning or schema management
- ⚠️ Use this as a **foundation** to build your own secure backend

---

## 🧪 Testing Strategy

- Integration tests use **PostgreSQL** via a Spring profile
- No native SQL – JPA-based and **DB-agnostic**
- Easily switch DB for tests and development using Spring profiles

---

## 📚 API Documentation

OpenAPI/Swagger UI available after startup:  
👉 `http://localhost:8080/swagger-ui.html`

All endpoints are documented for quick integration and testing.

---

## 🔐 Security Design

- Implements **Spring Security OAuth2 Resource Server**
- Generates **custom JWTs** on successful login
- Validates tokens for secured endpoints
- CSRF protection is enabled and adapted for SPAs (e.g., React, Angular)
- Based on the official Spring docs:  
  [Spring OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html#oauth2-resource-server)

---

## 📈 Monitoring

- Exposes key endpoints via Spring Boot Actuator:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`
- **Prometheus-compatible**, ready for integration into monitoring systems

---

## 📂 Project Structure

```bash
com.sonastan.jwt_auth
├── application       # Application layer
├── domain            # JPA entities and repositories
├── infrastructure    # DB configuration, security, etc.
├── interfaces.rest   # REST controllers and DTOs
└── JwtAuthApplication.java
