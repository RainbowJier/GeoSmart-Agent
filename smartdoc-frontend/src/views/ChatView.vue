<template>
  <div class="app-shell animate-fade-in">

    <aside class="sidebar">
      <div class="sidebar-head">
        <div class="sidebar-logo">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <span class="sidebar-title gelica-font">SPRING_CODE</span>
      </div>
      <div class="sidebar-divider" />
      <div class="sidebar-body">
        <SessionList />
      </div>
    </aside>

    <div class="main-col">
      <header class="topbar">
        <div class="topbar-indicator animate-pulse-glow"></div>
        <span class="topbar-title mono-text">SESSION_CONSOLE // active</span>
        <div class="topbar-spacer" />
      </header>

      <main class="chat-main">
        <div class="chat-scroll">
          <MessageList />
        </div>
        <div class="chat-input-area">
          <MessageInput />
        </div>
      </main>
    </div>

  </div>
</template>

<script setup lang="ts">
import SessionList from '@/components/SessionList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  background: var(--color-bg);
  overflow: hidden;
}

/* ── Sidebar ─────────────────────────────────────────────── */
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  z-index: 11;
}

.sidebar-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 var(--spacing-20);
  height: var(--nav-height);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-success-green);
}

.sidebar-title {
  font-family: var(--font-gelica);
  font-size: 14px;
  font-weight: 700;
  color: var(--color-midnight-ink);
  letter-spacing: 0.04em;
}

.sidebar-divider {
  height: 1px;
  background: var(--color-border);
}

.sidebar-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ── Main Column ─────────────────────────────────────────── */
.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* ── Topbar ──────────────────────────────────────────────── */
.topbar {
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-24);
  height: var(--nav-height);
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  z-index: 10;
}

.topbar-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success-green);
  box-shadow: 0 0 6px var(--color-success-green);
  margin-right: 10px;
  flex-shrink: 0;
}

.topbar-title {
  font-size: 11px;
  color: var(--color-text-dim);
  letter-spacing: 0.06em;
}

.topbar-spacer { flex: 1; }

/* ── Chat Main ───────────────────────────────────────────── */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  background: var(--color-bg);
  /* Premium radial grid backdrop pattern */
  background-image: radial-gradient(rgba(0, 0, 0, 0.04) 1.2px, transparent 1.2px);
  background-size: 24px 24px;
}

.chat-scroll {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.chat-input-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  padding: var(--spacing-16) var(--spacing-24) var(--spacing-24);
  border-top: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(8px);
}
</style>
