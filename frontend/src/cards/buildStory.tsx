import type { Wrapped } from '../api/types';
import type { StoryEntry } from '../components/StoryPlayer';
import { FavoriteDayCard } from './FavoriteDayCard';
import { HeatmapCard } from './HeatmapCard';
import { LanguagesCard } from './LanguagesCard';
import { PersonaCard } from './PersonaCard';
import { StreakCard } from './StreakCard';
import { TopReposCard } from './TopReposCard';
import { TotalCard } from './TotalCard';

/**
 * Construit la suite de cartes. Les cartes sans donnée (ex. aucun repo, aucune série)
 * sont omises plutôt qu'affichées vides ; le total et le persona sont toujours présents.
 */
export function buildStory(data: Wrapped): StoryEntry[] {
  const entries: Array<StoryEntry | false> = [
    { id: 'total', label: 'Total des contributions', content: <TotalCard data={data} /> },
    data.calendar.length > 0 && { id: 'heatmap', label: 'Calendrier', content: <HeatmapCard data={data} /> },
    data.longestStreak.days > 0 && { id: 'streak', label: 'Plus longue série', content: <StreakCard data={data} /> },
    data.topRepositories.length > 0 && { id: 'repos', label: 'Top repos', content: <TopReposCard data={data} /> },
    data.topLanguages.length > 0 && { id: 'languages', label: 'Top langages', content: <LanguagesCard data={data} /> },
    data.favoriteWeekday !== null && { id: 'day', label: 'Jour favori', content: <FavoriteDayCard data={data} /> },
    { id: 'persona', label: 'Persona', content: <PersonaCard data={data} /> },
  ];
  return entries.filter((entry): entry is StoryEntry => entry !== false);
}
