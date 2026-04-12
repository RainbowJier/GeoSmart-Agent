<template>
  <div class="message-input">
    <el-input
      v-model="inputText"
      type="textarea"
      :rows="2"
      placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
      resize="none"
      @keydown="handleKeydown"
      :disabled="chatStore.isLoading"
    />
    <el-button
      type="primary"
      @click="handleSend"
      :disabled="!inputText.trim() && !chatStore.isLoading"
    >
      {{ chatStore.isLoading ? '停止' : '发送' }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const inputText = ref('')

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  if (chatStore.isLoading) {
    chatStore.stopStreaming()
    return
  }

  const text = inputText.value.trim()
  if (!text) return

  chatStore.sendMessage(text)
  inputText.value = ''
}
</script>

<style scoped>
.message-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  margin-top: 8px;
}

.message-input .el-textarea {
  flex: 1;
}
</style>
