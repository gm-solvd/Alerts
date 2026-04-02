-- Test user 1: user@test.com / password123
-- BCrypt hash for "password123" with cost 10
INSERT INTO users (id, email, password_hash, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'user@test.com',
        '$2b$10$0q7pxOwqb6pEjZc72vtqcOKXD.gIdmuW899SkCubqZn4uBB54Lrru',
        '2025-01-01T00:00:00Z');

-- Test user 2: user2@test.com / password123 (for cross-user isolation tests)
INSERT INTO users (id, email, password_hash, created_at)
VALUES ('22222222-2222-2222-2222-222222222222', 'user2@test.com',
        '$2b$10$0q7pxOwqb6pEjZc72vtqcOKXD.gIdmuW899SkCubqZn4uBB54Lrru',
        '2025-01-02T00:00:00Z');
