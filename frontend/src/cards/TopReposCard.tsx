import type { Wrapped } from '../api/types';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { safeColor } from '../components/LanguageBar';
import { formatNumber, pluralize } from '../lib/format';
import styles from './cards.module.css';

function splitName(nameWithOwner: string): { owner: string; repo: string } {
  const slash = nameWithOwner.indexOf('/');
  return slash < 0
    ? { owner: '', repo: nameWithOwner }
    : { owner: nameWithOwner.slice(0, slash), repo: nameWithOwner.slice(slash + 1) };
}

export function TopReposCard({ data }: { readonly data: Wrapped }) {
  return (
    <StoryCard theme="ocean" eyebrow="Ton podium" login={data.login} year={data.year} exportName="repos">
      <Reveal>
        <p className={styles.lead}>Les repos où tu as le plus commité</p>
      </Reveal>
      <ol className={styles.podium}>
        {data.topRepositories.map((repo, i) => {
          const { owner, repo: name } = splitName(repo.nameWithOwner);
          return (
            <Reveal key={repo.nameWithOwner} as="li" delay={0.3 + i * 0.2} className={styles.repo}>
              <span className={styles.rank} aria-hidden="true">
                {i + 1}
              </span>
              <span className={styles.repoText}>
                {owner && <span className={styles.repoOwner}>{owner}/</span>}
                <span className={styles.repoName}>{name}</span>
                <span className={styles.repoMeta}>
                  <span>
                    {formatNumber(repo.commits)} {pluralize(repo.commits, 'commit')}
                  </span>
                  {repo.language !== null && (
                    <span>
                      <span className={styles.dot} style={{ background: safeColor(repo.languageColor) }} aria-hidden="true" />
                      {repo.language}
                    </span>
                  )}
                </span>
              </span>
            </Reveal>
          );
        })}
      </ol>
    </StoryCard>
  );
}
