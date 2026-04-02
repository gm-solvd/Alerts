-- Additional users for admin pagination and stats testing
-- (user1 and user2 come from common-fixtures.sql)
INSERT INTO users (id, email, password_hash, full_name, created_at)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'user3@test.com',
     '$2b$10$0q7pxOwqb6pEjZc72vtqcOKXD.gIdmuW899SkCubqZn4uBB54Lrru',
     'User Three', '2025-01-03T00:00:00Z'),
    ('44444444-4444-4444-4444-444444444444', 'user4@test.com',
     '$2b$10$0q7pxOwqb6pEjZc72vtqcOKXD.gIdmuW899SkCubqZn4uBB54Lrru',
     'User Four', '2025-01-04T00:00:00Z'),
    ('55555555-5555-5555-5555-555555555555', 'user5@test.com',
     '$2b$10$0q7pxOwqb6pEjZc72vtqcOKXD.gIdmuW899SkCubqZn4uBB54Lrru',
     'User Five', '2025-01-05T00:00:00Z');

-- Alerts spread across users for stats
INSERT INTO alerts (id, user_id, category, severity, title, description, resolved, created_at)
VALUES
    ('aaaa3333-0000-0000-0000-000000000001', '33333333-3333-3333-3333-333333333333',
     'DATA_BREACH', 'CRITICAL', 'Breach alert for user3', 'Test alert.', false, '2025-01-10T00:00:00Z'),
    ('aaaa4444-0000-0000-0000-000000000001', '44444444-4444-4444-4444-444444444444',
     'IDENTITY_EXPOSURE', 'HIGH', 'Identity alert for user4', 'Test alert.', false, '2025-01-11T00:00:00Z');

-- Score for user3
INSERT INTO score_history (id, user_id, score, recorded_at)
VALUES
    ('cccc3333-0000-0000-0000-000000000001', '33333333-3333-3333-3333-333333333333', 85, '2025-01-15T00:00:00Z');
