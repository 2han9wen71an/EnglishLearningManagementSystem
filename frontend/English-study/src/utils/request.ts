import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Cookies from 'js-cookie'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API的基础URL，使用相对路径，将由Vite代理转发
  timeout: 15000 // 请求超时时间
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

      // 401: 未登录或token过期
      if (response.status === 401) {
        // 重新登录
        ElMessageBox.confirm('您已登出，可以取消继续留在该页面，或者重新登录', '确定登出', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 清除token
          Cookies.remove('token')
          // 跳转到登录页面
          location.href = '/login'
        })
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    NProgress.done()
    console.log('err' + error)
    ElMessage({
      message: error.message || '请求失败',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
