# Personalized Data API

A production-ready REST API service that provides personalised product shelves to eCommerce servers.

---

## Architecture

```
┌─────────────┐   POST /internal/**   ┌──────────────────────┐   ┌─────────┐
│  Data Team  │ ─────────────────────▶│                      │──▶│  MySQL  │
│ (INTERNAL)  │                       │  Spring Boot API     │   └─────────┘
└─────────────┘                       │                      │
                                      │                      │
┌─────────────┐   GET /api/external** │                      │
│  eCommerce  │ ─────────────────────▶│                      │
│  (EXTERNAL) │                       │                      │
└─────────────┘                       └──────────────────────┘
```

---

## Tech Stack

| Layer | Technology                              |
|---|-----------------------------------------|
| Language | Java 21                                 |
| Framework | Spring Boot 3.5.14                      |
| Security | Spring Security + JWT (JJWT 0.12.7)       |
| Database | MySQL 8.0                               |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI        |
| Tests | JUnit 5, Mockito, Spring Security Test  |

---

## Profiles

| Profile | Database | Cache | Purpose |
|---|---|---|---|
| `local` (default) | MySQL local | Simple (in-memory) | Local development |
| `prod` | MySQL prod | Simple (in-memory) | Production |

---

## Quick Start (Local)

### Prerequisites
- Java 21+
- Maven 3.9+ (or use included `./mvnw`)
- MySQL 8.0+ running locally

### Step 1 — Create MySQL database

```sql
CREATE DATABASE personalized_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### Step 2 — Configure credentials

Set environment variables (or use the defaults for local dev):

```bash
export DB_NAME=personalized_db
export DB_USERNAME=yourusername
export DB_PASSWORD=yourpassword
```

Or update `application-local.properties` directly for local dev.

### Step 3 — Run

```bash
./mvnw spring-boot:run

# App starts on:   http://localhost:8080
# Swagger UI:      http://localhost:8080/swagger-ui/index.html
```

Schema is created automatically on first startup via `schema.sql`.

### Step 4 — Run tests

```bash
# Run all tests (MySQL needed for tests)
./mvnw test

# Run tests + enforce 90% coverage
./mvnw verify

# Coverage report: target/site/jacoco/index.html
```

---

## Production Deployment

### Required Environment Variables

All secrets come from environment variables — nothing is hardcoded.


| Variable | Description                                   | Required |
|---|-----------------------------------------------|---|
| `DB_HOST` | MySQL host                                    | ✅ |
| `DB_PORT` | MySQL port (default: `3306`)                  | Optional |
| `DB_NAME` | Database name                                 | ✅ |
| `DB_USERNAME` | Database user                                 | ✅ |
| `DB_PASSWORD` | Database password                             | ✅ |
| `JWT_SECRET` | Base64-encoded 256-bit HMAC key               | ✅ |
| `JWT_EXPIRATION_MS` | Token TTL in ms (default: `900000` = 15 min ) | Optional |
| `SECURITY_USERNAME` | team username                            | ✅ |
| `SECURITY_PASSWORD` | team password                            | ✅ |

### Generate a secure JWT secret

```bash
openssl rand -base64 32
```

### Run with production profile

```bash
export DB_HOST=your-db-host
export DB_NAME=personalized_db
export DB_USERNAME=your-user
export DB_PASSWORD=your-password
export JWT_SECRET=$(openssl rand -base64 32)
export SECURITY_USERNAME=team username
export SECURITY_PASSWORD=your-strong-password

java -jar -Dspring.profiles.active=prod target/personalized-data-api-1.0.0.jar
```

---

## Authentication

The API uses a **single JWT token** to access all endpoints.

### Step 1 — Get a token

```bash
# team token (access to /internal/** & /api/**)
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Step 2 — Use the token on every request

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## API Reference

### POST /auth/token — Get JWT token

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

### POST /internal/product — Register product metadata

Requires **INTERNAL** token.

```bash
curl -X POST http://localhost:8080/internal/product \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "BB-2144746855",
    "category":  "Babies",
    "brand":     "Babyom"
  }'
```

| Field | Type | Required |
|---|---|---|
| `productId` | String | ✅ |
| `category` | String | ✅ |
| `brand` | String | ✅ |

