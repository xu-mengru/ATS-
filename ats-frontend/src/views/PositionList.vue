<template>
  <div class="position-list">
    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon size="22"><Briefcase /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">岗位总数</div>
          <div class="stat-value">{{ stats.total }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon published">
          <el-icon size="22"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">已发布</div>
          <div class="stat-value">{{ stats.published }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon draft">
          <el-icon size="22"><EditPen /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">草稿</div>
          <div class="stat-value">{{ stats.draft }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon closed">
          <el-icon size="22"><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">已关闭</div>
          <div class="stat-value">{{ stats.closed }}</div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="content-card">
      <div class="card-header">
        <h2>岗位列表</h2>
        <div class="action-bar">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon> 新建岗位
          </el-button>
          <el-button @click="uploadVisible = true">
            <el-icon><Upload /></el-icon> 批量导入
          </el-button>
        </div>
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索岗位名称..."
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="审核中" value="REVIEWING" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- Table -->
      <PositionTable
        :data="tableData"
        :loading="loading"
        @view="handleView"
        @edit="handleEdit"
        @delete="handleDelete"
        @submit="handleSubmit"
        @approve="handleApprove"
        @reject="handleReject"
      />

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next, jumper"
          background
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- Upload Dialog -->
    <FileUpload
      v-model:visible="uploadVisible"
      @success="handleUploadSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Plus, Upload, Search, Briefcase, CircleCheck, EditPen, CircleClose } from '@element-plus/icons-vue'
import { getPositions, deletePosition, submitForReview, approvePosition, rejectPosition, getStatistics } from '../api/position'
import PositionTable from '../components/PositionTable.vue'
import FileUpload from '../components/FileUpload.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const status = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const uploadVisible = ref(false)

const stats = reactive({
  total: 0,
  published: 0,
  draft: 0,
  closed: 0
})

const fetchStats = async () => {
  try {
    const res = await getStatistics()
    const d = res.data
    stats.total = d.total || 0
    stats.published = d.published || 0
    stats.draft = (d.draft || 0) + (d.reviewing || 0) + (d.rejected || 0)
    stats.closed = d.closed || 0
  } catch { /* ignore */ }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPositions({
      keyword: keyword.value,
      status: status.value,
      page: page.value,
      size: size.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const handleReset = () => {
  keyword.value = ''
  status.value = ''
  page.value = 1
  fetchData()
}

const handleCreate = () => router.push('/positions/create')
const handleView = (row) => router.push(`/positions/${row.id}`)
const handleEdit = (row) => router.push(`/positions/${row.id}/edit`)

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除岗位「${row.title}」吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deletePosition(row.id)
    fetchData()
    fetchStats()
  })
}

const handleSubmit = async (row) => {
  await submitForReview(row.id)
  fetchData()
  fetchStats()
}

const handleApprove = async (row) => {
  await approvePosition(row.id)
  fetchData()
  fetchStats()
}

const handleReject = async (row) => {
  await rejectPosition(row.id)
  fetchData()
  fetchStats()
}

const handleUploadSuccess = () => {
  uploadVisible.value = false
  fetchData()
  fetchStats()
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>
