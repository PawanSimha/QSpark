# QSpark - Workspace Context

## 1. System Overview & Strategic Intent

QSpark is a **production-grade smart parking management system** for commercial buildings. It manages **480 parking slots across 4 basements** with a server-rendered MVC dashboard, QR-code-based vehicle identification, and full audit trail. The system replaces manual logbooks and spreadsheets with a real-time web UI.

**Core operational goals:**
- Eliminate double-booking through centralized slot tracking
- Provide real-time slot occupancy visibility via a client-rendered grid
- Enable QR-based vehicle lookup for fast check-in
- Maintain complete entry/exit audit history for dispute resolution
- Operate with zero frontend framework dependencies (vanilla JS only)

**Target users:** Parking administrators (daily ops), Security guards (QR scanning), System admins (monitoring)

**Current maturity:** Production Ready v1.0 - all P0 features shipped, 66 tests passing, GPLv3 licensed.

---

## 2. The Production Tech Stack

| Layer | Technology | Version | Key Dependencies / Notes |
|-------|-----------|---------|--------------------------|
| **Backend** | Java | 17 | OpenJDK 17, records not used yet |
| **Framework** | Spring Boot | 3.3.1 | web, data-jpa, thymeleaf, devtools |
| **Security** | Spring Security | 6.3.x | BCryptPasswordEncoder (strength 10), formLogin(), CSRF enabled (deferred token), security headers |
| **Template Engine** | Thymeleaf | 3.1 | Server-rendered HTML, no frontend framework |
| **Database (prod)** | MySQL | 8.0 | Connector: mysql-connector-j |
| **Database (dev)** | H2 | 2.x | In-memory mode, console at /h2-console |
| **ORM** | Hibernate | 6.5.2 | Via Spring Data JPA, ddl-auto=update |
| **Validation** | Jakarta Validation | 3.0 | @Pattern regex for Indian vehicle numbers |
| **QR Generation** | ZXing | 3.5.3 | core + javase, PNG output via MatrixToImageWriter |
| **QR Scanning** | html5-qrcode | CDN | Camera-based scan, requires HTTPS or localhost |
| **Build Tool** | Maven Wrapper | 3.x | mvnw.cmd for Windows, mvnw for Unix |
| **CI/CD** | GitHub Actions | - | Automated test runner on push/PR to main |
| **Frontend JS** | Vanilla JavaScript | ES5+ | 508 lines, no frameworks |
| **Frontend CSS** | Custom design system | - | 1193 lines, CSS custom properties, dark mode |
| **Testing** | JUnit 5 + Spring Test | - | @WebMvcTest, @DataJpaTest, @SpringBootTest |

---

## 3. Core Architectural Design Patterns

### MVC + Repository (Server-Rendered Monolith)

```
Browser (HTML+CSS+JS) --> Controller (Spring @Controller)
                                |
                                v
                          Service Layer (none - controllers call repos directly)
                                |
                                v
                          Repository (Spring Data JPA)
                                |
                                v
                          Database (MySQL / H2)
                                |
                                v
                          Thymeleaf renders HTML <-- Controller adds model data
```

