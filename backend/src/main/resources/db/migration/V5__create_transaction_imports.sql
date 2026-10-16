-- =============================================================================
-- V5: Create Transaction Imports and Import Rows Tables
-- =============================================================================

CREATE TABLE IF NOT EXISTS transaction_imports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    file_hash VARCHAR(64) NOT NULL,
    total_rows INTEGER NOT NULL DEFAULT 0,
    valid_rows INTEGER NOT NULL DEFAULT 0,
    invalid_rows INTEGER NOT NULL DEFAULT 0,
    duplicate_rows INTEGER NOT NULL DEFAULT 0,
    column_mapping TEXT,
    date_format VARCHAR(50),
    currency VARCHAR(3),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_imports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_import_status CHECK (status IN ('UPLOADED', 'MAPPING', 'PREVIEWING', 'CONFIRMED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_imports_user_file_hash ON transaction_imports (user_id, file_hash);
CREATE INDEX IF NOT EXISTS idx_imports_user_created ON transaction_imports (user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS transaction_import_rows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    import_id UUID NOT NULL,
    row_number INTEGER NOT NULL,
    raw_data TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    transaction_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_import_rows_import FOREIGN KEY (import_id) REFERENCES transaction_imports(id) ON DELETE CASCADE,
    CONSTRAINT chk_import_row_status CHECK (status IN ('PENDING', 'VALID', 'INVALID', 'DUPLICATE', 'COMMITTED', 'SKIPPED'))
);

CREATE INDEX IF NOT EXISTS idx_import_rows_import_id ON transaction_import_rows (import_id);
