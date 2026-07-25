# URL Shortener & Link Analytics

A small, honest URL shortener written in **Java 21 + Spring Boot 3.5**, with a high-grade **React** UI (Vite + Tailwind + Framer Motion + Recharts). It shortens long URLs to short codes, redirects on hit, and records per-link click analytics. Runs on H2 locally with zero setup and on Postgres in production. The SPA ships **inside the same jar** as the API — one Render service, one domain.

## Live deployment

The service is deployed on **Render** (Docker) with a **Neon Postgres** backend:

> **Base URL**: <https://url-shortener-link-analytics-dzlz.onrender.com>  
> Open that URL in a browser for the **PulseLink** UI. The JSON API lives on the same host.

Quick smoke tests against the live service:

```bash
BASE=https://url-shortener-link-analytics-dzlz.onrender.com

# Shorten
curl -sX POST $BASE/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://www.mnnit.ac.in"}'

# Shorten with a custom alias
curl -sX POST $BASE/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/launch","alias":"launch-2026"}'

# Redirect (301) — replace HBgOQki with the code you got back
curl --head $BASE/HBgOQki

# Stats aggregates
curl -s $BASE/stats/HBgOQki

# Recent raw clicks (paginated)
curl -s "$BASE/stats/HBgOQki/clicks?limit=100&offset=0"
```

The service runs on Render's free tier, so cold-start after ~15 min of idle can take ~30s on the first request.

## Features

**API**
- `POST /shorten` — accept a URL (+ optional custom alias) and return a short code
- `GET /{code}` — **301** redirect to the original URL; unknown → **404**
- `GET /stats/{code}` — click totals, unique visitors, first/last click, top referrers, top user-agents, 14-day daily series
- `GET /stats/{code}/clicks?limit=&offset=` — paginated raw click events
- `GET /health` — liveness probe
- Custom aliases with reserved-path protection and 409 conflict handling
- Idempotent behaviour on duplicate URLs (documented below)
- Collision-safe short-code generator backed by a DB unique constraint
- Privacy-conscious analytics — client IPs are stored as **SHA-256(salt || ip)**, never raw

**UI (PulseLink)**
- Aurora hero + glassmorphism shortener with optional custom alias
- Animated result card: copy, open, QR code, jump-to-analytics
- Full analytics dashboard: KPI cards, 14-day area chart, top referrers / user-agents, recent clicks
- Dark mode by default with light-mode toggle, responsive to 375px, skeleton loaders, toasts
- Cold-start friendly (“Waking up the server…”) for Render’s free tier

## Requirements

- **JDK 21** (`java --version` must report `21.x`)
- **Maven 3.6.3+** (bundled `mvnw` is not included; use system Maven)
- **Node 22+** and npm — only needed for local UI development / building the SPA

## Install, run, test

### API only

```bash
# Compile
mvn -q -DskipTests package

# Run the app (uses H2 file DB at ./data/urlshortener)
mvn spring-boot:run
# or
java -jar target/url-shortener-analytics-0.1.0.jar

# Run all tests (43 tests: unit + MockMvc integration on in-memory H2)
mvn test
```

By default the service listens on **http://localhost:8080**. The H2 file lives under `./data/` (gitignored).

### UI + API (local development)

