import request from '@/utils/request'

// 获取考试列表
export function getExamList() {
  return request({
    url: '/exams',
    method: 'get'
  })
}

// 获取考试详情
export function getExamDetail(examId: number) {
  return request({
    url: `/exams/${examId}`,
    method: 'get'
  })
}

// 开始考试
export function startExam(examId: number, userId: number) {
  return request({
    url: `/exams/${examId}/start`,
    method: 'post',
    data: { userId }
  })
}

// 提交答案
export function submitAnswer(data: { 
  recordId: number; 
  questionId: number; 
  userAnswer: string 
}) {
  return request({
    url: '/exams/answers',
    method: 'post',
    data
  })
}

// 提交考试
export function submitExam(recordId: number) {
  return request({
    url: `/exams/records/${recordId}/submit`,
    method: 'post'
  })
}

// 获取考试结果
export function getExamResult(recordId: number) {
  return request({
    url: `/exams/records/${recordId}`,
    method: 'get'
  })
}
