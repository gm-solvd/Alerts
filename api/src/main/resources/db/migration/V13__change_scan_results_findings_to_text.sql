ALTER TABLE scan_results ALTER COLUMN findings TYPE TEXT USING findings::text;
ALTER TABLE scan_results ALTER COLUMN findings SET DEFAULT '';
