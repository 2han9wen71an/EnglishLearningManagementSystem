<template>
  <div class="admin-words">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>单词管理</h2>
          <el-button type="primary" @click="handleAddWord">
            <el-icon><Plus /></el-icon>添加单词
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索单词"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="gradeFilter" placeholder="等级筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in gradeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="wordList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="wordId" label="序号" width="80" />
        <el-table-column prop="wordName" label="单词" />
        <el-table-column prop="audio" label="读音" />
        <el-table-column prop="explanation" label="解释" show-overflow-tooltip />
        <el-table-column label="等级" width="100">
          <template #default="scope">
            <el-tag :type="getGradeTagType(scope.row.grade)">
              {{ getGradeName(scope.row.grade) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditWord(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteWord(scope.row)"
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

    <!-- 单词表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加单词' : '编辑单词'"
      width="500px"
    >
      <el-form
        ref="wordFormRef"
        :model="wordForm"
        :rules="wordFormRules"
        label-width="100px"
      >
        <el-form-item label="单词" prop="wordName">
          <el-input v-model="wordForm.wordName" placeholder="请输入单词" />
        </el-form-item>
        <el-form-item label="读音" prop="audio">
          <el-input v-model="wordForm.audio" placeholder="请输入读音（音标）" />
        </el-form-item>
        <el-form-item label="解释" prop="explanation">
          <el-input v-model="wordForm.explanation" type="textarea" :rows="3" placeholder="请输入解释" />
        </el-form-item>
        <el-form-item label="例句" prop="example">
          <el-input v-model="wordForm.example" type="textarea" :rows="3" placeholder="请输入例句" />
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="wordForm.grade" placeholder="请选择等级">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitWordForm">确定</el-button>
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
import { getWordList, getWordGradeList, addWord, updateWord, deleteWord } from '@/api/word'

// 单词列表数据
const wordList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const gradeFilter = ref('')

// 等级选项
const gradeOptions = ref([
  { value: 1, label: '四级' },
  { value: 2, label: '六级' },
  { value: 3, label: '雅思' },
  { value: 4, label: '托福' }
])

// 获取等级选项
const fetchGradeOptions = async () => {
  try {
    const res = await getWordGradeList()
    if (res.success && res.data) {
      gradeOptions.value = res.data.map((item: any) => ({
        value: item.gradeId,
        label: item.gradeName
      }))
    }
  } catch (error) {
    console.error('获取等级选项失败', error)
  }
}

// 获取等级名称
const getGradeName = (gradeId: number) => {
  const grade = gradeOptions.value.find(item => item.value === gradeId)
  return grade ? grade.label : '未知'
}

// 获取等级标签类型
const getGradeTagType = (gradeId: number) => {
  switch (gradeId) {
    case 1: return ''
    case 2: return 'success'
    case 3: return 'warning'
    case 4: return 'danger'
    default: return 'info'
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const wordFormRef = ref<FormInstance>()
const wordForm = reactive({
  wordId: 0,
  wordName: '',
  audio: '',
  explanation: '',
  example: '',
  grade: 1,
  study: 0,
  remember: 0,
  collection: 0
})

// 表单验证规则
const wordFormRules = reactive<FormRules>({
  wordName: [
    { required: true, message: '请输入单词', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  audio: [
    { required: true, message: '请输入读音', trigger: 'blur' }
  ],
  explanation: [
    { required: true, message: '请输入解释', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ]
})

// 获取单词列表
const fetchWordList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value,
      gradeId: gradeFilter.value || undefined
    }

    const res = await getWordList(params)
    if (res.success) {
      wordList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取单词列表失败')
    }
  } catch (error) {
    console.error('获取单词列表失败', error)
    ElMessage.error('获取单词列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchWordList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchWordList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchWordList()
}

// 添加单词
const handleAddWord = () => {
  dialogType.value = 'add'
  wordForm.wordId = 0
  wordForm.wordName = ''
  wordForm.audio = ''
  wordForm.explanation = ''
  wordForm.example = ''
  wordForm.grade = 1
  wordForm.study = 0
  wordForm.remember = 0
  wordForm.collection = 0
  dialogVisible.value = true
}

// 编辑单词
const handleEditWord = (row: any) => {
  dialogType.value = 'edit'
  wordForm.wordId = row.wordId
  wordForm.wordName = row.wordName
  wordForm.audio = row.audio
  wordForm.explanation = row.explanation
  wordForm.example = row.example
  wordForm.grade = row.grade
  wordForm.study = row.study || 0
  wordForm.remember = row.remember || 0
  wordForm.collection = row.collection || 0
  dialogVisible.value = true
}

// 删除单词
const handleDeleteWord = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除单词 ${row.wordName} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteWord(row.wordId)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchWordList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除单词失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交单词表单
const submitWordForm = async () => {
  if (!wordFormRef.value) return

  await wordFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        let res
        if (dialogType.value === 'add') {
          // 添加单词
          res = await addWord(wordForm)
        } else {
          // 更新单词
          res = await updateWord(wordForm.wordId, wordForm)
        }

        if (res.success) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          fetchWordList()
        } else {
          ElMessage.error(res.message || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加单词失败' : '更新单词失败', error)
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchGradeOptions()
  fetchWordList()
})
</script>

<style scoped>
.admin-words {
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
