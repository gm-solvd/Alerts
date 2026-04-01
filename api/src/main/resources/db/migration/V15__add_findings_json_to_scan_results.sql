ALTER TABLE scan_results ADD COLUMN findings_json JSONB NOT NULL DEFAULT '[]'::jsonb;
