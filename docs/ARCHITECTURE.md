# Mosaic — Architecture Document

## 1. System Overview

Mosaic follows a **modular monorepo** architecture with three primary services and a shared database.

```mermaid
graph TB
    subgraph "Client Layer"
        WEB["🌐 React Frontend<br/>(Vite + TypeScript)"]
        IOS["📱 iOS App<br/>(SwiftUI — Phase 2)"]
    end

    subgraph "API Gateway / Reverse Proxy"
        NGINX["Nginx / Traefik"]
    end

    subgraph "Application Layer"
        BACKEND["☕ Backend Service<br/>Java 21 + Spring Boot"]
        AI["🤖 AI Service<br/>Python 3.12 + FastAPI"]
    end

    subgraph "Data Layer"
        PG[("🐘 PostgreSQL 16")]
        REDIS[("⚡ Redis<br/>(Phase 2)")]
        S3["📦 S3-Compatible Storage<br/>(Phase 2)"]
    end

    subgraph "External Services"
        LLM["🧠 LLM Provider<br/>(OpenAI / Anthropic / Local)"]
    end

    subgraph "Observability"
        OTEL["OpenTelemetry Collector"]
        PROM["Prometheus"]
        GRAF["Grafana"]
    end

    WEB -->|HTTPS| NGINX
    IOS -->|HTTPS| NGINX
    NGINX -->|/api/v1/*| BACKEND
    NGINX -->|/ai/v1/*| AI
    NGINX -->|Static files| WEB

    BACKEND -->|JDBC| PG
    BACKEND -->|Internal HTTP| AI
    AI -->|SQLAlchemy Read-Only| PG
    AI -->|API Call| LLM

    BACKEND --> OTEL
    AI --> OTEL
    OTEL --> PROM
    PROM --> GRAF
```

## 2. Service Architecture

### 2.1 Backend Service (Java / Spring Boot)

The backend is the **single source of truth** for all business logic, financial calculations, and data integrity.

```mermaid
graph LR
    subgraph "Spring Boot Backend"
        direction TB
        CTRL["Controllers<br/>(REST API)"]
        DTO["DTOs<br/>(Request/Response)"]
        SVC["Services<br/>(Business Logic)"]
        REPO["Repositories<br/>(Data Access)"]
        ENT["Entities<br/>(JPA)"]
        SEC["Security<br/>(JWT + AuthZ)"]
        MIG["Migrations<br/>(Flyway)"]
        VAL["Validation<br/>(Bean Validation)"]
    end

    CTRL --> DTO
    CTRL --> SVC
    SVC --> REPO
    REPO --> ENT
    CTRL --> SEC
    CTRL --> VAL
```

**Package Structure:**

```
com.mosaic
├── config/              # Spring configuration, security, CORS
├── common/
│   ├── dto/             # Shared DTOs (ErrorResponse, PageResponse)
│   ├── exception/       # Custom exceptions + global handler
│   ├── model/           # Shared value objects (Money, Currency)
│   └── util/            # Date, money, string utilities
├── auth/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   └── repository/
├── transaction/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   ├── repository/
│   └── importer/        # CSV import logic
├── category/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   └── repository/
├── categorization/
│   ├── service/          # Rule engine, merchant alias matcher
│   ├── entity/
│   └── repository/
├── recurring/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   └── repository/
├── group/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   └── repository/
├── savings/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   └── repository/
├── dashboard/
│   ├── controller/
│   ├── dto/
│   └── service/
├── insight/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   └── entity/
├── ai/
│   ├── controller/       # Proxy to AI service
│   ├── dto/
│   └── client/           # HTTP client for AI service
└── audit/
    ├── entity/
    ├── repository/
    └── service/
```

### 2.2 AI Service (Python / FastAPI)

The AI service handles **intent classification, LLM orchestration, and explanation generation**. It does NOT own financial calculations.

```mermaid
graph LR
    subgraph "FastAPI AI Service"
        direction TB
        ROUTES["API Routes"]
        INTENT["Intent Classifier"]
        QUERY["Query Builder<br/>(Safe Templates)"]
        DATA["Data Fetcher<br/>(Read-Only SQL)"]
        LLM_ABS["LLM Provider<br/>Abstraction"]
        RESP["Response Builder<br/>(Evidence + Metadata)"]
        GUARD["Safety Guards<br/>(Injection, PII, Limits)"]
    end

    ROUTES --> GUARD
    GUARD --> INTENT
    INTENT --> QUERY
    QUERY --> DATA
    DATA --> RESP
    RESP --> LLM_ABS
    LLM_ABS --> RESP
```

**Package Structure:**

