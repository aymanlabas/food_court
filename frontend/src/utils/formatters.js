import { CATEGORY_MAP, UNIT_LABELS, ROLE_LABELS } from './constants';

/**
 * Format a price amount to French currency format.
 * @param {number} amount
 * @returns {string}
 */
export function formatPrice(amount) {
  if (amount == null || isNaN(amount)) return '0,00 €';
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);
}

/**
 * Format a date to French locale string.
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDate(date) {
  if (!date) return '';
  const d = new Date(date);
  return new Intl.DateTimeFormat('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(d);
}

/**
 * Format a date to short French locale string (date only).
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDateShort(date) {
  if (!date) return '';
  const d = new Date(date);
  return new Intl.DateTimeFormat('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(d);
}

/**
 * Get the French label for a category value.
 * @param {string} category
 * @returns {string}
 */
export function formatCategory(category) {
  return CATEGORY_MAP[category] || category || 'Inconnu';
}

/**
 * Get the French label for a unit value.
 * @param {string} unit
 * @returns {string}
 */
export function formatUnit(unit) {
  return UNIT_LABELS[unit] || unit || '';
}

/**
 * Get the French label for a role value.
 * @param {string} role
 * @returns {string}
 */
export function formatRole(role) {
  return ROLE_LABELS[role] || role || 'Inconnu';
}

/**
 * Truncate text to a given length.
 * @param {string} text
 * @param {number} maxLength
 * @returns {string}
 */
export function truncateText(text, maxLength = 80) {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '…';
}

/**
 * Get initials from a name.
 * @param {string} name
 * @returns {string}
 */
export function getInitials(name) {
  if (!name) return '?';
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}
