<template>
  <div class="admin-reading">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>阅读管理</h2>
          <el-button type="primary" @click="handleAddReading">
            <el-icon><Plus /></el-icon>添加阅读材料
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索标题"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="levelFilter" placeholder="等级筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in levelOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="readingList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)">
              {{ getLevelName(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditReading(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handlePreviewReading(scope.row)"
            >
              预览
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteReading(scope.row)"
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

    <!-- 阅读材料表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加阅读材料' : '编辑阅读材料'"
      width="800px"
    >
      <el-form
        ref="readingFormRef"
        :model="readingForm"
        :rules="readingFormRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="readingForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="readingForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="readingForm.level" placeholder="请选择等级">
            <el-option
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="readingForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div class="editor-container">
            <div id="editor"></div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReadingForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="阅读材料预览"
      width="800px"
    >
      <h2 class="preview-title">{{ previewReading.title }}</h2>
      <div v-if="previewReading.author" class="preview-author">作者：{{ previewReading.author }}</div>
      <div class="preview-level">等级：{{ getLevelName(previewReading.level) }}</div>
      <div class="preview-time">创建时间：{{ previewReading.createTime }}</div>
      <div v-if="previewReading.description" class="preview-description">
        <h3>描述</h3>
        <p>{{ previewReading.description }}</p>
      </div>
      <div class="preview-content">
        <h3>内容</h3>
        <div v-html="previewReading.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getReadingList, addReading, updateReading, deleteReading } from '@/api/reading'

// 引入富文本编辑器
let editor: any = null

// 阅读材料列表数据
const readingList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const levelFilter = ref('')

// 等级选项
const levelOptions = [
  { value: 'cet4', label: '四级' },
  { value: 'cet6', label: '六级' },
  { value: 'ielts', label: '雅思' },
  { value: 'toefl', label: '托福' }
]

// 获取等级名称
const getLevelName = (levelId: string) => {
  const level = levelOptions.find(item => item.value === levelId)
  return level ? level.label : '未知'
}

// 获取等级标签类型
const getLevelTagType = (levelId: string) => {
  switch (levelId) {
    case 'cet4': return ''
    case 'cet6': return 'success'
    case 'ielts': return 'warning'
    case 'toefl': return 'danger'
    default: return 'info'
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const readingFormRef = ref<FormInstance>()
const readingForm = reactive({
  id: 0,
  title: '',
  level: 'cet4',
  content: '',
  author: '',
  description: ''
})

// 预览相关
const previewVisible = ref(false)
const previewReading = reactive({
  title: '',
  level: '',
  content: '',
  createTime: '',
  author: '',
  description: ''
})

// 表单验证规则
const readingFormRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  author: [
    { required: false, message: '请输入作者', trigger: 'blur' },
    { max: 50, message: '长度不超过 50 个字符', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ],
  description: [
    { required: false, message: '请输入描述', trigger: 'blur' },
    { max: 500, message: '长度不超过 500 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
})

// 初始化富文本编辑器
const initEditor = () => {
  // 这里应该引入并初始化富文本编辑器，如 wangEditor, CKEditor 等
  // 示例代码，实际使用时需要替换为真实的编辑器初始化代码
  setTimeout(() => {
    const editorElement = document.getElementById('editor')
    if (editorElement) {
      // 模拟编辑器初始化
      editorElement.innerHTML = '<textarea style="width: 100%; height: 300px;" placeholder="请输入阅读材料内容"></textarea>'
      const textarea = editorElement.querySelector('textarea')
      if (textarea) {
        textarea.value = readingForm.content
        textarea.addEventListener('input', (e) => {
          readingForm.content = (e.target as HTMLTextAreaElement).value
        })
      }
    }
  }, 100)
}

// 原始阅读材料列表数据（未筛选）
const originalReadingList = ref([])

// 获取阅读材料列表
const fetchReadingList = () => {
  loading.value = true

  // 调用API获取阅读材料列表（不带筛选参数）
  getReadingList({})
    .then(res => {
      if (res.success) {
        // 保存原始数据
        originalReadingList.value = res.data || []

        // 将Book对象映射为阅读材料对象
        originalReadingList.value = originalReadingList.value.map((book: any) => ({
          id: book.bookId,
          title: book.bookName,
          level: mapGradeToLevel(book.grade),
          content: book.content,
          createTime: book.createTime || new Date().toISOString().split('T')[0],
          description: book.description,
          author: book.bookUser
        }))

        // 应用本地筛选
        applyLocalFilters()
      } else {
        ElMessage.error(res.message || '获取阅读材料列表失败')
        originalReadingList.value = []
        readingList.value = []
        total.value = 0
      }
    })
    .catch(err => {
      console.error('获取阅读材料列表出错:', err)
      ElMessage.error('获取阅读材料列表失败，请稍后重试')
      originalReadingList.value = []
      readingList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

// 将数字等级映射为字符串等级代码
const mapGradeToLevel = (grade: number) => {
  switch (grade) {
    case 1: return 'cet4'
    case 2: return 'cet6'
    case 3: return 'toefl'
    case 4: return 'ielts'
    default: return 'cet4'
  }
}

// 将字符串等级代码映射为数字等级
const mapLevelToGrade = (level: string) => {
  switch (level) {
    case 'cet4': return 1
    case 'cet6': return 2
    case 'toefl': return 3
    case 'ielts': return 4
    default: return 1
  }
}

// 在前端应用筛选
const applyLocalFilters = () => {
  // 从原始数据开始筛选
  let filteredData = [...originalReadingList.value]

  // 应用等级筛选
  if (levelFilter.value) {
    filteredData = filteredData.filter(item => item.level === levelFilter.value)
  }

  // 应用搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filteredData = filteredData.filter(item =>
      item.title.toLowerCase().includes(query)
    )
  }

  // 计算总数
  total.value = filteredData.length

  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  readingList.value = filteredData.slice(startIndex, endIndex)
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

// 添加阅读材料
const handleAddReading = () => {
  dialogType.value = 'add'
  readingForm.id = 0
  readingForm.title = ''
  readingForm.level = 'cet4'
  readingForm.content = ''
  readingForm.author = ''
  readingForm.description = ''
  dialogVisible.value = true
  // 初始化编辑器
  setTimeout(() => {
    initEditor()
  }, 100)
}

// 编辑阅读材料
const handleEditReading = (row: any) => {
  dialogType.value = 'edit'
  readingForm.id = row.id
  readingForm.title = row.title
  readingForm.level = row.level
  readingForm.content = row.content
  readingForm.author = row.author || ''
  readingForm.description = row.description || ''
  dialogVisible.value = true
  // 初始化编辑器
  setTimeout(() => {
    initEditor()
  }, 100)
}

// 预览阅读材料
const handlePreviewReading = (row: any) => {
  previewReading.title = row.title
  previewReading.level = row.level
  previewReading.content = row.content
  previewReading.createTime = row.createTime
  previewReading.author = row.author || ''
  previewReading.description = row.description || ''
  previewVisible.value = true
}

// 删除阅读材料
const handleDeleteReading = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除阅读材料 ${row.title} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用API删除阅读材料
      const result = await deleteReading(row.id)
      if (result.success) {
        ElMessage.success('删除成功')
        fetchReadingList() // 重新获取完整列表
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除阅读材料失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交阅读材料表单
const submitReadingForm = async () => {
  if (!readingFormRef.value) return

  await readingFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 获取编辑器内容
        const textarea = document.querySelector('#editor textarea') as HTMLTextAreaElement
        if (textarea) {
          readingForm.content = textarea.value
        }

        // 准备提交的数据（将前端字段映射为后端字段）
        const submitData = {
          bookId: readingForm.id,
          bookName: readingForm.title,
          bookUser: readingForm.author,
          description: readingForm.description,
          content: readingForm.content,
          grade: mapLevelToGrade(readingForm.level)
        }

        let result
        if (dialogType.value === 'add') {
          // 添加阅读材料
          result = await addReading(submitData)
          if (result.success) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            fetchReadingList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '添加失败')
          }
        } else {
          // 更新阅读材料
          result = await updateReading(readingForm.id, submitData)
          if (result.success) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            fetchReadingList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '更新失败')
          }
        }
      } catch (error) {
        console.error('提交阅读材料表单失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchReadingList()
})

// 组件卸载前清理编辑器
onBeforeUnmount(() => {
  if (editor) {
    // 清理编辑器资源
    editor = null
  }
})
</script>

<style scoped>
.admin-reading {
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

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 300px;
}

.preview-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 24px;
  font-weight: bold;
}

.preview-level {
  color: #409EFF;
  font-size: 14px;
  margin-bottom: 5px;
}

.preview-author {
  color: #409EFF;
  font-size: 14px;
  margin-bottom: 5px;
}

.preview-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.preview-description {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.preview-description h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}

.preview-content {
  line-height: 1.6;
}

.preview-content h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}
</style>
