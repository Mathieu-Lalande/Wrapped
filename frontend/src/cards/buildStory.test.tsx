import { buildStory } from './buildStory';
import { makeWrapped } from '../test/fixtures';

describe('buildStory', () => {
  it('produit les 7 cartes quand toutes les données sont présentes', () => {
    expect(buildStory(makeWrapped()).map((e) => e.id)).toEqual([
      'total',
      'heatmap',
      'streak',
      'repos',
      'languages',
      'day',
      'persona',
    ]);
  });

  it('omet les cartes sans donnée pour une année vide', () => {
    const empty = makeWrapped({
      totals: { contributions: 0, commits: 0, pullRequests: 0, issues: 0 },
      calendar: [],
      longestStreak: { days: 0, start: null, end: null },
      favoriteWeekday: null,
      mostActiveMonth: null,
      topRepositories: [],
      topLanguages: [],
    });

    expect(buildStory(empty).map((e) => e.id)).toEqual(['total', 'persona']);
  });
});
