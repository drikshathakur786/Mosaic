# Mosaic — API Reference

## API Design Conventions

| Convention | Value |
|---|---|
| Base URL | `/api/v1` |
| Content-Type | `application/json` |
| Authentication | `Authorization: Bearer <JWT>` |
| Pagination | `?page=0&size=20&sort=createdAt,desc` |
| Date format | ISO 8601 (`2024-03-15T10:30:00Z`) |
| Money | Integer minor units + ISO 4217 currency code |
| IDs | UUID v4 |
| Errors | Structured JSON (see below) |

### Standard Error Response

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "amountMinor",
      "message": "must be greater than 0"
    }
  ],
  "timestamp": "2024-03-15T10:30:00Z",
  "path": "/api/v1/transactions",
  "correlationId": "abc-123-def"
}
```

### Standard Page Response

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 156,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

---

## Phase 1 — Health & System

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/health` | No | Application health check |
| GET | `/ready` | No | Readiness check (DB connectivity) |
| GET | `/api/v1/info` | No | App version, build info |

---

## Phase 2 — Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | No | Create user account |
| POST | `/api/v1/auth/login` | No | Authenticate and get tokens |
| POST | `/api/v1/auth/refresh` | Token | Refresh access token |
| POST | `/api/v1/auth/logout` | Token | Revoke refresh token |
| GET | `/api/v1/auth/me` | Token | Get current user info |
| PATCH | `/api/v1/users/me/profile` | Token | Update user profile |
| PATCH | `/api/v1/users/me/preferences` | Token | Update user preferences |
| DELETE | `/api/v1/users/me` | Token | Delete account and all data |

### Register

```
POST /api/v1/auth/register

Request:
{
  "email": "alex@example.com",
  "password": "SecureP@ss123",
  "displayName": "Alex",
  "defaultCurrency": "USD",
  "countryCode": "US",
  "timezone": "America/New_York",
  "locale": "en-US"
}

Response: 201 Created
{
  "id": "uuid",
  "email": "alex@example.com",
  "displayName": "Alex",
  "createdAt": "2024-03-15T10:30:00Z"
}
```

### Login

```
POST /api/v1/auth/login

Request:
{
  "email": "alex@example.com",
  "password": "SecureP@ss123"
}

Response: 200 OK
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "alex@example.com",
    "displayName": "Alex"
  }
}
```

---

## Phase 3 — Transactions

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/transactions` | Token | Create transaction |
| GET | `/api/v1/transactions` | Token | List transactions (paginated, filtered) |
| GET | `/api/v1/transactions/{id}` | Token | Get transaction by ID |
| PATCH | `/api/v1/transactions/{id}` | Token | Update transaction |
| DELETE | `/api/v1/transactions/{id}` | Token | Soft-delete transaction |

### Create Transaction

```
POST /api/v1/transactions

Request:
{
  "amountMinor": 4299,
  "currency": "USD",
  "merchantName": "Whole Foods Market",
  "description": "Weekly groceries",
  "categoryId": "uuid-of-groceries-category",
  "transactionType": "EXPENSE",
  "transactionDate": "2024-03-15T10:30:00Z",
  "timezone": "America/New_York"
}

Response: 201 Created
{
  "id": "uuid",
  "amountMinor": 4299,
  "currency": "USD",
  "amountFormatted": "$42.99",
  "merchantName": "Whole Foods Market",
  "normalizedMerchantName": "whole foods",
  "description": "Weekly groceries",
  "category": {
    "id": "uuid",
    "name": "Groceries",
    "icon": "🛒",
    "color": "#4CAF50"
  },
  "transactionType": "EXPENSE",
  "transactionStatus": "ACTIVE",
  "transactionDate": "2024-03-15T10:30:00Z",
  "timezone": "America/New_York",
  "source": "MANUAL",
  "isReviewed": true,
  "createdAt": "2024-03-15T10:30:00Z"
}
```

### List Transactions (with filters)

```
GET /api/v1/transactions?page=0&size=20&sort=transactionDate,desc
    &fromDate=2024-03-01
    &toDate=2024-03-31
    &categoryId=uuid
    &merchantName=whole+foods
    &transactionType=EXPENSE
    &source=MANUAL
    &isReviewed=false

