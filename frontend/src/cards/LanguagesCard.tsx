import type { Wrapped } from '../api/types';
import { LanguageBar } from '../components/LanguageBar';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import styles from './cards.module.css';

export function LanguagesCard({ data }: { readonly data: Wrapped }) {
  const [top] = data.topLanguages;

  return (
    <StoryCard theme="midnight" eyebrow="Tes langages" login={data.login} year={data.year} exportName="langages">
      <Reveal>
        <p className={styles.lead}>Ta langue maternelle cette année</p>
      </Reveal>
      {top !== undefined && (
        <Reveal delay={0.1}>
          <p className={styles.word}>{top.name}</p>
        </Reveal>
      )}
      <LanguageBar languages={data.topLanguages} />
      <Reveal delay={1.2} as="p" className={styles.note}>
        Pondéré par tes commits sur tes repos les plus actifs.
      </Reveal>
    </StoryCard>
  );
}
