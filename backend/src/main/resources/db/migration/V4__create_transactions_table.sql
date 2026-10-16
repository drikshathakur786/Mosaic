-- =============================================================================
-- V4: Create Transactions Table
-- =============================================================================

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    normalized_merchant_name VARCHAR(255),
    description TEXT,
    category_id UUID,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    transaction_date TIMESTAMPTZ NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    source_reference VARCHAR(255),
    import_id UUID,
    is_reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'TRANSFER', 'REFUND', 'FEE', 'ADJUSTMENT')),
    CONSTRAINT chk_transaction_status CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_transaction_source CHECK (source IN ('MANUAL', 'CSV_IMPORT', 'STATEMENT_IMPORT', 'RECEIPT_SCAN', 'API_IMPORT')),
    CONSTRAINT chk_currency_len CHECK (LENGTH(currency) = 3)
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions (user_id, transaction_date DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_user_category ON transactions (user_id, category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_merchant ON transactions (user_id, normalized_merchant_name);
CREATE INDEX IF NOT EXISTS idx_transactions_user_type ON transactions (user_id, transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_not_deleted ON transactions (user_id) WHERE deleted_at IS NULL;
