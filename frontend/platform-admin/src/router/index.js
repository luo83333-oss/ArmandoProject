import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/request'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Users from '../views/Users.vue'
import Orders from '../views/Orders.vue'
import Rank from '../views/Rank.vue'
import Stats from '../views/Stats.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/', component: Home, meta: { requiresAuth: true } },
    { path: '/users', component: Users, meta: { requiresAuth: true } },
    { path: '/orders', component: Orders, meta: { requiresAuth: true } },
    { path: '/rank', component: Rank, meta: { requiresAuth: true } },
    { path: '/stats', component: Stats, meta: { requiresAuth: true } }
  ]
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/'
  }
})

export default router
