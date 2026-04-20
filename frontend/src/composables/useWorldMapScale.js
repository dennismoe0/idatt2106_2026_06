import { ref, onMounted, onUnmounted } from 'vue'

export function useWorldMapScale() {
  const containerRef = ref(null)
  const scale = ref(1)
  let observer

  function updateScale() {
    if (!containerRef.value) return
    const { width, height } = containerRef.value.getBoundingClientRect()
    if (width === 0 || height === 0) return
    const s = Math.min(width / 1600, height / 900)
    scale.value = s
    console.log('[worldMapScale] scale:', s.toFixed(3))
  }

  onMounted(() => {
    observer = new ResizeObserver(updateScale)
    if (containerRef.value) observer.observe(containerRef.value)
    // Two rAF frames ensure the browser has committed layout before we measure
    requestAnimationFrame(() => requestAnimationFrame(updateScale))
  })

  onUnmounted(() => observer?.disconnect())

  return { containerRef, scale }
}
