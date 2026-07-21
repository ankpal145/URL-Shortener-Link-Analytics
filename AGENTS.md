# AGENTS.md

## Cursor Cloud specific instructions

Single-service Spring Boot 3.5 / Java 21 REST API (URL shortener + click analytics). No frontend, no external services required for local dev.

- Build/test/run commands live in `README.md` ("Install, run, test"). Use system `mvn` (Java 21 and Maven are already installed in the VM snapshot).
- Lint: there is no separate linter; `mvn test` (43 tests) is the correctness gate.
- Run in dev mode with `mvn spring-boot:run` (listens on `http://localhost:8080`). Do not use the packaged jar for dev.
- Datastore defaults to an H2 file DB at `./data/urlshortener` (gitignored, auto-created on first run) — no DB setup needed. Postgres env vars in `.env.example` are only for deployment; leave them unset locally.
- Tests use in-memory H2 (`create-drop`) via the `test` profile, so they never touch `./data/`.
- Because H2 uses `AUTO_SERVER=TRUE`, only one `spring-boot:run` process can hold the file DB at a time; stop an existing instance before starting another.
