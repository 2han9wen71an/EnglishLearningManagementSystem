<template>
  <div class="essay-container">
    <!-- 作文列表 -->
    <div v-if="!showWriteEssay && !showEssayDetail">
      <el-card class="essay-list-card">
        <template #header>
          <div class="card-header">
            <span>作文列表</span>
            <el-button type="primary" @click="showWriteEssay = true">写新作文</el-button>
          </div>
        </template>
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="essays.length === 0" class="empty-container">
          <el-empty description="暂无作文">
            <el-button type="primary" @click="showWriteEssay = true">写新作文</el-button>
          </el-empty>
        </div>
        <div v-else class="essay-list">
          <el-table :data="essays" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'info' : 'success'">
                  {{ scope.row.status === 0 ? '未批改' : '已批改' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewEssay(scope.row)">
                  查看
                </el-button>
                <el-button
                  v-if="scope.row.status === 0"
                  type="success"
                  size="small"
                  @click="correctEssay(scope.row)"
                >
                  批改
                </el-button>
                <el-button type="danger" size="small" @click="deleteEssay(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 写作文表单 -->
    <div v-if="showWriteEssay">
      <el-card class="write-essay-card">
        <template #header>
          <div class="card-header">
            <span>写新作文</span>
            <el-button @click="showWriteEssay = false">返回列表</el-button>
          </div>
        </template>
        <el-form :model="essayForm" :rules="essayRules" ref="essayFormRef" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="essayForm.title" placeholder="请输入作文标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="essayForm.content"
              type="textarea"
              :rows="15"
              placeholder="请输入作文内容"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitEssay">提交作文</el-button>
            <el-button @click="showWriteEssay = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 作文详情 -->
    <div v-if="showEssayDetail && currentEssay">
      <el-card class="essay-detail-card">
        <template #header>
          <div class="card-header">
            <span>{{ currentEssay.title }}</span>
            <el-button @click="showEssayDetail = false">返回列表</el-button>
          </div>
        </template>

        <div class="essay-detail">
          <div class="essay-meta">
            <span>提交时间: {{ formatDate(currentEssay.submitTime) }}</span>
            <el-tag :type="currentEssay.status === 0 ? 'info' : 'success'">
              {{ currentEssay.status === 0 ? '未批改' : '已批改' }}
            </el-tag>
          </div>

          <div class="essay-section">
            <h3>原文</h3>
            <div class="essay-content">{{ currentEssay.content }}</div>
          </div>

          <div v-if="currentEssay.status === 0" class="correction-actions">
            <el-button
              type="primary"
              :loading="correctingEssay"
              @click="correctEssay(currentEssay)"
            >
              请求AI批改
            </el-button>
            <p class="tip-text">点击上方按钮，AI将分析您的作文并提供详细的批改意见</p>
          </div>

          <div v-if="correction" class="correction-section">
            <div class="score-section">
              <div class="score-circle">
                <span class="score-value">{{ correction.overallScore }}</span>
              </div>
              <span class="score-label">总体评分</span>
            </div>

            <div class="essay-section">
              <h3>批改后的内容</h3>
              <div class="corrected-content" v-html="correction.correctedContent"></div>
            </div>

            <div class="essay-section">
              <h3>语法错误</h3>
              <el-table v-if="grammarErrors.length > 0" :data="grammarErrors" border style="width: 100%">
                <el-table-column prop="original" label="原文" min-width="150" />
                <el-table-column prop="correction" label="修正" min-width="150" />
                <el-table-column prop="explanation" label="解释" min-width="200" />
                <el-table-column prop="type" label="错误类型" width="120" />
              </el-table>
              <el-empty v-else description="未发现语法错误" />
            </div>

            <div class="essay-section">
              <h3>词汇建议</h3>
              <el-table v-if="vocabularySuggestions.length > 0" :data="vocabularySuggestions" border style="width: 100%">
                <el-table-column prop="original" label="原词" min-width="150" />
                <el-table-column prop="suggestion" label="建议" min-width="150" />
                <el-table-column prop="explanation" label="解释" min-width="200" />
              </el-table>
              <el-empty v-else description="未提供词汇建议" />
            </div>

            <div class="essay-section">
              <h3>结构评价</h3>
              <div class="structure-comments">{{ correction.structureComments }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getUserId } from '@/utils/auth'

// 数据
const loading = ref(true)
const essays = ref<any[]>([])
const showWriteEssay = ref(false)
const showEssayDetail = ref(false)
const currentEssay = ref<any>(null)
const correction = ref<any>(null)
const correctingEssay = ref(false)
const grammarErrors = ref<any[]>([])
const vocabularySuggestions = ref<any[]>([])

// 表单
const essayFormRef = ref()
const essayForm = reactive({
  title: '',
  content: ''
})
const essayRules = {
  title: [
    { required: true, message: '请输入作文标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入作文内容', trigger: 'blur' },
    { min: 50, message: '作文内容至少50个字符', trigger: 'blur' }
  ]
}

// 方法
const fetchEssays = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/essays',
      method: 'get',
      params: { userId: getUserId() }
    })
    essays.value = response.data || []
  } catch (error) {
    console.error('获取作文列表失败:', error)
    ElMessage.error('获取作文列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 使用auth工具类中的getUserId方法

const submitEssay = async () => {
  if (!essayFormRef.value) return

  await essayFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await request({
          url: '/essays',
          method: 'post',
          data: {
            ...essayForm,
            userId: getUserId()
          }
        })

        if (response.success) {
          ElMessage.success('作文提交成功')
          showWriteEssay.value = false
          fetchEssays()
          // 重置表单
          essayForm.title = ''
          essayForm.content = ''
        } else {
          ElMessage.error(response.message || '提交失败，请稍后重试')
        }
      } catch (error) {
        console.error('提交作文失败:', error)
        ElMessage.error('提交作文失败，请稍后重试')
      }
    }
  })
}

