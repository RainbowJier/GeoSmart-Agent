<template>
  <div class="not-found-container">
    <div class="content-box">
      <div class="icon-wrap">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 2 12 12 22 22 12 12 2" />
        </svg>
      </div>
      
      <h1 class="display-code">404</h1>
      <h2 class="sub-title">Lost in documentation</h2>
      <p class="description">
        The handbooks and guides you are looking for seem to have relocated or don't exist yet. Let's get you back on track.
      </p>
      
      <router-link :to="{ name: 'chat' }" class="home-btn">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12" />
          <polyline points="12 19 5 12 12 5" />
        </svg>
        <span>Return Home</span>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
// Minimal setup, routing is handled by standard vue-router links
</script>

<style scoped>
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100vh;
  background: var(--color-canvas-pearl);
  position: relative;
  overflow: hidden;
}



.content-box {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 440px;
  padding: 40px;
  z-index: 1;
  animation: fadeInUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-default);
  background: var(--color-snowdrift-white);
  border: 1px solid var(--color-ash-border);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: var(--color-ink-black);
}

.display-code {
  font-family: var(--font-display);
  font-size: 110px;
  font-weight: 300;
  color: var(--color-ink-black);
  letter-spacing: -4px;
  line-height: 0.9;
  margin-bottom: 12px;
}

.sub-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 600;
  color: var(--color-ink-black);
  margin-bottom: 12px;
}

.description {
  font-family: var(--font-sans);
  font-size: 14px;
  color: var(--color-stone-whisper);
  line-height: 1.5;
  margin-bottom: 32px;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 11px 22px;
  border-radius: var(--radius-buttons);
  background: var(--color-light-lime);
  border: 1px solid var(--color-lime-spritz);
  color: var(--color-ink-black);
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.home-btn:hover {
  background: #ade900;
  border-color: #9cd200;
  transform: translateY(-1.5px);
  box-shadow: 0 4px 12px rgba(173, 233, 0, 0.15);
}

.home-btn:active {
  transform: translateY(0);
}
</style>
