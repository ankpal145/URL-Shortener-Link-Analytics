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
