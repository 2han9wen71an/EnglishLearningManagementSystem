import Cookies from 'js-cookie'

// Token相关操作
export const getToken = (): string | undefined => {
  return Cookies.get('token')
}

export const setToken = (token: string, expires: number = 1): void => {
  Cookies.set('token', token, { expires })
}

export const removeToken = (): void => {
  Cookies.remove('token')
}

// 用户ID相关操作
export const getUserId = (): string | undefined => {
  return Cookies.get('userId')
}

export const setUserId = (userId: string, expires: number = 1): void => {
  Cookies.set('userId', userId, { expires })
}

export const removeUserId = (): void => {
  Cookies.remove('userId')
}

// 用户角色相关操作
export const getUserRole = (): string | undefined => {
  return Cookies.get('role')
}

export const setUserRole = (role: string, expires: number = 1): void => {
  Cookies.set('role', role, { expires })
}

export const removeUserRole = (): void => {
  Cookies.remove('role')
}

// 用户信息相关操作
export const getUserInfo = (): any => {
  const userInfoStr = localStorage.getItem('userInfo')
  return userInfoStr ? JSON.parse(userInfoStr) : null
}

export const setUserInfo = (userInfo: any): void => {
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

export const removeUserInfo = (): void => {
  localStorage.removeItem('userInfo')
}

// 清除所有认证信息
export const clearAuth = (): void => {
  removeToken()
  removeUserId()
  removeUserRole()
  removeUserInfo()
}

// 检查是否已登录
export const isAuthenticated = (): boolean => {
  return !!getToken()
}

// 检查是否是管理员
export const isAdmin = (): boolean => {
  return getUserRole() === '1'
}
