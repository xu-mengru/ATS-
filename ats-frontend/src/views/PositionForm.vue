<template>
  <div class="position-form">
    <div class="breadcrumb-nav">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/positions' }">岗位列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ isEdit ? '编辑岗位' : '新建岗位' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-card form-card">
      <div class="card-header">
        <h2>{{ isEdit ? '编辑岗位' : '新建岗位' }}</h2>
      </div>

      <div class="card-body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="right"
          v-loading="loading"
        >
          <el-form-item label="岗位名称" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入岗位名称，如「Java 后端开发实习生」"
              maxlength="200"
              show-word-limit
              size="large"
            />
          </el-form-item>

          <el-form-item label="岗位状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio-button value="DRAFT">草稿</el-radio-button>
              <el-radio-button value="REVIEWING">审核中</el-radio-button>
              <el-radio-button value="PUBLISHED">已发布</el-radio-button>
              <el-radio-button value="CLOSED">已关闭</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="有效期至" prop="expireDate">
            <el-date-picker
              v-model="form.expireDate"
              type="date"
              placeholder="选择岗位有效期（留空则长期有效）"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              style="width:260px"
            />
            <span style="margin-left:12px;font-size:12px;color:var(--color-text-muted)">
              留空表示岗位长期有效，到期后系统将自动关闭
            </span>
          </el-form-item>

          <el-form-item label="岗位描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="16"
              placeholder="请输入岗位描述，支持 Markdown 格式。建议包含：岗位职责、任职要求、加分项"
            />
            <el-collapse style="margin-top:12px;width:100%">
              <el-collapse-item title="预览 Markdown 渲染效果" name="preview">
                <div class="markdown-preview" v-html="renderedDescription || '<em style=color:#94a3b8>输入描述内容后此处将显示渲染预览</em>'"></div>
              </el-collapse-item>
            </el-collapse>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
              <el-icon><Check /></el-icon> 保存
            </el-button>
            <el-button @click="handleCancel" size="large">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { createPosition, updatePosition, getPositionById } from '../api/position'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const route = useRoute()
const md = new MarkdownIt({ breaks: true, html: false })

const isEdit = computed(() => !!route.params.id)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  title: '',
  description: '',
  status: 'DRAFT',
  expireDate: ''
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // disable past dates
}

const renderedDescription = computed(() => {
  return form.value.description ? md.render(form.value.description) : ''
})

const rules = {
  title: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { max: 200, message: '岗位名称长度不能超过200个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入岗位描述', trigger: 'blur' }
  ]
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await getPositionById(route.params.id)
      const data = res.data
      form.value = {
        title: data.title,
        description: data.description,
        status: data.status,
        expireDate: data.expireDate || ''
      }
    } finally {
      loading.value = false
    }
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePosition(route.params.id, form.value)
      ElMessage.success('岗位更新成功')
    } else {
      await createPosition(form.value)
      ElMessage.success('岗位创建成功')
    }
    router.push('/positions')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>
