CREATE TABLE scan_results (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scan_type  VARCHAR(50) NOT NULL,
    scan_input VARCHAR(255) NOT NULL,
    findings   JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_scan_results_user_type ON scan_results(user_id, scan_type);
CREATE INDEX idx_scan_results_created ON scan_results(created_at);
