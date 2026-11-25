import type { Wrapped } from '../api/types';

export function makeWrapped(overrides: Partial<Wrapped> = {}): Wrapped {
  return {
    login: 'octocat',
    name: 'The Octocat',
    year: 2025,
    totals: { contributions: 1234, commits: 900, pullRequests: 80, issues: 12 },
    calendar: [
      { date: '2025-01-01', count: 3 },
      { date: '2025-01-02', count: 0 },
      { date: '2025-01-03', count: 7 },
    ],
    longestStreak: { days: 12, start: '2025-03-01', end: '2025-03-12' },
    currentStreak: { days: 0, start: null, end: null },
    favoriteWeekday: { day: 'TUESDAY', contributions: 300 },
    mostActiveMonth: { month: 'MARCH', contributions: 210 },
    topRepositories: [
      { nameWithOwner: 'octocat/hello', commits: 400, language: 'Java', languageColor: '#b07219' },
      { nameWithOwner: 'octocat/web', commits: 200, language: 'TypeScript', languageColor: '#3178c6' },
    ],
    topLanguages: [
      { name: 'Java', color: '#b07219', commits: 400, percentage: 66.7 },
      { name: 'TypeScript', color: '#3178c6', commits: 200, percentage: 33.3 },
    ],
    persona: { code: 'STEADY_BUILDER', title: 'Bâtisseur régulier', description: 'Une constance redoutable.' },
    ...overrides,
  };
}
