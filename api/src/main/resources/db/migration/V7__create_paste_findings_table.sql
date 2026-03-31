CREATE TABLE paste_findings (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source        VARCHAR(100) NOT NULL,
    paste_url     VARCHAR(512) NOT NULL UNIQUE,
    title         VARCHAR(255),
    snippet       TEXT,
    discovered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_paste_findings_source ON paste_findings(source);
