# Mosaic — Product Requirements Document

> **"Financial clarity for everyday life."**

---

## 1. Problem Statement

Young adults today juggle multiple payment methods — cards, wallets, UPI, bank transfers, cash — across different currencies and countries. Subscriptions silently drain accounts. Shared expenses with flatmates, partners, and travel groups create invisible debts. Financial information becomes fragmented, and people struggle to understand *why* money disappears or *what* decisions to make next.

There is no calm, trustworthy tool that converts this fragmented data into clear, transparent, evidence-backed insights — without requiring bank access or pushing financial products.

## 2. Product Vision

Mosaic is a **privacy-first personal finance clarity application** for young adults worldwide. It helps users:

| Capability | Description |
|---|---|
| **Import transactions** | Manual entry, CSV upload, sample datasets |
| **Understand spending** | Categorized breakdowns, trends, comparisons |
| **Detect commitments** | Recurring payments, subscriptions |
| **Share expenses** | Groups for flatmates, couples, trips |
| **Ask questions** | Natural-language queries about their own data |
| **Get explanations** | Evidence-backed, traceable insights |
| **Simulate decisions** | "What if I buy this?" impact analysis |
| **Set goals** | Savings targets with progress tracking |
| **Stay informed** | Monthly financial stories |

### What Mosaic Is NOT

- ❌ An investment platform
- ❌ A bank or neobank
- ❌ A lending or credit app
- ❌ A trading platform
- ❌ A financial advice service
- ❌ A fraud detection system

## 3. Target Users

### Primary Personas

| Persona | Description | Key Need |
|---|---|---|
| **Independent Student** | Living away from home, managing budget for the first time | Track where money goes |
| **First-Job Professional** | Earning salary, new to financial responsibility | Understand spending patterns |
| **Freelancer** | Irregular income, multiple payment sources | Income vs. expense clarity |
| **Flatmates** | Sharing rent, utilities, groceries | Fair expense splitting |
| **Couple** | Shared and individual finances | Joint spending visibility |
| **Travel Group** | Multi-currency expenses during trips | Who owes whom, across currencies |

### Global Design Requirements

- Multiple currencies (ISO 4217)
- Country-independent transaction formats
- Timezone-aware date handling
- Locale-aware number formatting (decimal/comma conventions)
- No hardcoding around any single country, currency, or payment method

## 4. Initial Product Boundaries (v1)

### Must Do
- Accept manually entered transactions
- Accept CSV file uploads
- Ship with sample/demo datasets
- Support multiple currencies in the data model
- Provide financial insights (not regulated advice)
- Use cautious, evidence-backed AI language

### Must NOT Do
- Connect to banks directly
- Store bank passwords or credentials
- Initiate payments or transfers
- Automatically cancel subscriptions
- Promise savings or financial outcomes
- Claim fraud detection with certainty
- Give investment, tax, legal, or lending advice

### AI Language Policy

| ✅ Acceptable | ❌ Not Acceptable |
|---|---|
| "Based on the transactions you provided..." | "You will definitely save..." |
| "This appears to be a recurring payment..." | "This investment is safe." |
| "Your food spending seems higher than last month..." | "You can definitely afford this loan." |
| "I don't have enough data to answer this." | "This transaction is definitely fraud." |

## 5. Core User Flow

```
1. Create account
2. Select currency, country, locale, timezone
3. Add transactions (manual / CSV)
4. System validates & normalizes
5. System categorizes (rules → ML later)
6. User reviews & corrects categories
7. System learns merchant aliases & preferences
8. Dashboard displays financial activity
9. System detects recurring payments
10. User creates shared-expense groups
11. User asks natural-language questions
12. AI generates evidence-backed explanations
13. User creates savings goals
14. User receives monthly financial story
15. User can export or permanently delete data
```

## 6. Feature Requirements by Phase

### Phase 1 — Backend Foundation
- Project scaffolding & monorepo structure
- PostgreSQL connection & migrations
- Health endpoint (`/health`, `/ready`)
- Global exception handling & structured error format
- Configuration management (profiles, env vars)
- Docker Compose for local development
- Structured logging with correlation IDs
- OpenAPI/Swagger documentation

