// ============================================================================
// router/index.ts - Vue Router 路由配置
// ============================================================================
//
// 【教学要点】Vue Router 是 Vue.js 的官方路由管理器，负责：
//   1. 将 URL 路径映射到对应的 Vue 组件
//   2. 管理浏览器历史记录（前进/后退）
//   3. 支持路由守卫（beforeEach）实现权限控制
//
// createWebHistory 使用 HTML5 History API，URL 更干净（无 # 号）。
// 如果需要兼容老浏览器，可改用 createWebHashHistory（URL 带 # 号）。
// ============================================================================

import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '@/views/ChatView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  // 使用 HTML5 History 模式（URL 无 # 号）
  history: createWebHistory(import.meta.env.BASE_URL),

  // 路由表：定义 URL 路径与组件 of mappings
  routes: [
    {
      path: '/',                // 根路径
      name: 'chat',             // 路由名称，可用于 <router-link :to="{ name: 'chat' }">
      component: ChatView       // 对应的 Vue 组件
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView
    }
  ]
})

export default router

