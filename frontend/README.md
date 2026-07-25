# PulseLink UI

Vite + React 19 + TypeScript + Tailwind CSS v4 frontend for the URL Shortener & Link Analytics service.

## Dev

```bash
# Terminal A — API
mvn spring-boot:run

# Terminal B — UI
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/shorten`, `/stats`, and `/health` to `http://localhost:8080`.

## Build

```bash
npm run build
```

Output lands in `frontend/dist/`. The root `Dockerfile` copies this into Spring Boot's `classpath:/static/` so the same Render service serves API + UI.

## Deploy on Vercel

[`vercel.json`](vercel.json) proxies API calls to the Render backend so the SPA can keep relative `fetch('/shorten')` paths (no CORS):

| Path on Vercel | Proxied to |
|---|---|
| `/shorten` | `https://url-shortener-link-analytics-dzlz.onrender.com/shorten` |
| `/stats/*` | same host `/stats/*` |
| `/health` | same host `/health` |

Short-link redirects (`GET /{code}`) stay on **Render** — `shortUrl` in API responses uses `APP_BASE_URL` there.

### Dashboard steps

1. [vercel.com](https://vercel.com) → **Add New…** → **Project** → import this GitHub repo
2. Configure:

   | Setting | Value |
   |---|---|
   | Framework Preset | Vite |
   | Root Directory | `frontend` |
   | Build Command | `npm run build` |
   | Output Directory | `dist` |

3. Deploy, then smoke-test:

```bash
BASE=https://your-app.vercel.app

curl -sX POST $BASE/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com"}'

# Open the UI
open $BASE
```

If you change the Render URL later, update the destinations in `vercel.json` and redeploy.
