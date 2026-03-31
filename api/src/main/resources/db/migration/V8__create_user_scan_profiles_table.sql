CREATE TABLE user_scan_profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email_encrypted   BYTEA NOT NULL,
    phone_encrypted   BYTEA,
    name_encrypted    BYTEA,
    address_encrypted BYTEA,
    dob_encrypted     BYTEA,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
