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
            <th style="width: 30px"></th>
            <th>Date</th>
            <th>Scanner</th>
            <th>Findings</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="scan in scans" :key="scan.id">
            <tr class="scan-row" @click="toggle(scan.id)">
              <td class="toggle-cell">{{ expandedId === scan.id ? '\u25BC' : '\u25B6' }}</td>
              <td>{{ formatDateTime(scan.createdAt) }}</td>
              <td><span class="scan-type">{{ scan.scanType }}</span></td>
              <td class="findings-preview">{{ truncate(scan.findings, 80) }}</td>
            </tr>
            <tr v-if="expandedId === scan.id" class="detail-row">
              <td colspan="4">
                <div class="findings-detail" v-if="scan.details && scan.details.length > 0">
                  <div v-for="(finding, idx) in scan.details" :key="idx" class="finding-card">
                    <div class="finding-header">
                      <span class="finding-type" :class="'type-' + finding.type">{{ finding.type }}</span>
                      <strong>{{ finding.name }}</strong>
                      <AlertBadge v-if="finding.severity" type="severity" :value="finding.severity" />
                    </div>
                    <div class="finding-meta">
                      <span v-if="finding.date" class="meta-item">{{ finding.date }}</span>
                      <span v-if="finding.recordCount" class="meta-item">{{ finding.recordCount.toLocaleString() }} records</span>
                    </div>
                    <div v-if="finding.dataClasses && finding.dataClasses.length > 0" class="finding-tags">
                      <span v-for="dc in finding.dataClasses" :key="dc" class="tag">{{ dc }}</span>
                    </div>
                    <div v-if="finding.exposedFields && finding.exposedFields.length > 0" class="finding-tags">
                      <span v-for="field in finding.exposedFields" :key="field" class="tag tag-field">{{ field }}</span>
                    </div>
                    <a v-if="finding.sourceUrl" :href="finding.sourceUrl" target="_blank" rel="noopener" class="source-link">
                      {{ finding.sourceUrl }} &#8599;
                    </a>
                  </div>
                </div>
                <pre v-else class="findings-text">{{ scan.findings || 'No findings' }}</pre>
              </td>
            </tr>
          </template>
          <tr v-if="scans.length === 0">
            <td colspan="4" class="empty">No scans yet</td>
          </tr>
        </tbody>
      </table>

      <Pagination :page="page" :total-pages="totalPages" @change="loadPage" />
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
      scans: [],
      page: 0,
      totalPages: 0,
      loading: true,
      error: null,
      expandedId: null,
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

.toggle-cell {
  text-align: center;
  color: #999;
  font-size: 0.8em;
  user-select: none;
}

.detail-row td {
  padding: 0 16px 16px;
  background: #fafafa;
}

.finding-card {
  padding: 12px;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
}

.finding-card:last-child {
  margin-bottom: 0;
}

.finding-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.finding-type {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 0.75em;
  font-weight: 600;
  text-transform: uppercase;
}

.type-breach { background: #fce4ec; color: #c62828; }
.type-identity { background: #e8eaf6; color: #283593; }
.type-pii { background: #fff3e0; color: #e65100; }
.type-social { background: #e0f2f1; color: #00695c; }

.finding-meta {
  display: flex;
  gap: 16px;
  font-size: 0.85em;
  color: #666;
  margin-bottom: 6px;
}

.finding-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e8f0fe;
  color: #1a73e8;
  border-radius: 10px;
  font-size: 0.75em;
}

.tag-field {
  background: #fef3cd;
  color: #856404;
}

.source-link {
  display: inline-block;
  font-size: 0.85em;
  color: #1a73e8;
  text-decoration: none;
  word-break: break-all;
}

.source-link:hover {
  text-decoration: underline;
}

.findings-text {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  font-size: 0.85em;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
  margin: 0;
}

.loading, .error {
  padding: 40px;
  text-align: center;
  color: #999;
}

.error { color: #e74c3c; }
.empty { text-align: center; color: #999; padding: 24px; }
</style>
