<template>
  <div class="session-list">
    <div class="session-header">
      <span>历史对话</span>
      <el-button type="primary" size="small" @click="chatStore.createSession()">
        新建
      </el-button>
    </div>
    <div class="session-items">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        :class="['session-item', { active: session.id === chatStore.currentSessionId }]"
        @click="chatStore.selectSession(session.id)"
      >
        <span class="session-title">{{ session.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

if (chatStore.sessions.length === 0) {
  chatStore.createSession()
}
</script>

<style scoped>
.session-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-weight: 600;
  color: #303133;
}

.session-items {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #e4e7ed;
}

.session-item.active {
  background-color: #409eff;
  color: #fff;
}

.session-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
