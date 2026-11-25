import type { DayOfWeek, Month } from '../api/types';

const LOCALE = 'fr-FR';
const numberFormatter = new Intl.NumberFormat(LOCALE);

export function formatNumber(value: number): string {
  return numberFormatter.format(value);
}

export const WEEKDAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'lundi',
  TUESDAY: 'mardi',
  WEDNESDAY: 'mercredi',
  THURSDAY: 'jeudi',
  FRIDAY: 'vendredi',
  SATURDAY: 'samedi',
  SUNDAY: 'dimanche',
};

export const MONTH_LABELS: Record<Month, string> = {
  JANUARY: 'janvier',
  FEBRUARY: 'février',
  MARCH: 'mars',
  APRIL: 'avril',
  MAY: 'mai',
  JUNE: 'juin',
  JULY: 'juillet',
  AUGUST: 'août',
  SEPTEMBER: 'septembre',
  OCTOBER: 'octobre',
  NOVEMBER: 'novembre',
  DECEMBER: 'décembre',
};

const dateFormatter = new Intl.DateTimeFormat(LOCALE, { day: 'numeric', month: 'long', timeZone: 'UTC' });

/** "2026-03-08" → "8 mars" (date interprétée en UTC, sans décalage de fuseau). */
export function formatShortDate(isoDate: string): string {
  return dateFormatter.format(new Date(`${isoDate}T00:00:00Z`));
}

export function pluralize(count: number, singular: string, plural = `${singular}s`): string {
  return count > 1 ? plural : singular;
}
