import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/request'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Apply from '../views/Apply.vue'
import Products from '../views/Products.vue'
import ProductForm from '../views/ProductForm.vue'
import Orders from '../views/Orders.vue'
import Dashboard from '../views/Dashboard.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/', component: Home, meta: { requiresAuth: true } },
    { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
    { path: '/apply', component: Apply, meta: { requiresAuth: true } },
    { path: '/products', component: Products, meta: { requiresAuth: true } },
    { path: '/products/new', component: ProductForm, meta: { requiresAuth: true } },
    { path: '/products/:id/edit', component: ProductForm, meta: { requiresAuth: true } },
    { path: '/orders', component: Orders, meta: { requiresAuth: true } }
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
