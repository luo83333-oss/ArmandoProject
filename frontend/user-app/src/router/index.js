import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/login', name: 'login', component: Login },
    { path: '/register', name: 'register', component: Register },
    { path: '/products', name: 'products', component: ProductList },
    { path: '/products/:id', name: 'product-detail', component: ProductDetail },
    { path: '/cart', name: 'cart', component: Cart },
    { path: '/checkout', name: 'checkout', component: Checkout },
    { path: '/orders', name: 'orders', component: Orders },
    { path: '/orders/:id', name: 'order-detail', component: OrderDetail }
  ]
})

export default router
