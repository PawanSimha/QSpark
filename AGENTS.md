# ParkEase (Q-Park) — Agent Guide

## Build & Run

```bash
# Compile
mvnw.cmd compile

# Run with H2 (no MySQL needed) — QUOTE the -D arg
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"

# Run with MySQL (default profile)
mvnw.cmd spring-boot:run

# Tests
mvnw.cmd test

# Clean stale target/ (e.g., after switching from another project)
mvnw.cmd clean
```

- Java 17, Spring Boot 3.3.1
- Port: **8081** (Jenkins occupies 8080 on this dev machine)
- Login: `SPPN` / `[REDACTED]`
- Windows launcher: `start_app.bat` (handles port check, launch, auto-open browser, shutdown)

## Database

- **Default profile** → MySQL (`application.properties`), uses `data.sql` (MySQL `INSERT IGNORE`)
- **`h2` profile** → H2 in-memory (`application-h2.properties`), uses `data-h2.sql` (H2 `MERGE INTO`)
- Never rename/delete `data-h2.sql` — it's the only admin seed for H2
- H2 console: enabled at `/h2-console` when profile=H2

## Architecture

- **No Spring Security** — manual `session.getAttribute("admin")` guard in every controller method
- **No config package** — no `DataInitializerConfig` or `SecurityConfig` in source (those were from old `peterparker` project)
- Package: `com.parkease` → `controller/`, `model/`, `repository/`
- Entry point: `ParkEaseApplication.java` (`@SpringBootApplication`)

## Controllers

| Controller | Endpoints |
|---|---|
| `LoginController` | `GET /` (landing), `GET /login`, `POST /login`, `GET /logout` |
| `ParkingController` | `GET /dashboard`, `POST /save`, `GET /delete/{id}`, `GET /left/{id}`, `GET /edit/{id}`, `POST /update` |

All guarded endpoints check `session.getAttribute("admin") != null`, redirect to `"login"` if missing.

## Tests

- `@WebMvcTest` → import: `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`
- `@DataJpaTest` → import: `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest`
- `@MockBean` → import: `org.springframework.boot.test.mock.mockito.MockBean`
- No separate test starters needed — `spring-boot-starter-test` covers everything

## Frontend

- Vanilla HTML/CSS/JS + Thymeleaf — no frontend framework
- Templates: `landing.html`, `login.html`, `index.html` (dashboard), `edit.html`
- Fragments: `fragments.html` (dark mode + clock scripts)
- Dark mode persisted to `localStorage`
- Three-tab dashboard: Booking / Occupied / History (CSS translate, no JS router)
- Cascading slot dropdowns: B1–B4, 50 slots each

## Known Gotchas

- `-Dspring-boot.run.profiles=h2` must be **quoted** when passed to `mvnw.cmd` from PowerShell/cmd, otherwise Maven parses it as a lifecycle phase
- `data.sql` uses MySQL `INSERT IGNORE` — will **fail** on H2. The H2 profile explicitly loads only `data-h2.sql`
- `target/` may contain stale classes from a different project (`com.peterparker`) — always `mvn clean` first if switching
- No Spring Security CSRF protection — sessions are the only auth layer
- All controller-returned views are server-rendered Thymeleaf templates (no SPA routing)
