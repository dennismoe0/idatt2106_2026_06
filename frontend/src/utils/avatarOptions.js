export const optionTranslations = {
  neutral: 'Nøytral',
  female: 'Jente',
  male: 'Gutt',
  blue: 'Blå',
  brown: 'Brun',
  green: 'Grønn',
  gray: 'Grå',
  light: 'Lys',
  medium: 'Middels',
  dark: 'Mørk',
  black: 'Svart',
  blonde: 'Blond',
  red: 'Rød',
  short: 'Kort',
  curly: 'Krøllete',
  ponytail: 'Hestehale',
  buzz: 'Kortklipt',
  'detective-coat': 'Detektivfrakk',
  hoodie: 'Hettegenser',
  uniform: 'Uniform',
  raincoat: 'Regnjakke',
  none: 'Ingen',
  badge: 'Merke',
  glasses: 'Briller',
  magnifier: 'Forstørrelsesglass',
}

export function formatOption(value) {
  return optionTranslations[value] || value
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
