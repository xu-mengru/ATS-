<template>
  <div class="ats-table-wrapper">
    <el-table :data="data" v-loading="loading" stripe style="width: 100%" row-key="id">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="岗位名称" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button type="primary" link @click="$emit('view', row)">
            {{ row.title }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110" align="center">
        <template #default="{ row }">
          <span :class="['status-tag', statusClass(row.status)]">
            {{ statusLabel(row.status) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="expireDate" label="有效期至" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.expireDate" :style="{ color: row.expireDate < today ? 'var(--color-danger)' : '' }">
            {{ row.expireDate }}
            <el-tag v-if="row.expireDate < today" type="danger" size="small" style="margin-left:4px">已过期</el-tag>
          </span>
          <span v-else style="color: var(--color-text-muted)">长期有效</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="$emit('view', row)">查看</el-button>
          <el-button v-if="row.status === 'DRAFT' || row.status === 'REJECTED'" type="warning" link size="small" @click="$emit('submit', row)">提交</el-button>
          <el-button v-if="row.status === 'REVIEWING'" type="success" link size="small" @click="$emit('approve', row)">通过</el-button>
          <el-button v-if="row.status === 'REVIEWING'" type="danger" link size="small" @click="$emit('reject', row)">驳回</el-button>
          <el-button type="primary" link size="small" @click="$emit('edit', row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="$emit('delete', row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <div class="empty-icon">📄</div>
          <div class="empty-title">暂无岗位数据</div>
          <div class="empty-desc">点击上方「新建岗位」创建第一条招聘岗位，或通过「批量导入」上传 Excel 数据</div>
        </div>
      </template>
    </el-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'

defineProps({
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

defineEmits(['edit', 'delete', 'view', 'submit', 'approve', 'reject'])

const today = computed(() => new Date().toISOString().split('T')[0])

const statusClass = (status) => {
  const map = { PUBLISHED: 'published', DRAFT: 'draft', REVIEWING: 'reviewing', REJECTED: 'rejected', CLOSED: 'closed' }
  return map[status] || 'draft'
}

const statusLabel = (status) => {
  const map = { PUBLISHED: '已发布', DRAFT: '草稿', REVIEWING: '审核中', REJECTED: '已驳回', CLOSED: '已关闭' }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>
