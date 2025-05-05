<template>
  <div class="admin-listening">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>听力管理</h2>
          <el-button type="primary" @click="handleAddListening">
            <el-icon><Plus /></el-icon>添加听力
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
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
        :data="listeningList"
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
        <el-table-column prop="year" label="年份" width="100" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditListening(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handlePreviewListening(scope.row)"
            >
              预览
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteListening(scope.row)"
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

    <!-- 听力表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加听力' : '编辑听力'"
      width="600px"
    >
      <el-form
        ref="listeningFormRef"
        :model="listeningForm"
        :rules="listeningFormRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="listeningForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="listeningForm.level" placeholder="请选择等级">
            <el-option
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-input v-model="listeningForm.year" placeholder="请输入年份，如2023" />
        </el-form-item>
        <el-form-item label="音频文件" prop="audioUrl">
          <el-upload
            class="upload-demo"
            action="/api/upload/audio"
            :on-success="handleAudioUploadSuccess"
            :on-error="handleAudioUploadError"
            :before-upload="beforeAudioUpload"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传MP3格式音频文件，大小不超过20MB
              </div>
            </template>
          </el-upload>
          <div v-if="listeningForm.audioUrl" class="audio-preview">
            <audio controls :src="listeningForm.audioUrl"></audio>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitListeningForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

// 听力列表数据
const listeningList = ref([])
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
const listeningFormRef = ref<FormInstance>()
const listeningForm = reactive({
  id: 0,
  title: '',
  level: 'cet4',
  year: '',
  audioUrl: ''
})

// 表单验证规则
const listeningFormRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ],
  year: [
    { required: true, message: '请输入年份', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '年份格式不正确', trigger: 'blur' }
  ],
  audioUrl: [
    { required: true, message: '请上传音频文件', trigger: 'change' }
  ]
})

// 获取听力列表
const fetchListeningList = () => {
  loading.value = true
  // 这里应该调用API获取听力列表
  // 暂时使用模拟数据
  setTimeout(() => {
    const mockListenings = [
      { id: 1, title: '四级听力2023年6月第一套', level: 'cet4', year: '2023', audioUrl: '/audio/cet4-202306-1.mp3' },
      { id: 2, title: '四级听力2023年6月第二套', level: 'cet4', year: '2023', audioUrl: '/audio/cet4-202306-2.mp3' },
      { id: 3, title: '六级听力2023年6月第一套', level: 'cet6', year: '2023', audioUrl: '/audio/cet6-202306-1.mp3' },
      { id: 4, title: '雅思听力2023年7月', level: 'ielts', year: '2023', audioUrl: '/audio/ielts-202307.mp3' }
    ]

    // 根据搜索条件过滤
    let filteredListenings = [...mockListenings]
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredListenings = filteredListenings.filter(listening =>
        listening.title.toLowerCase().includes(query)
      )
    }

    if (levelFilter.value !== '') {
      filteredListenings = filteredListenings.filter(listening => listening.level === levelFilter.value)
    }

    total.value = filteredListenings.length
    listeningList.value = filteredListenings
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchListeningList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchListeningList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchListeningList()
}

// 添加听力
const handleAddListening = () => {
  dialogType.value = 'add'
  listeningForm.id = 0
  listeningForm.title = ''
  listeningForm.level = 'cet4'
  listeningForm.year = new Date().getFullYear().toString()
  listeningForm.audioUrl = ''
  dialogVisible.value = true
}

// 编辑听力
const handleEditListening = (row: any) => {
  dialogType.value = 'edit'
  listeningForm.id = row.id
  listeningForm.title = row.title
  listeningForm.level = row.level
  listeningForm.year = row.year
  listeningForm.audioUrl = row.audioUrl
  dialogVisible.value = true
}

// 预览听力
const handlePreviewListening = (row: any) => {
  // 这里可以打开一个预览对话框或者新窗口
  window.open(row.audioUrl, '_blank')
}

// 删除听力
const handleDeleteListening = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除听力 ${row.title} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 这里应该调用API删除听力
    ElMessage.success('删除成功')
    fetchListeningList()
  }).catch(() => {
    // 取消删除
  })
}

// 音频上传相关
const handleAudioUploadSuccess = (response: any, file: any) => {
  listeningForm.audioUrl = response.data.url
  ElMessage.success('上传成功')
}

const handleAudioUploadError = () => {
  ElMessage.error('上传失败')
}

const beforeAudioUpload = (file: any) => {
  const isMP3 = file.type === 'audio/mp3'
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isMP3) {
    ElMessage.error('上传音频只能是MP3格式!')
  }
  if (!isLt20M) {
    ElMessage.error('上传音频大小不能超过20MB!')
  }
  return isMP3 && isLt20M
}

// 提交听力表单
const submitListeningForm = async () => {
  if (!listeningFormRef.value) return

  await listeningFormRef.value.validate((valid, fields) => {
    if (valid) {
      // 这里应该调用API添加或更新听力
      if (dialogType.value === 'add') {
        // 添加听力
        ElMessage.success('添加成功')
      } else {
        // 更新听力
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchListeningList()
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchListeningList()
})
</script>

<style scoped>
.admin-listening {
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

.audio-preview {
  margin-top: 10px;
}
</style>