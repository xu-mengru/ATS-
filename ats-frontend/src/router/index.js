import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/positions'
  },
  {
    path: '/positions',
    name: 'PositionList',
    component: () => import('../views/PositionList.vue')
  },
  {
    path: '/positions/create',
    name: 'PositionCreate',
    component: () => import('../views/PositionForm.vue')
  },
  {
    path: '/positions/:id/edit',
    name: 'PositionEdit',
    component: () => import('../views/PositionForm.vue')
  },
  {
    path: '/positions/:id',
    name: 'PositionDetail',
    component: () => import('../views/PositionDetail.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
