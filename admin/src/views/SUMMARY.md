# Vue Views (Pages)

Full-page route components rendered by Vue Router.

## Files

| File | Route | Description |
|------|-------|-------------|
| `Dashboard.vue` | `/` | Platform stats overview: total users, total alerts, breakdown by category and severity. Fetches `GET /api/v1/admin/stats` |
| `UserList.vue` | `/users` | Paginated table of all users with alert counts. Links to user detail. Supports search. Fetches `GET /api/v1/admin/users` |
| `UserDetail.vue` | `/users/:id` | Full user profile: personal info, alert list (paginated, filterable), delete user button. Fetches `GET /api/v1/admin/users/:id` and `GET /api/v1/admin/users/:id/alerts` |
| `CreateUser.vue` | `/users/create` | Form to create a new user (email, password, fullName, phone). Posts to `POST /api/v1/admin/users` |
| `ScanHistory.vue` | `/users/:id/scans` | Paginated scan history for a user with scan type, findings count, and timestamp. Trigger scan button calls `POST /api/v1/admin/users/:id/scan` |
