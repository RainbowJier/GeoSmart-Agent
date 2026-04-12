<template>
  <el-container class="chat-container">
    <el-aside width="260px" class="sidebar">
      <SessionList />
    </el-aside>
    <el-container>
      <el-header class="chat-header">
        <h2>GeoSmart 智能助手</h2>
      </el-header>
      <el-main class="chat-main">
        <MessageList />
      </el-main>
      <el-footer height="auto" class="chat-footer">
        <DocUpload />
        <MessageInput />
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import SessionList from '@/components/SessionList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import DocUpload from '@/components/DocUpload.vue'
</script>

<style scoped>
.chat-container {
  height: 100vh;
}

.sidebar {
  border-right: 1px solid #e4e7ed;
  background-color: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.chat-header h2 {
  font-size: 18px;
  color: #303133;
}

.chat-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #fafafa;
}

.chat-footer {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}
</style>
