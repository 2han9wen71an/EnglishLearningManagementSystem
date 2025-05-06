<template>
  <div class="admin-exams">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>考试管理</h2>
          <el-button type="primary" @click="handleAddExam">
            <el-icon><Plus /></el-icon>添加考试
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索考试标题"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="examList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="examId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="duration" label="时长(分钟)" width="120" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="passScore" label="及格分" width="80" />
        <el-table-column prop="gradeName" label="等级" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditExam(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleManageQuestions(scope.row)"
            >
              题目管理
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteExam(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 考试表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加考试' : '编辑考试'"
      width="600px"
    >
      <el-form
        ref="examFormRef"
        :model="examForm"
        :rules="examFormRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="examForm.description" type="textarea" :rows="3" placeholder="请输入考试描述" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="examForm.duration" :min="1" :max="240" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="examForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="及格分" prop="passScore">
          <el-input-number v-model="examForm.passScore" :min="1" :max="examForm.totalScore" />
        </el-form-item>
        <el-form-item label="等级" prop="gradeId">
          <el-select v-model="examForm.gradeId" placeholder="请选择等级">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="examForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExamForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { getExamList, getExamDetail, addExam, updateExam, deleteExam } from '@/api/exam'

const router = useRouter()

// 考试列表数据
const examList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const statusFilter = ref('')

// 状态选项
const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

// 等级选项
const gradeOptions = [
  { value: 1, label: '四级' },
  { value: 2, label: '六级' },
  { value: 3, label: '雅思' },
  { value: 4, label: '托福' }
]

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const examFormRef = ref<FormInstance>()
const examForm = reactive({
  examId: 0,
  title: '',
  description: '',
  duration: 60,
  totalScore: 100,
  passScore: 60,
  gradeId: 1,
  status: 1
})

// 表单验证规则
const examFormRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入考试标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 240, message: '时长在 1 到 240 分钟之间', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' },
    { type: 'number', min: 1, max: 1000, message: '总分在 1 到 1000 之间', trigger: 'blur' }
  ],
  passScore: [
    { required: true, message: '请输入及格分', trigger: 'blur' },
    { type: 'number', min: 1, message: '及格分必须大于 0', trigger: 'blur' }
  ],
  gradeId: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ]
})

// 原始考试列表数据（未筛选）
const originalExamList = ref([])

// 获取考试列表
const fetchExamList = () => {
  loading.value = true

  // 调用API获取考试列表（不带筛选参数）
  getExamList({})
    .then(res => {
      if (res.success) {
        // 保存原始数据
        originalExamList.value = res.data || []
        // 应用本地筛选
        applyLocalFilters()
      } else {
        ElMessage.error(res.message || '获取考试列表失败')
        originalExamList.value = []
        examList.value = []
        total.value = 0
      }
    })
    .catch(err => {
      console.error('获取考试列表出错:', err)
      ElMessage.error('获取考试列表失败，请稍后重试')
      originalExamList.value = []
      examList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

// 在前端应用筛选
const applyLocalFilters = () => {
  // 从原始数据开始筛选
  let filteredData = [...originalExamList.value]

  // 应用状态筛选
  if (statusFilter.value !== '') {
    filteredData = filteredData.filter(item => item.status === statusFilter.value)
  }

  // 应用搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filteredData = filteredData.filter(item =>
      (item.title && item.title.toLowerCase().includes(query)) ||
      (item.description && item.description.toLowerCase().includes(query))
    )
  }

  // 计算总数
  total.value = filteredData.length

  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  examList.value = filteredData.slice(startIndex, endIndex)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  applyLocalFilters()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  applyLocalFilters()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  applyLocalFilters()
}

// 添加考试
const handleAddExam = () => {
  dialogType.value = 'add'
  examForm.examId = 0
  examForm.title = ''
  examForm.description = ''
  examForm.duration = 60
  examForm.totalScore = 100
  examForm.passScore = 60
  examForm.gradeId = 1
  examForm.status = 1
  dialogVisible.value = true
}

// 编辑考试
const handleEditExam = (row: any) => {
  dialogType.value = 'edit'
  examForm.examId = row.examId
  examForm.title = row.title
  examForm.description = row.description
  examForm.duration = row.duration
  examForm.totalScore = row.totalScore
  examForm.passScore = row.passScore
  examForm.gradeId = row.gradeId
  examForm.status = row.status
  dialogVisible.value = true
}

// 管理题目
const handleManageQuestions = (row: any) => {
  router.push(`/admin/exams/${row.examId}/questions`)
}

// 删除考试
const handleDeleteExam = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除考试 ${row.title} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用API删除考试
      const result = await deleteExam(row.examId)
      if (result.success) {
        ElMessage.success('删除成功')
        fetchExamList() // 重新获取完整列表
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除考试失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交考试表单
const submitExamForm = async () => {
  if (!examFormRef.value) return

  await examFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 准备提交的数据
        const submitData = {
          examId: examForm.examId,
          title: examForm.title,
          description: examForm.description,
          duration: examForm.duration,
          totalScore: examForm.totalScore,
          passScore: examForm.passScore,
          gradeId: examForm.gradeId,
          status: examForm.status
        }

        let result
        if (dialogType.value === 'add') {
          // 添加考试
          result = await addExam(submitData)
          if (result.success) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            fetchExamList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '添加失败')
          }
        } else {
          // 更新考试
          result = await updateExam(examForm.examId, submitData)
          if (result.success) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            fetchExamList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '更新失败')
          }
        }
      } catch (error) {
        console.error('提交考试表单失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchExamList()
})
</script>

<style scoped>
.admin-exams {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-weight: 600;
}

.search-container {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>