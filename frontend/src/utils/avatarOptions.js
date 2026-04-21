export const SKIN_COLORS  = ['#FDDBB4','#EDB98A','#D08B5B','#AE5D29','#694D3D','#3B1F0E']
export const HAIR_COLORS  = ['#1a1a1a','#8B4513','#D2691E','#F4D150','#E8E1E1','#CC2200','#FF69B4','#9B59B6']
export const EYE_COLORS   = ['#4a3000','#1e40af','#15803d','#6b7280','#92400e']
export const OUTFIT_COLORS = ['#2563eb','#dc2626','#16a34a','#d97706','#1f2937']

export const HAIR_STYLES   = ['short','long','curly','ponytail','buzz','braids','bun','afro','bald']
export const EYE_STYLES    = ['round','narrow','wide']
export const OUTFITS       = ['detective-coat','hoodie','sweater','uniform','raincoat']
export const ACCESSORIES   = ['none','badge','glasses','magnifier','hat']

export const optionTranslations = {
  female: 'Jente', male: 'Gutt', neutral: 'Nøytral',
  short: 'Kort', long: 'Langt', curly: 'Krøllete', ponytail: 'Hestehale',
  buzz: 'Kort-klipp', braids: 'Fletter', bun: 'Knute', afro: 'Afro', bald: 'Skallet',
  round: 'Runde', narrow: 'Smale', wide: 'Store',
  'detective-coat': 'Detektivfrakk', hoodie: 'Hettegenser', sweater: 'Genser',
  uniform: 'Uniform', raincoat: 'Regnjakke',
  none: 'Ingen', badge: 'Merke', glasses: 'Briller',
  magnifier: 'Forstørrelsesglass', hat: 'Hatt',
}

export function formatOption(value) {
  return optionTranslations[value] || value.split('-').map(p => p[0].toUpperCase() + p.slice(1)).join(' ')
}
