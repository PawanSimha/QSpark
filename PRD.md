# QSpark - Product Requirements Document

| Field | Detail |
|-------|--------|
| **Project Name** | QSpark - Quick & Smart Parking |
| **Target Release** | Q3 2026 (Current) |
| **Status** | Production Ready v1.0 |
| **Author** | Pawan Simha R |
| **Document Version** | 1.0 |

---

## Executive Summary

QSpark is a **production-grade smart parking management system** that digitizes the entire parking lifecycle - booking, tracking, and auditing - for commercial buildings. It replaces manual logbooks and spreadsheets with a real-time, server-rendered web dashboard that manages **480 parking slots across 4 basements**, supports **QR-code-based vehicle identification**, and provides **full audit trails with entry/exit timestamps**. The system is built with Spring Boot, secured with Spring Security + BCrypt, and deploys with zero frontend framework dependencies.

---

## Problem Statement

Commercial parking lots face systemic operational friction:

| Pain Point | Impact |
|------------|--------|
| **Manual logbooks** | Administrators waste hours recording vehicle entries and exits by hand |
| **No real-time visibility** | Guards cannot tell which slots are free without physically inspecting the lot |
| **Double-booking conflicts** | Two vehicles assigned the same slot due to lack of centralized tracking |
| **No historical audit** | Lost records make dispute resolution and occupancy analysis impossible |
| **Slow vehicle identification** | No standard way to look up a vehicle's registration or previous bookings |
| **Spreadsheet fragility** | Shared Excel files corrupt, go missing, or allow inconsistent data entry |

QSpark solves all six problems with a single, unified, server-authoritative application.

---

## Target Personas

| Persona | Role | Primary Need | System Interaction |
|---------|------|--------------|--------------------|
| **Parking Administrator** | Building staff managing daily parking operations | Real-time slot visibility, quick booking, vehicle lookup | Dashboard → Book/Edit/Release slots → Export reports |
| **Security Guard** | Entry/exit personnel verifying vehicles | Fast vehicle identification via QR scan | QR Scanner → Auto-fill dashboard → Confirm slot |
| **System Admin** | IT or facility manager overseeing the system | User management, uptime monitoring, data integrity | Login → Monitor slots → Audit history (future: role admin) |

**Current scope serves Persona 1 and 2.** Persona 3 capabilities are limited to the single admin account (`QSparker`).

---

## Success Metrics (OKRs / KPIs)

| Metric | Target | Rationale |
|--------|--------|-----------|
| **Test Pass Rate** | 100% (66/66) | Zero regression tolerance for a parking operations system |
| **Booking Creation Time** | < 30 seconds | Admin should book a slot in under 30 seconds from login |
| **Slot Grid Accuracy** | 100% | Occupied slots must always match database state (single source of truth) |
| **QR Scan → Form Auto-Fill** | < 3 seconds | Camera scan must populate dashboard form with sub-second delay |
| **Auth Response Time** | < 500ms | Login validation should complete within half a second |
| **Dark Mode Persistence** | 100% | Preference must survive page reloads and navigation |
| **Zero Plaintext Secrets** | 1/1 (BCrypt) | No plaintext passwords anywhere in the deployment pipeline |

---

## Core Features & Requirements (MoSCoW Method)

### P0 - Must Have (Shipping v1.0)

| ID | Feature | Description | Status |
|----|---------|-------------|--------|
| F-01 | **Spring Security Auth** | Admin login with username/password, BCrypt hashing, form login/logout, CSRF protection on all POST forms, API 401 entry points | ✅ Built |
| F-02 | **BCrypt Password Hashing** | Passwords stored as BCrypt hashes (60-char column), never plaintext | ✅ Built |
| F-03 | **CSRF Protection** | All POST forms (login, save, update, logout, scan) include CSRF tokens; AJAX calls send CSRF header | ✅ Built |
| F-04 | **Three-Tab Dashboard** | Booking form, Occupied table, History table with JS tab switching | ✅ Built |
| F-05 | **480-Slot Grid (4×120)** | Clickable 15-column grid per basement, quick-jump navigation, color-coded occupancy | ✅ Built |
| F-06 | **Full CRUD Operations** | Create booking, update booking, delete booking, mark vehicle as left | ✅ Built |
| F-07 | **Vehicle Number Validation** | Regex enforcement for Indian formats: `KA 04 A 2256`, `DL 03 CA 1234`, `ML 04 ASD 5256`, `26 BH 9876 AB` | ✅ Built |
| F-08 | **Duplicate Prevention** | Slot double-booking guard, duplicate active vehicle detection | ✅ Built |
| F-09 | **QR Code Generation** | Generate downloadable QR PNG via ZXing, register vehicle in database | ✅ Built |
| F-10 | **QR Code Scanning** | Camera-based scan via html5-qrcode - auto-fill dashboard form, error on unregistered | ✅ Built |
| F-11 | **Entry/Exit Timestamps** | `entry_time` set on booking, `exit_time` set on "Mark as Left" | ✅ Built |
| F-12 | **Audit Trail (History)** | All completed bookings visible in History tab with full details | ✅ Built |
| F-13 | **Dark Mode** | Toggle persisted in `localStorage` (`qspark-dark-mode`), applied across all pages except login | ✅ Built |
| F-14 | **Client-Side Search & Sort** | real-time `filterTable()` across columns, click-to-sort with asc/desc | ✅ Built |
| F-15 | **CSV Export** | Download visible rows from Occupied and History tables | ✅ Built |
| F-16 | **Security Headers** | X-XSS-Protection, X-Content-Type-Options (nosniff), X-Frame-Options (DENY), Referrer-Policy (same-origin) | ✅ Built |