### Phase 2 — Authentication & Authorization
- User registration with email
- Login with JWT
- Password hashing (bcrypt/argon2)
- Token refresh & revocation
- Password reset flow (design only for v1)
- Per-resource authorization (tenant isolation)
- Rate limiting on auth endpoints

### Phase 3 — Transaction Management
- Full CRUD for transactions
- Filtering (date, category, merchant, type, source)
- Pagination & sorting
- Multi-layer validation (API → service → DB)
- Money stored as `BIGINT` minor units + ISO 4217 currency

### Phase 4 — CSV Import
- File upload with size/type validation
- Column mapping UI
- Date format & currency selection
- Amount parsing (positive/negative conventions)
- Merchant normalization
- Duplicate detection (idempotent imports)
- Per-row validation errors
- Import preview → confirm workflow
- Background processing for large files
- Import history

### Phase 5 — Categorization Engine
- Rule-based categorization (merchant aliases, keywords)
- User corrections & category overrides
- Categorization metadata (method, confidence, version)
- ML pipeline preparation (future phase)

### Phase 6 — Dashboard & Analytics
- Income / Expense / Net cash flow
- Spending by category & merchant
- Monthly comparison
- Daily spending trend
- Largest transactions
- Unreviewed transaction count
- Recurring payment radar
- Savings goal progress
- All calculations by backend — never by LLM

### Phase 7 — Recurring Payment Detection
- Pattern detection (merchant + amount + interval)
- Confidence scoring
- User confirmation workflow
- Next-expected-date projection
- Never auto-cancels

### Phase 8 — Shared Expenses
- Group creation & member management
- Expense creation with multiple split modes (equal, percentage, item-based)
- Tax & tip handling
- Settlement tracking
- Multi-currency balance display
- Deterministic, tested calculations

### Phase 9 — AI Assistant
- Intent classification → structured query → deterministic result → LLM explanation
- Safe question templates
- Read-only database access for AI
- Evidence metadata in every response
- Audit logging of all queries
- Prompt injection resistance
- Graceful degradation if LLM unavailable

### Phase 10 — Monthly Financial Story
- Generated from structured, verified data
- Income, expense, net change, top categories
- Month-over-month comparison
- Recurring payment summary
- Positive trends & practical observations
- Links to supporting transactions

## 7. Non-Functional Requirements

| Requirement | Target |
|---|---|
| **Response time** | < 200ms for reads, < 500ms for writes |
| **AI response time** | < 3s for simple queries |
| **CSV import** | < 30s for 1,000 rows |
| **Uptime** | 99.5% (post-launch) |
| **Data retention** | User-controlled; full deletion supported |
| **Accessibility** | WCAG 2.1 AA |
| **Browser support** | Latest 2 versions of Chrome, Firefox, Safari, Edge |
| **Mobile** | iOS app in Phase 2 (same backend APIs) |

## 8. Success Metrics

| Metric | Measurement |
|---|---|
| User activation | % completing onboarding + first 5 transactions |
| Import completion | % of CSV imports successfully committed |
| Categorization accuracy | % auto-categorized correctly (user corrections as signal) |
| AI query satisfaction | % of AI answers not corrected or retried |
| Shared group usage | % of users creating at least one group |
| Data trust | % of users who have NOT deleted their account within 30 days |
| Monthly story engagement | % of users who open their monthly story |

## 9. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Users distrust AI with financial data | Low adoption | Transparency, evidence links, cautious language |
| CSV format diversity | Import failures | Flexible column mapping, preview step, clear errors |
| Currency conversion complexity | Incorrect totals | Never silently convert; show original currency |
| LLM hallucination | Misleading insights | AI explains computed results, never calculates |
| Regulatory risk | Legal issues | No advice, no bank access, clear disclaimers |
| Prompt injection | Data leakage | Intent router, allowlists, output validation |
| Scope creep | Delayed launch | Strict phase boundaries, MVP-first |
