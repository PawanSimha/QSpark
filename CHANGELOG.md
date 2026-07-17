# Changelog

All notable changes to QSpark will be documented in this file.

## 1.0.0 (2026-07-17)

### Added
- Spring Boot 3.3.1 project scaffolding with Maven Wrapper
- Spring Security 6.3 integration with BCrypt password hashing
- Form-based authentication with CSRF protection on all state-changing endpoints
- 480-slot parking management across 4 basements (B1-001 through B4-120)
- Full CRUD operations: create, edit, update, delete, mark-as-left
- Slot double-booking prevention and duplicate active vehicle detection
- Vehicle number validation via @Pattern (4 Indian registration formats)
- QR code generation with ZXing (PNG download)
- Camera-based QR scanning via html5-qrcode with dashboard auto-fill
- Vehicle registration table for QR-managed vehicles
- Entry/exit timestamp audit trail with history view
- Dark mode toggle with localStorage persistence (key: `qspark-dark-mode`)
- Client-side table sorting, filtering, pagination, and CSV export
- Toast notification system and modal dialog system with focus trap
- SEO/GEO-optimized landing page (JSON-LD, Open Graph, Twitter Cards, AI crawler directives)
- Dual database profile: MySQL (production) and H2 (development/testing)
- Windows quick launcher (QSpark.bat)
- 66 passing tests covering controllers, repositories, and validation

### Security
- BCrypt password encoding (strength 10)
- CSRF protection on all POST forms and AJAX calls
- Security headers: XSS protection, nosniff, frame-deny, referrer-policy
- State-changing endpoints migrated from GET to POST
- Mobile number validation with @Pattern regex
- Eager CSRF token resolution to prevent session-after-response-committed crash
- Git history purged of hardcoded credentials ([REDACTED], [REDACTED], backend.log)
- Admin password now configurable via ADMIN_PASSWORD environment variable
- Rate limiting on /login (5 failed attempts = 5-min cooldown per IP)
- HSTS header with includeSubDomains and 1-year max-age

### Changed
- Project renamed from ParkEase to QSpark
- Login page dark mode removed per user request
- Documentation fully synchronized: README, AGENTS, PRD, context
- Copyright headers standardized to "Pawan Simha R"
- Blue color palette unified to #2563EB across all pages
- Vehicle format handling centralized in app.js VEHICLE_FORMATS
- pom.xml version updated to 1.0.0

### Fixed
- Login page crash: deferred CSRF token now eagerly resolved in controller
- Dark mode persistence: localStorage key unified to `qspark-dark-mode`
- @Valid + BindingResult added to update() method
- delete() existence check prevents EmptyResultDataAccessException
- Login redirect corrected to /dashboard
- Dead code, CSS, and JS elements removed
- Cache-busting query parameter added to styles.css link
