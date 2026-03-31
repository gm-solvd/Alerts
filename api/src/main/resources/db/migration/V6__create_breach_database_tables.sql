CREATE TABLE known_breaches (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL UNIQUE,
    domain       VARCHAR(255),
    breach_date  DATE,
    data_classes TEXT[],
    record_count BIGINT,
    source_url   VARCHAR(512),
    ingested_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_known_breaches_domain ON known_breaches(domain);

CREATE TABLE breached_credentials (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    breach_id    UUID NOT NULL REFERENCES known_breaches(id) ON DELETE CASCADE,
    email_sha256 VARCHAR(64),
    phone_sha256 VARCHAR(64),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (email_sha256, breach_id),
    UNIQUE (phone_sha256, breach_id)
);

CREATE INDEX idx_breached_creds_email ON breached_credentials(email_sha256);
CREATE INDEX idx_breached_creds_phone ON breached_credentials(phone_sha256);