Response: `204 No Content`

---

### POST /internal/shopper/shelf — Upload shopper shelf

Requires **INTERNAL** token.
All `productId` values must be registered first via `POST /internal/product`.
Replaces the entire existing shelf for the shopper atomically.

```bash
curl -X POST http://localhost:8080/internal/shopper/shelf \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shopperId": "S-1000",
    "shelf": [
      {"productId": "MB-2093193398", "relevancyScore": 31.089},
      {"productId": "BB-2144746855", "relevancyScore": 55.166},
      {"productId": "MD-543564697",  "relevancyScore": 73.014}
    ]
  }'
```

| Field | Type | Required |
|---|---|---|
| `shopperId` | String | ✅ |
| `shelf` | Array | ✅ min 1 item |
| `shelf[].productId` | String | ✅ must exist |
| `shelf[].relevancyScore` | Double | ✅ |

Response: `204 No Content`

---

### GET /api/products — Get personalised products

Requires **EXTERNAL** token.
Returns products sorted by `relevancyScore` descending.

```bash
# All products for a shopper
curl "http://localhost:8080/api/products?shopperId=S-1000" \
  -H "Authorization: Bearer $TOKEN"

# With category filter
curl "http://localhost:8080/api/products?shopperId=S-1000&category=Babies" \
  -H "Authorization: Bearer $TOKEN"

# With all filters
curl "http://localhost:8080/api/products?shopperId=S-1000&category=Babies&brand=Babyom&limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

| Parameter | Type | Required | Default | Max |
|---|---|---|---|---|
| `shopperId` | String | ✅ | — | — |
| `category` | String | ❌ | — | — |
| `brand` | String | ❌ | — | — |
| `limit` | Integer | ❌ | 10 | 100 |

Response:
```json
[
  {"productId": "MD-543564697",  "category": "Medical", "brand": "MedBrand",    "relevancyScore": 73.014},
  {"productId": "BB-2144746855", "category": "Babies",  "brand": "Babyom",      "relevancyScore": 55.166},
  {"productId": "MB-2093193398", "category": "Mobiles", "brand": "MobileBrand", "relevancyScore": 31.089}
]
```

---

## Error Responses

All errors return RFC 7807 Problem Detail format:

| Status | Meaning |
|---|---|
| `400` | Validation failed — missing or blank required fields |
| `401` | Missing or invalid JWT token |
| `403` | Token does not have the required role for this endpoint |
| `422` | Business rule violation — e.g. productId not registered |
| `500` | Unexpected server error |

Example `401` response:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

---

## Useful SQL Queries (for review call)

```sql
-- All products
SELECT * FROM products;

-- Shelf for a shopper with metadata, sorted by relevancy
SELECT s.shopper_id, p.product_id, p.category, p.brand, s.relevancy_score
FROM shopper_shelf_items s
JOIN products p ON s.product_id = p.product_id
WHERE s.shopper_id = 'S-1000'
ORDER BY s.relevancy_score DESC;

-- Count products per category
SELECT category, COUNT(*) AS product_count
FROM products
GROUP BY category
ORDER BY product_count DESC;

-- Shoppers with most products on shelf
SELECT shopper_id, COUNT(*) AS shelf_size
FROM shopper_shelf_items
GROUP BY shopper_id
ORDER BY shelf_size DESC;
```

---

## Project Structure

```
src/main/java/com/personalized/api/
├── config/
│   ├── OpenApiConfig.java          # Swagger UI setup
│   ├── SecurityConfig.java         # Route rules, UserDetailsService
├── controller/
│   ├── AuthController.java         # POST /auth/token
│   ├── ExternalController.java     # GET /api/external/products
│   └── InternalController.java     # POST /internal/**
├── entity/
│   ├── Product.java
│   └── ShopperShelf.java
├── exception/
│   └── GlobalExceptionHandler.java
├── model/
│   ├── AuthRequest.java
│   ├── ProductMetadataRequest.java
│   ├── ProductResponse.java
│   └── ShopperShelfRequest.java
├── repository/
│   ├── ProductRepository.java
│   └── ShopperShelfRepository.java
├── security/
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
└── service/
    ├── ExternalQueryService.java
    ├── JwtService.java
    └── InternalDataService.java
```
