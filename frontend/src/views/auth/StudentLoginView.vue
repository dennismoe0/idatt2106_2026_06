<template>
	<main class="student-login-page">
		<div class="student-login-card">
			<h1>Elevinnlogging</h1>
			<p class="subtitle">Logg inn med brukernavn for å fortsette</p>

			<form class="student-login-form" @submit.prevent="handleSubmit" novalidate>
				<div class="field">
					<label for="username">Brukernavn</label>
					<input
						id="username"
						v-model="form.username"
						type="text"
						autocomplete="username"
						:aria-describedby="errors.username ? 'username-error' : undefined"
						:aria-invalid="!!errors.username"
						required
					/>
					<span v-if="errors.username" id="username-error" class="error" role="alert">
						{{ errors.username }}
					</span>
				</div>

				<span v-if="serverError" class="error" role="alert">{{ serverError }}</span>

				<BaseButton class="submit-btn" type="submit" :loading="loading">
					Logg inn som elev
				</BaseButton>
			</form>

			<div class="auth-links">
				<RouterLink to="/login">Lærer? Logg inn her</RouterLink>
			</div>
		</div>
	</main>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ username: '' })
const errors = reactive({ username: '' })
const serverError = ref('')
const loading = ref(false)

function validate() {
	errors.username = ''
	if (!form.username.trim()) {
		errors.username = 'Brukernavn er påkrevd'
		return false
	}
	return true
}

async function handleSubmit() {
	serverError.value = ''
	if (!validate()) return

	const normalizedUsername = form.username.trim()
	const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'

	loading.value = true
	try {
		await authStore.studentLogin(normalizedUsername)

		if (hasSeenIntro) {
			router.push('/')
			return
		}

		router.push('/intro')
	} catch (err) {
		serverError.value = err?.response?.data?.error || 'Innlogging feilet. Prøv igjen.'
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.student-login-page {
	min-height: 100vh;
	display: grid;
	place-items: center;
	padding: 1rem;
}

.student-login-card {
	width: min(100%, 28rem);
	padding: 1.25rem;
	border: 1px solid #d8dee6;
	border-radius: 0.75rem;
	background: #fff;
}

.subtitle {
	margin-top: 0.25rem;
	margin-bottom: 1rem;
	color: #4b5563;
}

.student-login-form {
	display: grid;
	gap: 0.75rem;
}

.field {
	display: grid;
	gap: 0.4rem;
}

label {
	font-weight: 600;
}

input {
	padding: 0.65rem 0.75rem;
	border: 1px solid #94a3b8;
	border-radius: 0.5rem;
	font-size: 1rem;
}

input:focus {
	outline: 2px solid #2563eb;
	outline-offset: 1px;
	border-color: #2563eb;
}

.submit-btn {
	margin-top: 0.25rem;
}

.error {
	color: #b91c1c;
	font-size: 0.9rem;
}

.auth-links {
	margin-top: 1rem;
}
</style>