```bash
# Terminal A — API
mvn spring-boot:run

# Terminal B — Vite UI (proxies /shorten, /stats, /health → :8080)
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Details in [`frontend/README.md`](frontend/README.md).

### Deploy UI on Vercel (optional)

The SPA can also be hosted on Vercel while the API stays on Render. [`frontend/vercel.json`](frontend/vercel.json) rewrites `/shorten`, `/stats/*`, and `/health` to the Render service (no CORS). In the Vercel dashboard: set **Root Directory** to `frontend`, Framework **Vite**, output `dist`. Full steps: [`frontend/README.md`](frontend/README.md#deploy-on-vercel).

### Production-shaped local build (UI baked into the jar)

The Docker image builds the SPA and copies it into `classpath:/static/` automatically. To mimic that without Docker:

```bash
cd frontend && npm ci && npm run build
mkdir -p ../src/main/resources/static
cp -R dist/* ../src/main/resources/static/
cd .. && mvn -DskipTests package
java -jar target/url-shortener-analytics-0.1.0.jar
# UI at http://localhost:8080/
```

(`src/main/resources/static/` is gitignored — it is generated, not source.)

## API examples

The examples below use `http://localhost:8080` for a locally-running instance. Swap the base URL for `https://url-shortener-link-analytics-dzlz.onrender.com` to hit the live deployment.

### Shorten (auto code)

```bash
curl -sX POST http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/some/very/long/path?x=1"}'
```

```json
{
  "code": "aB3xK9p",
  "shortUrl": "http://localhost:8080/aB3xK9p",
  "originalUrl": "https://example.com/some/very/long/path?x=1",
  "customAlias": false,
  "createdAt": "2026-07-21T15:41:00.123456Z"
}
```

### Shorten with a custom alias

```bash
curl -sX POST http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/launch","alias":"launch-2026"}'
```

- Alias must match `^[A-Za-z0-9_-]{3,32}$`
- Reserved words (`shorten`, `stats`, `health`, `actuator`, `error`, `favicon.ico`) → 400
- Alias already used for a different URL → 409
- Alias already used for the same URL → returns the existing mapping (idempotent)

### Redirect

```bash
curl -sI http://localhost:8080/aB3xK9p
# HTTP/1.1 301
# Location: https://example.com/some/very/long/path?x=1
```

### Stats

```bash
curl -s http://localhost:8080/stats/aB3xK9p
```

```json
{
  "code": "aB3xK9p",
  "originalUrl": "https://example.com/some/very/long/path?x=1",
  "customAlias": false,
  "createdAt": "2026-07-21T15:41:00Z",
  "totalClicks": 42,
  "uniqueVisitors": 17,
  "firstClickAt": "2026-07-21T15:41:20Z",
  "lastClickAt":  "2026-07-21T16:03:41Z",
  "topReferrers":   [{ "label": "https://news.example", "count": 20 }, ...],
  "topUserAgents":  [{ "label": "Mozilla/5.0 ...",       "count": 33 }, ...],
  "clicksByDay":    [{ "date": "2026-07-08", "count": 0 }, ..., { "date": "2026-07-21", "count": 12 }]
}
```

### Recent clicks

```bash
curl -s 'http://localhost:8080/stats/aB3xK9p/clicks?limit=100&offset=0'
```

Each row has `clickedAt`, `referer`, `userAgent`, `ipHash` (SHA-256 hex).

## Deliberate design decisions

| Concern | Decision & rationale |
|---|---|
| Duplicate URL, no alias | **Idempotent** — return the existing mapping. Rationale: users pasting the same link twice expect the same short URL, and it keeps the mapping table tidy. If they *want* a distinct code, they can pass an alias. |
| Duplicate URL with alias | Alias free → create a second mapping (custom alias in *addition* to any auto code). Alias taken by the same URL → return existing. Alias taken by different URL → **409**. |
| Short-code generator | 7-char Base62 from `SecureRandom` ≈ 3.52 × 10¹² codes. Uniqueness is **enforced by the DB unique constraint** on `link.code`; the service retries a small number of times on the extremely rare race, and after N attempts returns 503 rather than looping forever. |
| URL validation | Must be absolute `http`/`https` with a host. Length capped at 2048. Normalization is conservative: scheme/host lower-cased, default ports stripped, single trailing `/` stripped. **Query params are not reordered** and fragments are preserved — two URLs that differ in tracking params get separate mappings, intentionally. |
| Redirect status | **301 Moved Permanently** with `Cache-Control: no-cache` so intermediaries and browsers still hit the origin (and we still see the click server-side). |
| Analytics privacy | Only `SHA-256(app_salt || ip)` is stored, not the raw IP. Same-IP repeat clicks still collapse for unique-visitor count, but the reverse map to a specific IP requires knowing the salt and guessing an IP. Referer / user-agent are length-capped to 512 chars. |
| Reserved paths | `/shorten`, `/stats`, `/health`, `/actuator`, `/error`, UI asset names (`assets`, `favicon.svg`, …) are reserved. The redirect route also has a regex constraint (`{code:[A-Za-z0-9_-]{3,32}}`) so it can never shadow an application or SPA route. |
| UI packaging | SPA is built in Docker and copied into `classpath:/static/` so API + UI share one Render service and one origin (no CORS). Rejected alternative: separate Vercel deploy. |

## Data model

- `link(id, code UNIQUE, original_url, original_url_normalized UNIQUE, custom_alias, created_at)`
- `click_event(id, link_id, clicked_at, referer, user_agent, ip_hash)` with indexes on `(link_id, clicked_at)` and `(link_id, ip_hash)`

## Project structure

```
frontend/                    # Vite + React 19 + TS + Tailwind SPA (PulseLink)
  src/components/            # Hero, Shortener, ResultCard, Analytics*, ui/*
  src/api.ts                 # Typed client for /shorten and /stats
Dockerfile                   # Node UI stage → Maven stage → slim JRE runtime
src/main/java/com/urlshortener/
  UrlShortenerApplication.java
  config/          # AppProperties, ClockConfig, WebConfig (static cache)
  domain/          # Link, ClickEvent
  repository/      # LinkRepository, ClickEventRepository
  service/         # ShortCodeGenerator, UrlValidator, AliasPolicy,
                   # IpHasher, LinkService, AnalyticsService
  web/             # ShortenController, RedirectController,
                   # StatsController, HealthController,
                   # GlobalExceptionHandler
  web/dto/         # request/response DTOs
  web/error/       # domain-specific exceptions
src/test/java/...  # unit + MockMvc integration tests
```

## Configuration

`application.yml` exposes:

- `app.base-url` — used when building `shortUrl` in responses
- `app.ip-hash-salt` — **change in production** (env override recommended)
- `app.short-code.length` (default 7), `app.short-code.max-collision-retries` (default 5)
- `app.alias.pattern` — the regex both aliases and generated codes must match
- `app.reserved-paths` — words that cannot become codes or aliases

## Deploy your own

The repo is deploy-ready for any container platform + managed Postgres. The live instance uses Render + Neon.

**Environment variables** (set in the platform's dashboard — never in `application.yml`):

| Key | Example / how to get |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST/DB?sslmode=require` (from your Postgres provider; **drop `channel_binding=require`** from Neon strings — libpq-only, JDBC will reject it) |
| `SPRING_DATASOURCE_USERNAME` | e.g. `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | mark as secret in the dashboard |
| `APP_IP_HASH_SALT` | `openssl rand -base64 48`; mark as secret. Rotating invalidates historical unique-visitor counts |
| `APP_BASE_URL` | `https://<your-service>.onrender.com` — used to build `shortUrl` in responses |
| `PORT` | auto-injected by Render; `server.port` reads it |

The [`Dockerfile`](Dockerfile) is a multi-stage build (Maven+JDK 21 → slim JRE, non-root user). Render auto-detects it; no build/start commands to configure. `/health` doubles as the platform health check.

`.env.example` in the repo is a template you can copy for a local `.env` file (already gitignored) when developing against Postgres instead of H2.

## Non-goals for this exercise

Authentication, rate limiting, an admin UI, TTLs/expiry, a distributed ID scheme (Snowflake-style), open-redirect allowlists beyond http(s), horizontal scaling behind Redis, and OpenAPI generation are intentionally out of scope. See `WRITEUP.md` for what I would add next.
