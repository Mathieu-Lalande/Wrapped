import type { DayStat } from '../api/types';

export type HeatmapLevel = 0 | 1 | 2 | 3 | 4;

/** Une semaine = 7 cases (dimanche → samedi) ; `null` pour les jours hors période. */
export type Week = ReadonlyArray<DayStat | null>;

/**
 * Intensité 0–4 d'une case, relative au maximum de l'année.
 * 0 = aucune contribution ; toute contribution vaut au moins 1.
 */
export function contributionLevel(count: number, max: number): HeatmapLevel {
  if (count <= 0 || max <= 0) {
    return 0;
  }
  return Math.min(4, Math.max(1, Math.ceil((count / max) * 4))) as HeatmapLevel;
}

/** Regroupe des jours triés en semaines de 7 cases, en complétant la première et la dernière. */
export function buildWeeks(days: readonly DayStat[]): Week[] {
  const first = days[0];
  if (first === undefined) {
    return [];
  }
  const leading = new Date(`${first.date}T00:00:00Z`).getUTCDay();
  const cells: Array<DayStat | null> = [...Array<null>(leading).fill(null), ...days];
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }
  const weeks: Week[] = [];
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7));
  }
  return weeks;
}

/** Coupe les semaines en deux blocs (≈ semestres) pour rester lisible sur mobile. */
export function splitInHalves(weeks: readonly Week[]): [Week[], Week[]] {
  const middle = Math.ceil(weeks.length / 2);
  return [weeks.slice(0, middle), weeks.slice(middle)];
}
