# Admin Source

Vue 3 + Vite single-page admin dashboard. Communicates exclusively with the `/api/v1/admin` endpoints.

## Files

| File | Description |
|------|-------------|
| `main.js` | Vue app entry point — creates app, mounts router |
| `App.vue` | Root component: shows `<Navbar>` + `<router-view>` |
| `router.js` | Vue Router config: `/` → Dashboard, `/users` → UserList, `/users/:id` → UserDetail, `/users/create` → CreateUser, `/users/:id/scans` → ScanHistory |
| `api.js` | Axios client (`baseURL: /api/v1/admin`). Attaches `Authorization: Bearer <adminToken>` from localStorage. Functions: `getStats()`, `getUsers(page, size)`, `getUser(id)`, `createUser(data)`, `getUserAlerts(id, page, size)`, `deleteUser(id)`, `triggerScan(id)`, `getScanHistory(id, page, size)` |

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`components/`](components/SUMMARY.md) | `AlertBadge`, `Navbar`, `Pagination` — reusable UI |
| [`views/`](views/SUMMARY.md) | `Dashboard`, `UserList`, `UserDetail`, `CreateUser`, `ScanHistory` — full pages |
