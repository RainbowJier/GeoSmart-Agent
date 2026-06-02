<template>
  <div class="input-bar">
    <div class="input-shell" :class="{ focused, disabled: chatStore.isLoading }">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        class="input-field"
        :placeholder="chatStore.isLoading ? 'Thinking...' : 'Type a message...'"
        :disabled="chatStore.isLoading"
        rows="1"
        @keydown="handleKeydown"
        @input="autoResize"
        @focus="focused = true"
        @blur="focused = false"
        id="chat-input"
        aria-label="Chat input"
      />
      <button
        class="send-btn"
        :class="{ active: inputText.trim() || chatStore.isLoading }"
        @click="handleSend"
        :disabled="!inputText.trim() && !chatStore.isLoading"
        id="send-btn"
        aria-label="Send message"
      >
        <svg v-if="chatStore.isLoading" width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <rect x="6" y="6" width="12" height="12" rx="2" />
        </svg>
        <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="19" x2="12" y2="5" />
          <polyline points="5 12 12 5 19 12" />
        </svg>
      </button>
    </div>
    <p class="input-hint">
      <kbd>Enter</kbd> send · <kbd>Shift+Enter</kbd> newline
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const inputText = ref('')
const focused = ref(false)
const textareaRef = ref<HTMLTextAreaElement>()

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  if (chatStore.isLoading) { chatStore.stopStreaming(); return }
  const text = inputText.value.trim()
  if (!text) return
  chatStore.sendMessage(text)
  inputText.value = ''
  nextTick(() => { if (textareaRef.value) textareaRef.value.style.height = 'auto' })
}
</script>

<style scoped>
.input-bar {
  width: 100%;
  max-width: var(--chat-max-width);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-8);
}

.input-shell {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-8);
  padding: 10px 12px 10px 18px;
  background: var(--color-input-bg);
  border: 1px solid var(--color-input-border);
  border-radius: var(--radius-md);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02), inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.input-shell.focused {
  border-color: var(--color-midnight-ink);
  box-shadow: 0 0 0 1px var(--color-midnight-ink), 0 4px 16px rgba(0, 0, 0, 0.04);
}

.input-shell.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-field {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  color: var(--color-text);
  max-height: 160px;
  overflow-y: auto;
  padding: 4px 0;
  line-height: 1.5;
}

.input-field::placeholder {
  color: var(--color-text-dim);
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-dim);
  cursor: default;
  flex-shrink: 0;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.send-btn.active {
  background: var(--color-steel-gray);
  border-color: var(--color-steel-gray);
  color: var(--color-canvas-white);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(24, 24, 27, 0.15);
}

.send-btn.active:hover {
  transform: scale(1.06);
  background: var(--color-midnight-ink);
  border-color: var(--color-midnight-ink);
  box-shadow: 0 4px 12px rgba(10, 10, 10, 0.25);
  opacity: 1;
}

.send-btn.active:active {
  transform: scale(0.94);
}

.input-hint {
  text-align: center;
  font-family: var(--font-sans);
  font-size: 11px;
  color: var(--color-text-dim);
  letter-spacing: 0.02em;
}

.input-hint kbd {
  background: var(--color-soft-fog);
  border: 1px solid var(--color-cloud-cover);
  border-bottom: 2px solid var(--color-pale-stone);
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 10px;
  font-family: var(--font-mono);
  color: var(--color-text-muted);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02);
}
</style>
