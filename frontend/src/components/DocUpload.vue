<template>
  <div class="doc-upload">
    <el-upload
      :auto-upload="false"
      :on-change="handleFileChange"
      :show-file-list="false"
      accept=".txt,.pdf,.docx,.doc"
    >
      <el-button size="small" type="success" plain>上传文档</el-button>
    </el-upload>
    <span v-if="uploading" class="upload-status">上传中...</span>
    <span v-if="uploadSuccess" class="upload-success">上传成功</span>
    <span v-if="uploadError" class="upload-error">{{ uploadError }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadDocument } from '@/api/chat'
import type { UploadFile } from 'element-plus'

const uploading = ref(false)
const uploadSuccess = ref(false)
const uploadError = ref('')

async function handleFileChange(file: UploadFile) {
  if (!file.raw) return

  uploading.value = true
  uploadSuccess.value = false
  uploadError.value = ''

  try {
    await uploadDocument(file.raw)
    uploadSuccess.value = true
    setTimeout(() => (uploadSuccess.value = false), 3000)
  } catch (e: any) {
    uploadError.value = e.message || '上传失败'
    setTimeout(() => (uploadError.value = ''), 5000)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.doc-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-status {
  color: #409eff;
  font-size: 13px;
}

.upload-success {
  color: #67c23a;
  font-size: 13px;
}

.upload-error {
  color: #f56c6c;
  font-size: 13px;
}
</style>
