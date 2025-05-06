import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Cookies from 'js-cookie'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API的基础URL，使用相对路径，将由Vite代理转发
  timeout: 60000 // 请求超时时间，增加到60秒以适应LLM处理时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    NProgress.start()

    // 从cookie中获取token
    const token = Cookies.get('token')
    if (token) {
      // 让每个请求携带token
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    console.log(error)
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    NProgress.done()
    const res = response.data

    // 如果返回的状态码不是200，说明接口请求失败
    // 这里可以根据后端API的实际情况进行修改
    if (!res.success) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })

      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    NProgress.done()

    // 处理HTTP状态码错误
    if (error.response) {
      const { status } = error.response

      // 401: 未登录或token过期
      if (status === 401) {
        // 清除token和用户信息
        Cookies.remove('token')
        Cookies.remove('userId')
        Cookies.remove('role')
        localStorage.removeItem('userInfo')

        // 重新登录
        ElMessageBox.confirm('您的登录已过期，请重新登录', '登录过期', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 跳转到登录页面
          location.href = '/login'
        })
      }
      // 403: 权限不足
      else if (status === 403) {
        ElMessage({
          message: '权限不足，无法访问',
          type: 'error',
          duration: 5 * 1000
        })
      }
      // 404: 资源不存在
      else if (status === 404) {
        ElMessage({
          message: '请求的资源不存在',
          type: 'error',
          duration: 5 * 1000
        })
      }
      // 500: 服务器错误
      else if (status === 500) {
        ElMessage({
          message: '服务器错误，请联系管理员',
          type: 'error',
          duration: 5 * 1000
        })
      }
      // 其他错误
      else {
        ElMessage({
          message: error.response.data?.message || '请求失败',
          type: 'error',
          duration: 5 * 1000
        })
      }
    } else {
      // 网络错误
      ElMessage({
        message: '网络错误，请检查您的网络连接',
        type: 'error',
        duration: 5 * 1000
      })
    }

    return Promise.reject(error)
  }
)

export default service
