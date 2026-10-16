# Mosaic — Risks & Limitations

## Technical Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| 1 | **LLM Hallucination** — AI invents transactions or numbers | High | Critical | AI explains pre-computed results only; never calculates independently |
| 2 | **Prompt Injection** — User crafts input to bypass AI safety | High | Critical | Intent router + allowlist; no arbitrary SQL; output validation |
| 3 | **Currency Mixing** — Silent summing of different currencies | Medium | High | Never auto-convert; show per-currency totals; explicit warnings |
| 4 | **CSV Format Diversity** — Infinite CSV variations cause import failures | High | Medium | Flexible column mapping; preview step; per-row error reporting |
| 5 | **Float/Rounding Errors** — Using float for money causes silent bugs | Low (mitigated) | Critical | BIGINT minor units from day one; no float/double for money anywhere |
| 6 | **JWT Secret Compromise** — Leaked secret enables account takeover | Low | Critical | Strong secrets via env vars; token rotation; short expiry |
| 7 | **Data Isolation Failure** — User A sees User B's data | Low | Critical | user_id filter on every query; authorization tests; tenant isolation |
| 8 | **LLM Provider Outage** — External API down | Medium | Medium | Graceful degradation; mock provider for testing; cached responses |
| 9 | **Performance at Scale** — Slow queries with many transactions | Medium | Medium | Database indexes; pagination; query optimization; monitoring |
| 10 | **Split Calculation Rounding** — Penny/cent rounding in group splits | Medium | Medium | Deterministic rounding; remainder allocation; extensive testing |

## Product Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| 1 | **Low Adoption** — Users unwilling to manually enter data | High | High | CSV import; sample data; make manual entry fast (3 taps) |
| 2 | **Trust Deficit** — Users don't trust AI with finances | Medium | High | Transparency; evidence links; cautious language; explain assumptions |
| 3 | **Feature Creep** — Adding too much before core is solid | High | Medium | Strict phase gates; MVP boundaries; approval before next phase |
| 4 | **Regulatory Confusion** — Users mistake insights for financial advice | Medium | High | Clear disclaimers; never use imperative financial language |
| 5 | **Localization Gaps** — Non-English users can't use the product | Medium | Medium | English MVP; i18n architecture in place for post-MVP |

## Security Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| 1 | **SQL Injection via CSV** — Malicious CSV content in DB queries | Medium | Critical | Parameterized queries; treat all imported text as untrusted |
| 2 | **XSS via Merchant Names** — Malicious content in displayed text | Medium | High | Output encoding; React auto-escaping; CSP headers |
| 3 | **Brute Force Login** — Automated password guessing | High | Medium | Rate limiting; account lockout after N failures; CAPTCHA later |
| 4 | **File Upload Abuse** — Oversized or malicious files | Medium | Medium | Size limits; type whitelist; content validation |
| 5 | **PII in AI Logs** — Account numbers or personal data in audit logs | Medium | High | PII filtering; sensitive data redaction before logging |
| 6 | **Token Theft** — JWT stolen from client-side storage | Medium | High | Short-lived tokens; refresh rotation; HttpOnly cookies option |

## AI-Specific Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| 1 | **Data Leakage to LLM Provider** — Sending user data to external API | High | High | Minimize data sent; aggregates over raw transactions; privacy review |
| 2 | **Cross-User Data in LLM Context** — Mixing users in shared model | Low | Critical | Strict user isolation; single-user context per request |
| 3 | **System Prompt Extraction** — User tricks AI into revealing prompts | Medium | Low | Prompt injection detection; refusal training; output filtering |
| 4 | **Cost Runaway** — Excessive LLM API costs | Medium | Medium | Token limits per query; daily cost caps; monitoring |
| 5 | **Model Deprecation** — LLM provider removes model version | Medium | Medium | Provider abstraction; version pinning; migration plan |

## Known Limitations (MVP)

| Limitation | Explanation | Future Plan |
|---|---|---|
| No bank integrations | Privacy-first v1; manual/CSV only | Plaid/Open Banking in v2 |
| No real-time sync | Data only updates on manual entry/import | API integrations in v2 |
| No receipt scanning | No OCR/document processing | ML-based OCR in v2 |
| English only | Single language UI | i18n framework ready for post-MVP |
| No mobile app | Web only | iOS app planned for Phase 2 |
| No push notifications | No notification infrastructure | Post-MVP with mobile app |
| No budgeting | No budget creation or alerts | Planned feature |
| No multi-user household | One account = one person | Household accounts in future |
| Rule-based categorization | No ML model yet | ML pipeline after collecting training data |
| Limited AI intents | ~15 pre-defined question types | Expand as user patterns emerge |
| No offline support | Requires internet connection | Service worker + local storage in mobile |
| No data backup to user cloud | Export is manual download | Google Drive/iCloud sync later |

## Compliance Considerations

| Area | Status | Notes |
|---|---|---|
| GDPR (EU) | Partially addressed | Data export + deletion supported; DPO/DPA not yet needed |
| CCPA (California) | Partially addressed | Data deletion supported; disclosure language needed |
| App Store (iOS) | Not yet | Privacy manifest, data disclosure required for Phase 2 |
| PCI DSS | Not applicable | We never handle card numbers or initiate payments |
| Financial regulation | Not applicable | We provide insights, not advice; no transactions initiated |
| SOC 2 | Not applicable (MVP) | Required if enterprise customers are targeted later |

## Dependency Risks

| Dependency | Risk | Mitigation |
|---|---|---|
| OpenAI / Anthropic API | Price changes, deprecation, outage | Provider abstraction; mock provider; fallback |
| PostgreSQL | Very low risk | Widely supported; managed options available |
| Spring Boot | Very low risk | LTS support; large community |
| React + Vite | Low risk | Active ecosystem; stable APIs |
| Docker | Very low risk | Industry standard |
