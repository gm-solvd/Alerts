<template>
  <div>
    <div class="header">
      <router-link to="/users" class="back">Back to Users</router-link>
      <h1 v-if="user">{{ user.fullName || user.email }}</h1>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <template v-else-if="user">
      <div class="info-grid">
        <div class="card">
          <h3>User Info</h3>
          <dl>
            <dt>Email</dt>
            <dd>{{ user.email }}</dd>
            <dt>Full Name</dt>
            <dd>{{ user.fullName || '-' }}</dd>
            <dt>Phone</dt>
            <dd>{{ user.phoneNumber || '-' }}</dd>
            <dt>Address</dt>
            <dd>{{ user.homeAddress || '-' }}</dd>
            <dt>Date of Birth</dt>
            <dd>{{ user.dateOfBirth || '-' }}</dd>
            <dt>Provider</dt>
            <dd>{{ user.oauthProvider || 'admin' }}</dd>
            <dt>Score</dt>
            <dd>
              <span v-if="user.score != null" class="score" :class="scoreClass(user.score)">{{ user.score }}</span>
              <span v-else>-</span>
            </dd>
            <dt>Created</dt>
            <dd>{{ formatDate(user.createdAt) }}</dd>
            <dt>Total Alerts</dt>
            <dd>{{ user.alertCount }}</dd>
          </dl>
          <div class="user-actions">
            <button class="btn btn-primary" :disabled="scanning" @click="runScan">
              {{ scanning ? scanProgressLabel : 'Run Scan' }}
            </button>
            <router-link :to="`/users/${user.id}/scans`" class="btn btn-secondary">Scan History</router-link>
            <button class="btn btn-danger" @click="confirmDelete">Delete User</button>
          </div>
        </div>
      </div>

      <!-- Scan Results (shown after running a scan) -->
      <div v-if="scanResults" class="scan-results card">
        <h3>Scan complete — {{ scanResults.totalAlerts }} alerts found</h3>
      </div>

      <h2>Alerts</h2>
      <table>
        <thead>
          <tr>
            <th>Category</th>
            <th>Severity</th>
            <th>Title</th>
            <th>Resolved</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="alert in alerts" :key="alert.id">
            <td><AlertBadge type="category" :value="alert.category" /></td>
            <td><AlertBadge type="severity" :value="alert.severity" /></td>
            <td>{{ alert.title }}</td>
            <td>{{ alert.resolved ? 'Yes' : 'No' }}</td>
            <td>{{ formatDate(alert.createdAt) }}</td>
          </tr>
          <tr v-if="alerts.length === 0">
            <td colspan="5" class="empty">No alerts for this user</td>
          </tr>
        </tbody>
      </table>

      <Pagination :page="alertPage" :total-pages="alertTotalPages" @change="loadAlerts" />
    </template>
  </div>
</template>

<script>
import api from '../api.js'
import AlertBadge from '../components/AlertBadge.vue'
import Pagination from '../components/Pagination.vue'

export default {
  components: { AlertBadge, Pagination },
  data() {
    return {
      user: null,
      alerts: [],
      alertPage: 0,
      alertTotalPages: 0,
      loading: true,
      error: null,
      scanning: false,
      scanResults: null,
      scanJobId: null,
      scanProgress: null,
      pollInterval: null,
    }
  },
  computed: {
    scanProgressLabel() {
      switch (this.scanProgress) {
        case 'breach': return 'Scanning breaches...'
        case 'identity': return 'Scanning identity...'
        case 'pii': return 'Scanning PII...'
        case 'social': return 'Scanning social...'
        default: return 'Starting scan...'
      }
    },
  },
  async created() {
    await this.loadUser()
  },
  beforeUnmount() {
    this.stopPolling()
  },
  methods: {
    async loadUser() {
      try {
        const { data } = await api.getUser(this.$route.params.id)
        this.user = data
        await this.loadAlerts(0)
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to load user'
      } finally {
        this.loading = false
      }
    },
    async loadAlerts(page) {
      try {
        const { data } = await api.getUserAlerts(this.$route.params.id, page)
        this.alerts = data.content
        this.alertPage = data.page
        this.alertTotalPages = data.totalPages
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to load alerts'
      }
    },
    async runScan() {
      this.scanning = true
      this.scanResults = null
      this.error = null
      try {
        const { data } = await api.triggerScan(this.$route.params.id)
        this.scanJobId = data.jobId
        this.scanProgress = data.progress
        this.startPolling()
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to start scan'
        this.scanning = false
      }
    },
    startPolling() {
      this.pollInterval = setInterval(async () => {
        try {
          const { data } = await api.getScanJobStatus(this.$route.params.id, this.scanJobId)
          this.scanProgress = data.progress
          if (data.status === 'COMPLETED') {
            this.stopPolling()
            this.scanning = false
            this.scanResults = { totalAlerts: data.totalAlerts }
            // Reload user and alerts to reflect new data
            const userResp = await api.getUser(this.$route.params.id)
            this.user = userResp.data
            await this.loadAlerts(0)
          } else if (data.status === 'FAILED') {
            this.stopPolling()
            this.scanning = false
            this.error = data.errorMessage || 'Scan failed'
          }
        } catch (e) {
          this.stopPolling()
          this.scanning = false
          this.error = 'Lost connection to scan job'
        }
      }, 2000)
    },
    stopPolling() {
      if (this.pollInterval) {
        clearInterval(this.pollInterval)
        this.pollInterval = null
      }
    },
    async confirmDelete() {
      if (!confirm(`Delete user ${this.user.email}? This cannot be undone.`)) return
      try {
        await api.deleteUser(this.user.id)
        this.$router.push('/users')
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to delete user'
      }
    },
    formatDate(iso) {
      return new Date(iso).toLocaleDateString()
    },
    scoreClass(score) {
      if (score >= 80) return 'score-good'
      if (score >= 50) return 'score-fair'
      return 'score-poor'
    },
  },
}
</script>

<style scoped>
.header {
  margin-bottom: 24px;
}

.back {
  font-size: 0.85em;
  color: #999;
}

h1 {
  font-size: 1.5em;
  margin-top: 4px;
}

h2 {
  font-size: 1.2em;
  margin: 24px 0 12px;
}

.info-grid {
  margin-bottom: 16px;
}

.card h3 {
  margin-bottom: 12px;
  font-size: 1em;
  color: #666;
}

dl {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 8px 16px;
  margin-bottom: 16px;
}

dt {
  font-weight: 600;
  color: #666;
  font-size: 0.85em;
}

dd {
  font-size: 0.95em;
}

.user-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.btn-primary {
  background: #4a90d9;
  color: #fff;
}
.btn-primary:hover:not(:disabled) { background: #357abd; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-secondary {
  background: #eee;
  color: #333;
  text-align: center;
}
.btn-secondary:hover { background: #ddd; text-decoration: none; }

.scan-results {
  margin-bottom: 24px;
}

.scan-results h3 {
  font-size: 1em;
  color: #27ae60;
  margin: 0;
}

.score { font-weight: 600; }
.score-good { color: #27ae60; }
.score-fair { color: #f39c12; }
.score-poor { color: #e74c3c; }

.loading, .error {
  padding: 40px;
  text-align: center;
  color: #999;
}

.error { color: #e74c3c; }
.empty { text-align: center; color: #999; padding: 24px; }
</style>
