# Mosaic — MVP Scope

## MVP Definition

The MVP (Minimum Viable Product) is the smallest version of Mosaic that delivers real value to a user: the ability to **import transactions, understand where money goes, and get evidence-backed answers** — all without bank access.

---

## ✅ In Scope for MVP

### Backend Foundation (Phase 1)
- [x] Monorepo structure with backend, ai-service, frontend, docs
- [x] Spring Boot 3.x + Java 21 backend
- [x] PostgreSQL with Flyway migrations
- [x] Health & readiness endpoints
- [x] Global exception handler with structured JSON errors
- [x] Environment-based configuration
- [x] Docker Compose (backend + database)
- [x] OpenAPI/Swagger auto-generated docs
- [x] Structured logging with correlation IDs

### Authentication (Phase 2)
- [x] Email + password registration
- [x] JWT-based authentication (access + refresh tokens)
- [x] bcrypt password hashing
- [x] Per-resource authorization (users see only their own data)
- [x] Rate limiting on auth endpoints
- [ ] Password reset (design only, email integration deferred)

### Transaction Management (Phase 3)
- [x] CRUD API for transactions
- [x] Money as BIGINT minor units + ISO 4217 currency
- [x] Filter by date, category, merchant, type
- [x] Pagination and sorting
- [x] Input validation at API, service, and DB layers

### CSV Import (Phase 4)
- [x] CSV file upload
- [x] Column mapping
- [x] Date format and currency selection
- [x] Amount parsing (positive/negative conventions)
- [x] Duplicate detection
- [x] Per-row validation errors
- [x] Import preview → confirm workflow
- [x] Import history
- [ ] Background processing (deferred unless files > 1,000 rows)

### Categorization (Phase 5)
- [x] Default category seed data
- [x] Rule-based categorization (merchant alias → category)
- [x] User corrections
- [x] Categorization metadata (method, confidence, confirmed)
- [ ] ML-based categorization (deferred to post-MVP)

### Dashboard (Phase 6)
- [x] Income / Expense / Net cash flow
- [x] Spending by category (pie/bar chart)
- [x] Spending by merchant (top N)
- [x] Monthly comparison (current vs. previous)
- [x] Largest transactions
- [x] Unreviewed transaction count
- [ ] Daily trend chart (deferred to post-MVP polish)

### Recurring Payment Detection (Phase 7)
- [x] Pattern detection (merchant + amount + interval)
- [x] Confidence scoring
- [x] Display: merchant, amount, frequency, last/next date
- [x] User confirmation
- [ ] Smart notification when payment is missed (deferred)

### Shared Expenses (Phase 8)
- [x] Group creation and member management
- [x] Add shared expense
- [x] Equal split
- [x] Percentage split
- [ ] Item-based split (deferred)
- [x] Balance calculation (who owes whom)
- [x] Settlement recording
- [ ] Multi-currency in groups (display only, no conversion)

### AI Assistant (Phase 9)
- [x] Intent classification
- [x] Safe query templates (spending totals, comparisons, merchant queries)
- [x] Deterministic computation → LLM explanation
- [x] Evidence metadata in responses
- [x] Audit logging
- [x] Prompt injection resistance
- [x] Graceful degradation if LLM unavailable
- [ ] Free-form follow-up questions (limited to pre-defined intents)

### Monthly Story (Phase 10)
- [x] Auto-generated from structured data
- [x] Income, expense, net, top categories
- [x] Month-over-month changes
- [x] Recurring payment summary
- [ ] Personalized observations (basic version)

### Frontend (Across Phases)
- [x] Landing page
- [x] Sign up / Login
- [x] Onboarding (currency, locale, timezone)
- [x] Dashboard
- [x] Transaction list with filters
- [x] CSV import flow
- [x] Category management
- [x] Subscription/recurring radar
- [x] Shared expense groups
- [x] AI assistant chat
- [x] Monthly story viewer
- [x] Settings page
- [x] Privacy & data controls (export + delete)
- [ ] Dark mode (post-MVP polish)

---

## ❌ Out of Scope for MVP

| Feature | Reason |
|---|---|
| Bank integrations (Plaid, Open Banking) | Privacy-first v1; requires partnerships |
| Receipt/document scanning | Requires OCR pipeline |
| Mobile app (iOS/Android) | Phase 2; backend APIs designed for it |
| ML categorization | Rules-based first; ML needs training data |
| Multi-language UI | English only for MVP |
| Push notifications | Requires mobile app or email infrastructure |
| Budgeting / budget alerts | Future feature |
| Investment tracking | Out of product scope |
| Bill payment / auto-pay | Never in scope |
| Real-time bank feeds | Out of v1 scope |
| Social features | Out of scope |
| Gamification | Out of scope |
| Cryptocurrency tracking | Out of scope |

---

## MVP Success Criteria

1. A new user can sign up, add 10+ transactions, and see a meaningful dashboard in under 5 minutes.
2. A user can import a CSV of 500 transactions with correct categorization for 70%+ of known merchants.
3. A user can ask "How much did I spend on food this month?" and get a correct, evidence-backed answer.
4. Two users cannot see each other's data under any circumstance.
5. Re-uploading the same CSV does not create duplicate transactions.
6. The demo flow works end-to-end without external dependencies.

---

## MVP Timeline Estimate

| Phase | Estimated Duration | Dependencies |
|---|---|---|
| Phase 1: Backend Foundation | 1 week | — |
| Phase 2: Authentication | 1 week | Phase 1 |
| Phase 3: Transactions | 1 week | Phase 2 |
| Phase 4: CSV Import | 1.5 weeks | Phase 3 |
| Phase 5: Categorization | 1 week | Phase 3 |
| Phase 6: Dashboard | 1.5 weeks | Phase 3, 5 |
| Phase 7: Recurring Detection | 1 week | Phase 3 |
| Phase 8: Shared Expenses | 1.5 weeks | Phase 2 |
| Phase 9: AI Assistant | 2 weeks | Phase 3, 5, 6 |
| Phase 10: Monthly Story | 1 week | Phase 6 |
| Frontend (parallel) | 4–5 weeks | Phases 1–10 APIs |
| Testing & Polish | 1.5 weeks | All |
| **Total** | **~12–14 weeks** | — |
