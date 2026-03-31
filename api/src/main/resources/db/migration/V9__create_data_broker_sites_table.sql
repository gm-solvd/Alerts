CREATE TABLE data_broker_sites (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL UNIQUE,
    base_url        VARCHAR(512) NOT NULL,
    search_url_tpl  VARCHAR(512),
    result_selector VARCHAR(255),
    pii_fields      TEXT[] NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