```
ai_service/
├── main.py                # FastAPI app
├── config.py              # Settings
├── api/
│   ├── routes.py          # API endpoints
│   └── models.py          # Pydantic request/response models
├── intent/
│   ├── classifier.py      # Intent classification
│   └── intents.py         # Intent definitions
├── query/
│   ├── builder.py         # Safe query builder
│   └── templates.py       # Query templates
├── data/
│   ├── fetcher.py         # Read-only data access
│   └── connection.py      # DB connection (read-only)
├── llm/
│   ├── provider.py        # Abstract LLM provider
│   ├── openai_provider.py # OpenAI implementation
│   ├── mock_provider.py   # Mock for testing
│   └── prompts.py         # System prompts
├── safety/
│   ├── guards.py          # Input/output validation
│   ├── pii_filter.py      # PII detection/redaction
│   └── injection.py       # Prompt injection detection
├── evaluation/
│   ├── dataset.py         # Evaluation dataset
│   └── runner.py          # Evaluation runner
└── tests/
```

### 2.3 Frontend (React + TypeScript)

```mermaid
graph LR
    subgraph "React Frontend"
        direction TB
        PAGES["Pages / Routes"]
        COMP["Components"]
        HOOKS["Custom Hooks"]
        API_CL["API Client<br/>(TanStack Query)"]
        STORE["State Management<br/>(Context / Zustand)"]
        DESIGN["Design System<br/>(Tailwind CSS)"]
    end

    PAGES --> COMP
    PAGES --> HOOKS
    HOOKS --> API_CL
    HOOKS --> STORE
    COMP --> DESIGN
```

## 3. Data Flow Architecture

### 3.1 Transaction Import Flow

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend
    participant BE as Backend
    participant DB as PostgreSQL

    U->>FE: Upload CSV file
    FE->>BE: POST /api/v1/imports/upload
    BE->>BE: Validate file (size, type)
    BE->>BE: Parse CSV headers
    BE-->>FE: Return columns for mapping

    U->>FE: Map columns + select options
    FE->>BE: POST /api/v1/imports/preview
    BE->>BE: Parse rows with mapping
    BE->>BE: Validate each row
    BE->>BE: Detect duplicates
    BE-->>FE: Return preview (valid/invalid rows)

    U->>FE: Confirm import
    FE->>BE: POST /api/v1/imports/{id}/confirm
    BE->>BE: Categorize transactions
    BE->>DB: INSERT transactions (batch)
    BE->>DB: UPDATE import status
    BE-->>FE: Import complete (summary)
```

### 3.2 AI Query Flow

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend
    participant BE as Backend
    participant AI as AI Service
    participant DB as PostgreSQL
    participant LLM as LLM Provider

    U->>FE: "How much did I spend on food?"
    FE->>BE: POST /api/v1/ai/query
    BE->>BE: Authenticate + authorize
    BE->>AI: Forward query + user context

    AI->>AI: Safety guard check
    AI->>AI: Classify intent
    AI->>AI: Build safe query
    AI->>DB: Execute read-only query
    DB-->>AI: Raw results
    AI->>AI: Compute deterministic answer
    AI->>LLM: Request explanation
    LLM-->>AI: Natural language explanation
    AI->>AI: Validate output (PII, safety)
    AI-->>BE: Structured response + evidence

    BE->>DB: Log audit entry
    BE-->>FE: Response with answer + evidence
    FE-->>U: Display answer with sources
```

### 3.3 Shared Expense Flow

```mermaid
sequenceDiagram
    participant U as User (Creator)
    participant FE as Frontend
    participant BE as Backend
    participant DB as PostgreSQL

    U->>FE: Create group "Berlin Trip"
    FE->>BE: POST /api/v1/groups
    BE->>DB: INSERT group + creator as member
    BE-->>FE: Group created

    U->>FE: Add expense: Dinner €120
    FE->>BE: POST /api/v1/groups/{id}/expenses
    Note over BE: Split: Equal among 4 members<br/>€30.00 each
    BE->>BE: Calculate splits (deterministic)
    BE->>DB: INSERT expense + splits
    BE-->>FE: Expense with splits

    U->>FE: View balances
    FE->>BE: GET /api/v1/groups/{id}/balances
    BE->>BE: Compute net balances
    BE-->>FE: Who owes whom
```

## 4. Infrastructure Architecture

### 4.1 Local Development (Docker Compose)

```mermaid
graph TB
    subgraph "Docker Compose"
        direction TB
        FE_C["frontend<br/>Node 20 + Vite<br/>:5173"]
        BE_C["backend<br/>Java 21 + Spring Boot<br/>:8080"]
        AI_C["ai-service<br/>Python 3.12 + FastAPI<br/>:8000"]
        PG_C["postgres<br/>PostgreSQL 16<br/>:5432"]
    end

    FE_C -->|proxy /api| BE_C
    FE_C -->|proxy /ai| AI_C
    BE_C --> PG_C
    AI_C -->|read-only| PG_C
```

