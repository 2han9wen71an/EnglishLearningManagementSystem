import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

// 布局组件
const MainLayout = () => import('@/layouts/MainLayout.vue')

// 路由配置
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/login/RegisterView.vue'),
      meta: { title: '注册', requiresAuth: false }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        // 主页/仪表盘
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '首页', requiresAuth: true }
        },
        // 个人中心
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/user/ProfileView.vue'),
          meta: { title: '个人中心', requiresAuth: true }
        },
        // 用户信息编辑
        {
          path: 'profile/edit',
          name: 'ProfileEdit',
          component: () => import('@/views/user/ProfileEditView.vue'),
          meta: { title: '编辑个人信息', requiresAuth: true }
        },
        // 单词学习
        {
          path: 'words',
          name: 'Words',
          component: () => import('@/views/words/WordsView.vue'),
          meta: { title: '单词学习', requiresAuth: true }
        },
        // 听力练习
        {
          path: 'listening',
          name: 'Listening',
          component: () => import('@/views/listening/ListeningView.vue'),
          meta: { title: '听力练习', requiresAuth: true }
        },
        // 阅读
        {
          path: 'reading',
          name: 'Reading',
          component: () => import('@/views/reading/ReadingView.vue'),
          meta: { title: '阅读', requiresAuth: true }
        },
        // 情景对话
        {
          path: 'dialogue',
          name: 'Dialogue',
          component: () => import('@/views/dialogue/DialogueView.vue'),
          meta: { title: '情景对话', requiresAuth: true }
        },
        // 作文批改
        {
          path: 'essay',
          name: 'Essay',
          component: () => import('@/views/essay/EssayView.vue'),
          meta: { title: '作文批改', requiresAuth: true }
        },
        // 发音评测
        {
          path: 'pronunciation',
          name: 'Pronunciation',
          component: () => import('@/views/pronunciation/PronunciationView.vue'),
          meta: { title: '发音评测', requiresAuth: true }
        },
        // 考试
        {
          path: 'exam',
          name: 'Exam',
          component: () => import('@/views/exam/ExamView.vue'),
          meta: { title: '考试', requiresAuth: true }
        },
        // 单词卡片
        {
          path: 'wordcards',
          name: 'WordCards',
          component: () => import('@/views/wordcards/WordCardsView.vue'),
          meta: { title: '单词卡片', requiresAuth: true }
        }
      ]
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFoundView.vue'),
      meta: { title: '404', requiresAuth: false }
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 英语学习管理系统` : '英语学习管理系统'

  // 检查是否需要登录权限
  if (to.meta.requiresAuth) {
    const token = Cookies.get('token')
    if (!token) {
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
