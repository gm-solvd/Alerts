CREATE TABLE scan_jobs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress       VARCHAR(50),
    total_alerts   INT,
    error_message  TEXT,
    started_at     TIMESTAMPTZ,
    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_scan_jobs_user ON scan_jobs(user_id);
