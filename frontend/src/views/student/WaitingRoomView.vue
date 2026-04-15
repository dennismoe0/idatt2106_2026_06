<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const isLoading = ref(true);
const errorMessage = ref("");
const kickedMessage = ref("");
const currentStatus = ref("");

let pollInterval = null;

async function fetchStudentStatus() {
  try {
    errorMessage.value = "";
    
    //placeholder
    const response = await fetch("/api/student/status");

    if (!response.ok) {
      throw new Error("Failed to fetch student status");
    }

    const data = await response.json();
    const status = data.status;

    
    if (currentStatus.value !== status) {
      console.log("[WaitingRoom] Status changed:", currentStatus.value, "→", status);
    }

    currentStatus.value = status;
    isLoading.value = false;

    if (status === "APPROVED") {
      console.log("[WaitingRoom] Student approved → redirecting to /");
      stopPolling();
      router.push("/");
      return;
    }

    if (status === "KICKED") {
      console.warn("[WaitingRoom] Student was kicked");
      kickedMessage.value = "You have been removed from the waiting room.";
      stopPolling();
      return;
    }

  } catch (error) {
    isLoading.value = false;
    errorMessage.value = "Could not update waiting room status.";

    console.error("[WaitingRoom] fetchStudentStatus failed:", error);
  }
}

function startPolling() {
  console.log("[WaitingRoom] Starting polling...");
  
  fetchStudentStatus();

  pollInterval = setInterval(() => {
    fetchStudentStatus();
  }, 3000);
}

function stopPolling() {
  if (pollInterval) {
    console.log("[WaitingRoom] Stopping polling");
    clearInterval(pollInterval);
    pollInterval = null;
  }
}

onMounted(() => {
  console.log("[WaitingRoom] Component mounted");
  startPolling();
});

onUnmounted(() => {
  console.log("[WaitingRoom] Component unmounted");
  stopPolling();
});
</script>

<template>
  <section class="waiting-room-view">
    <h1>venterom</h1>

    <p v-if="isLoading">sjekker statusen...</p>

    <p v-else-if="kickedMessage">{{ kickedMessage }}</p>

    <div v-else>
      <p>You are waiting for approval.</p>
      <p v-if="currentStatus">Current status: {{ currentStatus }}</p>
    </div>

    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>
  </section>
</template>