# QSpark - Agent Guide

## Project Identity

QSpark is a **production-grade smart parking management system** built with Java 17, Spring Boot 3.3.1, Thymeleaf, and MySQL/H2. It manages 480 parking slots across 4 basements with session-based auth, QR code vehicle identification, full CRUD operations, and an audit trail.

**Port:** 8081 (Jenkins occupies 8080 on this dev machine)
**Login:** `QSparker` / `QSpark@Secure`

---

## Mental Model - How the System Works

The system is a **server-rendered MVC monolith** with Thymeleaf templates and vanilla JavaScript enhancement. There is no SPA, no React, no frontend framework. Every navigation is a full page load.

```
Browser (HTML + CSS + JS) → Spring Boot Controller → Repository (JPA) → Database
                                                                         ↓
Thymeleaf renders HTML ← Controller adds model data ← Repository returns entities
         ↓
Browser JS enhances: live clock, slot grid, table search/sort, QR scanner, dark mode
```

**Key concept:** The server is in full control. Controllers check session auth on every guarded endpoint, then pass model data to Thymeleaf templates that render HTML. JavaScript only adds convenience features on top of server-rendered pages - it never replaces server-side logic.

---

## File Inventory (Every Source File)

### Java Source - `src/main/java/com/qspark/`

| File | Line Count | Purpose |
|------|-----------|---------|
| `QSparkApplication.java` | 9 | Entry point. `@SpringBootApplication` with `SpringApplication.run()`. |
| `controller/LoginController.java` | 51 | Handles landing page (`/`), login form (`GET/POST /login`), logout (`GET /logout`). |
| `controller/ParkingController.java` | 185 | Dashboard (`/dashboard`), create (`POST /save`), edit/get (`GET /edit/{id}`), update (`POST /update`), mark left (`GET /left/{id}`), delete (`GET /delete/{id}`). |
| `controller/QRCodeController.java` | 100 | QR page (`GET /qrcode`), generate QR PNG (`POST /qrcode/generate`), scan redirect (`POST /qrcode/scan`), JSON list (`GET /qrcode/list`). |
| `model/ParkingSlot.java` | 79 | `@Entity` - id, ownerName, vehicleNumber (with `@Pattern` validation), vehicleType, slotNumber, mobileNumber, entryTime, exitTime. |
| `model/Admin.java` | 30 | `@Entity` - id, username (unique), password. |
| `model/VehicleRegistration.java` | 40 | `@Entity` - id, vehicleNumber (unique, not null), ownerName, vehicleType, mobileNumber, createdAt (set in constructor). |
| `repository/AdminRepository.java` | 9 | `findByUsernameAndPassword(username, password)`. |
| `repository/ParkingRepository.java` | 9 | `findBySlotNumber()`, `findByVehicleNumberContainingIgnoreCase()`, `existsByVehicleNumberAndExitTimeIsNull()`. |
| `repository/VehicleRegistrationRepository.java` | 8 | `findByVehicleNumber()` returns `Optional`, `existsByVehicleNumber()`. |

### Test Source - `src/test/java/com/qspark/`

| File | Line Count | Tests | Purpose |
|------|-----------|-------|---------|
| `QSparkApplicationTests.java` | 9 | 1 | Context loads successfully. |
| `controller/LoginControllerTest.java` | 61 | 2 | WebMvcTest: landing page, login page access. Full auth flow tested in LoginIntegrationTest. |
| `controller/LoginIntegrationTest.java` | -- | 4 | Full-stack login flow with H2: valid login redirect, invalid login stays, logout redirect, landing page. |
| `controller/ParkingControllerTest.java` | 281 | 23 | Dashboard access, save (valid/duplicate/error/same-vehicle), delete (valid/invalid), left (valid/invalid/error), edit (valid/invalid), update (valid/invalid/error), scanned params. |
| `controller/QRCodeControllerTest.java` | 88 | 8 | QR page access, generate (no session -> 401), scan (no session -> login, unregistered error, registered redirect), list JSON (with/without session). |
| `model/ParkingSlotValidationTest.java` | 84 | 9 | Vehicle number regex validation - valid formats (KA 01 AB 1234, TN 07 A 1234, 26 BH 9876 AB, ML 04 ASD 5256) and invalid. |
| `repository/AdminRepositoryTest.java` | 44 | 4 | Find by username+password (valid, wrong password, wrong username, no match). |
| `repository/ParkingRepositoryTest.java` | 71 | 6 | Find by slot number (exists/null), vehicle number search (partial match, case insensitive, no match), persist. |
| `repository/VehicleRegistrationRepositoryTest.java` | 49 | 6 | Find by vehicle number (exists/empty), exists check, save with createdAt, case sensitivity. |

