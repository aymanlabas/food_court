export const CATEGORIES = [
  { value: 'ALL', label: 'Tous' },
  { value: 'ENTREE', label: 'Entrée' },
  { value: 'PLAT_PRINCIPAL', label: 'Plat Principal' },
  { value: 'DESSERT', label: 'Dessert' },
  { value: 'BOISSON', label: 'Boisson' },
  { value: 'ACCOMPAGNEMENT', label: 'Accompagnement' },
];

export const CATEGORY_MAP = {
  ENTREE: 'Entrée',
  PLAT_PRINCIPAL: 'Plat Principal',
  DESSERT: 'Dessert',
  BOISSON: 'Boisson',
  ACCOMPAGNEMENT: 'Accompagnement',
};

export const CATEGORY_EMOJI = {
  ENTREE: '🥗',
  PLAT_PRINCIPAL: '🍽️',
  DESSERT: '🍰',
  BOISSON: '🥤',
  ACCOMPAGNEMENT: '🥖',
};

export const CATEGORY_GRADIENT = {
  ENTREE: 'from-emerald-400 to-teal-500',
  PLAT_PRINCIPAL: 'from-orange-400 to-red-500',
  DESSERT: 'from-pink-400 to-rose-500',
  BOISSON: 'from-blue-400 to-indigo-500',
  ACCOMPAGNEMENT: 'from-amber-400 to-yellow-500',
};

export const ROLES = {
  ADMIN: 'ADMIN',
  EMPLOYEE: 'EMPLOYEE',
  KITCHEN: 'KITCHEN',
};

export const ROLE_LABELS = {
  ADMIN: 'Administrateur',
  EMPLOYEE: 'Employé',
  KITCHEN: 'Cuisine',
};

export const ORDER_STATUSES = {
  PENDING: 'En attente',
  PREPARING: 'En préparation',
  READY: 'Prêt',
  DELIVERED: 'Livré',
  CANCELLED: 'Annulé',
};

export const ORDER_STATUS_COLORS = {
  PENDING: 'warning',
  PREPARING: 'info',
  READY: 'success',
  DELIVERED: 'default',
  CANCELLED: 'error',
};

export const UNITS = [
  { value: 'KG', label: 'Kilogrammes (kg)' },
  { value: 'G', label: 'Grammes (g)' },
  { value: 'L', label: 'Litres (L)' },
  { value: 'ML', label: 'Millilitres (mL)' },
  { value: 'UNITE', label: 'Unité(s)' },
];

export const UNIT_LABELS = {
  KG: 'kg',
  G: 'g',
  L: 'L',
  ML: 'mL',
  UNITE: 'unité(s)',
};

export const ALLERGENS = [
  'Gluten',
  'Crustacés',
  'Oeufs',
  'Poisson',
  'Arachides',
  'Soja',
  'Lait',
  'Fruits à coque',
  'Céleri',
  'Moutarde',
  'Sésame',
  'Sulfites',
  'Lupin',
  'Mollusques',
];

export const DIETARY_OPTIONS = [
  { value: 'OMNIVORE', label: 'Omnivore' },
  { value: 'VEGETARIEN', label: 'Végétarien' },
  { value: 'VEGAN', label: 'Végan' },
  { value: 'SANS_GLUTEN', label: 'Sans gluten' },
  { value: 'HALAL', label: 'Halal' },
];