### 4.2 Production Architecture (Future)

```mermaid
graph TB
    subgraph "CDN"
        CF["CloudFront / Cloudflare"]
    end

    subgraph "Load Balancer"
        ALB["Application LB"]
    end

    subgraph "Compute"
        BE1["Backend Instance 1"]
        BE2["Backend Instance 2"]
        AI1["AI Service Instance 1"]
    end

    subgraph "Data"
        PG_P["PostgreSQL<br/>(Primary)"]
        PG_R["PostgreSQL<br/>(Read Replica)"]
        REDIS_P["Redis"]
        S3_P["S3 Bucket"]
    end

    CF --> ALB
    ALB --> BE1
    ALB --> BE2
    ALB --> AI1

    BE1 --> PG_P
    BE2 --> PG_P
    AI1 --> PG_R
    BE1 --> REDIS_P
    BE1 --> S3_P
```

## 5. Security Architecture

```mermaid
graph TB
    subgraph "Security Layers"
        direction TB
        L1["Layer 1: Network<br/>HTTPS, CORS, Rate Limiting"]
        L2["Layer 2: Authentication<br/>JWT, Token Rotation"]
        L3["Layer 3: Authorization<br/>Per-Resource Ownership Check"]
        L4["Layer 4: Input Validation<br/>Bean Validation, File Checks"]
        L5["Layer 5: Data Protection<br/>Encryption, PII Redaction"]
        L6["Layer 6: AI Safety<br/>Prompt Guards, Read-Only DB, Audit"]
        L7["Layer 7: Audit<br/>Logging, Monitoring, Alerting"]
    end

    L1 --> L2 --> L3 --> L4 --> L5 --> L6 --> L7
```

### Key Security Decisions

| Decision | Approach |
|---|---|
| Password storage | bcrypt with cost factor 12 |
| Authentication | JWT (short-lived access + refresh tokens) |
| Authorization | Ownership check on every repository query |
| Money storage | BIGINT minor units — never float/double |
| AI database access | Separate read-only connection |
| AI queries | Intent router + safe templates — no arbitrary SQL |
| File uploads | Size limit, type whitelist, content scanning |
| Secrets | Environment variables — never in source code |
| Audit | Structured logs + audit_logs table |

## 6. Key Architecture Decisions (ADRs)

### ADR-001: Monorepo Structure
**Decision:** Use a single Git repository with separate service directories.
**Rationale:** Simplifies dependency management, CI/CD, and cross-service refactoring for a small team. Can split later if team grows.

### ADR-002: Separate AI Service
**Decision:** Run AI/LLM logic as a separate Python FastAPI service, not embedded in the Java backend.
**Rationale:** Python has the strongest ecosystem for ML/NLP. Separation allows independent scaling, language-optimal tooling, and clear security boundary (read-only DB access).

### ADR-003: Money as Integer Minor Units
**Decision:** Store all monetary values as `BIGINT` representing the smallest currency unit (cents, paise, etc.).
**Rationale:** Floating-point arithmetic causes rounding errors in financial calculations. Integer arithmetic is deterministic and exact.

### ADR-004: Backend Owns All Financial Calculations
**Decision:** The backend service performs all financial computations. The AI service only explains pre-computed results.
**Rationale:** LLMs are non-deterministic. Financial calculations must be reproducible and auditable. The AI service receives computed data and generates natural-language explanations.

### ADR-005: Intent Router over Text-to-SQL
**Decision:** Use an intent classification + safe query template approach instead of text-to-SQL.
**Rationale:** Text-to-SQL is a major security risk (SQL injection, data leakage). Intent routing limits queries to a known-safe set and is more predictable for an MVP.

### ADR-006: Flyway for Database Migrations
**Decision:** Use Flyway for versioned, repeatable database migrations.
**Rationale:** Flyway is well-integrated with Spring Boot, supports SQL-based migrations, and is simpler than Liquibase for our needs.

### ADR-007: JWT Authentication
**Decision:** Use stateless JWT for API authentication with short-lived access tokens and longer-lived refresh tokens.
**Rationale:** Stateless auth simplifies horizontal scaling. Refresh tokens allow token rotation without frequent re-login. Token revocation handled via a denylist in Redis (Phase 2) or DB.

### ADR-008: LLM Provider Abstraction
**Decision:** Abstract LLM access behind a provider interface to avoid vendor lock-in.
**Rationale:** LLM market is evolving rapidly. We may need to switch providers for cost, performance, or privacy reasons. Mock provider enables testing without API costs.
