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
      <div class="preview-level">等级：{{ getLevelName(previewReading.level) }}</div>
      <div class="preview-time">创建时间：{{ previewReading.createTime }}</div>
      <div class="preview-content" v-html="previewReading.content"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

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
  content: ''
})

// 预览相关
const previewVisible = ref(false)
const previewReading = reactive({
  title: '',
  level: '',
  content: '',
  createTime: ''
})

// 表单验证规则
const readingFormRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择等级', trigger: 'change' }
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

// 获取阅读材料列表
const fetchReadingList = () => {
  loading.value = true
  // 这里应该调用API获取阅读材料列表
  // 暂时使用模拟数据
  setTimeout(() => {
    const mockReadings = [
      { id: 1, title: '四级阅读：The Digital Age', level: 'cet4', content: '<p>The digital age has transformed how we live, work, and communicate...</p>', createTime: '2023-05-01' },
      { id: 2, title: '六级阅读：Climate Change', level: 'cet6', content: '<p>Climate change is one of the most pressing issues facing our planet today...</p>', createTime: '2023-05-02' },
      { id: 3, title: '雅思阅读：Urban Planning', level: 'ielts', content: '<p>Urban planning is the process of developing and designing urban areas...</p>', createTime: '2023-05-03' }
    ]
    
    // 根据搜索条件过滤
    let filteredReadings = [...mockReadings]
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredReadings = filteredReadings.filter(reading => 
        reading.title.toLowerCase().includes(query)
      )
    }
    
    if (levelFilter.value !== '') {
      filteredReadings = filteredReadings.filter(reading => reading.level === levelFilter.value)
    }
    
    total.value = filteredReadings.length
    readingList.value = filteredReadings
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReadingList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchReadingList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchReadingList()
}

// 添加阅读材料
const handleAddReading = () => {
  dialogType.value = 'add'
  readingForm.id = 0
  readingForm.title = ''
  readingForm.level = 'cet4'
  readingForm.content = ''
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
  ).then(() => {
    // 这里应该调用API删除阅读材料
    ElMessage.success('删除成功')
    fetchReadingList()
  }).catch(() => {
    // 取消删除
  })
}

// 提交阅读材料表单
const submitReadingForm = async () => {
  if (!readingFormRef.value) return
  
  await readingFormRef.value.validate((valid, fields) => {
    if (valid) {
      // 获取编辑器内容
      const textarea = document.querySelector('#editor textarea') as HTMLTextAreaElement
      if (textarea) {
        readingForm.content = textarea.value
      }
      
      // 这里应该调用API添加或更新阅读材料
      if (dialogType.value === 'add') {
        // 添加阅读材料
        ElMessage.success('添加成功')
      } else {
        // 更新阅读材料
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchReadingList()
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

.preview-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.preview-content {
  line-height: 1.6;
}
</style>
