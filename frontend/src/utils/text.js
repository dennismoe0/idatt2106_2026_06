export function splitLines(value) {
  if (Array.isArray(value)) return value
  return String(value ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
}
