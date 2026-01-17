# Resa Backend

Spring Boot + Postgres backend with JWT auth, Google OAuth2, Flyway migrations, and RBAC (OWNER/SELLER/CUSTOMER).

## Prereqs
- Java 17
- Maven
- Postgres (or Docker)

## Quick Start (Local)
1) Start Postgres (Docker option):
```
docker compose -f /home/skywalker/Projects/resa_backend/docker-compose.yml up postgres
```

2) Set required environment variables:
```
export OWNER_EMAIL="owner@example.com"
export OWNER_PASSWORD="change-me-strong"
export JWT_SECRET="a-very-long-secret-at-least-32-chars"
```

Optional Google login:
```
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

3) Run the API:
```
mvn -f /home/skywalker/Projects/resa_backend/pom.xml spring-boot:run
```

## Role Basics
- OWNER: approves sellers, full access to orders/customers
- SELLER: manages their own products (must be verified by OWNER)
- CUSTOMER: can place orders

## Key Endpoints
- Auth
  - `POST /api/auth/register`
  - `POST /api/auth/register-seller`
  - `POST /api/auth/login`
- Owner
  - `GET /api/admin/sellers`
  - `POST /api/admin/sellers/{id}/approve`
- Seller
  - `GET /api/seller/products`
- Products
  - `GET /api/products`
  - `POST /api/products` (OWNER or verified SELLER)

## Notes
- Flyway migrations live in `src/main/resources/db/migration`.
- The initial OWNER user is created on startup from `OWNER_EMAIL`/`OWNER_PASSWORD`.
