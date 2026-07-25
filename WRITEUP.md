# Write-up

## 1. What did I ask the AI to do, and what did I write or decide myself?

I drove Cursor's agent to a plan first (locked stack, endpoint list, data model, duplicate policy, later the UI architecture) and only then to implementation. I asked the AI to:

- Scaffold Spring Boot 3.5 / Java 21 / H2 with a sensible `pom.xml` and `application.yml`.
- Draft entities and Spring Data repositories once I had specified the columns, indexes, and unique constraints.
- Fill in the "boring but easy to get wrong" pieces: exception → HTTP mapping in a `@RestControllerAdvice`, MockMvc setup, DTO records, `ClientInfo` header parsing.
- Turn my one-liner "generate Base62 with SecureRandom, rely on the DB constraint, retry a few times" into concrete code.
- Draft the first pass of the integration test file from a checklist I wrote.
- Scaffold a Vite + React + Tailwind SPA and wire it to the existing JSON APIs, including the Docker multi-stage bake into `classpath:/static/`.

What I decided and wrote myself (or heavily edited):

- The **duplicate policy** — idempotent by normalized URL, additive when an alias is supplied, 409 on cross-URL alias collision.
- The **URL normalization rules** — conservative on purpose: I do *not* strip tracking params or reorder query strings, because two clients pasting different tracked versions of the same URL usually *want* distinct short codes.
- The **short-code contract**: correctness comes from the DB unique constraint, not from the RNG. The 7-char Base62 space is an optimization to keep collisions rare enough that retry-then-503 is a rounding error, not a hot path.
- The **reserved-path guardrail**, plus the redirect route regex `{code:[A-Za-z0-9_-]{3,32}}` so `/shorten` and `/stats` can never be shadowed by a generated code.
- The **analytics schema and privacy stance** — no raw IPs, only salted SHA-256; column length caps; 14-day daily bucketing done in Java to keep JPQL portable.
- The **UI packaging decision**: one Render service, SPA inside the jar — no CORS, no second deploy. Dark-default aurora aesthetic, typed error mapping to the existing `ApiError.error` codes, and a cold-start affordance for free-tier spin-up.
- The **commit strategy** — logical feature commits (scaffold → domain → shorten → redirect → alias → analytics → tests → docs → deploy → UI → docker bake).

## 2. Where did I override, correct, or throw away the AI's output?

- **Portability of JPQL.** The AI's first stab at clicks-by-day used `function('formatdatetime', ...)`, which leaks H2 into the query layer and would have been a maintenance trap. I replaced it with a plain "fetch timestamps since T" query and did the bucketing in the service in UTC.
- **A silent 500 for unmapped paths.** My catch-all `@ExceptionHandler(Exception.class)` swallowed Spring 6's `NoResourceFoundException` and turned a genuine 404 into a 500. The integration test `codeShorterThanAllowedIsNotAShortenerRoute` caught it; I added a dedicated handler.
- **Idempotency wording.** The AI drafted a version that treated alias-on-existing-URL as a 409. I rewrote it to be additive (a URL can have multiple codes; an alias is just another code for the same URL), which matches what a user pasting `?alias=brand-x` actually wants.
- **Test fixture noise.** Early drafts of the integration test hit the real time-of-day and would flake near midnight. I switched the day-bucket assertion from "today's count == 2" to "the series has 14 entries", which stays robust without freezing the clock.
- **`spring-boot-starter-actuator`.** The AI wanted to add it "for `/health`". I dropped it and wrote a five-line `HealthController` instead — actuator is a lot of surface area (and CVE noise) for a probe I can satisfy in five lines.
- **Separate Vercel frontend.** Tempting for DX; I rejected it so the portfolio demo stays one URL and credentials/CORS don't become a second product.
- **Analytics double-fetch.** An early SPA effect re-loaded stats whenever the callback identity changed; I tightened the effect to parent-driven code changes only.

## 3. Two or three biggest trade-offs

1. **H2 file DB vs Postgres.** H2 with `AUTO_SERVER=TRUE` is zero-install and lets `mvn test` run against in-memory H2 (`create-drop`) with identical mappings. The trade-off: no `LISTEN/NOTIFY`, no real concurrent-writer story, and I've had to keep JPQL portable. Production on Render uses Neon Postgres via env vars; local defaults still hit H2.
2. **Synchronous click recording.** `AnalyticsService.recordClick` runs inline before the 301 is returned. Simplest correct thing: writes are visible to `/stats` immediately after a redirect, which makes the tests deterministic and the demo satisfying. The cost is a small extra database write on the hot path. In production I would move this to a bounded async queue (`@Async` with a rejection policy) and accept a tiny lag on stats in exchange for faster redirects. The current code already logs and swallows failures so a broken analytics table cannot break redirects.
3. **SPA-in-jar vs separate frontend host.** Baking the Vite build into Spring `static/` keeps one domain, one deploy, and zero CORS. The cost is a heavier Docker build (Node stage + Maven) and a larger jar (~+UI assets). For this exercise the single-service story wins; at scale I'd put the SPA on a CDN and leave the API on Render/ECS.
4. **Random Base62 + DB constraint vs a sequential/Snowflake ID.** Random codes are dead simple, need no coordination, and give me nice-looking short codes. On a single instance the constraint enforces uniqueness perfectly. In a multi-instance world a Snowflake-style monotonic ID is more predictable, at the price of coordination.

## 4. What's missing, or what I'd do with another day?

- **Persist the schema properly.** Introduce Flyway and stop relying on Hibernate `ddl-auto=update`.
- **Async click writes** with a small in-memory buffer and a scheduled flush; keeps redirects at low-single-digit ms.
- **Rate limiting** on `POST /shorten` (per-IP token bucket) and on `GET /{code}` (per-code) to make the service abuse-resistant.
- **Open-redirect defence in depth.** Currently we validate `http`/`https` + host but don't check that the host isn't a private/loopback address; a small deny-list would tighten this without much cost.
- **"My recent links" via localStorage** (no auth backend yet) so returning users see what they shortened in this browser.
- **OpenAPI (`springdoc-openapi`)** so the API is self-documenting.
- **Metrics** — Micrometer counters for shorten/redirect/not-found/conflict and a histogram of click-recording latency.
- **Code-split the SPA** (Recharts is the bulk of the ~780 KB JS) so first paint on free-tier cold starts is snappier.
- **Load test** with `wrk` or `k6` to size the collision-retry window empirically rather than by hand-wave.
