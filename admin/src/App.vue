<template>
  <div id="app">
    <Navbar />
    <main class="container">
      <router-view />
    </main>
  </div>
</template>

<script>
import Navbar from './components/Navbar.vue'

export default {
  components: { Navbar },
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f5f5;
  color: #333;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

a {
  color: #4a90d9;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #fafafa;
  font-weight: 600;
  font-size: 0.85em;
  text-transform: uppercase;
  color: #666;
}

tr:hover {
  background: #f9f9f9;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.btn {
  display: inline-block;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 500;
}

.btn-danger {
  background: #e74c3c;
  color: #fff;
}

.btn-danger:hover {
  background: #c0392b;
}
</style>
