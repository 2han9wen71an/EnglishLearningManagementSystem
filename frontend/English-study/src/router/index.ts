import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isAuthenticated, isAdmin } from '@/utils/auth'

// 布局组件
const MainLayout = () => import('@/layouts/MainLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

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
    // 管理员路由
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        // 管理员主页
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/dashboard/DashboardView.vue'),
          meta: { title: '控制面板', requiresAuth: true, requiresAdmin: true }
        },
        // 用户管理
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/users/UsersView.vue'),
          meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
        },
        // 单词管理
        {
          path: 'words',
          name: 'AdminWords',
          component: () => import('@/views/admin/words/WordsView.vue'),
          meta: { title: '单词管理', requiresAuth: true, requiresAdmin: true }
        },
        // 听力管理
        {
          path: 'listening',
          name: 'AdminListening',
          component: () => import('@/views/admin/listening/ListeningView.vue'),
          meta: { title: '听力管理', requiresAuth: true, requiresAdmin: true }
        },
        // 阅读管理
        {
          path: 'reading',
          name: 'AdminReading',
          component: () => import('@/views/admin/reading/ReadingView.vue'),
          meta: { title: '阅读管理', requiresAuth: true, requiresAdmin: true }
        },
        // 考试管理
        {
          path: 'exams',
          name: 'AdminExams',
          component: () => import('@/views/admin/exams/ExamsView.vue'),
          meta: { title: '考试管理', requiresAuth: true, requiresAdmin: true }
        },
        // 考试题目管理
        {
          path: 'exams/:examId/questions',
          name: 'AdminExamQuestions',
          component: () => import('@/views/admin/exams/QuestionsView.vue'),
          meta: { title: '题目管理', requiresAuth: true, requiresAdmin: true }
        },
        // 成绩管理
        {
          path: 'scores',
          name: 'AdminScores',
          component: () => import('@/views/admin/scores/ScoresView.vue'),
          meta: { title: '成绩管理', requiresAuth: true, requiresAdmin: true }
        },
        // 公告管理
        {
          path: 'notices',
          name: 'AdminNotices',
          component: () => import('@/views/admin/notices/NoticesView.vue'),
          meta: { title: '公告管理', requiresAuth: true, requiresAdmin: true }
        },
        // 数据统计
        /*{
          path: 'statistics',
          name: 'AdminStatistics',
          component: () => import('@/views/admin/statistics/StatisticsView.vue'),
          meta: { title: '数据统计', requiresAuth: true, requiresAdmin: true }
        },*/
        // 个人资料
        {
          path: 'profile',
          name: 'AdminProfile',
          component: () => import('@/views/admin/profile/ProfileView.vue'),
          meta: { title: '个人资料', requiresAuth: true, requiresAdmin: true }
        },
        // 修改密码
        {
          path: 'change-password',
          name: 'AdminChangePassword',
          component: () => import('@/views/admin/profile/ProfileView.vue'),
          meta: { title: '修改密码', requiresAuth: true, requiresAdmin: true }
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
  document.title = to.meta.title ? `${to.meta.title} - 英语知识应用网站系统` : '英语知识应用网站系统'

  // 检查是否需要登录权限
  if (to.meta.requiresAuth) {
    if (!isAuthenticated()) {
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin) {
      // 检查用户是否是管理员
      if (!isAdmin()) {
        ElMessage.error('您没有管理员权限')
        next({ path: '/dashboard' })
        return
      }
    }
  }

  next()
})

export default router