**Key architectural traits:**
- **No SPA, no client-side router** - every navigation is a full page load. JS is progressive enhancement only.
- **No service layer** - controllers interact directly with JPA repositories. Business logic lives in controllers and repository methods.
- **No DTOs** - entities are passed directly to views. ParkingSlot, Admin, VehicleRegistration serve as both JPA entities and form backing objects.
- **State management** - the server holds all authoritative state. Client holds only dark mode preference (localStorage `qspark-dark-mode`). No client-side state library.
- **Auth** - Spring Security filter chain guards every route except public paths (/, /login, /css/**, /js/**, /images/**). CSRF tokens on all POST forms. API-style endpoints (/qrcode/generate, /qrcode/list) return 401 instead of redirect.
- **Error handling** - try/catch in every controller method with model-level error/success attributes. No global exception handler.
- **Dual database profile** - `default` (MySQL) vs `h2` (H2 in-memory). Seeding is handled by DataInitializer.java (no SQL seed files).

### Data Flow Example (Creating a Booking)

1. Form POST /save with CSRF token
2. Spring Security validates auth + CSRF
3. ParkingController.save() validates @Valid + @Pattern, checks slot availability + duplicate vehicle
4. Sets entryTime=LocalDateTime.now(), calls repository.save()
5. Re-fetches all records, splits into active/history
6. Returns "index" view with model attributes
7. Thymeleaf renders HTML, browser JS rebuilds slot grid

---

## 4. Folder Architecture & Mapping

```
QSpark/
|-- pom.xml                          # Maven build - Spring Boot 3.3.1 parent, all dependencies
|-- mvnw.cmd / mvnw                  # Maven wrapper - cross-platform build without local Maven
|-- QSpark.bat                       # Windows launcher - port check, auto-start, clean shutdown
|-- AGENTS.md                        # AI agent guide for development assistants
|-- PRD.md                           # Product requirements document (MoSCoW feature matrix)
|-- README.md                        # User-facing documentation
|-- CONTACTS.md                      # Developer contact info
|-- context.md                       # THIS FILE - workspace state & architecture reference
|-- CHANGELOG.md                     # Release history for v1.0.0
|-- SECURITY.md                      # Security policy and vulnerability reporting
|-- LICENSE                          # GNU General Public License v3.0
|-- .env.example                     # Environment variable template (DB_PASSWORD, ADMIN_PASSWORD)
|-- data.sql / data-h2.sql           # 0-byte remnants - seeding handled by DataInitializer.java
|-- .github/
|   |-- workflows/
|       |-- maven.yml                # GitHub Actions CI - runs mvn test on push/PR
|
|-- src/main/java/com/qspark/
|   |-- QSparkApplication.java       # Entry point - @SpringBootApplication
|   |-- config/
|   |   |-- SecurityConfig.java      # Filter chain - auth rules, CSRF, headers, 401 entry points, HSTS
|   |   |-- AdminDetailsService.java # UserDetailsService - BCrypt-based admin lookup
|   |   |-- DataInitializer.java     # Seeds admin with BCrypt on first run (ADMIN_USERNAME/ADMIN_PASSWORD env vars)
|   |   |-- LoginRateLimitFilter.java # Rate limiter - 5 failed login attempts = 5-min cooldown per IP
|   |-- controller/
|   |   |-- LoginController.java     # GET / (landing), GET /login - public only
|   |   |-- ParkingController.java   # Dashboard + CRUD - /dashboard, /save, /update, /delete, /left, /edit
|   |   |-- QRCodeController.java    # QR lifecycle - /qrcode, /qrcode/generate, /qrcode/scan, /qrcode/list
|   |-- model/
|   |   |-- Admin.java               # @Entity - id, username (unique), password (BCrypt 60 chars)
|   |   |-- ParkingSlot.java         # @Entity - 480 slots, @Pattern vehicle number, entry/exit timestamps
|   |   |-- VehicleRegistration.java # @Entity - QR-registered vehicles, createdAt set in constructor
|   |-- repository/
|       |-- AdminRepository.java     # findByUsername()
|       |-- ParkingRepository.java   # findBySlotNumber(), existsByVehicleNumberAndExitTimeIsNull()
|       |-- VehicleRegistrationRepository.java  # findByVehicleNumber() with Optional, existsByVehicleNumber()
|
|-- src/main/resources/
|   |-- application.properties       # MySQL config - port 8081, DB_PASSWORD env var, Thymeleaf cache=false
|   |-- application-h2.properties    # H2 config - in-memory, console enabled, port 8081
|   |-- templates/                   # Thymeleaf views (server-rendered pages)
|   |   |-- landing.html             # Marketing page - hero-banner.webp, SEO/GEO meta, JSON-LD, FAQ
|   |   |-- login.html               # Auth page - 2-column, shake animation on error, CSRF
|   |   |-- index.html               # Dashboard - 3-tab, slot grid, tables, dark mode
|   |   |-- edit.html                # Edit booking - cascading dropdowns, auto-format, validation
|   |   |-- qrcode.html              # QR page - generate/download tab, camera scan tab
|   |-- static/
|       |-- css/styles.css           # Full design system - tokens, layout, components, dark mode
|       |-- js/app.js                # Shared utilities - Toast, Modal, sort, filter, paginate, CSV, slot grid
|       |-- manifest.json            # Web app manifest (PWA support)
|       |-- robots.txt               # SEO directives
|       |-- images/
|       |   |-- logo-light.webp / logo-dark.webp        # Brand logos (light & dark variants)
|       |   |-- hero-banner-light.webp / hero-banner-dark.webp  # Hero banners (light & dark variants)
|       |   |-- pawan-simha.webp / prajwal-r.webp       # Developer avatars
|       |-- favicon.ico
|
|-- src/test/java/com/qspark/        # 66 tests total
|   |-- QSparkApplicationTests.java  # 1 test - context loads
|   |-- controller/
|   |   |-- LoginControllerTest.java        # 2 tests - WebMvcTest public endpoints
|   |   |-- LoginIntegrationTest.java       # 4 tests - full login flow with H2
|   |   |-- ParkingControllerTest.java      # 24 tests - CRUD + auth guards + validation
|   |   |-- QRCodeControllerTest.java       # 8 tests - QR generate/scan/list + auth
|   |-- model/
|   |   |-- ParkingSlotValidationTest.java  # 14 tests - vehicle number + mobile regex
|   |-- repository/
|       |-- AdminRepositoryTest.java        # 4 tests
|       |-- ParkingRepositoryTest.java      # 3 tests
|       |-- VehicleRegistrationRepositoryTest.java  # 6 tests
```

---

## 5. Engineering Guardrails & Anti-Patterns

### Style Conventions

| Rule | Standard | Enforcement |
|------|----------|-------------|
| **Java naming** | camelCase for methods/vars, PascalCase for classes | Standard Java conventions |
| **Database columns** | snake_case (e.g., `owner_name`, `vehicle_number`) | `@Column(name = ...)` in entities |
| **CSS naming** | kebab-case for classes (e.g., `.slot-tile`, `.modal-overlay`) | Manual code review |
| **JS naming** | camelCase for functions/vars, PascalCase for constructors | Manual code review |
| **Template IDs** | camelCase for HTML id attributes | Manual code review |
| **URL paths** | lowercase with hyphens (e.g., `/qrcode/generate`) | Manual code review |
| **Line endings** | LF for Java/XML/HTML/CSS/JS/MD/PROPERTIES; CRLF for .cmd/.bat | `.gitattributes` enforces |
| **GPLv3 header** | All source files must include the GPLv3 license header | Manual audit |

### Strict Anti-Patterns

- **No frontend frameworks.** Do not import React, Vue, Alpine, Tailwind, Bootstrap, or any JS/CSS framework. All code must be vanilla JS and hand-written CSS with custom properties.
- **No SPA routing.** Every page navigation is a full server round-trip via Thymeleaf. Do not implement client-side routing or hash-based navigation.
- **No manual session checks.** Spring Security handles authentication declaratively in the filter chain. Controllers must never check `session.getAttribute("admin")`.
- **No JWT or OAuth.** Auth is form-based with server sessions only. No API tokens, no stateless auth.
- **No service layer.** Controllers call repositories directly. Do not introduce @Service classes between controller and repository unless the pattern is explicitly discussed.
- **No DTOs.** Entities serve as both persistence models and view models. Do not create separate DTO classes unless required for a specific JSON response.
- **No global exception handler.** Each controller method handles its own errors with try/catch and model attributes. No @ControllerAdvice.
- **No client-side data fetching for critical UI.** Slot grid rendering depends on model attributes passed from the server. Do not fetch slot data via AJAX on page load.
- **Do not skip CSRF on POST forms.** Every state-changing POST must include a CSRF token: `<input type="hidden" name="_csrf" th:value="${_csrf.token}">` for forms, `X-CSRF-TOKEN` header for AJAX.
- **No em dashes in documentation files.** Use ` - ` instead of ` - ` (em dash U+2014) in all .md documentation files.
- **data.sql and data-h2.sql are both 0-byte remnants.** All seeding is handled by DataInitializer.java. Do not add SQL seed commands to these files.

---

## 6. Active Session State & Milestone Checklist

### Completed Features (Milestones Reached)

- [x] Spring Boot 3.3.1 project scaffolding with Maven Wrapper
- [x] MVC architecture with Thymeleaf templates (5 views: landing, login, dashboard, edit, QR)
- [x] Spring Security 6.0 integration - BCryptPasswordEncoder, form login/logout, CSRF, secure headers
- [x] AdminDetailsService (UserDetailsService impl) + DataInitializer (seeds QSparker/QSpark@Secure)
- [x] 480-slot management (4 basements x 120 slots, format B1-001 through B4-120)
- [x] Full CRUD - create, read, update, delete bookings
- [x] Slot double-booking prevention + duplicate active vehicle detection
- [x] Vehicle number validation via @Pattern (4 Indian registration formats)
- [x] QR code generation (ZXing PNG download) + scanning (html5-qrcode camera)
- [x] Vehicle registration table for QR-managed vehicles
- [x] Entry/exit timestamp audit trail
- [x] Dark mode toggle with localStorage persistence
- [x] Client-side table sorting, filtering, pagination, CSV export
- [x] Toast notification system, Modal dialog system with focus trap
- [x] SEO/GEO-optimized landing page (JSON-LD, Open Graph, Twitter, AI crawler meta)
- [x] GPLv3 license headers in all source files (31 files)
- [x] Dual database profile (MySQL production / H2 development)
- [x] MySQL password via environment variable `${DB_PASSWORD}`
- [x] Windows quick launcher (QSpark.bat - port check, single window, auto-open browser)
- [x] 66 tests passing (0 failures, 0 errors)
- [x] Em dashes removed from all .md documentation files
- [x] AGENTS.md fully rewritten with accurate file inventory, test counts, and architecture patterns
- [x] README.md synced with correct line counts, test counts, and endpoint methods
- [x] PRD.md test count updated, dual-seed reference corrected
- [x] Security audit: DELETE/LEFT migrated from GET→POST, @Valid+BindingResult added to update(), mobileNumber @Pattern enforcement, dead code removed, blue color standardized, dark mode unified across all pages
- [x] Login page CSRF fix: eager token resolution in controller to prevent session-after-response-committed crash
- [x] Login page dark mode removed: toggle button, CSS, and body.dark rule removed per user request

### In-Progress Workloads (Active Focus)

- [ ] Investigate `Integer` vs `Long` ID consistency (ParkingSlot uses Integer, VehicleRegistration uses Long)

### Upcoming Implementations (Next Steps)

- [ ] Multi-admin roles (super admin, operator, viewer) with distinct permissions
- [ ] PDF parking receipts with QR code on exit
- [ ] Server-side pagination for large history datasets
- [ ] REST API for third-party integrations (mobile apps, kiosks)
- [ ] Real-time WebSocket slot grid updates
- [ ] Automated email/SMS notifications at occupancy thresholds
- [ ] Monthly occupancy reports (charts + PDF)
- [ ] License plate recognition (ANPR) as future enhancement

### Completed Since Last Session

- [x] **Git history purge** — Removed `[REDACTED]`, `[REDACTED]`, `backend.log` from all 17 commits using git-filter-repo
- [x] **Admin password via env var** — DataInitializer.reads `ADMIN_USERNAME`/`ADMIN_PASSWORD` env vars with dev fallback
- [x] **Rate limiting on /login** — LoginRateLimitFilter: 5 failed attempts per IP, 5-min cooldown
- [x] **HSTS header** — includeSubDomains, 1-year max-age added to SecurityConfig
- [x] **QR scan redirect** — PII removed from URL params; uses RedirectAttributes (flash attributes) instead
- [x] **GitHub Actions CI** — .github/workflows/maven.yml runs tests on push/PR to main
- [x] **Copyright headers** — All 33+ source files: "PawanSimha" → "Pawan Simha R"
- [x] **pom.xml version** — 0.0.1-SNAPSHOT → 1.0.0
- [x] **CHANGELOG.md, SECURITY.md, .env.example** — Created for release
- [x] **manifest.json, robots.txt** — Created for SEO/PWA
- [x] **Stale ParkEase references** — Removed from .idea/compiler.xml
