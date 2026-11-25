import type { Wrapped } from '../api/types';
import { AnimatedCounter } from '../components/AnimatedCounter';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { formatShortDate, pluralize } from '../lib/format';
import styles from './cards.module.css';

export function StreakCard({ data }: { readonly data: Wrapped }) {
  const { longestStreak: longest, currentStreak: current } = data;
  const isCurrentYear = data.year === new Date().getUTCFullYear();

  return (
    <StoryCard theme="ember" eyebrow="Ta plus longue série" login={data.login} year={data.year} exportName="serie">
      <Reveal>
        <p className={styles.lead}>Tu n'as rien lâché pendant</p>
      </Reveal>
      <Reveal delay={0.15}>
        <AnimatedCounter value={longest.days} className={styles.giant} duration={1.2} delay={0.2} />
        <p className={styles.unit}>{pluralize(longest.days, "jour d'affilée", "jours d'affilée")}</p>
      </Reveal>
      {longest.start !== null && longest.end !== null && (
        <Reveal delay={1} as="p" className={styles.note}>
          Du <strong>{formatShortDate(longest.start)}</strong> au <strong>{formatShortDate(longest.end)}</strong>.
        </Reveal>
      )}
      <Reveal delay={1.3} as="p" className={styles.note}>
        {current.days > 0 ? (
          <>
            {isCurrentYear ? 'Série en cours' : 'Série au 31 décembre'} : <strong>{current.days}</strong>{' '}
            {pluralize(current.days, 'jour')}.
          </>
        ) : (
          isCurrentYear ? 'Pas de série en cours… pour l’instant.' : 'Aucune série en cours au 31 décembre.'
        )}
      </Reveal>
    </StoryCard>
  );
}
