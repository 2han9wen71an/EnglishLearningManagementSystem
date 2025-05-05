import request from '@/utils/request'

// 获取作文列表
export function getEssayList() {
  return request({
    url: '/essays',
    method: 'get'
  })
}

// 提交作文
export function submitEssay(data: { title: string; content: string; userId: number }) {
  return request({
    url: '/essays',
    method: 'post',
    data
  })
}

// 获取作文详情
export function getEssayDetail(essayId: number) {
  return request({
    url: `/essays/${essayId}`,
    method: 'get'
  })
}

// 请求AI批改
export function requestAiCorrection(essayId: number) {
  return request({
    url: `/essays/${essayId}/correct`,
    method: 'post'
  })
}

// 获取批改结果
export function getCorrectionResult(essayId: number) {
  return request({
    url: `/essays/${essayId}/correction`,
    method: 'get'
  })
}
