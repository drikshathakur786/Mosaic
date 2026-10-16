-- =============================================================================
-- V3: Create Categories Table and Seed Default System Categories
-- =============================================================================

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    parent_id UUID,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);

-- Seed System Categories
INSERT INTO categories (name, slug, icon, color, is_system, sort_order) VALUES
('Housing & Rent', 'housing', 'home', '#3B82F6', TRUE, 1),
('Food & Dining', 'food-and-dining', 'utensils', '#EF4444', TRUE, 2),
('Groceries', 'groceries', 'shopping-cart', '#10B981', TRUE, 3),
('Transportation', 'transportation', 'car', '#F59E0B', TRUE, 4),
('Utilities & Bills', 'utilities', 'zap', '#6366F1', TRUE, 5),
('Entertainment & Subscriptions', 'entertainment', 'film', '#8B5CF6', TRUE, 6),
('Health & Wellness', 'health-and-wellness', 'heart', '#EC4899', TRUE, 7),
('Shopping & Lifestyle', 'shopping', 'shopping-bag', '#14B8A6', TRUE, 8),
('Travel & Holidays', 'travel', 'plane', '#06B6D4', TRUE, 9),
('Education & Learning', 'education', 'book-open', '#F97316', TRUE, 10),
('Savings & Investments', 'savings-investments', 'trending-up', '#84CC16', TRUE, 11),
('Income & Salary', 'income', 'dollar-sign', '#22C55E', TRUE, 12),
('Miscellaneous', 'miscellaneous', 'tag', '#6B7280', TRUE, 13)
ON CONFLICT (slug) DO NOTHING;
