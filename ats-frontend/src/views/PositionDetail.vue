<template>
  <div class="position-detail" v-loading="loading">
    <div class="breadcrumb-nav">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/positions' }">岗位列表</el-breadcrumb-item>
        <el-breadcrumb-item>岗位详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="detail-header">
          <div>
            <h2 class="detail-title">{{ position.title }}</h2>
            <div style="margin-top:8px">
              <span :class="['status-tag', statusClass(position.status)]">
                {{ statusLabel(position.status) }}
              </span>
            </div>
          </div>
          <div class="action-bar">
            <el-button type="primary" @click="handleEdit">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" @click="handleDelete">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="card-body">
        <div class="detail-meta">
          <div class="detail-meta-item">
            <span class="meta-label">岗位 ID</span>
            <span class="meta-value">#{{ position.id }}</span>
          </div>
          <div class="detail-meta-item">
            <span class="meta-label">创建时间</span>
            <span class="meta-value">{{ formatTime(position.createTime) }}</span>
          </div>
          <div class="detail-meta-item">
            <span class="meta-label">最后更新</span>
            <span class="meta-value">{{ formatTime(position.updateTime) }}</span>
          </div>
        </div>

        <h3 class="detail-section-title">岗位描述</h3>
        <div class="markdown-preview" v-html="renderedDescription"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { getPositionById, deletePosition } from '../api/position'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const route = useRoute()

const md = new MarkdownIt({ breaks: true, html: false })

const loading = ref(false)
const position = ref({})

const renderedDescription = computed(() => {
  return position.value.description ? md.render(position.value.description) : ''
})

const statusClass = (status) => {
  const map = { PUBLISHED: 'published', DRAFT: 'draft', CLOSED: 'closed' }
  return map[status] || 'draft'
}

const statusLabel = (status) => {
  const map = { PUBLISHED: '已发布', DRAFT: '草稿', CLOSED: '已关闭' }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const handleEdit = () => {
  router.push(`/positions/${position.value.id}/edit`)
}

const handleDelete = () => {
  ElMessageBox.confirm(`确定要删除岗位「${position.value.title}」吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deletePosition(position.value.id)
    router.push('/positions')
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getPositionById(route.params.id)
    position.value = res.data
  } finally {
    loading.value = false
  }
})
</script>