Response: 200 OK
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

---

## Phase 4 — CSV Import

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/imports/upload` | Token | Upload CSV file |
| GET | `/api/v1/imports/{id}` | Token | Get import details |
| POST | `/api/v1/imports/{id}/preview` | Token | Preview with column mapping |
| POST | `/api/v1/imports/{id}/confirm` | Token | Confirm and commit import |
| POST | `/api/v1/imports/{id}/cancel` | Token | Cancel import |
| GET | `/api/v1/imports` | Token | List import history |

### Upload CSV

```
POST /api/v1/imports/upload
Content-Type: multipart/form-data

Form fields:
  file: <CSV file>
  currency: "USD"
  dateFormat: "MM/dd/yyyy"

Response: 201 Created
{
  "id": "uuid",
  "filename": "march_transactions.csv",
  "status": "UPLOADED",
  "totalRows": 150,
  "columns": ["Date", "Description", "Amount", "Category"],
  "sampleRows": [ ... first 5 rows ... ]
}
```

### Preview Import

```
POST /api/v1/imports/{id}/preview

Request:
{
  "columnMapping": {
    "date": "Date",
    "merchantName": "Description",
    "amountMinor": "Amount",
    "category": "Category"
  },
  "dateFormat": "MM/dd/yyyy",
  "currency": "USD",
  "amountIsNegativeForExpense": true
}

Response: 200 OK
{
  "importId": "uuid",
  "totalRows": 150,
  "validRows": 142,
  "invalidRows": 5,
  "duplicateRows": 3,
  "preview": [
    {
      "rowNumber": 1,
      "status": "VALID",
      "parsed": {
        "merchantName": "Whole Foods",
        "amountMinor": 4299,
        "currency": "USD",
        "transactionDate": "2024-03-15T00:00:00Z",
        "transactionType": "EXPENSE"
      }
    },
    {
      "rowNumber": 7,
      "status": "INVALID",
      "error": "Invalid date format: '15-Mar-24'"
    },
    {
      "rowNumber": 12,
      "status": "DUPLICATE",
      "existingTransactionId": "uuid"
    }
  ]
}
```

---

## Phase 5 — Categories

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/categories` | Token | List all categories |
| POST | `/api/v1/categories` | Token | Create custom category |
| PATCH | `/api/v1/categories/{id}` | Token | Update category |
| GET | `/api/v1/merchant-aliases` | Token | List user's merchant aliases |
| POST | `/api/v1/merchant-aliases` | Token | Create merchant alias |
| PATCH | `/api/v1/merchant-aliases/{id}` | Token | Update alias |
| DELETE | `/api/v1/merchant-aliases/{id}` | Token | Delete alias |

---

## Phase 6 — Dashboard & Analytics

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/dashboard/summary` | Token | Financial snapshot |
| GET | `/api/v1/dashboard/spending-by-category` | Token | Category breakdown |
| GET | `/api/v1/dashboard/spending-by-merchant` | Token | Top merchants |
| GET | `/api/v1/dashboard/monthly-comparison` | Token | Month-over-month |
| GET | `/api/v1/dashboard/daily-trend` | Token | Daily spending trend |
| GET | `/api/v1/dashboard/largest-transactions` | Token | Top N by amount |

### Financial Summary

```
GET /api/v1/dashboard/summary?fromDate=2024-03-01&toDate=2024-03-31&currency=USD

Response: 200 OK
{
  "period": {
    "from": "2024-03-01",
    "to": "2024-03-31"
  },
  "currency": "USD",
  "totalIncome": 520000,
  "totalExpense": 345000,
  "netCashFlow": 175000,
  "transactionCount": 87,
  "unreviewed": 12,
  "currencyWarning": "3 transactions in EUR not included in totals"
}
```

---

## Phase 7 — Recurring Payments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/recurring` | Token | List detected recurring payments |
| PATCH | `/api/v1/recurring/{id}/confirm` | Token | Confirm as subscription |
| PATCH | `/api/v1/recurring/{id}/dismiss` | Token | Dismiss detection |
| GET | `/api/v1/subscriptions` | Token | List confirmed subscriptions |
| PATCH | `/api/v1/subscriptions/{id}` | Token | Update subscription details |

