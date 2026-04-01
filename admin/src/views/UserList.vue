<template>
  <div>
    <div class="header-row">
      <h1>Users</h1>
      <router-link to="/users/new" class="btn btn-primary">+ Create User</router-link>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <template v-else>
      <table>
        <thead>
          <tr>
            <th>Email</th>
            <th>Name</th>
            <th>Provider</th>
            <th>Alerts</th>
            <th>Score</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" class="clickable" @click="$router.push('/users/' + user.id)">
            <td>{{ user.email }}</td>
            <td>{{ user.fullName || '-' }}</td>
            <td>{{ user.oauthProvider || 'email' }}</td>
            <td>{{ user.alertCount }}</td>
            <td>
              <span v-if="user.score != null" class="score" :class="scoreClass(user.score)">{{ user.score }}</span>
              <span v-else class="no-score">-</span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="6" class="empty">No users found</td>
          </tr>
        </tbody>
      </table>

      <Pagination :page="page" :total-pages="totalPages" @change="loadPage" />
    </template>
  </div>
</template>

<script>
import api from '../api.js'
import Pagination from '../components/Pagination.vue'

export default {
  components: { Pagination },
  data() {
    return {
      users: [],
      page: 0,
      totalPages: 0,
      loading: true,
      error: null,
    }
  },
  async created() {
    await this.loadPage(0)
  },
  methods: {
    async loadPage(page) {
      this.loading = true
      this.error = null
      try {
        const { data } = await api.getUsers(page)
        this.users = data.content
        this.page = data.page
        this.totalPages = data.totalPages
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to load users'
      } finally {
        this.loading = false
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
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h1 {
  font-size: 1.5em;
}

.btn-primary {
  background: #4a90d9;
  color: #fff;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9em;
  font-weight: 600;
}

.btn-primary:hover { background: #357abd; }

.clickable {
  cursor: pointer;
}

.score {
  font-weight: 600;
}

.score-good { color: #27ae60; }
.score-fair { color: #f39c12; }
.score-poor { color: #e74c3c; }
.no-score { color: #ccc; }

.loading, .error {
  padding: 40px;
  text-align: center;
  color: #999;
}

.error { color: #e74c3c; }
.empty { text-align: center; color: #999; padding: 24px; }
</style>
