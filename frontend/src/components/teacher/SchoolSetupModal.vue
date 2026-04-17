<template>
  <BaseModal v-model="modelValue" title="Koble til skole" @update:modelValue="$emit('update:modelValue', $event)">
    <div class="tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'create' }"
        @click="setTab('create')"
      >Opprett skole</button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'join' }"
        @click="setTab('join')"
      >Bli med i skole</button>
    </div>

    <form @submit.prevent="submit" class="form">
      <div v-if="activeTab === 'create'">
        <label class="form-label">Skolenavn</label>
        <input
          v-model="name"
          class="form-input"
          placeholder="F.eks. Vågsbygd ungdomsskole"
          required
          maxlength="200"
        />
      </div>
      <div v-else>
        <label class="form-label">Invitasjonskode</label>
        <input
          v-model="code"
          class="form-input"
          placeholder="F.eks. nord-01"
          required
        />
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? 'Lagrer…' : activeTab === 'create' ? 'Opprett' : 'Bli med' }}
      </button>
    </form>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import { useSchoolStore } from '@/stores/school'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'school-set'])

const schoolStore = useSchoolStore()

const activeTab = ref('create')
const name = ref('')
const code = ref('')
const loading = ref(false)
const error = ref('')

watch(() => props.modelValue, (open) => {
  if (open) {
    activeTab.value = 'create'
    name.value = ''
    code.value = ''
    error.value = ''
  }
})

function setTab(tab) {
  activeTab.value = tab
  if (tab === 'create') code.value = ''
  else name.value = ''
  error.value = ''
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    if (activeTab.value === 'create') {
      await schoolStore.createSchool(name.value)
    } else {
      await schoolStore.joinSchool(code.value)
    }
    emit('update:modelValue', false)
    emit('school-set')
  } catch (err) {
    console.error('[SchoolSetupModal] submit failed:', err)
    error.value = err?.response?.data?.message ?? 'Noe gikk galt. Prøv igjen.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}
.tab-btn {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
  background: var(--color-surface-alt);
  cursor: pointer;
}
.tab-btn.active {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border-color: var(--color-primary);
  font-weight: var(--font-semibold);
}
.form { display: flex; flex-direction: column; gap: var(--space-3); }
.form-label { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--color-text); }
.form-input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  background: var(--color-surface);
  color: var(--color-text);
}
.error-msg { font-size: var(--text-sm); color: var(--color-error); }
.btn-submit {
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border-radius: var(--radius-md);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  width: 100%;
}
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
