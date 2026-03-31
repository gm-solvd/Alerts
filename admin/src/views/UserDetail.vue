<template>
  <div>
    <div class="header">
      <router-link to="/users" class="back">Back to Users</router-link>
      <h1 v-if="user">{{ user.email }}</h1>
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
            <dt>Provider</dt>
            <dd>{{ user.oauthProvider || 'email' }}</dd>
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
          <button class="btn btn-danger" @click="confirmDelete">Delete User</button>
        </div>
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
    }
  },
  async created() {
    await this.loadUser()
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
