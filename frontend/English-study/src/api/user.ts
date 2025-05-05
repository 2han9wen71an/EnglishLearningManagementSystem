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

// 获取用户列表（管理员）
export function getUserList(params?: { role?: number; activeStatus?: number; page?: number; size?: number; query?: string }) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 添加用户（管理员）
export function addUser(data: any) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

// 更新用户（管理员）
export function updateUser(userId: number, data: any) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'put',
    data
  })
}

// 删除用户（管理员）
export function deleteUser(userId: number) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

// 修改密码
export function changePassword(userId: number, data: { oldPassword: string; newPassword: string }) {
  return request({
    url: `/users/${userId}/password`,
    method: 'put',
    data
  })
}
