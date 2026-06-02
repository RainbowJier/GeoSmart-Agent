<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
.fade-page-enter-active,
.fade-page-leave-active {
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1), transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

