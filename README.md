# GitHub Wrapped

Saisis un login GitHub, choisis une année, et obtiens ton année racontée en cartes animées plein écran, façon « stories ». Chaque carte s'exporte en PNG.

| Carte | Contenu |
|---|---|
| 1. Total | contributions de l'année (compteur animé), commits, PR, issues |
| 2. Calendrier | heatmap qui se remplit, mois le plus actif |
| 3. Série | plus longue série de jours consécutifs, série en cours |
| 4. Podium | top 3 des repos par nombre de commits |
| 5. Langages | top langages pondérés par les commits, aux couleurs GitHub |
| 6. Jour favori | jour de la semaine le plus actif |
| 7. Persona | profil déduit des statistiques et récapitulatif |

Les cartes sans données (aucun repo, aucune série…) sont omises plutôt qu'affichées vides.

---

## Architecture

```
Wrapped/
├── backend/    API Spring Boot : appel GitHub GraphQL, calculs, cache, sécurité
├── frontend/   SPA React : accueil, lecteur de stories, export PNG
├── .env.example
└── README.md
```

```
Navigateur ──GET /api/wrapped/{login}?year=──▶ Spring Boot ──POST /graphql──▶ api.github.com
 (React)                                        │ validation, rate limit, CORS
                                                │ cache Caffeine (1 h)
                                                └ calculs : séries, top repos/langages, persona
```

**Backend** (`backend/`, Java 21, Spring Boot 3.5.16, Maven) :

- `web/` : controller mince, validation des entrées, `@RestControllerAdvice` qui renvoie des `ProblemDetail` (RFC 9457), rate limiting par IP.
- `service/` : `WrappedService` (orchestration et `@Cacheable`), `StatsCalculator` (calculs purs, sans I/O), `PersonaResolver`.
- `github/` : interface `GitHubClient` et implémentation `RestClient` (requête GraphQL statique, entrées passées par les **variables** GraphQL).
- `domain/` : DTO immuables (`record`).

**Frontend** (`frontend/`, React 19, TypeScript strict, Vite 8, Framer Motion, html-to-image) :

- `components/` : `StoryPlayer`, `StoryCard`, `AnimatedCounter`, `Heatmap`, `LanguageBar`, `ProgressBar`, `ShareButton`.
- `cards/` : les 7 cartes, plus `buildStory` qui choisit les cartes à afficher.
- `hooks/` : `useWrapped` (appel API annulable), `useStoryNavigation`, `useExportPng`.

---

## Prérequis

| Outil | Version |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | **22.12+** (Vite 8 accepte `^20.19 \|\| >=22.12`, Vitest 5 exige `^22.12`) |
| npm | 10+ |
| Compte GitHub | pour créer un token d'accès |

---

## Créer un token GitHub (fine-grained, lecture seule)

L'application lit des données **publiques**. Elle n'a besoin d'aucune permission sur tes dépôts : le token sert seulement à s'authentifier auprès de l'API GraphQL, qui refuse les appels anonymes.

1. Sur GitHub, clique sur ta photo de profil (en haut à droite), puis sur **Settings**.
2. Dans la barre latérale gauche, clique sur **Developer settings**.
3. Sous **Personal access tokens**, clique sur **Fine-grained tokens**, puis sur **Generate new token**.
4. Remplis le formulaire :
   - **Token name** : par exemple `github-wrapped-local`.
   - **Expiration** : une durée courte, par exemple 30 jours. Évite « No expiration ».
   - **Resource owner** : ton compte personnel.
   - **Repository access** : l'option la plus restreinte, celle qui ne donne accès qu'aux dépôts publics. Ne choisis ni *All repositories* ni *Only select repositories*.
   - **Permissions** : **n'en ajoute aucune**.
5. Clique sur **Generate token** et copie la valeur. Elle ne sera plus affichée.

