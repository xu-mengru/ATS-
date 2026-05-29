<template>
  <div class="dashboard" v-loading="loading">
    <div class="breadcrumb-nav">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/positions' }">岗位列表</el-breadcrumb-item>
        <el-breadcrumb-item>岗位统计</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><el-icon size="22"><Briefcase /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">岗位总数</div>
          <div class="stat-value">{{ stats.total }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon published"><el-icon size="22"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">已发布</div>
          <div class="stat-value">{{ stats.published }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon reviewing"><el-icon size="22"><Clock /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">审核中</div>
          <div class="stat-value">{{ stats.reviewing }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon draft"><el-icon size="22"><EditPen /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">草稿</div>
          <div class="stat-value">{{ stats.draft || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rejected"><el-icon size="22"><WarningFilled /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">已驳回</div>
          <div class="stat-value">{{ stats.rejected || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon closed"><el-icon size="22"><CircleClose /></el-icon></div>
        <div class="stat-info">
          <div class="stat-label">已关闭</div>
          <div class="stat-value">{{ stats.closed }}</div>
        </div>
      </div>
    </div>

    <!-- Charts -->
    <div class="charts-row">
      <div class="content-card chart-card">
        <div class="card-header"><h2>岗位状态分布</h2></div>
        <div class="card-body">
          <div ref="pieChart" class="chart-container"></div>
        </div>
      </div>
      <div class="content-card chart-card">
        <div class="card-header"><h2>近6月新增趋势</h2></div>
        <div class="card-body">
          <div ref="lineChart" class="chart-container"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Briefcase, CircleCheck, Clock, EditPen, WarningFilled, CircleClose } from '@element-plus/icons-vue'
import { getStatistics } from '../api/position'
import * as echarts from 'echarts'

const loading = ref(false)
const pieChart = ref(null)
const lineChart = ref(null)

const stats = reactive({
  total: 0, draft: 0, reviewing: 0, rejected: 0, published: 0, closed: 0, monthlyTrend: []
})

let pieInstance = null
let lineInstance = null

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    Object.assign(stats, res.data)
    await nextTick()
    renderPieChart()
    renderLineChart()
  } finally {
    loading.value = false
  }
}

const renderPieChart = () => {
  if (!pieChart.value) return
  if (pieInstance) pieInstance.dispose()

  pieInstance = echarts.init(pieChart.value)
  pieInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: [
        { value: stats.published || 0, name: '已发布', itemStyle: { color: '#059669' } },
        { value: stats.reviewing || 0, name: '审核中', itemStyle: { color: '#6366f1' } },
        { value: stats.draft || 0, name: '草稿', itemStyle: { color: '#d97706' } },
        { value: stats.rejected || 0, name: '已驳回', itemStyle: { color: '#ef4444' } },
        { value: stats.closed || 0, name: '已关闭', itemStyle: { color: '#dc2626' } }
      ].filter(d => d.value > 0)
    }]
  })
}

const renderLineChart = () => {
  if (!lineChart.value) return
  if (lineInstance) lineInstance.dispose()

  lineInstance = echarts.init(lineChart.value)
  const trend = stats.monthlyTrend || []
  lineInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: trend.map(t => t.month), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      data: trend.map(t => t.count),
      smooth: true,
      lineStyle: { color: '#3b82f6', width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59,130,246,0.25)' },
          { offset: 1, color: 'rgba(59,130,246,0.02)' }
        ])
      },
      itemStyle: { color: '#3b82f6' }
    }]
  })
}

const handleResize = () => {
  pieInstance?.resize()
  lineInstance?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieInstance?.dispose()
  lineInstance?.dispose()
})
</script>

<style scoped>
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 0;
}

.chart-card { margin-top: 20px; }

.chart-container {
  width: 100%;
  height: 360px;
}

@media (max-width: 768px) {
  .charts-row { grid-template-columns: 1fr; }
  .chart-container { height: 280px; }
}
</style>
