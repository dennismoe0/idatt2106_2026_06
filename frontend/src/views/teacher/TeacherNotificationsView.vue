<template>
  <div class="notifications-page">
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { useClassroomStore } from '@/stores/classroom'

const router = useRouter()
const notificationStore = useNotificationStore()
const classroomStore = useClassroomStore()
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
