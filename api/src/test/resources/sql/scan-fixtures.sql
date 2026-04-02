-- Completed scan results with JSONB findings
INSERT INTO scan_results (id, user_id, scan_type, scan_input, findings, findings_json, created_at)
VALUES
    ('dddd1111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'breach', 'user@test.com', 'Found in 1 breach',
     '[{"type":"breach","name":"LinkedIn","sourceUrl":"https://linkedin.com","date":"2021-06-01","dataClasses":["email","password"],"severity":"CRITICAL","recordCount":700000000,"exposedFields":[]}]'::jsonb,
     '2025-01-10T00:00:00Z'),

    ('dddd1111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
     'identity', 'user@test.com', 'Identity found on 1 site',
     '[{"type":"identity","name":"WhitePages","sourceUrl":"https://whitepages.com","date":null,"dataClasses":[],"severity":"HIGH","recordCount":null,"exposedFields":["name","address"]}]'::jsonb,
     '2025-01-11T00:00:00Z');

-- Completed scan job
INSERT INTO scan_jobs (id, user_id, status, progress, total_alerts, error_message, started_at, completed_at, created_at)
VALUES
    ('eeee1111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'COMPLETED', NULL, 3, NULL, '2025-01-10T00:00:00Z', '2025-01-10T00:00:30Z', '2025-01-10T00:00:00Z');
