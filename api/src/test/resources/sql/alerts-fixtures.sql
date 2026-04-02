-- Alerts for user1 across different categories and severities
INSERT INTO alerts (id, user_id, category, severity, title, description, resolved, resolved_at, created_at)
VALUES
    ('aaaa1111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'DATA_BREACH', 'CRITICAL', 'LinkedIn data breach',
     'Your email was found in the LinkedIn breach (2021).', false, NULL, '2025-01-10T00:00:00Z'),

    ('aaaa1111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
     'IDENTITY_EXPOSURE', 'HIGH', 'Identity found on people search site',
     'Your name and address appear on WhitePages.', false, NULL, '2025-01-11T00:00:00Z'),

    ('aaaa1111-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
     'TRACKER_EXPOSURE', 'MEDIUM', 'Tracked by data broker',
     'Personal data found on data broker site.', false, NULL, '2025-01-12T00:00:00Z'),

    ('aaaa1111-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111',
     'SOCIAL_FOOTPRINT', 'LOW', 'Social media profile found',
     'Your GitHub profile is publicly linked.', false, NULL, '2025-01-13T00:00:00Z'),

    ('aaaa1111-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111',
     'APP_OVERPERMISSIONS', 'MEDIUM', 'Camera permission granted',
     'Risky permission: camera access.', false, NULL, '2025-01-14T00:00:00Z'),

    ('aaaa1111-0000-0000-0000-000000000006', '11111111-1111-1111-1111-111111111111',
     'DATA_BREACH', 'HIGH', 'Adobe data breach (resolved)',
     'Your email was found in the Adobe breach (2013).', true, '2025-02-01T00:00:00Z', '2025-01-09T00:00:00Z');

-- Mitigations for the first alert (LinkedIn breach)
INSERT INTO mitigations (id, alert_id, title, description, action_url, completed, completed_at, created_at)
VALUES
    ('bbbb1111-0000-0000-0000-000000000001', 'aaaa1111-0000-0000-0000-000000000001',
     'Change your LinkedIn password', 'Update your password immediately.',
     'https://www.linkedin.com/settings/password', false, NULL, '2025-01-10T00:01:00Z'),

    ('bbbb1111-0000-0000-0000-000000000002', 'aaaa1111-0000-0000-0000-000000000001',
     'Enable two-factor authentication', 'Add 2FA to your LinkedIn account.',
     'https://www.linkedin.com/settings/security', false, NULL, '2025-01-10T00:02:00Z');

-- Score history for user1
INSERT INTO score_history (id, user_id, score, recorded_at)
VALUES
    ('cccc1111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 72, '2025-01-15T00:00:00Z'),
    ('cccc1111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 69, '2025-01-20T00:00:00Z');
