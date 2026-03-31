import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1/admin',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('adminToken') || 'changeme'
  config.headers.Authorization = `Bearer ${token}`
  return config
})

export default {
  getStats() {
    return api.get('/stats')
  },

  getUsers(page = 0, size = 20) {
    return api.get('/users', { params: { page, size } })
  },

  getUser(id) {
    return api.get(`/users/${id}`)
  },

  getUserAlerts(id, page = 0, size = 20) {
    return api.get(`/users/${id}/alerts`, { params: { page, size } })
  },

  deleteUser(id) {
    return api.delete(`/users/${id}`)
  },
}
