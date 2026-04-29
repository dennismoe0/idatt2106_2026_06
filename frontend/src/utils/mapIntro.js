export const MAP_INTRO_STORAGE_KEY = 'hasSeenMapIntro'
export const MAP_INTRO_QUERY_FLAG = 'showMapIntro'

export function hasSeenMapIntro() {
  return localStorage.getItem(MAP_INTRO_STORAGE_KEY) === 'true'
}

export function shouldShowMapIntroPopup(route) {
  const queryValue = route?.query?.[MAP_INTRO_QUERY_FLAG]
  const requestedFromHome = Array.isArray(queryValue)
    ? queryValue.includes('1')
    : queryValue === '1'

  return requestedFromHome && !hasSeenMapIntro()
}

export function buildMapIntroRoute(name) {
  if (hasSeenMapIntro()) {
    return { name }
  }

  return {
    name,
    query: { [MAP_INTRO_QUERY_FLAG]: '1' },
  }
}
