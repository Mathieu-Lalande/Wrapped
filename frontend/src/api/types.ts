/** Miroir des records Java exposés par GET /api/wrapped/{login}. */

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export type Month =
  | 'JANUARY'
  | 'FEBRUARY'
  | 'MARCH'
  | 'APRIL'
  | 'MAY'
  | 'JUNE'
  | 'JULY'
  | 'AUGUST'
  | 'SEPTEMBER'
  | 'OCTOBER'
  | 'NOVEMBER'
  | 'DECEMBER';

export type PersonaCode =
  | 'HIBERNATING'
  | 'MARATHONER'
  | 'TEAM_PLAYER'
  | 'BUG_HUNTER'
  | 'POLYGLOT'
  | 'WEEKEND_WARRIOR'
  | 'LASER_FOCUS'
  | 'STEADY_BUILDER';

export interface Totals {
  readonly contributions: number;
  readonly commits: number;
  readonly pullRequests: number;
  readonly issues: number;
}

export interface DayStat {
  /** ISO yyyy-MM-dd */
  readonly date: string;
  readonly count: number;
}

export interface Streak {
  readonly days: number;
  readonly start: string | null;
  readonly end: string | null;
}

export interface WeekdayStat {
  readonly day: DayOfWeek;
  readonly contributions: number;
}

export interface MonthStat {
  readonly month: Month;
  readonly contributions: number;
}

export interface RepoStat {
  readonly nameWithOwner: string;
  readonly commits: number;
  readonly language: string | null;
  readonly languageColor: string | null;
}

export interface LanguageStat {
  readonly name: string;
  readonly color: string | null;
  readonly commits: number;
  readonly percentage: number;
}

export interface Persona {
  readonly code: PersonaCode;
  readonly title: string;
  readonly description: string;
}

export interface Wrapped {
  readonly login: string;
  readonly name: string | null;
  readonly year: number;
  readonly totals: Totals;
  readonly calendar: readonly DayStat[];
  readonly longestStreak: Streak;
  readonly currentStreak: Streak;
  readonly favoriteWeekday: WeekdayStat | null;
  readonly mostActiveMonth: MonthStat | null;
  readonly topRepositories: readonly RepoStat[];
  readonly topLanguages: readonly LanguageStat[];
  readonly persona: Persona;
}

/** RFC 9457 ProblemDetail renvoyé par le backend. */
export interface ProblemDetail {
  readonly type?: string;
  readonly title?: string;
  readonly status?: number;
  readonly detail?: string;
}
