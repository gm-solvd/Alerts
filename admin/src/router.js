import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from './views/Dashboard.vue'
import UserList from './views/UserList.vue'
import UserDetail from './views/UserDetail.vue'

const routes = [
  { path: '/', component: Dashboard },
  { path: '/users', component: UserList },
  { path: '/users/:id', component: UserDetail },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
