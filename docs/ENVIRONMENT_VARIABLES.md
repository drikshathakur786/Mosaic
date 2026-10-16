# Mosaic — Environment Variables Reference

This document outlines all environment variables utilized across the Mosaic backend and infrastructure services, their default values, and production recommendations.

---

## Backend Service Configuration

| Variable | Default (Dev) | Description | Required in Production |
|---|---|---|:---:|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev`, `test`, `prod`) | Yes |
| `SERVER_PORT` | `8080` | HTTP port on which the backend listens | No |
| `POSTGRES_HOST` | `localhost` (or `postgres` in Docker) | Hostname/IP of the PostgreSQL database | Yes |
| `POSTGRES_PORT` | `5432` | Port of the PostgreSQL instance | No |
| `POSTGRES_DB` | `mosaic` | Database name | Yes |
| `POSTGRES_USER` | `mosaic` | Database user | Yes |
| `POSTGRES_PASSWORD` | `mosaic_dev_password` | Database password | **Yes (Critical)** |
| `JWT_SECRET` | *(Random development string)* | 256+ bit secret key for signing HMAC-SHA256 JWT tokens | **Yes (Critical)** |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Comma-separated list of allowed origins for browser clients | Yes |

---

## Security Guidelines

1. **Secret Management**:
   - `JWT_SECRET` must be a high-entropy string of at least 32 bytes (256 bits).
   - In production, never commit `.env` files to source control. Use environment managers (e.g. AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets).

2. **Database Passwords**:
   - The default development password `mosaic_dev_password` is strictly for local containerized development and must never be used in publicly exposed environments.

---

## Local Development `.env` Example

Create a `.env` file in the root directory if overriding default container parameters:

```env
POSTGRES_DB=mosaic
POSTGRES_USER=mosaic
POSTGRES_PASSWORD=mosaic_dev_password
POSTGRES_PORT=5432
JWT_SECRET=super-secret-local-jwt-key-minimum-256-bits-length-value-12345
SPRING_PROFILES_ACTIVE=dev
```
