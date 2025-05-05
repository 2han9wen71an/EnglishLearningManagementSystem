import request from '@/utils/request'

// 获取听力列表
export function getListeningList(params: { level?: string; year?: string; page?: number; size?: number; query?: string }) {
  return request({
    url: '/listening',
    method: 'get',
    params
  })
}

// 获取听力详情
export function getListeningDetail(id: number) {
  return request({
    url: `/listening/${id}`,
    method: 'get'
  })
}

// 添加听力（管理员）
export function addListening(data: any) {
  return request({
    url: '/admin/listening',
    method: 'post',
    data
  })
}

// 更新听力（管理员）
export function updateListening(id: number, data: any) {
  return request({
    url: `/admin/listening/${id}`,
    method: 'put',
    data
  })
}

// 删除听力（管理员）
export function deleteListening(id: number) {
  return request({
    url: `/admin/listening/${id}`,
    method: 'delete'
  })
}

// 上传音频文件
export function uploadAudio(data: FormData) {
  return request({
    url: '/upload/audio',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取听力等级列表
export function getListeningLevelList() {
  return request({
    url: '/listening/levels',
    method: 'get'
  })
}