Selon la documentation GitHub, *« Tokens always include read-only access to all public repositories on GitHub »* et *« All fine-grained personal access tokens include read access to public repositories »*. Un token sans permission supplémentaire suffit donc ([Managing your personal access tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens), [Forming calls with GraphQL](https://docs.github.com/en/graphql/guides/forming-calls-with-graphql)).

> ⚠️ Le libellé exact de l'option « dépôts publics uniquement » dans *Repository access* peut changer selon les évolutions de l'interface GitHub. Choisis toujours l'accès minimal.

**Quota** : avec un token personnel, l'API GraphQL autorise *« 5,000 points per hour per user »* ([Rate limits for the GraphQL API](https://docs.github.com/en/graphql/overview/rate-limits-and-query-limits-for-the-graphql-api)). Le cache d'une heure côté serveur évite de consommer ce quota à chaque rechargement.

**Contributions privées** : un token « dépôts publics uniquement » ne voit pas le contenu des dépôts privés. D'après le schéma GraphQL, les contributions privées d'un utilisateur ne sont visibles qu'en nombre, et seulement *« when the user has chosen to share their private contribution counts »* (champ `restrictedContributionsCount`). Le calendrier peut donc différer de ce que l'utilisateur voit sur son propre profil.

---

## Lancement en local

### 1. Configuration

```bash
cp .env.example .env
# puis édite .env et renseigne GITHUB_TOKEN=<ton token>
```

`.env` est ignoré par Git (`.gitignore`). **Ne commite jamais ton token.**

| Variable | Défaut | Rôle |
|---|---|---|
| `GITHUB_TOKEN` | *(obligatoire)* | token GitHub ; le backend refuse de démarrer sans |
| `WRAPPED_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | origines autorisées, séparées par des virgules |
| `WRAPPED_RATE_LIMIT_MAX_REQUESTS` | `30` | requêtes autorisées par IP et par fenêtre |
| `WRAPPED_RATE_LIMIT_WINDOW` | `1m` | durée de la fenêtre (format Spring : `30s`, `1m`, `1h`) |

### 2. Backend (port 8080)

Spring Boot ne lit pas `.env` tout seul : charge-le dans le shell avant de lancer l'application.

```bash
set -a; source .env; set +a      # exporte les variables du .env
cd backend
mvn spring-boot:run
```

Vérification :

```bash
curl "http://localhost:8080/api/wrapped/octocat?year=2025"
```

### 3. Frontend (port 5173)

Dans un second terminal :

```bash
cd frontend
npm ci
npm run dev
```

Ouvre <http://localhost:5173>. En développement, Vite relaie `/api` vers `http://localhost:8080` : aucun réglage CORS n'est nécessaire en local. Pour viser un autre backend : `VITE_BACKEND_URL=http://autre-hote:8080 npm run dev`.

Un Wrapped est partageable par lien : `http://localhost:5173/?login=octocat&year=2025`.

### Build de production

```bash
cd backend  && mvn verify                # tests + jar dans target/
cd frontend && npm run build             # typecheck + bundle dans dist/
```

Si le front est servi sur une autre origine que l'API, renseigne `VITE_API_BASE_URL` au build (par exemple `https://api.exemple.com`) et ajoute l'origine du front à `WRAPPED_CORS_ALLOWED_ORIGINS`.

---

## Tests

```bash
cd backend  && mvn verify     # 56 tests : JUnit 5, Mockito, @WebMvcTest, @SpringBootTest
cd frontend && npm test       # 23 tests : Vitest + Testing Library
```

Aucun test n'appelle le vrai GitHub :

- côté service, `GitHubClient` est mocké avec Mockito ;
- le client lui-même est testé avec `MockRestServiceServer`, qui vérifie aussi que le login passe par les variables GraphQL ;
- côté front, `fetch` est simulé.

---

## API

`GET /api/wrapped/{login}?year=2025`

| Paramètre | Règle |
|---|---|
| `login` | `^[a-zA-Z0-9-]{1,39}$` |
| `year` | optionnel, de 2008 à l'année courante (UTC), défaut : année courante |

Exemple de réponse (tronquée) :

```json
{
  "login": "octocat",
  "year": 2025,
  "totals": { "contributions": 1805, "commits": 1180, "pullRequests": 96, "issues": 23 },
  "calendar": [{ "date": "2025-01-01", "count": 4 }],
  "longestStreak": { "days": 23, "start": "2025-03-03", "end": "2025-03-25" },
  "currentStreak": { "days": 4, "start": "2025-12-28", "end": "2025-12-31" },
  "favoriteWeekday": { "day": "TUESDAY", "contributions": 412 },
  "mostActiveMonth": { "month": "MARCH", "contributions": 287 },
  "topRepositories": [{ "nameWithOwner": "octocat/hello-world", "commits": 512, "language": "Java", "languageColor": "#b07219" }],
  "topLanguages": [{ "name": "Java", "color": "#b07219", "commits": 612, "percentage": 48.2 }],
  "persona": { "code": "TEAM_PLAYER", "title": "Esprit d'équipe", "description": "…" }
}
```

Erreurs au format `application/problem+json` :

| Statut | Cas |
|---|---|
| 400 | login ou année invalide (détail par paramètre dans `errors`) |
| 404 | utilisateur GitHub inconnu |
| 429 | trop de requêtes, avec l'en-tête `Retry-After` |
| 502 | GitHub indisponible, token invalide ou quota dépassé (message générique ; le détail n'est que dans les logs serveur) |

### Règles de calcul

- **Total** : somme du calendrier de contributions sur l'année, sans les jours futurs.
- **Série actuelle** : pour l'année en cours, une journée sans contribution *aujourd'hui* ne casse pas la série, puisque la journée n'est pas finie. Pour une année passée, elle est arrêtée au 31 décembre.
- **Langages** : le langage principal de chacun des 10 repos les plus actifs, pondéré par le nombre de commits. C'est une approximation : GitHub ne fournit pas le langage de chaque commit.
- **Persona** : la première règle vraie l'emporte. Dans l'ordre :
  1. *Hibernation* : aucune contribution.
  2. *Marathon* : série ≥ 30 jours.
  3. *Esprit d'équipe* : beaucoup de PR.
  4. *Chasse aux bugs* : beaucoup d'issues.
  5. *Polyglotte* : ≥ 4 langages.
  6. *Week-end warrior* : ≥ 40 % des contributions le week-end.
  7. *Laser focus* : un repo ≥ 70 % des commits.
  8. *Bâtisseur régulier* : sinon.

---

## Sécurité

- **Secret** : `GITHUB_TOKEN` n'est lu que depuis l'environnement. Il est masqué dans le `toString()` de la configuration et n'est jamais journalisé.
- **Injection** : la requête GraphQL est un fichier statique, et le login et les dates passent exclusivement par `variables`.
- **Entrées** : Bean Validation sur le login et l'année. Le front applique la même règle et n'accepte que des couleurs hexadécimales pour les styles inline.
- **Erreurs** : réponses `ProblemDetail` génériques, sans stacktrace, sans message GitHub brut et sans écho de la valeur saisie.
- **CORS** : limité aux origines configurées, méthode `GET` seule, sans credentials. Pas de session ni de cookie, donc pas de surface CSRF.
- **Rate limiting** : fenêtre fixe par IP, en mémoire, avec un nombre d'IP suivies borné. L'IP vient de `getRemoteAddr()` et **pas** de `X-Forwarded-For`, que le client peut falsifier. Derrière un reverse proxy de confiance, active `server.forward-headers-strategy=native` (ou `framework`) pour obtenir l'IP réelle.

### Limites connues

- Le rate limiting et le cache sont **en mémoire** : ils conviennent à une seule instance. Pour plusieurs instances, il faut un stockage partagé (Redis…).
- Le cas « utilisateur inconnu » repose sur l'erreur GraphQL `type: "NOT_FOUND"` renvoyée par GitHub. Elle n'a pas encore été vérifiée sur un appel réel dans ce projet. Si GitHub change ce format, la réponse serait un 502 au lieu d'un 404.
- Le partage natif (Web Share API) dépend du navigateur. Sinon, le PNG est téléchargé.
