import request from '@/utils/request'

// 获取考试列表
export function getExamList(params?: { gradeId?: number; status?: number; page?: number; size?: number; query?: string }) {
  return request({
    url: '/exams',
    method: 'get',
    params
  })
}

// 获取考试详情
export function getExamDetail(examId: number) {
  return request({
    url: `/exams/${examId}`,
    method: 'get'
  })
}

// 添加考试（管理员）
export function addExam(data: any) {
  return request({
    url: '/exams',
    method: 'post',
    data
  })
}

// 更新考试（管理员）
export function updateExam(examId: number, data: any) {
  return request({
    url: `/exams/${examId}`,
    method: 'put',
    data
  })
}

// 删除考试（管理员）
export function deleteExam(examId: number) {
  return request({
    url: `/exams/${examId}`,
    method: 'delete'
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

// 获取考试题目列表
export function getExamQuestionList(examId: number) {
  return request({
    url: `/exams/${examId}/questions`,
    method: 'get'
  })
}

// 添加考试题目（管理员）
export function addExamQuestion(examId: number, data: any) {
  return request({
    url: `/exams/${examId}/questions`,
    method: 'post',
    data
  })
}

// 更新考试题目（管理员）
export function updateExamQuestion(examId: number, questionId: number, data: any) {
  return request({
    url: `/exams/${examId}/questions/${questionId}`,
    method: 'put',
    data
  })
}

// 删除考试题目（管理员）
export function deleteExamQuestion(examId: number, questionId: number) {
  return request({
    url: `/exams/${examId}/questions/${questionId}`,
    method: 'delete'
  })
}

// 获取考试等级列表
export function getExamGradeList() {
  return request({
    url: '/exams/grades',
    method: 'get'
  })
}