### P1 - Should Have (Next Release)

| ID | Feature | Rationale |
|----|---------|-----------|
| F-17 | **Rate Limiting on `/login`** | Prevent brute-force password guessing | ✅ Built |
| F-18 | **Multi-Admin Roles** | Distinguish super admin, parking operator, and viewer roles with distinct permissions |
| F-19 | **MySQL Password via Env Var** | Replace hardcoded dev password with `${DB_PASSWORD}` in application.properties | ✅ Built |
| F-20 | **Parking Receipt (PDF)** | Generate a printable receipt with QR code when vehicle exits |
| F-21 | **Pagination on History Table** | Server-side pagination for large history datasets (480+ records) |

### P2 - Could Have (Future)

| ID | Feature | Rationale |
|----|---------|-----------|
| F-22 | **REST API for Third Parties** | Expose slot availability and booking endpoints for mobile apps or kiosks |
| F-23 | **Real-Time WebSocket Updates** | Push slot grid changes to all open browser sessions without page reload |
| F-24 | **Automated Email/SMS Notifications** | Notify admin when occupancy exceeds threshold or vehicle is due for exit |
| F-25 | **Monthly Occupancy Reports** | Generate charts and PDF reports from history data |
| F-26 | **License Plate Recognition (ANPR)** | Camera-based automatic plate detection using ML |

---

## AI & Technical Constraints

| Constraint | Requirement | Source |
|------------|-------------|--------|
| **Password Storage** | BCrypt hashing only - no plaintext, MD5, SHA-1, or reversible encryption | `Admin.password` - `@Column(length=60)`, `BCryptPasswordEncoder` |
| **Auth Mechanism** | Form-based login with server session - no JWT, no OAuth, no API tokens | Spring Security `formLogin()` |
| **CSRF Enforcement** | Every state-changing POST must include a valid CSRF token | `SecurityConfig` default + manual tokens in all templates |
| **Session Invalidation** | Logout must invalidate session and clear authentication | `.invalidateHttpSession(true).clearAuthentication(true)` |
| **Vehicle Number Format** | Must match exactly one of 4 predefined Indian registration regex patterns | `ParkingSlot.vehicleNumber` - `@Pattern` |
| **Database Profiles** | MySQL for production, H2 in-memory for dev - seeding via DataInitializer.java | `application.properties` vs `application-h2.properties` |
| **Server Rendering** | No SPA - all pages are full Thymeleaf server renders; JS is progressive enhancement | Template architecture |
| **QR Code Format** | QR encodes the vehicle number string (uppercase, trimmed), not a URL or JSON | `QRCodeController.generateQR()` |
| **Scanner TLS Requirement** | Camera scan requires `localhost` or HTTPS - HTTP-only servers will fail | `html5-qrcode` browser security policy |
| **Test Coverage** | Every controller endpoint must have at minimum: without-auth → 401, with-auth → success | All controller tests follow this pattern |

---

## User Journey / Flow

### Primary Flow: Booking a Vehicle

```
Step 1: Admin navigates to http://localhost:8081
        └── Landing page → clicks "Get Started" → /login

Step 2: Admin enters username + password → POST /login (with CSRF)
        ├── Invalid → /login?error=true → shake animation → retry
        └── Valid → 302 redirect → GET /dashboard

Step 3: Dashboard loads with 3 tabs
        ├── Booking tab (default) → slot grid + form
        ├── Occupied tab → active bookings table
        └── History tab → completed bookings

Step 4: Admin selects basement → slot grid renders 120 slots
        └── Clicks free slot (green) → slot number populates hidden field

Step 5: Admin enters owner name, vehicle number (with format selector), type, mobile
        ├── Invalid number → server returns error → shown on form
        └── Valid → POST /save (with CSRF)

Step 6: Server checks
        ├── Slot already occupied? → error "Slot Already Booked!"
        ├── Vehicle already active? → error "Vehicle already parked!"
        └── All clear → saves with entryTime=now → success toast

Step 7: Dashboard re-renders with updated slot grid (red = occupied)
```

### Secondary Flow: QR Scan

```
Step 1: Admin opens /qrcode → Scan tab → clicks "Start Camera"

Step 2: Camera scans QR code → decodes vehicle number

Step 3: POST /qrcode/scan (dynamic form with CSRF token)
        ├── Vehicle not registered → error → return to QR page
        └── Registered → 302 redirect → /dashboard (via flash attributes, no PII in URL)

Step 4: Dashboard receives flash attributes → form auto-fills → fields locked
        └── Admin only needs to pick a slot → confirm booking
```

---

## Out of Scope (Non-Goals)

| Non-Goal | Rationale |
|----------|-----------|
| **No mobile app** | The system is a server-rendered web app; mobile browsers are supported responsively but no native app exists |
| **No real-time WebSocket** | All updates require a page refresh or manual tab switch (history data is always fresh from server) |
| **No payment processing** | QSpark does not handle parking fees, billing, or payment gateways |
| **No multi-tenant architecture** | The system manages a single parking facility per deployment |
| **No third-party SSO** | Only form-based auth with the built-in admin credential - no Google, GitHub, or LDAP login |
| **No automated vehicle entry/exit gates** | QSpark is a management layer, not a physical gate controller - a human operator books/releases slots |
| **No ANPR / license plate recognition** | Vehicle identification is via QR code only - no camera-based plate reading |
| **No API-first design** | All endpoints return HTML views (except `/qrcode/list` JSON and `/qrcode/generate` PNG) - no RESTful API for external consumers |
