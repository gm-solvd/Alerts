import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from './views/Dashboard.vue'
import UserList from './views/UserList.vue'
import UserDetail from './views/UserDetail.vue'
import CreateUser from './views/CreateUser.vue'
import ScanHistory from './views/ScanHistory.vue'

const routes = [
  { path: '/', component: Dashboard },
  { path: '/users', component: UserList },
  { path: '/users/new', component: CreateUser },
  { path: '/users/:id', component: UserDetail },
  { path: '/users/:id/scans', component: ScanHistory },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
