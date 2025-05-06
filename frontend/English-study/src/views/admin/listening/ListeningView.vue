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
        <el-table-column prop="listenId" label="ID" width="80" />
        <el-table-column prop="listenName" label="标题" />
        <el-table-column prop="grade" label="等级" width="100">
          <template #default="scope">
            <el-tag :type="getLevelTagType(getGradeCode(scope.row.grade))">
              {{ getGradeName(scope.row.grade) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="音频" width="100">
          <template #default="scope">
            <el-button
              size="small"
              type="info"
              @click="previewAudio(scope.row)"
              v-if="scope.row.path"
            >
              播放
            </el-button>
            <span v-else>无音频</span>
          </template>
        </el-table-column>
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
      width="700px"
    >
      <el-form
        ref="listeningFormRef"
        :model="listeningForm"
        :rules="listeningFormRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="listenName">
          <el-input v-model="listeningForm.listenName" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="listeningForm.grade" placeholder="请选择等级">
            <el-option
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="听力内容" prop="content">
          <el-input
            v-model="listeningForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入听力内容，支持HTML格式"
          />
        </el-form-item>
        <el-form-item label="音频文件" prop="path">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => { beforeAudioUpload(file.raw) }"
            :limit="1"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传音频文件，大小不超过20MB
              </div>
            </template>
          </el-upload>
          <div v-if="listeningForm.path" class="audio-preview">
            <p>当前音频: {{ listeningForm.path }}</p>
            <audio v-if="listeningForm.path.startsWith('/')" controls :src="`/api/files/audio/${listeningForm.path}`"></audio>
          </div>
          <div v-if="listeningForm.audioFile" class="audio-preview">
            <p>已选择新文件: {{ listeningForm.audioFile.name }}</p>
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
import { getListeningList, addListening, updateListening, deleteListening, uploadAudio } from '@/api/listening'

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
  { value: 1, label: '四级', code: 'cet4' },
  { value: 2, label: '六级', code: 'cet6' },
  { value: 3, label: '托福', code: 'toefl' },
  { value: 4, label: '雅思', code: 'ielts' }
]

// 获取等级名称
const getGradeName = (gradeId: number) => {
  const level = levelOptions.find(item => item.value === gradeId)
  return level ? level.label : '未知'
}

// 获取等级代码
const getGradeCode = (gradeId: number) => {
  const level = levelOptions.find(item => item.value === gradeId)
  return level ? level.code : ''
}

// 获取等级标签类型
const getLevelTagType = (levelCode: string) => {
  switch (levelCode) {
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
  listenId: 0,
  listenName: '',
  grade: 1, // 默认四级
  path: '',
  content: '',
  audioFile: null as File | null
})

// 表单验证规则
const listeningFormRules = reactive<FormRules>({
  listenName: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ],
  content: [
    { required: false, message: '请输入听力内容', trigger: 'blur' }
  ],
  path: [
    { required: false, message: '请上传音频文件', trigger: 'change' }
  ]
})

// 原始听力列表数据（未筛选）
const originalListeningList = ref([])

