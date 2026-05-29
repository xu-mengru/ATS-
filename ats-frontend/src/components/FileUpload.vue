<template>
  <el-dialog
    :model-value="visible"
    title="批量导入岗位"
    width="560px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <div class="upload-content">
      <el-alert
        title="操作说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 24px"
      >
        <template #default>
          <ol style="margin:8px 0 0 16px;line-height:1.8">
            <li>下载 Excel 模板，按模板格式填写岗位数据</li>
            <li>上传填写好的 Excel 文件（.xlsx / .xls）</li>
            <li>系统将逐行解析并导入岗位信息</li>
          </ol>
        </template>
      </el-alert>

      <el-button @click="handleDownloadTemplate" :loading="downloading" size="large">
        <el-icon><Download /></el-icon> 下载 Excel 模板
      </el-button>

      <el-divider />

      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload" size="32"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将 Excel 文件拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式，单次导入一张工作表</div>
        </template>
      </el-upload>

      <div v-if="file" style="margin-top:12px;color:var(--color-text-secondary);font-size:13px">
        已选择: <strong>{{ file.name }}</strong> ({{ (file.size / 1024).toFixed(1) }} KB)
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleUpload" :loading="uploading" :disabled="!file" size="large">
        开始导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import { uploadExcel, downloadTemplate } from '../api/position'

defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'success'])

const file = ref(null)
const uploading = ref(false)
const downloading = ref(false)

const handleFileChange = (uploadFile) => {
  file.value = uploadFile.raw
}

const handleFileRemove = () => {
  file.value = null
}

const handleDownloadTemplate = async () => {
  downloading.value = true
  try {
    const res = await downloadTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '岗位导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch {
    // error handled by interceptor
  } finally {
    downloading.value = false
  }
}

const handleUpload = async () => {
  if (!file.value) {
    ElMessage.warning('请选择要上传的 Excel 文件')
    return
  }
  uploading.value = true
  try {
    await uploadExcel(file.value)
    emit('success')
  } catch {
    // error handled by interceptor
  } finally {
    uploading.value = false
  }
}

const handleClose = () => {
  file.value = null
  emit('update:visible', false)
}
</script>