const viewEssay = async (essay: any) => {
  currentEssay.value = essay
  showEssayDetail.value = true

  // 如果已批改，获取批改结果
  if (essay.status === 1) {
    try {
      const response = await request({
        url: `/essays/${essay.essayId}/correction`,
        method: 'get'
      })
      if (response.success) {
        correction.value = response.data

        // 解析语法错误和词汇建议
        try {
          grammarErrors.value = JSON.parse(correction.value.grammarErrors || '[]')
          vocabularySuggestions.value = JSON.parse(correction.value.vocabularySuggestions || '[]')
        } catch (e) {
          console.error('解析批改结果出错:', e)
          grammarErrors.value = []
          vocabularySuggestions.value = []
        }
      }
    } catch (error) {
      console.error('获取批改结果失败:', error)
      ElMessage.error('获取批改结果失败，请稍后重试')
    }
  } else {
    correction.value = null
    grammarErrors.value = []
    vocabularySuggestions.value = []
  }
}

const correctEssay = async (essay: any) => {
  if (correctingEssay.value) return

  try {
    correctingEssay.value = true
    const response = await request({
      url: `/essays/${essay.essayId}/correct`,
      method: 'post'
    })

    if (response.success) {
      ElMessage.success('批改成功')
      // 刷新作文列表
      fetchEssays()

      // 如果在详情页，刷新详情
      if (showEssayDetail.value && currentEssay.value) {
        // 更新状态
        currentEssay.value.status = 1
        // 获取批改结果
        viewEssay(currentEssay.value)
      }
    } else {
      ElMessage.error(response.message || '批改失败，请稍后重试')
    }
  } catch (error) {
    console.error('批改作文失败:', error)
    ElMessage.error('批改作文失败，请稍后重试')
  } finally {
    correctingEssay.value = false
  }
}

const deleteEssay = (essay: any) => {
  ElMessageBox.confirm('确定要删除这篇作文吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await request({
        url: `/essays/${essay.essayId}`,
        method: 'delete'
      })
      if (response.success) {
        ElMessage.success('删除成功')
        // 如果在详情页且删除的是当前查看的作文，返回列表
        if (showEssayDetail.value && currentEssay.value && currentEssay.value.essayId === essay.essayId) {
          showEssayDetail.value = false
        }
        // 刷新作文列表
        fetchEssays()
      } else {
        ElMessage.error(response.message || '删除失败，请稍后重试')
      }
    } catch (error) {
      console.error('删除作文失败:', error)
      ElMessage.error('删除作文失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 初始化
onMounted(() => {
  fetchEssays()
})
</script>

<style scoped>
.essay-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.essay-list {
  margin-top: 10px;
}

.write-essay-card, .essay-detail-card {
  margin-bottom: 20px;
}

.essay-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #909399;
}

.essay-section {
  margin-bottom: 30px;
}

.essay-section h3 {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.essay-content, .corrected-content, .structure-comments {
  white-space: pre-wrap;
  line-height: 1.8;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.corrected-content {
  background-color: #f0f9eb;
}

.correction-actions {
  text-align: center;
  margin: 30px 0;
}

.tip-text {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}

.score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.score-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.score-value {
  font-size: 24px;
  font-weight: bold;
}

.score-label {
  font-size: 16px;
  color: #606266;
}
</style>
