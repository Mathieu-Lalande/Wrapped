import type { DayOfWeek, Wrapped } from '../api/types';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { WEEKDAY_LABELS, formatNumber, pluralize } from '../lib/format';
import styles from './cards.module.css';

const TAGLINES: Record<DayOfWeek, string> = {
  MONDAY: 'Le lundi ne te fait pas peur.',
  TUESDAY: 'Le mardi, c’est ton jour de gloire.',
  WEDNESDAY: 'Le milieu de semaine, ton point d’équilibre.',
  THURSDAY: 'Le jeudi, tu passes la seconde.',
  FRIDAY: 'Tu finis la semaine en beauté.',
  SATURDAY: 'Le week-end, c’est fait pour coder, non ?',
  SUNDAY: 'Le dimanche, tu prépares la semaine… en commits.',
};

export function FavoriteDayCard({ data }: { readonly data: Wrapped }) {
  const favorite = data.favoriteWeekday;
  if (favorite === null) {
    return null;
  }
  const label = WEEKDAY_LABELS[favorite.day];

  return (
    <StoryCard theme="sun" eyebrow="Ton jour préféré" login={data.login} year={data.year} exportName="jour">
      <Reveal>
        <p className={styles.lead}>Ton jour le plus productif, c’est le</p>
      </Reveal>
      <Reveal delay={0.2}>
        <p className={styles.word}>{label}</p>
      </Reveal>
      <Reveal delay={0.7} as="p" className={styles.note}>
        <strong>{formatNumber(favorite.contributions)}</strong>{' '}
        {pluralize(favorite.contributions, 'contribution cumulée', 'contributions cumulées')} les {label}s.
      </Reveal>
      <Reveal delay={1} as="p" className={styles.lead}>
        {TAGLINES[favorite.day]}
      </Reveal>
    </StoryCard>
  );
}
