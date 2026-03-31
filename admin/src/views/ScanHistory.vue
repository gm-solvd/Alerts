<template>
  <div>
    <div class="header">
      <router-link :to="`/users/${$route.params.id}`" class="back">Back to User</router-link>
      <h1>Scan History</h1>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <template v-else>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Scanner</th>
            <th>Findings</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="scan in scans" :key="scan.id" class="scan-row" @click="toggle(scan.id)">
            <td>{{ formatDateTime(scan.createdAt) }}</td>
            <td><span class="scan-type">{{ scan.scanType }}</span></td>
            <td class="findings-preview">{{ truncate(scan.findings, 80) }}</td>
          </tr>
          <tr v-if="scans.length === 0">
            <td colspan="3" class="empty">No scans yet</td>
          </tr>
        </tbody>
      </table>

      <!-- Expanded findings -->
      <div v-if="expanded" class="card findings-detail">
        <h3>{{ expandedScan.scanType }} — {{ formatDateTime(expandedScan.createdAt) }}</h3>
        <pre>{{ expandedScan.findings }}</pre>
      </div>

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
      scans: [],
      page: 0,
      totalPages: 0,
      loading: true,
      error: null,
      expandedId: null,
    }
  },
  computed: {
    expanded() {
      return this.expandedId != null
    },
    expandedScan() {
      return this.scans.find(s => s.id === this.expandedId)
    },
  },
  async created() {
    await this.loadPage(0)
  },
  methods: {
    async loadPage(page) {
      this.loading = true
      this.error = null
      try {
        const { data } = await api.getScanHistory(this.$route.params.id, page)
        this.scans = data.content
        this.page = data.page
        this.totalPages = data.totalPages
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to load scan history'
      } finally {
        this.loading = false
      }
    },
    toggle(id) {
      this.expandedId = this.expandedId === id ? null : id
    },
    formatDateTime(iso) {
      const d = new Date(iso)
      return d.toLocaleDateString() + ' ' + d.toLocaleTimeString()
    },
    truncate(text, len) {
      if (!text) return '-'
      return text.length > len ? text.substring(0, len) + '...' : text
    },
  },
}
</script>

<style scoped>
.header { margin-bottom: 24px; }
.back { font-size: 0.85em; color: #999; }
h1 { font-size: 1.5em; margin-top: 4px; }

.scan-row {
  cursor: pointer;
}

.scan-row:hover {
  background: #f8f9fa;
}

.scan-type {
  display: inline-block;
  padding: 2px 8px;
  background: #e8f0fe;
  color: #1a73e8;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: 600;
}

.findings-preview {
  font-size: 0.85em;
  color: #666;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.findings-detail {
  margin-top: 16px;
}

.findings-detail h3 {
  font-size: 0.95em;
  color: #555;
  margin-bottom: 12px;
}

.findings-detail pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  font-size: 0.85em;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.loading, .error {
  padding: 40px;
  text-align: center;
  color: #999;
}

.error { color: #e74c3c; }
.empty { text-align: center; color: #999; padding: 24px; }
</style>
