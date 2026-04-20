import { ref } from 'vue'

export function useAvatarWalk() {
  const currentNodeIndex = ref(0)
  const isWalking = ref(false)

  function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  function determineTarget(stops) {
    if (!stops.length) return 0
    const idx = stops.findIndex(s => !s.locked && !s.completed)
    if (idx !== -1) return idx
    // Fall back to last unlocked stop if all are completed; 0 if none are unlocked
    const lastUnlocked = stops.reduce((acc, s, i) => (!s.locked ? i : acc), -1)
    return lastUnlocked !== -1 ? lastUnlocked : 0
  }

  async function walkTo(targetIndex) {
    if (isWalking.value || targetIndex === currentNodeIndex.value) return
    isWalking.value = true
    console.log('[avatarWalk] Walking from', currentNodeIndex.value, 'to', targetIndex)
    const dir = targetIndex > currentNodeIndex.value ? 1 : -1
    while (currentNodeIndex.value !== targetIndex) {
      await sleep(700)
      currentNodeIndex.value += dir
      console.log('[avatarWalk] At node', currentNodeIndex.value)
    }
    isWalking.value = false
    console.log('[avatarWalk] Arrived at node', currentNodeIndex.value)
  }

  async function initAutoWalk(stops) {
    if (!stops.length) return
    currentNodeIndex.value = 0
    await sleep(400)
    const target = determineTarget(stops)
    if (target > 0) await walkTo(target)
  }

  return { currentNodeIndex, isWalking, walkTo, initAutoWalk, determineTarget }
}
