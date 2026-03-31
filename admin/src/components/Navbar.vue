<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand">Privacy Alert Admin</router-link>
      <div class="nav-links">
        <router-link to="/">Dashboard</router-link>
        <router-link to="/users">Users</router-link>
      </div>
      <div class="token-input">
        <label>Token:</label>
        <input
          type="password"
          :value="token"
          @input="updateToken($event.target.value)"
          placeholder="Admin token"
        />
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  data() {
    return {
      token: localStorage.getItem('adminToken') || 'changeme',
    }
  },
  methods: {
    updateToken(value) {
      this.token = value
      localStorage.setItem('adminToken', value)
    },
  },
}
</script>

<style scoped>
.navbar {
  background: #2c3e50;
  color: #fff;
  padding: 0 24px;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 56px;
  gap: 32px;
}

.brand {
  font-weight: 700;
  font-size: 1.1em;
  color: #fff;
}

.brand:hover {
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 16px;
}

.nav-links a {
  color: #bdc3c7;
  font-size: 0.9em;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #fff;
  text-decoration: none;
}

.token-input {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85em;
}

.token-input label {
  color: #bdc3c7;
}

.token-input input {
  padding: 4px 8px;
  border: 1px solid #4a5568;
  border-radius: 4px;
  background: #34495e;
  color: #fff;
  font-size: 0.9em;
  width: 160px;
}
</style>
