<template>
  <div class="admin-notices">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>公告管理</h2>
          <el-button type="primary" @click="handleAddNotice">
            <el-icon><Plus /></el-icon>添加公告
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索公告标题"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
        />

        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="noticeList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="noticeId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="creatTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditNotice(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handlePreviewNotice(scope.row)"
            >
              预览
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteNotice(scope.row)"
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

    <!-- 公告表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加公告' : '编辑公告'"
      width="800px"
    >
      <el-form
        ref="noticeFormRef"
        :model="noticeForm"
        :rules="noticeFormRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
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
          <el-button type="primary" @click="submitNoticeForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="公告预览"
      width="800px"
    >
      <h2 class="preview-title">{{ previewNotice.title }}</h2>
      <div class="preview-time">发布时间：{{ previewNotice.creatTime }}</div>
      <div class="preview-content" v-html="previewNotice.content"></div>
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

// 公告列表数据
const noticeList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const dateRange = ref<[string, string] | null>(null)

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const noticeFormRef = ref<FormInstance>()
const noticeForm = reactive({
  noticeId: 0,
  title: '',
  content: ''
})

// 预览相关
const previewVisible = ref(false)
const previewNotice = reactive({
  title: '',
  content: '',
  creatTime: ''
})

// 表单验证规则
const noticeFormRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
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
      editorElement.innerHTML = '<textarea style="width: 100%; height: 300px;" placeholder="请输入公告内容"></textarea>'
      const textarea = editorElement.querySelector('textarea')
      if (textarea) {
        textarea.value = noticeForm.content
        textarea.addEventListener('input', (e) => {
          noticeForm.content = (e.target as HTMLTextAreaElement).value
        })
      }
    }
  }, 100)
}

// 获取公告列表
const fetchNoticeList = () => {
  loading.value = true
  // 这里应该调用API获取公告列表
  // 暂时使用模拟数据
  setTimeout(() => {
    const mockNotices = [
      { noticeId: 1, title: '欢迎使用英语学习管理系统', content: '<p>欢迎使用英语学习管理系统！本系统提供单词学习、听力练习和阅读材料等功能，帮助您提高英语水平。</p><p>如有任何问题，请联系管理员。</p>', creatTime: '2023-05-01' },
      { noticeId: 2, title: '系统更新通知', content: '<p>尊敬的用户：</p><p>我们将于2023年5月10日凌晨2:00-4:00进行系统维护，期间系统可能无法正常访问。给您带来的不便，敬请谅解。</p>', creatTime: '2023-05-02' },
      { noticeId: 3, title: '新功能上线通知', content: '<p>我们新增了以下功能：</p><ol><li>单词发音功能</li><li>学习进度统计</li><li>个性化学习计划</li></ol><p>欢迎体验！</p>', creatTime: '2023-05-03' }
    ]

    // 根据搜索条件过滤
    let filteredNotices = [...mockNotices]
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredNotices = filteredNotices.filter(notice =>
        notice.title.toLowerCase().includes(query)
      )
    }

    // 根据日期范围过滤
    if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
      const startDate = new Date(dateRange.value[0]).getTime()
      const endDate = new Date(dateRange.value[1]).getTime()
      filteredNotices = filteredNotices.filter(notice => {
        const noticeDate = new Date(notice.creatTime).getTime()
        return noticeDate >= startDate && noticeDate <= endDate
      })
    }

    total.value = filteredNotices.length
    noticeList.value = filteredNotices
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchNoticeList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchNoticeList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchNoticeList()
}

// 添加公告
const handleAddNotice = () => {
  dialogType.value = 'add'
  noticeForm.noticeId = 0
  noticeForm.title = ''
  noticeForm.content = ''
  dialogVisible.value = true
  // 初始化编辑器
  setTimeout(() => {
    initEditor()
  }, 100)
}

// 编辑公告
const handleEditNotice = (row: any) => {
  dialogType.value = 'edit'
  noticeForm.noticeId = row.noticeId
  noticeForm.title = row.title
  noticeForm.content = row.content
  dialogVisible.value = true
  // 初始化编辑器
  setTimeout(() => {
    initEditor()
  }, 100)
}

// 预览公告
const handlePreviewNotice = (row: any) => {
  previewNotice.title = row.title
  previewNotice.content = row.content
  previewNotice.creatTime = row.creatTime
  previewVisible.value = true
}

// 删除公告
const handleDeleteNotice = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除公告 ${row.title} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 这里应该调用API删除公告
    ElMessage.success('删除成功')
    fetchNoticeList()
  }).catch(() => {
    // 取消删除
  })
}

// 提交公告表单
const submitNoticeForm = async () => {
  if (!noticeFormRef.value) return

  await noticeFormRef.value.validate((valid, fields) => {
    if (valid) {
      // 获取编辑器内容
      const textarea = document.querySelector('#editor textarea') as HTMLTextAreaElement
      if (textarea) {
        noticeForm.content = textarea.value
      }

      // 这里应该调用API添加或更新公告
      if (dialogType.value === 'add') {
        // 添加公告
        ElMessage.success('添加成功')
      } else {
        // 更新公告
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchNoticeList()
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchNoticeList()
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
.admin-notices {
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

.preview-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.preview-content {
  line-height: 1.6;
}
</style>