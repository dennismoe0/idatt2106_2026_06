<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-icon">🔍</span>
        <div>
          <div class="logo-title">Nettdetektivene</div>
          <div class="logo-sub">Lærerportal</div>
        </div>
      </div>

      <div class="sidebar-user">
        <div class="user-avatar">👩‍🏫</div>
        <div>
          <div class="user-name">{{ authStore.user?.email ?? authStore.email ?? '' }}</div>
          <div class="user-school">{{ schoolStore.school?.name ?? 'Ingen skole' }}</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <ul>
          <li>
            <router-link to="/teacher" class="nav-link">
              <span class="nav-icon">🏠</span> Mine klasser
            </router-link>
          </li>
          <li>
            <a href="#" class="nav-link">
              <span class="nav-icon">📊</span> Fremgang
            </a>
          </li>
          <li>
            <a href="#" class="nav-link">
              <span class="nav-icon">🕯️</span> Ukens mysterium
            </a>
          </li>
          <li>
            <router-link
              to="/teacher/notifications"
              class="nav-link active"
              data-testid="notifications-link"
            >
              <span class="nav-icon">🔔</span> Varsler
              <span
                v-if="notificationStore.unreadCount > 0"
                class="notification-badge"
                data-testid="notification-badge"
              >
                {{ notificationStore.unreadCount }}
              </span>
            </router-link>
          </li>
          <li>
            <a href="#" class="nav-link">
              <span class="nav-icon">⚙️</span> Innstillinger
            </a>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="authStore.logout()">← Logg ut</button>
      </div>
    </aside>

    <main class="notifications-page">
      <header class="notifications-header">
        <div>
          <RouterLink to="/teacher" class="back-link">Tilbake til dashboard</RouterLink>
          <h1>Varsler</h1>
          <p>{{ notificationStore.unreadCount }} uleste varsler</p>
        </div>
        <button
          class="btn btn-primary"
          :disabled="notificationStore.unreadCount === 0 || actionLoading"
          @click="markAll"
        >
          Marker alle som lest
        </button>
      </header>

      <div v-if="notificationStore.loading" class="state-card">Laster varsler...</div>
      <div v-else-if="notificationStore.error" class="error-card">{{ notificationStore.error }}</div>
      <div v-else-if="notificationStore.notifications.length === 0" class="state-card">
        Ingen varsler.
      </div>

      <section v-else class="notifications-list">
        <article
          v-for="notification in notificationStore.notifications"
          :key="notification.id"
          class="notification-card"
          :class="{ unread: !notification.isRead }"
      >
          <div class="notification-main">
            <span class="type-label">{{ typeLabel(notification.type) }}</span>
            <h2>{{ notification.message }}</h2>
            <p>{{ formatDate(notification.createdAt) }}</p>
          </div>

          <div class="notification-actions">
            <template v-if="notification.type === 'STUDENT_JOIN_REQUEST'">
              <button
                class="btn btn-primary btn-sm"
                :data-testid="`approve-notification-${notification.id}`"
                :disabled="actionLoading"
                @click="handleJoinRequest(notification, 'APPROVED')"
              >
                Godkjenn
              </button>
              <button
                class="btn btn-danger btn-sm"
                :data-testid="`deny-notification-${notification.id}`"
                :disabled="actionLoading"
                @click="handleJoinRequest(notification, 'KICKED')"
              >
                Avvis
              </button>
            </template>

            <button
              v-if="notification.type === 'MYSTERY_SUBMITTED'"
              class="btn btn-primary btn-sm"
              :data-testid="`view-notification-${notification.id}`"
              @click="viewMystery(notification)"
            >
              Se mysterium
            </button>

            <button
              v-if="!notification.isRead"
              class="btn btn-outline btn-sm"
              :disabled="actionLoading"
              @click="notificationStore.markAsRead(notification.id)"
            >
              Marker lest
            </button>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { useClassroomStore } from '@/stores/classroom'
import { useAuthStore } from '@/stores/auth'
import { useSchoolStore } from '@/stores/school'

const router = useRouter()
const notificationStore = useNotificationStore()
const classroomStore = useClassroomStore()
const authStore = useAuthStore()
const schoolStore = useSchoolStore()
const actionLoading = ref(false)

