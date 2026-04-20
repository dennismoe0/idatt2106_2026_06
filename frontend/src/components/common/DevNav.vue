<template>
  <nav class="dev-nav">
    <span class="dev-label">DEV</span>
    <RouterLink v-for="link in links" :key="link.path" :to="link.path" class="dev-link">
      {{ link.label }}
    </RouterLink>
  </nav>
</template>

<script setup>
const links = [
  { path: '/login',         label: 'Login' },
  { path: '/register',      label: 'Register' },
  { path: '/student-login', label: 'Student Login' },
  { path: '/',              label: 'Home' },
  { path: '/intro',         label: 'Intro' },
  { path: '/join',          label: 'Join' },
  { path: '/waiting',       label: 'Waiting' },
  { path: '/map',           label: 'Map' },
  { path: '/task',          label: 'Task' },
  { path: '/avatar',        label: 'Avatar' },
  { path: '/teacher',       label: 'Teacher' },
]
</script>

<style scoped>
.dev-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(4px);
  overflow-x: auto;
  scrollbar-width: none;
  font-family: monospace;
  font-size: 11px;
}
.dev-nav::-webkit-scrollbar { display: none; }

.dev-label {
  color: #ff5f57;
  font-weight: 900;
  font-size: 10px;
  letter-spacing: 1px;
  padding: 2px 6px;
  border: 1px solid #ff5f57;
  border-radius: 3px;
  margin-right: 6px;
  flex-shrink: 0;
  line-height: 1.6;
}

.dev-link {
  color: #aaa;
  text-decoration: none;
  padding: 3px 8px;
  border-radius: 3px;
  white-space: nowrap;
  flex-shrink: 0;
  transition: background 0.1s, color 0.1s;
}
.dev-link:hover {
  background: rgba(255,255,255,0.12);
  color: #fff;
}
.dev-link.router-link-active {
  background: rgba(255,255,255,0.18);
  color: #fff;
}
</style>