**Total: 63 tests** - all pass with `mvnw.cmd test`.

### Templates - `src/main/resources/templates/`

| File | Purpose |
|------|---------|
| `landing.html` | 754 lines. Marketing page with full-width hero-banner.webp hero, floating pill nav, features with icon cards, QR showcase with phone mockup, architecture diagram (3-tier stack), dark gradient stats, CTA, footer. Inter font, #C90213 primary, #EAEAEA background. IntersectionObserver scroll animations. |
| `login.html` | Full-screen centered auth card (80% viewport), 2-column layout: left panel (centered logo + Welcome Back heading + pill-shaped input fields with icons + Sign In button + social login + bottom text), right panel (full-height image, no text/overlay). Shake animation on error. |
| `index.html` | Dashboard with sidebar (Dashboard/Active/History/QR Code), top bar (dark mode, scan button, live clock, logout), 3-tab layout (Booking form with 4x120 slot grid, Occupied table with search/sort/export, History table). |
| `edit.html` | Edit booking form with basement/slot cascading dropdown, vehicle number auto-format, mobile validation. |
| `qrcode.html` | Two-tab layout: Generate QR (owner + vehicle number with format selector + type + mobile -> download PNG), Scan QR (camera via html5-qrcode -> auto-fill dashboard). |

### Static - `src/main/resources/static/`

| File | Purpose |
|------|---------|
| `css/styles.css` | Full design system with CSS custom properties (tokens), sidebar layout, top bar, cards, tables, forms, buttons, slot grid, toasts, modals, dark mode, pagination. 1240 lines. |
| `js/app.js` | Toast notification system, Modal dialog system, dark mode toggle (`qspark-dark-mode` localStorage), live clock, table sorting, table filtering, pagination, CSV export, vehicle format helpers, slot grid builder, quick-jump. 527 lines. |
| `logo.png` | QSpark brand logo. |
| `hero-banner.webp` | Full-width hero image (1920×1080, 121KB, WebP, quality 90). Displayed on landing page with no overlaid text or cards. |

### Config Files

