// Vitest setup — provides a working localStorage in Node v25+ where the
// native localStorage stub does not implement the Storage interface.
const store = {}
const localStorageMock = {
  getItem: (key) => (key in store ? store[key] : null),
  setItem: (key, value) => { store[key] = String(value) },
  removeItem: (key) => { delete store[key] },
  clear: () => { Object.keys(store).forEach(k => delete store[k]) },
  get length() { return Object.keys(store).length },
  key: (i) => Object.keys(store)[i] ?? null,
}
Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
  configurable: true,
})

// JSDOM does not implement media playback. The app expects play() to return a
// Promise so it can attach .catch(), matching browser behavior closely enough
// for component tests.
Object.defineProperty(globalThis.HTMLMediaElement.prototype, 'play', {
  value: () => Promise.resolve(),
  writable: true,
  configurable: true,
})

Object.defineProperty(globalThis.HTMLMediaElement.prototype, 'pause', {
  value: () => {},
  writable: true,
  configurable: true,
})

Object.defineProperty(globalThis, 'scrollTo', {
  value: () => {},
  writable: true,
  configurable: true,
})