---

## Phase 8 — Shared Expenses

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/groups` | Token | Create group |
| GET | `/api/v1/groups` | Token | List user's groups |
| GET | `/api/v1/groups/{id}` | Token | Get group details |
| PATCH | `/api/v1/groups/{id}` | Token | Update group |
| POST | `/api/v1/groups/{id}/members` | Token | Add member |
| DELETE | `/api/v1/groups/{id}/members/{memberId}` | Token | Remove member |
| POST | `/api/v1/groups/{id}/expenses` | Token | Add shared expense |
| GET | `/api/v1/groups/{id}/expenses` | Token | List group expenses |
| DELETE | `/api/v1/groups/{id}/expenses/{expenseId}` | Token | Remove expense |
| GET | `/api/v1/groups/{id}/balances` | Token | Get who-owes-whom |
| POST | `/api/v1/groups/{id}/settlements` | Token | Record settlement |

### Group Balances

```
GET /api/v1/groups/{id}/balances

Response: 200 OK
{
  "groupId": "uuid",
  "groupName": "Berlin Trip",
  "currency": "EUR",
  "balances": [
    { "memberId": "uuid", "name": "Alex", "netBalance": 4500 },
    { "memberId": "uuid", "name": "Sam", "netBalance": -2000 },
    { "memberId": "uuid", "name": "Jordan", "netBalance": -2500 }
  ],
  "settlements": [
    { "from": "Sam", "to": "Alex", "amount": 2000, "currency": "EUR" },
    { "from": "Jordan", "to": "Alex", "amount": 2500, "currency": "EUR" }
  ]
}
```

---

## Phase 9 — AI Assistant

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/ai/query` | Token | Ask a financial question |
| GET | `/api/v1/ai/conversations` | Token | List conversations |
| GET | `/api/v1/ai/conversations/{id}` | Token | Get conversation history |
| DELETE | `/api/v1/ai/conversations/{id}` | Token | Delete conversation |

### AI Query

```
POST /api/v1/ai/query

Request:
{
  "conversationId": "uuid (optional)",
  "question": "How much did I spend on food this month?"
}

Response: 200 OK
{
  "conversationId": "uuid",
  "answer": "Based on your transactions from March 1–31, 2024, you spent $342.50 on food-related purchases across 23 transactions.",
  "structuredData": {
    "totalAmountMinor": 34250,
    "currency": "USD",
    "transactionCount": 23,
    "period": { "from": "2024-03-01", "to": "2024-03-31" },
    "topMerchants": [
      { "name": "Whole Foods", "amountMinor": 12500, "count": 4 },
      { "name": "Chipotle", "amountMinor": 8700, "count": 6 }
    ]
  },
  "evidence": [
    { "transactionId": "uuid", "merchant": "Whole Foods", "amount": 4299, "date": "2024-03-15" }
  ],
  "metadata": {
    "intent": "CATEGORY_TOTAL",
    "confidence": 0.95,
    "dataRange": { "from": "2024-03-01", "to": "2024-03-31" },
    "currency": "USD",
    "assumptions": ["'Food' includes categories: Groceries, Dining Out, Fast Food"],
    "disclaimer": null
  }
}
```

---

## Phase 10 — Monthly Story

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/stories` | Token | List available monthly stories |
| GET | `/api/v1/stories/{year}/{month}` | Token | Get monthly story |
| POST | `/api/v1/stories/{year}/{month}/generate` | Token | Regenerate story |

---

## Data Management

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/data/export` | Token | Export all user data (JSON) |
| DELETE | `/api/v1/data/delete-all` | Token | Permanently delete all user data |

---

## AI Service Internal API (Backend → AI Service)

These endpoints are internal and not exposed to the frontend directly.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/ai/v1/query` | Process user query |
| POST | `/ai/v1/explain` | Generate explanation for computed data |
| POST | `/ai/v1/categorize` | Suggest category for transaction |
| POST | `/ai/v1/story/generate` | Generate monthly narrative |
| GET | `/ai/v1/health` | AI service health check |
