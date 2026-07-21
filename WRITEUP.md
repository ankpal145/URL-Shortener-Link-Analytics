# Write-up

## 1. What did I ask the AI to do, and what did I write or decide myself?

I drove Cursor's agent to a plan first (locked stack, endpoint list, data model, duplicate policy) and only then to implementation. I asked the AI to:

- Scaffold Spring Boot 3.5 / Java 21 / H2 with a sensible `pom.xml` and `application.yml`.
- Draft entities and Spring Data repositories once I had specified the columns, indexes, and unique constraints.
- Fill in the "boring but easy to get wrong" pieces: exception → HTTP mapping in a `@RestControllerAdvice`, MockMvc setup, DTO records, `ClientInfo` header parsing.
- Turn my one-liner "generate Base62 with SecureRandom, rely on the DB constraint, retry a few times" into concrete code.
- Draft the first pass of the integration test file from a checklist I wrote.

What I decided and wrote myself (or heavily edited):

- The **duplicate policy** — idempotent by normalized URL, additive when an alias is supplied, 409 on cross-URL alias collision.
- The **URL normalization rules** — conservative on purpose: I do *not* strip tracking params or reorder query strings, because two clients pasting different tracked versions of the same URL usually *want* distinct short codes.
- The **short-code contract**: correctness comes from the DB unique constraint, not from the RNG. The 7-char Base62 space is an optimization to keep collisions rare enough that retry-then-503 is a rounding error, not a hot path.
- The **reserved-path guardrail**, plus the redirect route regex `{code:[A-Za-z0-9_-]{3,32}}` so `/shorten` and `/stats` can never be shadowed by a generated code.
- The **analytics schema and privacy stance** — no raw IPs, only salted SHA-256; column length caps; 14-day daily bucketing done in Java to keep JPQL portable.
- The **commit strategy** — eight logical commits (`chore: scaffold`, `feat(domain)`, `feat(shorten)`, `feat(redirect)`, `feat(alias)`, `feat(analytics)`, `test:`, `docs:`).

## 2. Where did I override, correct, or throw away the AI's output?

- **Portability of JPQL.** The AI's first stab at clicks-by-day used `function('formatdatetime', ...)`, which leaks H2 into the query layer and would have been a maintenance trap. I replaced it with a plain "fetch timestamps since T" query and did the bucketing in the service in UTC.
- **A silent 500 for unmapped paths.** My catch-all `@ExceptionHandler(Exception.class)` swallowed Spring 6's `NoResourceFoundException` and turned a genuine 404 into a 500. The integration test `codeShorterThanAllowedIsNotAShortenerRoute` caught it; I added a dedicated handler.
- **Idempotency wording.** The AI drafted a version that treated alias-on-existing-URL as a 409. I rewrote it to be additive (a URL can have multiple codes; an alias is just another code for the same URL), which matches what a user pasting `?alias=brand-x` actually wants.
- **Test fixture noise.** Early drafts of the integration test hit the real time-of-day and would flake near midnight. I switched the day-bucket assertion from "today's count == 2" to "the series has 14 entries", which stays robust without freezing the clock.
- **`spring-boot-starter-actuator`.** The AI wanted to add it "for `/health`". I dropped it and wrote a five-line `HealthController` instead — actuator is a lot of surface area (and CVE noise) for a probe I can satisfy in five lines.

## 3. Two or three biggest trade-offs

1. **H2 file DB vs Postgres.** H2 with `AUTO_SERVER=TRUE` is zero-install and lets `mvn test` run against in-memory H2 (`create-drop`) with identical mappings. The trade-off: no `LISTEN/NOTIFY`, no real concurrent-writer story, and I've had to keep JPQL portable. For a service that would actually run in production I would move to Postgres, add Flyway, and drop the schema auto-generation.
2. **Synchronous click recording.** `AnalyticsService.recordClick` runs inline before the 301 is returned. Simplest correct thing: writes are visible to `/stats` immediately after a redirect, which makes the tests deterministic and the demo satisfying. The cost is a small extra database write on the hot path. In production I would move this to a bounded async queue (`@Async` with a rejection policy) and accept a tiny lag on stats in exchange for faster redirects. The current code already logs and swallows failures so a broken analytics table cannot break redirects.
3. **Random Base62 + DB constraint vs a sequential/Snowflake ID.** Random codes are dead simple, need no coordination, and give me nice-looking short codes. On a single instance the constraint enforces uniqueness perfectly. In a multi-instance world a Snowflake-style monotonic ID or a shared code counter is more predictable and gives shorter codes for a given code space, but at the price of coordination and worse "guessability" properties. I picked simplicity and left a hook (`app.short-code.length`, `maxCollisionRetries`) to tune it later.

## 4. What's missing, or what I'd do with another day?

- **Persist the schema properly.** Introduce Flyway and stop relying on Hibernate `ddl-auto=update`.
- **Async click writes** with a small in-memory buffer and a scheduled flush; keeps redirects at low-single-digit ms.
- **Rate limiting** on `POST /shorten` (per-IP token bucket) and on `GET /{code}` (per-code) to make the service abuse-resistant.
- **Open-redirect defence in depth.** Currently we validate `http`/`https` + host but don't check that the host isn't a private/loopback address; a small deny-list would tighten this without much cost.
- **A tiny UI** for `/stats/{code}` — the JSON series is already shaped for a bar chart.
- **OpenAPI (`springdoc-openapi`)** so the API is self-documenting.
- **Metrics** — Micrometer counters for shorten/redirect/not-found/conflict and a histogram of click-recording latency.
- **Load test** with `wrk` or `k6` to size the collision-retry window empirically rather than by hand-wave.
