<template>
  <div class="session-panel">
    <div class="panel-head">
      <span class="panel-label">History</span>
      <button class="new-btn" @click="onNew" id="sidebar-new-btn" aria-label="New chat">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        New
      </button>
    </div>

    <div class="panel-divider" />

    <div class="list-scroll">
      <p v-if="chatStore.sessions.length === 0" class="empty-msg">No conversations</p>

      <TransitionGroup name="session-fade" tag="div" class="session-list-wrapper">
        <button
          v-for="session in chatStore.sessions"
          :key="session.id"
          :class="['session-row', { active: session.id === chatStore.currentSessionId }]"
          @click="onSelect(session.id)"
          :id="`session-row-${session.id}`"
        >
          <span class="row-prefix">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="4 17 10 11 4 5"></polyline>
              <line x1="12" y1="19" x2="20" y2="19"></line>
            </svg>
          </span>
          <span class="row-title">{{ session.title }}</span>
          <button
            class="del-btn"
            @click.stop="chatStore.deleteSession(session.id)"
            aria-label="Delete"
            tabindex="-1"
          >
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </button>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'

const emit = defineEmits<{ 'session-selected': [] }>()
const chatStore = useChatStore()

if (chatStore.sessions.length === 0) chatStore.createSession()

function onSelect(id: string) {
  chatStore.selectSession(id)
  emit('session-selected')
}

function onNew() {
  chatStore.createSession()
  emit('session-selected')
}
</script>

<style scoped>
.session-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-12) var(--spacing-12) var(--spacing-8);
}

.panel-label {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-dim);
}

.new-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.15s;
}

.new-btn:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.panel-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0 var(--spacing-12);
}

.list-scroll {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-8) var(--spacing-8);
}

.empty-msg {
  text-align: center;
  padding: var(--spacing-24);
  font-family: var(--font-sans);
  font-size: 12px;
  color: var(--color-text-dim);
}

.session-list-wrapper {
  display: flex;
  flex-direction: column;
}

/* ── Session row whiteboard style ────────────────────────────── */
.session-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
  margin-bottom: 4px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-muted);
  font-family: var(--font-sans);
  font-size: 13px;
  cursor: pointer;
  text-align: left;
  position: relative;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform, opacity;
}

.session-row:hover {
  background: var(--color-bg-hover);
  color: var(--color-text);
  transform: translateX(3px);
}

.session-row.active {
  background: var(--color-bg-active);
  border-color: rgba(84, 177, 108, 0.2);
  color: var(--color-text);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.session-row.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--color-success-green);
  border-radius: 9999px;
  box-shadow: 0 0 6px var(--color-success-green);
}

.row-prefix {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-dim);
  flex-shrink: 0;
  transition: color 0.2s;
}

.session-row.active .row-prefix {
  color: var(--color-success-green);
}

.row-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.del-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: transparent;
  cursor: pointer;
  transition: all 0.15s ease;
}

.session-row:hover .del-btn {
  color: var(--color-text-dim);
}

.del-btn:hover {
  color: #ef4444 !important;
  background: rgba(239, 68, 68, 0.08) !important;
}

/* ── Transitions ────────────────────────────────────────── */
.session-fade-enter-active,
.session-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.session-fade-enter-from {
  opacity: 0;
  transform: translateX(-16px);
}
.session-fade-leave-to {
  opacity: 0;
  transform: translateX(16px);
}
.session-fade-move {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
</style>
