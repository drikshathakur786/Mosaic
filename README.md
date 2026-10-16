# Mosaic

> **Financial clarity for everyday life.**

Mosaic is a privacy-first personal finance clarity application for young adults worldwide. It helps users understand their everyday financial life — where money goes, what's recurring, who owes whom — without requiring direct bank access.

## What It Does

- **Import transactions** — manually or via CSV upload
- **Categorize spending** — automatic rules + user corrections
- **Track patterns** — detect recurring payments and subscriptions
- **Share expenses** — split costs with flatmates, partners, travel groups
- **Ask questions** — natural-language queries about your own financial data
- **Get clarity** — evidence-backed monthly financial stories

## What It Is NOT

Mosaic is not a bank, investment platform, lending app, trading tool, or financial advice service. It provides **insights**, not advice.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Maven |
| AI Service | Python 3.12+, FastAPI |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Infrastructure | Docker, Docker Compose |
| CI/CD | GitHub Actions |

## Project Structure

```
mosaic/
├── backend/          # Java Spring Boot API
├── ai-service/       # Python FastAPI AI service
├── frontend/         # React + TypeScript SPA
├── database/         # Seed data and sample CSVs
├── docs/             # Architecture, PRD, API reference
├── scripts/          # Dev and deployment scripts
└── docker-compose.yml
```

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local backend development)
- Node.js 20+ (for local frontend development)
- Python 3.12+ (for local AI service development)

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/your-username/mosaic.git
cd mosaic

# Start all services
docker compose up --build

# Backend API:    http://localhost:8080
# Swagger UI:     http://localhost:8080/swagger-ui.html
# Frontend:       http://localhost:5173
# AI Service:     http://localhost:8000
```

### Run Backend Locally (Development)

```bash
# Start only the database
docker compose up postgres -d

# Run the backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Environment Variables

See [docs/ENVIRONMENT_VARIABLES.md](docs/ENVIRONMENT_VARIABLES.md) for the full list.

Key variables:

| Variable | Description | Default |
|---|---|---|
| `POSTGRES_HOST` | Database host | `localhost` |
| `POSTGRES_PORT` | Database port | `5432` |
| `POSTGRES_DB` | Database name | `mosaic` |
| `POSTGRES_USER` | Database user | `mosaic` |
| `POSTGRES_PASSWORD` | Database password | *(required)* |
| `JWT_SECRET` | JWT signing secret | *(required)* |

## API Documentation

Once the backend is running, visit:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Testing

```bash
# Backend tests
cd backend && ./mvnw test

# AI service tests
cd ai-service && pytest

# Frontend tests
cd frontend && npm test
```

## Documentation

- [Product Requirements](docs/PRD.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Database Schema](docs/DATABASE_SCHEMA.md)
- [API Reference](docs/API_REFERENCE.md)
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)

## License

This project is for educational purposes.

## Status

🚧 **Under active development** — Phase 1: Backend Foundation