// 获取听力列表
const fetchListeningList = () => {
  loading.value = true

  // 调用API获取听力列表（不带筛选参数）
  getListeningList({})
    .then(res => {
      if (res.success) {
        // 保存原始数据
        originalListeningList.value = res.data || []
        // 应用本地筛选
        applyLocalFilters()
      } else {
        ElMessage.error(res.message || '获取听力列表失败')
        originalListeningList.value = []
        listeningList.value = []
        total.value = 0
      }
    })
    .catch(err => {
      console.error('获取听力列表出错:', err)
      ElMessage.error('获取听力列表失败，请稍后重试')
      originalListeningList.value = []
      listeningList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

// 在前端应用筛选
const applyLocalFilters = () => {
  // 从原始数据开始筛选
  let filteredData = [...originalListeningList.value]

  // 应用等级筛选
  if (levelFilter.value) {
    filteredData = filteredData.filter(item => item.grade === levelFilter.value)
  }

  // 应用搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filteredData = filteredData.filter(item =>
      item.listenName.toLowerCase().includes(query)
    )
  }

  // 计算总数
  total.value = filteredData.length

  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  listeningList.value = filteredData.slice(startIndex, endIndex)
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

// 添加听力
const handleAddListening = () => {
  dialogType.value = 'add'
  listeningForm.listenId = 0
  listeningForm.listenName = ''
  listeningForm.grade = 1 // 默认四级
  listeningForm.path = ''
  listeningForm.content = ''
  listeningForm.audioFile = null
  dialogVisible.value = true
}

// 编辑听力
const handleEditListening = (row: any) => {
  dialogType.value = 'edit'
  listeningForm.listenId = row.listenId
  listeningForm.listenName = row.listenName
  listeningForm.grade = row.grade
  listeningForm.path = row.path || ''
  listeningForm.content = row.content || ''
  listeningForm.audioFile = null
  dialogVisible.value = true
}

// 预览听力
const handlePreviewListening = (row: any) => {
  // 打开一个预览对话框，显示听力内容
  ElMessageBox.alert(
    `<div class="listening-preview">
      <h3>${row.listenName}</h3>
      <div>${row.content || '暂无内容'}</div>
      ${row.path ? `<div class="audio-preview mt-3">
        <audio controls src="/api/files/audio/${row.path}"></audio>
      </div>` : ''}
    </div>`,
    '听力预览',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 预览音频
const previewAudio = (row: any) => {
  if (!row.path) {
    ElMessage.warning('该听力没有音频文件')
    return
  }

  // 创建音频元素并播放
  const audio = new Audio(`/api/files/audio/${row.path}`)
  audio.play().catch(err => {
    console.error('播放音频失败:', err)
    ElMessage.error('播放音频失败，请稍后重试')
  })
}

// 删除听力
const handleDeleteListening = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除听力 ${row.listenName} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用API删除听力
      const result = await deleteListening(row.listenId)
      if (result.success) {
        ElMessage.success('删除成功')
        fetchListeningList() // 重新获取完整列表
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除听力失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 音频上传相关
const handleAudioUploadSuccess = (response: any, file: any) => {
  if (response.success) {
    listeningForm.path = response.data.path || response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAudioUploadError = (err: any) => {
  console.error('上传音频失败:', err)
  ElMessage.error('上传失败，请稍后重试')
}

const beforeAudioUpload = (file: any) => {
  // 支持更多音频格式
  const isAudio = file.type.startsWith('audio/')
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isAudio) {
    ElMessage.error('请上传音频格式文件!')
  }
  if (!isLt20M) {
    ElMessage.error('上传音频大小不能超过20MB!')
  }

  // 保存文件对象以便后续提交
  if (isAudio && isLt20M) {
    listeningForm.audioFile = file
  }

  return false // 阻止自动上传，改为手动上传
}

// 手动上传音频文件
const uploadAudioFile = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await uploadAudio(formData)
    if (res.success) {
      return res.data.path || res.data.url
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('上传音频文件失败:', error)
    throw error
  }
}

// 提交听力表单
const submitListeningForm = async () => {
  if (!listeningFormRef.value) return

  await listeningFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 如果有新上传的音频文件，先上传文件
        if (listeningForm.audioFile) {
          try {
            const formData = new FormData()
            formData.append('file', listeningForm.audioFile)
            const uploadResult = await uploadAudio(formData)
            if (uploadResult.success) {
              listeningForm.path = uploadResult.data.path || uploadResult.data.url
            } else {
              throw new Error(uploadResult.message || '上传音频失败')
            }
          } catch (error) {
            console.error('上传音频失败:', error)
            ElMessage.error('上传音频失败，请稍后重试')
            return
          }
        }

        // 准备提交的数据
        const submitData = {
          listenName: listeningForm.listenName,
          grade: listeningForm.grade,
          path: listeningForm.path,
          content: listeningForm.content
        }

        let result
        if (dialogType.value === 'add') {
          // 添加听力
          result = await addListening(submitData)
          if (result.success) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            fetchListeningList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '添加失败')
          }
        } else {
          // 更新听力
          result = await updateListening(listeningForm.listenId, submitData)
          if (result.success) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            fetchListeningList() // 重新获取完整列表
          } else {
            ElMessage.error(result.message || '更新失败')
          }
        }
      } catch (error) {
        console.error('提交听力表单失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
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