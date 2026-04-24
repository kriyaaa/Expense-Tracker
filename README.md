# Expense Tracker

A full-stack personal expense tracker built with **Java Spring Boot** (backend) and **React + TypeScript + Vite** (frontend).

---

## Tech Stack

| Layer     | Technology                         |
|-----------|------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database  | MySQL 8+ (local) / Postgres (Render) |
| Frontend  | React 18, TypeScript, Vite, Axios  |
| Testing   | JUnit 5, Spring MockMvc, H2 (test) |

---

## Persistence Choice: MySQL via JPA

MySQL was chosen for the following reasons:

- **Production-grade**: handles concurrent writes without data loss, unlike an in-memory store or JSON file.
- **Reliable decimal arithmetic**: the `amount` column uses `DECIMAL(12,2)`, so ₹0.10 + ₹0.20 = ₹0.30 exactly — no floating-point drift.
- **Query flexibility**: JPA/JPQL lets us filter and sort cleanly without loading all rows into memory.
- **Schema evolution**: `spring.jpa.hibernate.ddl-auto=update` keeps the schema in sync during development; this would be replaced with a migration tool (Flyway/Liquibase) before production.

---

## Project Structure

```
expense-tracker/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/expensetracker/
│       │   ├── ExpenseTrackerApplication.java
│       │   ├── config/CorsConfig.java
│       │   ├── controller/ExpenseController.java
│       │   ├── dto/  (ApiResponse, CreateExpenseRequest, ExpenseResponse)
│       │   ├── exception/ (GlobalExceptionHandler, ExpenseNotFoundException)
│       │   ├── model/Expense.java
│       │   ├── repository/ExpenseRepository.java
│       │   └── service/ExpenseService.java
│       └── test/java/com/expensetracker/
│           └── ExpenseControllerIntegrationTest.java
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── index.css
        ├── components/
        │   ├── ExpenseForm.tsx
        │   └── ExpenseList.tsx
        ├── services/api.ts
        └── types/index.ts
```

---

## Setup & Running

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+ running locally
- Node.js 18+

---

### 1. MySQL Setup

```sql
CREATE DATABASE expense_tracker;
-- The table is auto-created by Hibernate on first run.
```

Update credentials in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=your_password_here
```

---

### 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts at **http://localhost:8080**.

#### Database profiles

- Default: `local` (H2 file DB), no MySQL required.
- MySQL: `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`
- Postgres: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres` (set `DATABASE_URL` or `SPRING_DATASOURCE_*`)

#### Run tests

```bash
./mvnw test
```

Tests use an H2 in-memory database — no MySQL required.

---

### 3. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI starts at **http://localhost:5173** and proxies `/expenses` to the Spring Boot backend automatically.

---

## API Reference

### POST /expenses

Create a new expense.

**Request body:**
```json
{
  "amount": 250.50,
  "category": "Food & Drink",
  "description": "Lunch at Swiggy",
  "date": "2024-06-15"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Expense created successfully",
  "data": {
    "id": 1,
    "amount": 250.50,
    "category": "Food & Drink",
    "description": "Lunch at Swiggy",
    "date": "2024-06-15",
    "createdAt": "2024-06-15T12:34:56"
  }
}
```

---

### GET /expenses

Returns all expenses, newest first.

**Query params (all optional):**

| Param      | Description                       | Example                     |
|------------|-----------------------------------|-----------------------------|
| `category` | Filter by category (case-insensitive) | `?category=Transport`   |
| `sort`     | Sort order (only `date_desc` supported) | `?sort=date_desc`      |

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [ ... ]
}
```

---

### DELETE /expenses/{id}

Delete an expense.

**Response `204 No Content`** on success.
**Response `404 Not Found`** if id does not exist.

---

## Design Decisions

### Why BigDecimal for amounts?
`float`/`double` cannot represent many decimal values exactly (e.g., 0.1 + 0.2 ≠ 0.3). Money must be exact. `BigDecimal` with `DECIMAL(12,2)` in MySQL guarantees correct arithmetic.

### Why a service layer?
Keeping business logic in `ExpenseService` (not the controller) means we can unit-test the logic without HTTP, swap the storage layer, and keep controllers thin.

### Why H2 for tests?
Integration tests use an H2 in-memory database so no MySQL instance is needed in CI. The test profile (`src/test/resources/application.properties`) swaps the datasource automatically.

### Duplicate-submit safety
The "Add Expense" button is disabled once a request is in-flight (`submitting` state). On the backend, each POST creates exactly one record — there is no idempotency key, so the UI guard is the primary defence.

### CORS
`CorsConfig.java` explicitly allows only known dev origins. Before deploying, replace the `allowedOriginPatterns` list with the actual production domain.

---

## Timebox Notes

### Key design decisions (summary)
- Use a layered backend (controller -> service -> repository) with DTOs to keep HTTP concerns separate from business logic.
- Use `BigDecimal`/`DECIMAL(12,2)` for money to avoid floating point drift.
- Use MySQL for real usage and H2 for tests/CI to keep the developer workflow lightweight.

### Trade-offs due to the timebox
- Kept the API intentionally small (create/list/delete only) to focus on correctness and a smooth end-to-end flow.
- Limited querying (category filter + newest-first ordering) instead of building out full search, pagination, and multiple sort modes.
- No DB migrations yet (using Hibernate `ddl-auto` for dev); a real deployment should use Flyway/Liquibase.
- Duplicate-submit protection is primarily handled in the UI; the backend does not implement idempotency keys.

### Intentionally not done (out of scope)
- Authentication / multi-user accounts and per-user data isolation.
- Update/edit endpoints, recurring expenses, budgets, charts/analytics, exports.
- Production hardening (containerization, rate limiting, observability, structured audit logging, deployment configs).

---

## Deploy on Render

Render does not currently provide a native Java runtime, so the Spring Boot backend is deployed via Docker.

### Recommended: Deploy using the Blueprint (`render.yaml`)
1. Push the repo to GitHub.
2. In Render Dashboard: New -> Blueprint.
3. Select this repo and apply.
   - This creates a free Render Postgres database and the backend/frontend services.
   - The backend gets `DATABASE_URL` automatically from the database's internal connection string.

Render web services must bind to `0.0.0.0` and a port (default `PORT=10000`).

### 2. Database (pick one)
- Quick demo: keep `SPRING_PROFILES_ACTIVE=local` and accept that data is not durable on a Free web service (ephemeral filesystem + spin-down).
- Persistent MySQL on Render: deploy MySQL as a private service backed by a disk, then run the API with the `mysql` profile and point `spring.datasource.url` at the private hostname.
- Free-ish persistence: use Render Postgres (free databases expire after 30 days). This repo includes a `postgres` profile and `render.yaml` wiring for it.

### 3. Deploy the frontend (Static Site)
1. In Render Dashboard: New -> Static Site.
2. Root Directory: `frontend`
3. Build Command: `npm ci && npm run build`
4. Publish Directory: `dist`
5. Set env var `VITE_API_BASE_URL` to your backend URL (e.g. `https://<your-backend>.onrender.com`).
