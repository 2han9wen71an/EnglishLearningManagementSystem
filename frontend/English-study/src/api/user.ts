import request from '@/utils/request'

// 用户登录
export function login(data: { email: string; password: string }) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 管理员登录
export function adminLogin(data: { email: string; password: string }) {
  return request({
    url: '/users/admin/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data: { userName: string; password: string; email: string }) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo(userId: number) {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(userId: number, data: any) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

// 获取所有用户（管理员）
export function getAllUsers() {
  return request({
    url: '/users',
    method: 'get'
  })
}
