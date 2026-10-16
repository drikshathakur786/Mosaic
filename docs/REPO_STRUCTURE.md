# Mosaic — Repository Structure

```
mosaic/
│
├── README.md                           # Project overview and setup guide
├── LICENSE
├── .gitignore
├── .editorconfig
├── docker-compose.yml                  # Local development orchestration
├── docker-compose.prod.yml             # Production-like compose (future)
├── Makefile                            # Common dev commands
│
├── docs/
│   ├── PRD.md                          # Product Requirements Document
│   ├── MVP_SCOPE.md                    # MVP scope definition
│   ├── ARCHITECTURE.md                 # Architecture with Mermaid diagrams
│   ├── DATABASE_SCHEMA.md             # Database ER design
│   ├── API_REFERENCE.md               # Complete API documentation
│   ├── REPO_STRUCTURE.md              # This file
│   ├── IMPLEMENTATION_PLAN.md          # Phase-by-phase build plan
│   ├── SETUP_GUIDE.md                  # Local development setup
│   ├── ENVIRONMENT_VARIABLES.md        # Env var documentation
│   ├── SECURITY_THREAT_MODEL.md        # Security analysis
│   ├── PRIVACY_POLICY_DRAFT.md         # Privacy policy outline
│   ├── AI_BEHAVIOUR_POLICY.md          # AI guardrails documentation
│   ├── CHANGELOG.md                    # Version changelog
│   ├── TROUBLESHOOTING.md             # Common issues and fixes
│   └── adr/                            # Architecture Decision Records
│       ├── ADR-001-monorepo.md
│       ├── ADR-002-separate-ai-service.md
│       ├── ADR-003-money-as-integers.md
│       ├── ADR-004-backend-owns-calculations.md
│       ├── ADR-005-intent-router-not-text-to-sql.md
│       ├── ADR-006-flyway-migrations.md
│       ├── ADR-007-jwt-authentication.md
│       └── ADR-008-llm-provider-abstraction.md
│
├── backend/                            # Java 21 + Spring Boot
│   ├── Dockerfile
│   ├── pom.xml                         # Maven project file
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mosaic/
│   │   │   │   ├── MosaicApplication.java
│   │   │   │   │
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── JwtConfig.java
│   │   │   │   │   ├── OpenApiConfig.java
│   │   │   │   │   └── WebConfig.java
│   │   │   │   │
│   │   │   │   ├── common/
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   │   ├── PageResponse.java
│   │   │   │   │   │   └── ApiResponse.java
│   │   │   │   │   ├── exception/
│   │   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   │   ├── UnauthorizedException.java
│   │   │   │   │   │   ├── ForbiddenException.java
│   │   │   │   │   │   ├── ValidationException.java
│   │   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   │   └── ImportException.java
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Money.java
│   │   │   │   │   │   └── CurrencyCode.java
│   │   │   │   │   └── util/
│   │   │   │   │       ├── MoneyUtils.java
│   │   │   │   │       ├── DateUtils.java
│   │   │   │   │       └── StringUtils.java
│   │   │   │   │
│   │   │   │   ├── auth/
│   │   │   │   │   ├── controller/AuthController.java
│   │   │   │   │   ├── controller/UserController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   ├── AuthResponse.java
│   │   │   │   │   │   ├── RefreshRequest.java
│   │   │   │   │   │   ├── UserResponse.java
│   │   │   │   │   │   ├── ProfileUpdateRequest.java
│   │   │   │   │   │   └── PreferencesUpdateRequest.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── UserProfile.java
│   │   │   │   │   │   └── UserPreferences.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   │   ├── UserProfileRepository.java
│   │   │   │   │   │   └── UserPreferencesRepository.java
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── AuthService.java
│   │   │   │   │   │   ├── UserService.java
│   │   │   │   │   │   └── JwtService.java
│   │   │   │   │   └── security/
│   │   │   │   │       ├── JwtAuthenticationFilter.java
│   │   │   │   │       ├── JwtTokenProvider.java
│   │   │   │   │       └── UserDetailsServiceImpl.java
│   │   │   │   │
│   │   │   │   ├── transaction/
│   │   │   │   │   ├── controller/TransactionController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── TransactionCreateRequest.java
│   │   │   │   │   │   ├── TransactionUpdateRequest.java
│   │   │   │   │   │   ├── TransactionResponse.java
│   │   │   │   │   │   └── TransactionFilterRequest.java
│   │   │   │   │   ├── entity/Transaction.java
│   │   │   │   │   ├── repository/TransactionRepository.java
│   │   │   │   │   ├── service/TransactionService.java
│   │   │   │   │   └── importer/
│   │   │   │   │       ├── controller/ImportController.java
│   │   │   │   │       ├── dto/
│   │   │   │   │       │   ├── ImportUploadResponse.java
│   │   │   │   │       │   ├── ImportPreviewRequest.java
│   │   │   │   │       │   ├── ImportPreviewResponse.java
│   │   │   │   │       │   └── ImportHistoryResponse.java
│   │   │   │   │       ├── entity/
│   │   │   │   │       │   ├── TransactionImport.java
│   │   │   │   │       │   └── TransactionImportRow.java
│   │   │   │   │       ├── repository/
│   │   │   │   │       │   ├── TransactionImportRepository.java
│   │   │   │   │       │   └── TransactionImportRowRepository.java
│   │   │   │   │       └── service/
│   │   │   │   │           ├── CsvParserService.java
│   │   │   │   │           ├── ImportService.java
│   │   │   │   │           └── DuplicateDetectionService.java
│   │   │   │   │
│   │   │   │   ├── category/
│   │   │   │   │   ├── controller/CategoryController.java
│   │   │   │   │   ├── dto/CategoryResponse.java
│   │   │   │   │   ├── entity/Category.java
│   │   │   │   │   ├── repository/CategoryRepository.java
│   │   │   │   │   └── service/CategoryService.java
│   │   │   │   │
│   │   │   │   ├── categorization/
│   │   │   │   │   ├── entity/MerchantAlias.java
│   │   │   │   │   ├── repository/MerchantAliasRepository.java
│   │   │   │   │   ├── service/CategorizationService.java
│   │   │   │   │   └── service/MerchantAliasService.java
│   │   │   │   │
│   │   │   │   ├── recurring/
│   │   │   │   │   ├── controller/RecurringController.java
│   │   │   │   │   ├── dto/RecurringPaymentResponse.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── RecurringPaymentCandidate.java
│   │   │   │   │   │   └── Subscription.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── RecurringPaymentRepository.java
│   │   │   │   │   │   └── SubscriptionRepository.java
│   │   │   │   │   └── service/RecurringDetectionService.java
│   │   │   │   │
│   │   │   │   ├── group/
│   │   │   │   │   ├── controller/GroupController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── GroupCreateRequest.java
│   │   │   │   │   │   ├── GroupResponse.java
│   │   │   │   │   │   ├── SharedExpenseRequest.java
│   │   │   │   │   │   ├── SharedExpenseResponse.java
│   │   │   │   │   │   └── GroupBalanceResponse.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── Group.java
│   │   │   │   │   │   ├── GroupMember.java
│   │   │   │   │   │   ├── SharedExpense.java
│   │   │   │   │   │   └── SharedExpenseSplit.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── GroupRepository.java
│   │   │   │   │   │   ├── GroupMemberRepository.java
│   │   │   │   │   │   ├── SharedExpenseRepository.java
│   │   │   │   │   │   └── SharedExpenseSplitRepository.java
│   │   │   │   │   └── service/
│   │   │   │   │       ├── GroupService.java
│   │   │   │   │       └── SplitCalculationService.java
│   │   │   │   │
│   │   │   │   ├── savings/
│   │   │   │   │   ├── controller/SavingsGoalController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── SavingsGoalCreateRequest.java
│   │   │   │   │   │   └── SavingsGoalResponse.java
│   │   │   │   │   ├── entity/SavingsGoal.java
│   │   │   │   │   ├── repository/SavingsGoalRepository.java
│   │   │   │   │   └── service/SavingsGoalService.java
│   │   │   │   │
│   │   │   │   ├── dashboard/
│   │   │   │   │   ├── controller/DashboardController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── FinancialSummaryResponse.java
│   │   │   │   │   │   ├── SpendingByCategoryResponse.java
│   │   │   │   │   │   ├── SpendingByMerchantResponse.java
│   │   │   │   │   │   ├── MonthlyComparisonResponse.java
│   │   │   │   │   │   └── DailyTrendResponse.java
│   │   │   │   │   └── service/DashboardService.java
│   │   │   │   │
│   │   │   │   ├── insight/
│   │   │   │   │   ├── controller/InsightController.java
│   │   │   │   │   ├── dto/MonthlyStoryResponse.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── FinancialInsight.java
│   │   │   │   │   │   └── MonthlySummary.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── FinancialInsightRepository.java
│   │   │   │   │   │   └── MonthlySummaryRepository.java
│   │   │   │   │   └── service/MonthlyStoryService.java
│   │   │   │   │
│   │   │   │   ├── ai/
│   │   │   │   │   ├── controller/AiController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── AiQueryRequest.java
│   │   │   │   │   │   ├── AiQueryResponse.java
│   │   │   │   │   │   └── ConversationResponse.java
│   │   │   │   │   ├── client/AiServiceClient.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── AiConversation.java
│   │   │   │   │   │   ├── AiMessage.java
│   │   │   │   │   │   └── AiQueryAudit.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AiConversationRepository.java
│   │   │   │   │   │   ├── AiMessageRepository.java
│   │   │   │   │   │   └── AiQueryAuditRepository.java
│   │   │   │   │   └── service/AiProxyService.java
│   │   │   │   │
│   │   │   │   └── audit/
│   │   │   │       ├── entity/AuditLog.java
│   │   │   │       ├── repository/AuditLogRepository.java
│   │   │   │       └── service/AuditService.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-test.yml
│   │   │       ├── application-prod.yml
│   │   │       ├── db/migration/
│   │   │       │   ├── V1__create_users.sql
│   │   │       │   ├── V2__create_profiles_preferences.sql
│   │   │       │   ├── V3__create_categories.sql
│   │   │       │   ├── ...
│   │   │       │   └── V15__seed_categories.sql
│   │   │       └── logback-spring.xml
│   │   │
│   │   └── test/
│   │       └── java/com/mosaic/
│   │           ├── auth/...
│   │           ├── transaction/...
│   │           ├── group/...
│   │           └── ...
│   │
│   └── .env.example
│
├── ai-service/                         # Python 3.12 + FastAPI
│   ├── Dockerfile
│   ├── pyproject.toml
│   ├── requirements.txt
│   ├── ai_service/
│   │   ├── __init__.py
│   │   ├── main.py
│   │   ├── config.py
│   │   ├── api/
│   │   │   ├── __init__.py
│   │   │   ├── routes.py
│   │   │   └── models.py
│   │   ├── intent/
│   │   │   ├── __init__.py
│   │   │   ├── classifier.py
│   │   │   └── intents.py
│   │   ├── query/
│   │   │   ├── __init__.py
│   │   │   ├── builder.py
│   │   │   └── templates.py
│   │   ├── data/
│   │   │   ├── __init__.py
│   │   │   ├── fetcher.py
│   │   │   └── connection.py
│   │   ├── llm/
│   │   │   ├── __init__.py
│   │   │   ├── provider.py
│   │   │   ├── openai_provider.py
│   │   │   ├── mock_provider.py
│   │   │   └── prompts.py
│   │   ├── safety/
│   │   │   ├── __init__.py
│   │   │   ├── guards.py
│   │   │   ├── pii_filter.py
│   │   │   └── injection.py
│   │   └── evaluation/
│   │       ├── __init__.py
│   │       ├── dataset.json
│   │       └── runner.py
│   ├── tests/
│   │   ├── __init__.py
│   │   ├── test_intent.py
│   │   ├── test_query.py
│   │   ├── test_safety.py
│   │   └── test_api.py
│   └── .env.example
│
├── frontend/                           # React + TypeScript + Vite
│   ├── Dockerfile
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── index.html
│   ├── public/
│   │   ├── favicon.svg
│   │   └── mosaic-logo.svg
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── index.css
│   │   ├── api/
│   │   │   ├── client.ts                # Axios/fetch wrapper
│   │   │   ├── auth.ts
│   │   │   ├── transactions.ts
│   │   │   ├── imports.ts
│   │   │   ├── categories.ts
│   │   │   ├── dashboard.ts
│   │   │   ├── groups.ts
│   │   │   ├── ai.ts
│   │   │   └── stories.ts
│   │   ├── components/
│   │   │   ├── layout/
│   │   │   │   ├── AppLayout.tsx
│   │   │   │   ├── Sidebar.tsx
│   │   │   │   ├── Header.tsx
│   │   │   │   └── MobileNav.tsx
│   │   │   ├── common/
│   │   │   │   ├── Button.tsx
│   │   │   │   ├── Input.tsx
│   │   │   │   ├── Modal.tsx
│   │   │   │   ├── Card.tsx
│   │   │   │   ├── Badge.tsx
│   │   │   │   ├── EmptyState.tsx
│   │   │   │   ├── LoadingState.tsx
│   │   │   │   ├── ErrorState.tsx
│   │   │   │   └── Pagination.tsx
│   │   │   ├── dashboard/
│   │   │   │   ├── FinancialSnapshot.tsx
│   │   │   │   ├── SpendingChart.tsx
│   │   │   │   ├── CategoryBreakdown.tsx
│   │   │   │   ├── MonthlyComparison.tsx
│   │   │   │   ├── RecurringRadar.tsx
│   │   │   │   └── SavingsProgress.tsx
│   │   │   ├── transactions/
│   │   │   │   ├── TransactionList.tsx
│   │   │   │   ├── TransactionRow.tsx
│   │   │   │   ├── TransactionForm.tsx
│   │   │   │   ├── TransactionFilters.tsx
│   │   │   │   └── ImportWizard.tsx
│   │   │   ├── groups/
│   │   │   │   ├── GroupList.tsx
│   │   │   │   ├── GroupDetail.tsx
│   │   │   │   ├── ExpenseForm.tsx
│   │   │   │   └── BalanceView.tsx
│   │   │   ├── ai/
│   │   │   │   ├── ChatInterface.tsx
│   │   │   │   ├── MessageBubble.tsx
│   │   │   │   └── EvidencePanel.tsx
│   │   │   └── stories/
│   │   │       ├── MonthlyStory.tsx
│   │   │       └── InsightCard.tsx
│   │   ├── pages/
│   │   │   ├── LandingPage.tsx
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   ├── OnboardingPage.tsx
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── TransactionsPage.tsx
│   │   │   ├── ImportPage.tsx
│   │   │   ├── SubscriptionsPage.tsx
│   │   │   ├── GroupsPage.tsx
│   │   │   ├── GroupDetailPage.tsx
│   │   │   ├── SavingsPage.tsx
│   │   │   ├── AiAssistantPage.tsx
│   │   │   ├── MonthlyStoryPage.tsx
│   │   │   ├── SettingsPage.tsx
│   │   │   └── PrivacyPage.tsx
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   ├── useTransactions.ts
│   │   │   ├── useDashboard.ts
│   │   │   ├── useGroups.ts
│   │   │   └── useAi.ts
│   │   ├── store/
│   │   │   ├── authStore.ts
│   │   │   └── preferencesStore.ts
│   │   ├── types/
│   │   │   ├── auth.ts
│   │   │   ├── transaction.ts
│   │   │   ├── category.ts
│   │   │   ├── group.ts
│   │   │   ├── ai.ts
│   │   │   └── common.ts
│   │   └── utils/
│   │       ├── formatMoney.ts
│   │       ├── formatDate.ts
│   │       └── validators.ts
│   └── .env.example
│
├── database/
│   ├── seeds/
│   │   ├── categories.sql              # Default category data
│   │   ├── sample_users.sql            # Demo users
│   │   ├── sample_transactions_us.sql  # US sample data
│   │   ├── sample_transactions_uk.sql  # UK sample data
│   │   ├── sample_transactions_eu.sql  # EU sample data
│   │   ├── sample_transactions_in.sql  # India sample data
│   │   └── sample_groups.sql           # Sample shared groups
│   └── sample_csvs/
│       ├── us_bank_statement.csv
│       ├── uk_bank_statement.csv
│       ├── eu_bank_statement.csv
│       └── multi_currency_trip.csv
│
├── scripts/
│   ├── setup.sh                        # One-command local setup
│   ├── seed-demo-data.sh              # Load sample data
│   ├── run-tests.sh                    # Run all tests
│   ├── generate-api-docs.sh           # Generate OpenAPI docs
│   └── cleanup.sh                      # Remove Docker volumes
│
├── tests/
│   ├── e2e/                            # End-to-end tests
│   ├── integration/                    # Cross-service tests
│   └── performance/                    # Load tests
│
└── .github/
    └── workflows/
        ├── ci.yml                      # CI pipeline
        ├── backend-tests.yml
        ├── ai-service-tests.yml
        └── frontend-tests.yml
```

## Directory Purposes

| Directory | Purpose |
|---|---|
| `backend/` | Java 21 Spring Boot API — business logic, auth, financial calculations |
| `ai-service/` | Python FastAPI — intent classification, LLM orchestration, explanations |
| `frontend/` | React + TypeScript + Vite — user interface |
| `database/` | Seed data, sample CSVs, data fixtures |
| `docs/` | All documentation (PRD, architecture, API, ADRs) |
| `scripts/` | Development and deployment scripts |
| `tests/` | Cross-service integration and E2E tests |
| `.github/` | CI/CD pipeline definitions |