onMounted(async () => {
  await Promise.all([
    notificationStore.fetchNotifications(),
    notificationStore.fetchUnreadCount()
  ])
})

function typeLabel(type) {
  if (type === 'STUDENT_JOIN_REQUEST') return 'Innmeldingsforespørsel'
  if (type === 'MYSTERY_SUBMITTED') return 'Nytt mysterium'
  return 'Varsel'
}

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('nb-NO', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function handleJoinRequest(notification, status) {
  const studentId = notification.studentId || notification.referenceId
  actionLoading.value = true
  try {
    await classroomStore.updateStudentStatus(notification.classroomId, studentId, status)
    await notificationStore.markAsRead(notification.id)
  } finally {
    actionLoading.value = false
  }
}

async function markAll() {
  actionLoading.value = true
  try {
    await notificationStore.markAllAsRead()
  } finally {
    actionLoading.value = false
  }
}

function viewMystery(notification) {
  router.push({ name: 'WeeklyMysteryManage', params: { classroomId: notification.classroomId } })
}
</script>

<style scoped>
.dashboard-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  font-family: var(--font-sans), sans-serif;
  background: var(--color-bg);
  color: var(--color-text);
}

.sidebar {
  background: var(--color-primary-dark);
  color: var(--color-text-on-dark);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.logo-icon {
  font-size: 28px;
}

.logo-title {
  font-size: var(--text-sm);
  font-weight: 900;
}

.logo-sub {
  font-size: var(--text-xs);
  opacity: 0.65;
  font-weight: 600;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 800;
}

.user-school {
  font-size: var(--text-xs);
  opacity: 0.65;
}

.sidebar-nav {
  padding: var(--space-3) 0;
  flex: 1;
}

.sidebar-nav ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 11px 20px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 700;
  border-left: 3px solid transparent;
  transition: all var(--transition-fast);
}

.nav-link:hover,
.nav-link.active {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-on-dark);
  border-left-color: var(--color-accent);
}

.nav-icon {
  width: 20px;
  text-align: center;
  font-size: 16px;
}

.notification-badge {
  margin-left: auto;
  min-width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-size: var(--text-xs);
  font-weight: 900;
}

.sidebar-footer {
  padding: var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  font-family: inherit;
  font-size: var(--text-xs);
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  transition: color var(--transition-fast);
}

.logout-btn:hover {
  color: var(--color-text-on-dark);
}

.notifications-page {
  min-height: 100vh;
  padding: 32px;
  background: var(--color-bg);
  color: var(--color-text);
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  max-width: 960px;
  margin: 0 auto 24px;
}

.notifications-header h1 {
  margin: 8px 0 4px;
  font-size: var(--text-2xl);
  font-weight: 900;
}

.notifications-header p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.back-link {
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: 800;
  text-decoration: none;
}

.notifications-list {
  display: grid;
  gap: 14px;
  max-width: 960px;
  margin: 0 auto;
}

.notification-card,
.state-card,
.error-card {
  background: var(--color-surface);
  border: 2px solid transparent;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: 20px;
}

.notification-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.notification-card.unread {
  border-color: var(--color-primary);
}

.notification-main h2 {
  margin: 8px 0 6px;
  font-size: var(--text-lg);
  font-weight: 900;
}

.notification-main p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.type-label {
  display: inline-flex;
  border-radius: var(--radius-md);
  padding: 4px 10px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: var(--text-xs);
  font-weight: 900;
}

.notification-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) 18px;
  border-radius: var(--radius-full);
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: 800;
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-primary {
  background: var(--color-btn-primary-bg);
  color: var(--color-btn-primary-fg);
}

.btn-outline {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.btn-danger {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
}

.btn-sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-xs);
}

.error-card {
  max-width: 960px;
  margin: 0 auto;
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.state-card {
  max-width: 960px;
  margin: 0 auto;
  color: var(--color-text-muted);
  font-weight: 700;
}

@media (max-width: 700px) {
  .dashboard-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    height: auto;
  }

  .notifications-page {
    padding: 20px var(--space-4);
  }

  .notifications-header,
  .notification-card {
    flex-direction: column;
  }

  .notification-actions {
    justify-content: flex-start;
  }
}
</style>
