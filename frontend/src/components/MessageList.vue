<template>
  <div class="message-list" ref="listRef">
    <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
      <p>欢迎使用 GeoSmart 智能助手</p>
      <p class="hint">您可以询问国土空间规划相关政策、查询土地信息或业务办理进度</p>
    </div>
    <div
      v-for="msg in chatStore.currentMessages"
      :key="msg.id"
      :class="['message', msg.role]"
    >
      <div class="message-avatar">
        {{ msg.role === 'user' ? '我' : 'AI' }}
      </div>
      <div class="message-content">
        <div v-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
        <div v-else>{{ msg.content }}</div>
        <span v-if="msg.isStreaming" class="streaming-cursor">|</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import MarkdownIt from 'markdown-it'

const chatStore = useChatStore()
const listRef = ref<HTMLElement>()

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

function renderMarkdown(content: string): string {
  return md.render(content)
}

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

watch(() => chatStore.currentMessages.length, scrollToBottom)
watch(
  () => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content,
  scrollToBottom
)
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding-top: 120px;
  color: #909399;
}

.empty-state .hint {
  margin-top: 8px;
  font-size: 14px;
  color: #c0c4cc;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background-color: #409eff;
  color: #fff;
}

.message.assistant .message-avatar {
  background-color: #f0f2f5;
  color: #606266;
}

.message-content {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-content {
  background-color: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.message.assistant .message-content {
  background-color: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
}

.streaming-cursor {
  animation: blink 1s infinite;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.markdown-body :deep(pre) {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
</style>