| File | Purpose |
|------|---------|
| `application.properties` | MySQL connection (`jdbc:mysql://localhost:3306/qspark`), JPA ddl-auto=update, port 8081, Thymeleaf cache=false. |
| `application-h2.properties` | H2 in-memory (`jdbc:h2:mem:qspark`), H2 console enabled, loads `data-h2.sql`, port 8081. |
| `data.sql` | Admin seed for MySQL: `INSERT IGNORE INTO admin (username, password) VALUES ('QSparker', 'QSpark@Secure')`. |
| `data-h2.sql` | Admin seed for H2: `MERGE INTO admin (username, password) KEY(username) VALUES ('QSparker', 'QSpark@Secure')`. |
| `pom.xml` | Spring Boot 3.3.1 parent, dependencies: data-jpa, thymeleaf, web, mysql-connector-j, h2, validation, zxing-core 3.5.3, zxing-javase 3.5.3, spring-boot-starter-test. |
| `QSpark.bat` | Windows launcher: checks port 8081, launches mvnw with H2 profile in minimized window, polls for ready, opens browser, clean shutdown on keypress. |
| `.gitignore` | Ignores HELP.md, backend.log, target/, build/, .DS_Store, Thumbs.db, IDE files (.idea, .vscode, etc.), *.local.properties. |
| `.gitattributes` | Auto LF normalization, Java/*.xml/*.html/*.css/*.js/*.md/*.properties text with diff, mvnw eol=lf, .cmd/.bat eol=crlf, binaries marked. |
| `AGENTS.md` | This file. |
| `README.md` | Full project documentation with architecture diagrams, tech stack, quick start, API endpoints, database schema, troubleshooting. |

---

## Request Lifecycle (Detailed)

### Example: Creating a Booking

1. Admin clicks "Confirm Booking" in `index.html`
2. `prepareSubmit()` JS function validates slot is selected, enables disabled form fields
3. Form POST to `/save` with ownerName, vehicleNumber, vehicleType, slotNumber, mobileNumber
4. `ParkingController.save()` checks:
   - Spring Security ensures the user is authenticated before reaching the controller
   - Validation: `@Valid` triggers `@Pattern` regex on vehicleNumber -> returns to index with error if invalid
   - Slot availability: `findBySlotNumber()` with `exitTime == null` -> "Slot Already Booked!"
   - Vehicle duplicate: `existsByVehicleNumberAndExitTimeIsNull()` -> "Vehicle already parked in another slot!"
5. Sets `entryTime = LocalDateTime.now()`, saves via `repository.save()`
6. Re-fetches all records, splits into active (`exitTime == null`) and history (`exitTime != null`)
7. Returns `"index"` view with model attributes: `parking` (new empty form), `list` (active), `history` (completed), `occupied` (comma-separated occupied slot numbers)
8. Thymeleaf renders `index.html` with the updated data
9. Browser JS rebuilds the slot grid showing newly occupied slot in red

### Example: QR Scan Flow

1. Admin goes to `/qrcode` -> clicks Scan tab -> clicks "Start Camera"
2. `startScanner()` JS creates `Html5Qrcode` instance with rear camera
3. On successful scan, JS creates a hidden form and submits POST to `/qrcode/scan` with `vehicleNumber`
4. `QRCodeController.scanQR()`:
   - Checks session
   - Formats vehicle number to uppercase
   - Looks up `VehicleRegistrationRepository.findByVehicleNumber()`
   - If not found -> returns to qrcode view with error "Vehicle not registered"
   - If found -> redirects to `/dashboard` with URL parameters (ownerName, vehicleNumber, vehicleType, mobileNumber)
5. `ParkingController.dashboard()` receives URL params, adds them as `scanned*` model attributes
6. `applyScannedVehicle()` JS auto-fills and locks the form fields - admin only needs to pick a slot

### Example: Authentication

1. GET `/login` -> `LoginController.loginPage()` returns `login.html`
2. POST `/login` with username + password -> Spring Security's form login filter handles auth
3. `AdminDetailsService.loadUserByUsername()` fetches user via `AdminRepository.findByUsername()`
4. `BCryptPasswordEncoder` validates the password against the stored hash
5. If match -> Spring Security creates the security context, redirects to `/dashboard`
6. If no match -> returns login with `model.addAttribute("error", "Invalid Username or Password")`
7. Login page JS detects error message and applies shake animation

---

## Data Model (Database Schema)

### `admin` table
| Column | Type | Constraints |
|--------|------|:-----------:|
| `id` | `INT` | PK, AUTO_INCREMENT |
| `username` | `VARCHAR` | UNIQUE |
| `password` | `VARCHAR` | NOT NULL |

### `parking_slot` table
| Column | Type | Notes |
|--------|------|-------|
| `id` | `INT` | PK, AUTO_INCREMENT |
| `owner_name` | `VARCHAR` | Free text |
| `vehicle_number` | `VARCHAR` | Validated by `@Pattern` regex |
| `vehicle_type` | `VARCHAR` | One of: Car, Bike, Scooter, Auto Rickshaw, Electric Car, Electric Two Wheeler |
| `slot_number` | `VARCHAR` | Format: `B1-001` through `B4-120` |
| `mobile_number` | `VARCHAR` | 10 digits |
| `entry_time` | `DATETIME` | Set server-side via `LocalDateTime.now()` on booking |
| `exit_time` | `DATETIME` | `NULL` while active; set on "Left" action |

### `vehicle_registration` table
| Column | Type | Constraints |
|--------|------|:-----------:|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `vehicle_number` | `VARCHAR` | UNIQUE, NOT NULL |
| `owner_name` | `VARCHAR` | -- |
| `vehicle_type` | `VARCHAR` | Same 6 types |
| `mobile_number` | `VARCHAR` | 10 digits |
| `created_at` | `DATETIME` | Set via `LocalDateTime.now()` in constructor |

### Entity Relationships
- No JPA `@OneToMany`/`@ManyToOne` relationships exist - all lookups are explicit via repository methods
- `ParkingSlot` uses `Integer` for ID; `VehicleRegistration` uses `Long` for ID - intentional but inconsistent
- `ParkingSlot.vehicleNumber` and `VehicleRegistration.vehicleNumber` are independent strings (no FK constraint)

---

## Spring Security Architecture

### Filter Chain (SecurityConfig)
- All paths require authentication except `/`, `/login`, `/css/**`, `/js/**`, `/logo.png`
- `/qrcode/generate` and `/qrcode/list` return 401 (for API-like use)
- Form login at `/login` with default success redirect
- Logout via POST `/logout`
- CSRF enabled on all state-changing endpoints
- Security headers: XSS protection, content-type sniffing disabled, frame deny, referrer-policy same-origin

### Password Hashing
- `BCryptPasswordEncoder` with strength 10
- Admin password `QSpark@Secure` is hashed at app startup by `DataInitializer.java`
- Use `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"` to auto-create admin on first run

### CSRF Token Usage
| Context | How to Include |
|---------|---------------|
| Thymeleaf form | `<input type="hidden" name="_csrf" th:value="${_csrf.token}">` |
| AJAX POST | `headers: { 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content }` |
| Controller test | `.with(csrf())` |

---

## Key Patterns & Conventions

### Auth Pattern (Spring Security - No Manual Session Checks)

Auth is managed declaratively through `SecurityConfig.java` filter chain. Controllers no longer check `session.getAttribute("admin")` - that is handled by Spring Security before the controller method is reached.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Form login with /login page
    // POST /logout
    // CSRF enabled on all POST/PUT/DELETE
    // /qrcode/generate and /qrcode/list return 401 (not redirect)
    // All other endpoints require authentication
}
```

- `DataInitializer` seeds the admin user with BCrypt at startup
- `AdminDetailsService` implements `UserDetailsService` via `AdminRepository.findByUsername()`
- Every POST form must include `<input type="hidden" name="_csrf" th:value="${_csrf.token}">`
- AJAX POST requests (e.g., QR generate) need `X-CSRF-TOKEN` header from the meta tag
- CSRF token is available in templates as `${_csrf.token}` and in meta as `name="_csrf"`

### Controller Error Handling Pattern

```java
try {
    // business logic
    model.addAttribute("success", "Operation Completed Successfully");
} catch (Exception e) {
    model.addAttribute("error", "Unable To Perform Operation");
}
// Always re-populate model and return the view in both success and error paths
addModelAttributes(model);
return "index";
```

### `addModelAttributes` Pattern (ParkingController)

```java
private void addModelAttributes(Model model) {
    List<ParkingSlot> all = repository.findAll();
    List<ParkingSlot> active = all.stream().filter(s -> s.getExitTime() == null).collect(Collectors.toList());
    List<ParkingSlot> history = all.stream().filter(s -> s.getExitTime() != null).collect(Collectors.toList());
    model.addAttribute("parking", new ParkingSlot());  // Fresh form backing object
    model.addAttribute("list", active);                // Active bookings for Occupied tab
    model.addAttribute("history", history);            // Completed bookings for History tab
    model.addAttribute("occupied", getOccupiedSlots(all));  // Comma-separated for JS slot grid
}
```

### QRCodeController URL Encoding Pattern

```java
return "redirect:/dashboard?ownerName=" + urlEncode(reg.getOwnerName())
        + "&vehicleNumber=" + urlEncode(reg.getVehicleNumber())
        + "&vehicleType=" + urlEncode(reg.getVehicleType())
        + "&mobileNumber=" + urlEncode(reg.getMobileNumber());
```

The `urlEncode()` helper must be called manually since `RedirectAttributes` is not used.

### Frontend Vehicle Number Format Pattern

```javascript
var formatMap = {
    'std1': { pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{1} [0-9]{4}$', example: 'KA 04 A 2256' },
    'std2': { pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$', example: 'DL 03 CA 1234' },
    'std3': { pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{3} [0-9]{4}$', example: 'ML 04 ASD 5256' },
    'bhr':  { pattern: '^[0-9]{2} BH [0-9]{4} [A-Z]{2}$', example: '26 BH 9876 AB' }
};
```

This map is replicated in 3 places: `index.html` (dashboard form), `qrcode.html` (QR generation form), and `app.js` (as `VEHICLE_FORMATS`). When adding new formats, update ALL 3 locations.

---

## Testing Conventions

### WebMvcTest (Controller Tests)

```java
@WebMvcTest(YourController.class)
class YourControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private YourRepository yourRepo;

    @Test
    void endpoint_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/endpoint"))
                .andExpect(view().name("login"));
    }

    @Test
    void endpoint_WithSession_ShouldWork() throws Exception {
        when(yourRepo.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/endpoint").sessionAttr("admin", "QSparker"))
                .andExpect(status().isOk())
                .andExpect(view().name("expectedView"))
                .andExpect(model().attributeExists("expectedAttr"));
    }
}
```

**IMPORTANT:** When testing endpoints with `@RequestParam` parameters, always include the required params in the request - otherwise Spring throws `MissingServletRequestParameterException` (400) before the session check runs.

```java
// WRONG - fails with 400 before session check
mockMvc.perform(post("/qrcode/generate"))
        .andExpect(status().isUnauthorized());

// RIGHT - include required params
mockMvc.perform(post("/qrcode/generate")
        .param("vehicleNumber", "KA 01 AB 1234")
        .param("ownerName", "Test")
        .param("vehicleType", "Car")
        .param("mobileNumber", "9876543210"))
        .andExpect(status().isUnauthorized());
```

### DataJpaTest (Repository Tests)

```java
@DataJpaTest
class YourRepositoryTest {
    @Autowired private YourRepository repo;

    @Test
    void customMethod_ShouldReturnExpected() {
        // Create and save entity
        // Call repository method
        // Assert with assertThat (AssertJ)
    }
}
```

### Validation Tests

```java
// Manual validation without Spring context
class ParkingSlotValidationTest {
    private static Validator validator;
    @BeforeAll static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @Test void testSomePattern() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("KA 01 AB 1234");
        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");
        assertThat(violations).isEmpty();
    }
}
```

---

## Frontend Architecture

### CSS Design System (`styles.css`)

Uses CSS custom properties (design tokens) organized into:
- **Colors:** Gray scale (50-900), blue (50-700), green/red/amber (50-700)
- **Surfaces:** `--bg-body`, `--bg-card`, `--bg-sidebar`, `--bg-input`
- **Text:** `--text-primary`, `--text-secondary`, `--text-muted`
- **Borders:** `--border`, `--border-light`, `--border-input`
- **Shadows:** `--shadow-sm` through `--shadow-xl`
- **Dark mode:** `.dark` class on `<body>` overrides all surface/text/border tokens
- **Page background:** `#EAEAEA` (light gray)

**Dark mode:** Toggled via `toggleDarkMode()` in `app.js`. Persisted in `localStorage` key `qspark-dark-mode`. The initial load calls `applyDarkMode()` to restore the preference.

**Landing page (landing.html) design tokens:**
- **Primary color:** `#2a66e8` (blue) - buttons, links, section labels, architecture accent, "Popular" badge
- **Secondary accent:** `#C90213` (red) - not used on landing (reserved for future accents)
- **Page background:** `#EAEAEA`
- **Text:** `#000000` for all headings and body; `rgba(0,0,0,0.5)` for muted/secondary text
- **Font:** Inter only
- **Nav:** Floating pill, glassmorphism (`rgba(255,255,255,0.55)` + 24px blur), `#2a66e8` login button
- **Hero:** Full-width `hero-banner.webp` image (1920x1080), centered with max-width 1200px, 20px border-radius, 3px top padding
- **Shadows:** Neutral `rgba(0,0,0,..)` - no colored glows
- **Stats section:** Light `#EAEAEA` background with black stat numbers and grey dividers
- **Architecture diagram:** Accent box `#2a66e8`, borders `#EAEAEA`, text `#000000`
- **Feature cards:** All icon backgrounds unified `#EAEAEA` with `#2a66e8` SVG strokes
- **QR section:** Content wrapped in white card (`border-radius:24px`, shadow) inside `#EAEAEA` section
- **FAQ:** Accordion with blue `#2a66e8` questions, white cards, expand/collapse toggle
- **Footer:** Black `#000000` background, floating appearance (`border-radius:24px 24px 0 0`, shadow), logo-only branding

### JS Utilities (`app.js`)

- **Toast:** `Toast.success(msg)`, `Toast.error(msg)`, `Toast.info(msg)` - auto-dismissing notifications with optional undo callback
- **Modal:** `Modal.confirm({title, message, confirmText, confirmClass, onConfirm, onCancel})` - trap focus, escape key dismiss, overlay click dismiss
- **Table filtering:** `filterTable(tableId, inputId)` - real-time client-side search across all columns
- **Table sorting:** `makeSortable(tableId)` + click-to-sort with asc/desc indicators
- **CSV Export:** `exportCSV(tableId, filename)` - exports visible (non-filtered) rows
- **Slot Grid:** `buildSlotGrid(basement, occupiedSlots, gridId, hiddenId, statusId)` - builds clickable 15-column grid
- **Quick Jump:** `quickJumpSlot()` - type a slot number to auto-select or highlight

### Key Frontend States Every Page Must Handle

1. **Loading** - Initial page load shows skeleton/spinner (defined in CSS as `.skeleton` classes)
2. **Empty** - Empty state with icon, title, and description (e.g., "No active bookings")
3. **Error** - Error alerts (`.alert-error`) and error states with retry option
4. **Success** - Success alerts (`.alert-success`) and toast notifications
5. **Edge Cases** - Mobile responsive breakpoints at 1024px, 768px, 480px; dark mode

### Tab System (Dashboard & QR pages)

Uses simple JS tab switching with `display: none` / `display: block` via `.tab-panel.active`. No router. Sidebar and tab-bar both trigger the same `showTab()` function. URL params can deep-link: `?tab=occupied` or `?tab=history`.

---

## Build & Run Commands

```bash
# Compile
mvnw.cmd compile

# Run with H2 (no MySQL needed) - QUOTE the -D arg
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"

# Run with MySQL (default profile)
mvnw.cmd spring-boot:run

# Run all tests (62 tests)
mvnw.cmd test

# Run a single test class
mvnw.cmd test -Dtest=QRCodeControllerTest

# Clean stale target/ (needed when switching branches/projects)
mvnw.cmd clean

# Quick launcher (Windows)
double-click QSpark.bat
```

---

## Common Tasks - Step by Step

### Adding a New Entity

1. Create model class in `model/` package with `@Entity`, `@Table`, fields, getters/setters
2. Create repository interface in `repository/` extending `JpaRepository<Entity, IdType>`
3. Add admin seed to both `data.sql` (INSERT IGNORE) and `data-h2.sql` (MERGE INTO) if needed
4. Create tests: `@DataJpaTest` for repository, validation tests if `@Pattern` or `@NotNull` used
5. Controller test: `@WebMvcTest(YourController.class)` with `@MockBean` for the new repository

### Adding a New Endpoint

1. Add method to appropriate controller with `@GetMapping`/`@PostMapping`
2. Start with session guard: `if (session.getAttribute("admin") == null) return "login";`
3. Add model attributes needed by the view
4. Add test: without session -> redirects to login; with session -> returns expected view

### Adding a New Template

1. Create in `src/main/resources/templates/`
2. Include Thymeleaf namespace: `<html xmlns:th="http://www.thymeleaf.org">`
3. Reference shared CSS: `<link rel="stylesheet" href="/css/styles.css">`
4. Reference shared JS: `<script src="/js/app.js"></script>`
5. Include toast container: `<div id="toastContainer" class="toast-container"></div>`
6. Dark mode toggle button uses `onclick="toggleDarkMode()"`, live clock uses `id="liveDate"` and `id="liveTime"`

### Debugging a Failing Test

1. Run the specific test: `mvnw.cmd test -Dtest=FailingTestName`
2. Common failure: missing `@MockBean` for a repository the controller uses
3. Common failure: missing `@Autowired MockMvc`
4. Common failure: missing required `@RequestParam` in except block (always include params you're testing around)
5. Check target/surefire-reports/ for detailed XML output

---

## Database Profiles

| Profile | Config File | Seed File | Dialect | Console |
|---------|------------|-----------|---------|---------|
| default (MySQL) | `application.properties` | `data.sql` (INSERT IGNORE) | MySQL | No |
| h2 | `application-h2.properties` | `data-h2.sql` (MERGE INTO) | H2 | `/h2-console` |

**Never delete or rename `data-h2.sql`** - it's the only admin seed for H2. **Never use `INSERT IGNORE` in H2** and **never use `MERGE INTO` in MySQL** - they'll fail silently or loudly.

---

## Known Gotchas & Warnings

### Critical (Will Break Things)

- **`-Dspring-boot.run.profiles=h2` MUST be quoted** when passed to `mvnw.cmd` from PowerShell or cmd. Without quotes, Maven parses it as a lifecycle phase name: `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"`
- **Dark mode localStorage key is `qspark-dark-mode`** - `app.js` uses this key. `fragments.html` (now deleted - it was dead code) previously used different keys causing persistence issues.
- **Vehicle format map is duplicated in 3 places.** When adding/editing formats, update: `index.html` (dashboard booking form), `qrcode.html` (QR generation form), and `app.js` (as `VEHICLE_FORMATS`).
- **ParkingSlot uses `Integer` for ID, VehicleRegistration uses `Long`.** Don't change without a full migration plan.

### Medium (Common Mistakes)

- `data.sql` uses MySQL `INSERT IGNORE` - will **fail** on H2. The H2 profile loads only `data-h2.sql`.
- `target/` may contain stale classes from the old `com.peterparker` project - always `mvn clean` first when switching.
- **Login now redirects to `/dashboard`** after successful auth (was rendering `"index"` directly). If adding a new auth mechanism, follow the redirect pattern, not the old manual model-population pattern.
- **`@Valid` + `BindingResult` is now required on `update()`** - same as `save()`. The update endpoint previously skipped validation.
- No Spring Security CSRF protection - sessions are the only auth layer. If adding forms, no CSRF token needed.
- All controller views are server-rendered Thymeleaf templates (no SPA routing) - refreshing a page re-renders from the server.
- `GET /delete/{id}` is used with a GET request (bad practice but established convention) - use confirmation dialog (`confirmDelete()`) to prevent accidental deletes.
- Camera scanning requires `localhost` or HTTPS - deployed on HTTP-only servers, the camera won't work. Documented in README.



## History of Recent Fixes

| Date | What Changed | Why |
|------|-------------|-----|
| Jul 2026 | `README.md` updated | Spring Boot version (4.0 -> 3.3.1), slot count (200 -> 480), tech stack cleanup |
| Jul 2026 | Dark mode key unified | `fragments.html` used `sppn-dark-mode`, `app.js` used `qpark-dark-mode` - dark mode didn't persist across pages |
| Jul 2026 | `pom.xml` cleaned | Empty `<url/>`, `<licenses/>`, `<developers/>`, `<scm/>` tags removed |
| Jul 2026 | `.gitignore` expanded | Added `.DS_Store`, `Thumbs.db`, `*.log`, `*.swp`, `*.local.properties` |
| Jul 2026 | `.gitattributes` rewritten | Proper Java/Maven line-ending rules, binary file types, diff patterns |
| Jul 2026 | `QRCodeControllerTest.java` added | 7 tests covering session guard, generate, scan, and list endpoints |
| Jul 2026 | `VehicleRegistrationRepositoryTest.java` added | 6 tests covering CRUD, exists check, case sensitivity |
| Jul 2026 | `landing.html` redesigned | QR phone mockup showcase, IntersectionObserver animations, gradient stats, "Popular" badge, architecture diagram with QRController, mobile responsiveness improvements |
| Jul 2026 | **Bug fix: fragments.html dark mode class** | Used `dark-mode` CSS class but styles.css uses `.dark` - fixed to use `.dark` |
| Jul 2026 | **Bug fix: @Valid added to update()** | `ParkingController.update()` was missing `@Valid` + `BindingResult` - vehicle number format not enforced on updates |
| Jul 2026 | **Bug fix: delete() existence check** | `delete()` would throw `EmptyResultDataAccessException` on non-existent IDs - added `existsById()` check |
| Jul 2026 | **Bug fix: /qrcode/list session guard** | List endpoint was public (anyone could see all registered vehicles) - added 401 for unauthenticated |
| Jul 2026 | **Bug fix: login redirects to /dashboard** | Login success now redirects to `/dashboard` instead of manually loading model data (fixes missing `history` attribute on first login) |
| Jul 2026 | **Bug fix: removed dead form elements** | Non-functional "Remember me" checkbox and "Forgot password" link removed from login.html |
| Jul 2026 | **Code quality: removed fragments.html** | Dead code - no template used `th:include`/`th:replace` with it |
| Jul 2026 | **Tests expanded: 54 → 62** | Added 8 new tests: invalid vehicle number on save, duplicate vehicle, scanned params, update validation, delete non-existent, left error path, edit invalid, QR list auth |
| Jul 2026 | **Landing page redesigned (v2)** | Dark luxury aesthetic - gold/black theme, Sora + DM Sans fonts, animated QR scan line, geometric dot pattern, staggered reveals, light feature section for contrast |
| Jul 2026 | **Landing page redesigned (v3)** | Clean light theme: #C90213 primary, #EAEAEA background, Inter font only, floating pill nav, all headings #111111 black |
| Jul 2026 | **Hero replaced with hero-banner.webp** | Removed all hero text, cards, and buttons - full-width background image only (1920×1080, 121KB, WebP) |
| Jul 2026 | **Colored shadows removed** | All red glow effects replaced with neutral `rgba(0,0,0,..)` shadows across the landing page |
| Jul 2026 | **Architecture diagram redesigned** | Now shows accurate 3-tier stack (Controller/Repository/Entity layers) with restricted palette: #CED0D1, black, white, #EAEAEA, minimal #C90213 |
| Jul 2026 | **em dashes replaced** | All `&mdash;` occurrences replaced with `-` or `,` in landing.html |
| Jul 2026 | **File counts updated** | Accurate line counts synced across all source, test, template, and static files |
| Jul 2026 | **Color palette overhaul** | Replaced #C90213 primary with #2a66e8 blue; all text #000000; muted text rgba(0,0,0,0.5); unified feature icons to #EAEAEA bg + #2a66e8 strokes; lighter stats section; blue architecture accent |
| Jul 2026 | **FAQ section added** | 8 accordion-style questions with blue toggle icons, white cards, #EAEAEA borders |
| Jul 2026 | **Footer redesigned** | Black #000000 bg, floating appearance with border-radius and shadow, logo-only branding (no text) |
| Jul 2026 | **QR section carded** | All QR content + phone mockup wrapped in white div with border-radius 24px and shadow |
| Jul 2026 | **Nav glassmorphism** | Background lowered to rgba(255,255,255,0.55), blur 24px, border softened |
| Jul 2026 | **Bug fix: left() not-found handling** | `left()` returned success even when ID didn't exist - now returns error message |
| Jul 2026 | **Bug fix: README.md license (MIT → GPL-3.0)** | Badge and text said MIT but real license is GPLv3 |
| Jul 2026 | **Code quality: vehicle format centralized** | Format map was duplicated in 3 places (index.html, qrcode.html, app.js) - now populated dynamically from single source in app.js with `populateFormatSelect()` |
| Jul 2026 | **Code quality: SecurityConfig dead code** | `contentTypeOptions(customizer -> {})` was a no-op - replaced with `Customizer.withDefaults()` |
| Jul 2026 | **Code quality: *.webp in .gitattributes** | WebP binary files were missing from git LFS/binary config |
| Jul 2026 | **Code quality: AGENTS.md updated** | Auth documentation updated from old session-based pattern to Spring Security; test count corrected (62 → 63); LoginIntegrationTest added to file inventory |

---

## Design Principles for Making Changes

1. **Match existing patterns.** Every controller follows the same try/catch -> model-add -> return-view pattern. Auth is handled declaratively by Spring Security before the controller method is reached. Don't invent a new pattern.
2. **No frontend frameworks.** All JS is vanilla. All CSS is hand-written with custom properties. No Tailwind, Bootstrap, React, Vue, or Alpine.
3. **Server-rendered first.** JS is progressive enhancement. The app must work (with degraded UX) without JS.
4. **Thymeleaf templates are complete HTML.** Not partials/components. Each page is a full `<!DOCTYPE html>` document.
5. **Dark mode is a design constraint.** Every new UI element needs a dark mode variant (`body.dark .your-class`).
6. **Test every new endpoint.** At minimum: without session -> login redirect + with session -> success. Test error/edge cases too.
7. **Both databases must work.** MySQL is production, H2 is dev/testing. SQL seed files must be compatible with their respective databases.
8. **No secrets in code.** Database passwords and API keys should be referenced via environment variables or external config (though the current codebase has the MySQL password hardcoded for dev convenience).
