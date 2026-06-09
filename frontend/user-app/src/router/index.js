import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import ShopRank from '../views/ShopRank.vue'
import Favorites from '../views/Favorites.vue'
import Following from '../views/Following.vue'
import Profile from '../views/Profile.vue'
import { getToken } from '../api/request'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: Home, meta: { showTabbar: true } },
        { path: 'products', name: 'products', component: ProductList, meta: { showTabbar: true } },
        { path: 'cart', name: 'cart', component: Cart, meta: { showTabbar: true, requiresAuth: true } },
        { path: 'profile', name: 'profile', component: Profile, meta: { showTabbar: true } }
      ]
    },
    { path: '/login', name: 'login', component: Login },
    { path: '/register', name: 'register', component: Register },
    { path: '/products/:id', name: 'product-detail', component: ProductDetail },
    { path: '/checkout', name: 'checkout', component: Checkout, meta: { requiresAuth: true } },
    { path: '/orders', name: 'orders', component: Orders, meta: { requiresAuth: true } },
    { path: '/orders/:id', name: 'order-detail', component: OrderDetail, meta: { requiresAuth: true } },
    { path: '/shops/rank', name: 'shop-rank', component: ShopRank },
    { path: '/favorites', name: 'favorites', component: Favorites, meta: { requiresAuth: true } },
    { path: '/following', name: 'following', component: Following, meta: { requiresAuth: true } }
  ]
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
