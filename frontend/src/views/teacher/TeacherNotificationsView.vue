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
            <router-link to="/teacher/settings" class="nav-link">
              <span class="nav-icon">⚙️</span> Innstillinger
            </router-link>
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
          <RouterLink to="/teacher" class="btn btn-outline btn-sm back-btn">← Tilbake til dashboard</RouterLink>
          <h1>Varsler</h1>
          <p>{{ notificationStore.unreadCount }} uleste varsler</p>
        </div>
        <div class="header-actions">
          <button
            class="btn btn-primary"
            :disabled="notificationStore.unreadCount === 0 || actionLoading"
            @click="markAll"
          >
            Marker alle som lest
          </button>
          <button
            class="btn btn-danger"
            :disabled="sortedNotifications.length === 0 || actionLoading"
            @click="deleteAll"
          >
            Slett alle varsler
          </button>
        </div>
      </header>

      <div v-if="notificationStore.loading" class="state-card">Laster varsler...</div>
      <div v-else-if="notificationStore.error" class="error-card">{{ notificationStore.error }}</div>
      <div v-else-if="sortedNotifications.length === 0" class="state-card">
        Ingen varsler.
      </div>

      <section v-else class="notifications-list">
        <!-- Recent / active notifications -->
        <article
          v-for="notification in recentNotifications"
          :key="notification.id"
          class="notification-card"
          :class="{
            unread: !notification.isRead,
            'notification-card--handled': notification.isRead
          }"
        >
          <div class="notification-main">
            <span class="type-label">{{ typeLabel(notification.type) }}</span>
            <h2>{{ notification.message }}</h2>
            <p>{{ formatDate(notification.createdAt) }}</p>
          </div>

          <div class="notification-actions">
            <template v-if="notification.type === 'STUDENT_JOIN_REQUEST'">
              <span
                v-if="notification.status && notification.status !== 'PENDING'"
                class="handled-badge"
                :class="handledActions[notification.id] === 'KICKED' ? 'handled-badge--denied' : 'handled-badge--approved'"
              >
                {{ handledActions[notification.id] === 'KICKED' ? '✗ Avvist' : '✓ Godkjent' }}
              </span>
              <template v-else>
                <button
                  class="btn btn-primary btn-sm"
                  :data-testid="`approve-notification-${notification.id}`"
                  :disabled="actionLoading || handledActions[notification.id]"
                  @click="handleJoinRequest(notification, 'APPROVED')"
                >
                  Godkjenn
                </button>
                <button
                  class="btn btn-danger btn-sm"
                  :data-testid="`deny-notification-${notification.id}`"
                  :disabled="actionLoading || handledActions[notification.id]"
                  @click="handleJoinRequest(notification, 'KICKED')"
                >
                  Avvis
                </button>
              </template>
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

            <button
              class="btn btn-ghost btn-sm"
              :disabled="actionLoading"
              @click="deleteNotification(notification.id)"
              title="Slett varsel"
            >
              🗑️
            </button>
          </div>
        </article>

        <div v-if="oldNotifications.length > 0" class="old-divider">
          <span>Eldre varsler</span>
        </div>

        <article
          v-for="notification in oldNotifications"
          :key="notification.id"
          class="notification-card notification-card--old"
        >
          <div class="notification-main">
            <span class="type-label type-label--muted">{{ typeLabel(notification.type) }}</span>
            <h2>{{ notification.message }}</h2>
            <p>{{ formatDate(notification.createdAt) }}</p>
          </div>

          <div class="notification-actions">
            <button
              class="btn btn-ghost btn-sm"
              :disabled="actionLoading"
              @click="deleteNotification(notification.id)"
              title="Slett varsel"
            >
              🗑️
            </button>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, computed, ref } from 'vue'
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
const handledActions = ref({}) // { [notificationId]: 'APPROVED' | 'KICKED' }

// Notifications older than 8 hours are considered stale
const STALE_MS = 8 * 60 * 60 * 1000

function isOld(notification) {
  if (!notification.createdAt) return false
  return Date.now() - new Date(notification.createdAt).getTime() > STALE_MS
}

// All notifications sorted newest first
const sortedNotifications = computed(() =>
  [...(notificationStore.notifications ?? [])].sort(
    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
  )
)

const recentNotifications = computed(() =>
  sortedNotifications.value.filter(n => !isOld(n))
)

const oldNotifications = computed(() =>
  sortedNotifications.value.filter(n => isOld(n))
)

// Auto-delete old notifications every 8 hours
let autoCleanupInterval = null

let pollInterval = null

onMounted(async () => {
  await Promise.all([
    notificationStore.fetchNotifications(),
    notificationStore.fetchUnreadCount()
  ])

  pollInterval = setInterval(async () => {
    await notificationStore.fetchNotifications()
    await notificationStore.fetchUnreadCount()
  }, 5000)

  // Run once on mount, then every 8 hours
  purgeOldNotifications()
  autoCleanupInterval = setInterval(purgeOldNotifications, STALE_MS)
})

onUnmounted(() => {
  clearInterval(autoCleanupInterval)
  clearInterval(pollInterval)
})

async function purgeOldNotifications() {
  try {
    await notificationStore.deleteOldNotifications()
  } catch (err) {
    console.warn('[TeacherNotificationsView] Auto-purge failed', err)
  }
}

async function deleteNotification(id) {
  actionLoading.value = true
  try {
    await notificationStore.deleteNotification(id)
  } finally {
    actionLoading.value = false
  }
}

async function deleteAll() {
  actionLoading.value = true
  try {
    const all = [...(notificationStore.notifications ?? [])]
    for (const n of all) {
      try {
        await notificationStore.deleteNotification(n.id)
      } catch (err) {
        console.warn('[TeacherNotificationsView] Failed to delete notification', n.id, err)
      }
    }
  } finally {
    actionLoading.value = false
  }
}

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
    handledActions.value = { ...handledActions.value, [notification.id]: status }
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

.header-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  align-items: center;
}

.back-btn {
  align-self: flex-start;
  margin-bottom: var(--space-2);
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

/* Old/stale notifications */
.notification-card--old {
  opacity: 0.45;
  border-color: transparent !important;
  box-shadow: none;
}

.notification-card--old:hover {
  opacity: 0.65;
}

/* Handled (approved/denied) notifications */
.notification-card--handled {
  opacity: 0.5;
  border-color: transparent !important;
  box-shadow: none;
  transition: opacity var(--transition-fast);
}

.notification-card--handled:hover {
  opacity: 0.7;
}

.handled-badge {
  display: inline-flex;
  align-items: center;
  font-size: var(--text-xs);
  font-weight: 800;
  padding: 4px 12px;
  border-radius: var(--radius-full);
}

.handled-badge--approved {
  background: var(--color-success-light, #dcfce7);
  color: var(--color-success, #16a34a);
}

.handled-badge--denied {
  background: var(--color-danger-light, #fee2e2);
  color: var(--color-danger);
}

.old-divider {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-top: var(--space-2);
}

.old-divider::before,
.old-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
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

.type-label--muted {
  background: var(--color-border);
  color: var(--color-text-muted);
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

.btn-ghost {
  background: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
}

.btn-ghost:hover {
  background: var(--color-surface-hover, #f9fafb);
  color: var(--color-danger);
  border-color: var(--color-danger);
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

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
