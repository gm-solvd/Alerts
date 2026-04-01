<template>
  <div>
    <div class="header">
      <router-link to="/users" class="back">Back to Users</router-link>
      <h1>Create User</h1>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <form class="card form" @submit.prevent="submit">
      <div class="field">
        <label>Email *</label>
        <input v-model="form.email" type="email" required placeholder="user@example.com" />
      </div>
      <div class="field">
        <label>Full Name *</label>
        <input v-model="form.fullName" type="text" required placeholder="John Doe" />
      </div>
      <div class="field">
        <label>Phone Number</label>
        <input v-model="form.phoneNumber" type="text" placeholder="+1234567890" />
      </div>
      <div class="field">
        <label>Home Address</label>
        <input v-model="form.homeAddress" type="text" placeholder="123 Main St, City, State" />
      </div>
      <div class="field">
        <label>Date of Birth</label>
        <input v-model="form.dateOfBirth" type="date" />
      </div>
      <div class="actions">
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Creating...' : 'Create User' }}
        </button>
        <router-link to="/users" class="btn btn-secondary">Cancel</router-link>
      </div>
    </form>
  </div>
</template>

<script>
import api from '../api.js'

export default {
  data() {
    return {
      form: {
        email: '',
        fullName: '',
        phoneNumber: '',
        homeAddress: '',
        dateOfBirth: '',
      },
      submitting: false,
      error: null,
    }
  },
  methods: {
    async submit() {
      this.submitting = true
      this.error = null
      try {
        const payload = {
          email: this.form.email,
          fullName: this.form.fullName,
          phoneNumber: this.form.phoneNumber || null,
          homeAddress: this.form.homeAddress || null,
          dateOfBirth: this.form.dateOfBirth || null,
        }
        const { data } = await api.createUser(payload)
        this.$router.push(`/users/${data.id}`)
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to create user'
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>

<style scoped>
.header { margin-bottom: 24px; }
.back { font-size: 0.85em; color: #999; }
h1 { font-size: 1.5em; margin-top: 4px; }

.form { max-width: 500px; }

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  font-weight: 600;
  font-size: 0.85em;
  color: #555;
  margin-bottom: 4px;
}

.field input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95em;
}

.field input:focus {
  outline: none;
  border-color: #4a90d9;
  box-shadow: 0 0 0 2px rgba(74, 144, 217, 0.2);
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
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

.error {
  padding: 12px;
  background: #fde8e8;
  color: #c53030;
  border-radius: 6px;
  margin-bottom: 16px;
}
</style>
