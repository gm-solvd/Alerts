<template>
  <div>
    <h1>Dashboard</h1>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else class="stats-grid">
      <div class="card stat-card">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-label">Total Users</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value">{{ stats.totalAlerts }}</div>
        <div class="stat-label">Total Alerts</div>
      </div>

      <div class="card wide">
        <h3>Alerts by Category</h3>
        <div class="breakdown">
          <div v-for="(count, category) in stats.alertsByCategory" :key="category" class="breakdown-row">
            <span class="breakdown-label">{{ formatLabel(category) }}</span>
            <span class="breakdown-value">{{ count }}</span>
            <div class="breakdown-bar">
              <div class="bar-fill" :style="{ width: barWidth(count, stats.totalAlerts) }"></div>
            </div>
          </div>
          <div v-if="Object.keys(stats.alertsByCategory).length === 0" class="empty">No alerts yet</div>
        </div>
      </div>

      <div class="card wide">
        <h3>Alerts by Severity</h3>
        <div class="breakdown">
          <div v-for="(count, severity) in stats.alertsBySeverity" :key="severity" class="breakdown-row">
            <span class="breakdown-label">{{ formatLabel(severity) }}</span>
            <span class="breakdown-value">{{ count }}</span>
            <div class="breakdown-bar">
              <div class="bar-fill" :class="'severity-' + severity.toLowerCase()" :style="{ width: barWidth(count, stats.totalAlerts) }"></div>
            </div>
          </div>
          <div v-if="Object.keys(stats.alertsBySeverity).length === 0" class="empty">No alerts yet</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api.js'

export default {
  data() {
    return {
      stats: { totalUsers: 0, totalAlerts: 0, alertsByCategory: {}, alertsBySeverity: {} },
      loading: true,
      error: null,
    }
  },
  async created() {
    try {
      const { data } = await api.getStats()
      this.stats = data
    } catch (e) {
      this.error = e.response?.data?.message || 'Failed to load stats'
    } finally {
      this.loading = false
    }
  },
  methods: {
    formatLabel(value) {
      return value.replace(/_/g, ' ')
    },
    barWidth(count, total) {
      if (!total) return '0%'
      return Math.round((count / total) * 100) + '%'
    },
  },
}
</script>

<style scoped>
h1 {
  margin-bottom: 24px;
  font-size: 1.5em;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 32px;
}

.stat-value {
  font-size: 2.5em;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 0.9em;
  color: #999;
  margin-top: 4px;
  text-transform: uppercase;
}

.wide {
  grid-column: span 2;
}

.wide h3 {
  margin-bottom: 12px;
  font-size: 1em;
  color: #666;
}

.breakdown-row {
  display: grid;
  grid-template-columns: 160px 60px 1fr;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
}

.breakdown-label {
  font-size: 0.9em;
  text-transform: capitalize;
}

.breakdown-value {
  font-weight: 600;
  text-align: right;
}

.breakdown-bar {
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #4a90d9;
  border-radius: 4px;
  transition: width 0.3s;
}

.bar-fill.severity-critical { background: #e74c3c; }
.bar-fill.severity-high { background: #e67e22; }
.bar-fill.severity-medium { background: #f1c40f; }
.bar-fill.severity-low { background: #2ecc71; }

.loading, .error {
  padding: 40px;
  text-align: center;
  color: #999;
}

.error { color: #e74c3c; }
.empty { color: #999; font-size: 0.9em; padding: 8px 0; }
</style>
