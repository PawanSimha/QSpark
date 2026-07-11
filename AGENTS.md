# QSpark - Agent Guide

## Project Identity

QSpark is a **production-grade smart parking management system** built with Java 17, Spring Boot 3.3.1, Thymeleaf, and MySQL/H2. It manages 480 parking slots across 4 basements with Spring Security auth (BCrypt), QR code vehicle identification, full CRUD operations, and an audit trail.

**Port:** 8081 (Jenkins occupies 8080 on this dev machine)
**Login:** `QSparker` / `QSpark@Secure`

---

## Mental Model - How the System Works

The system is a **server-rendered MVC monolith** with Thyme leaf templates and vanilla JavaScript enhancement. There is no SPA, no React, no frontend framework. Every navigation is a full page load.

```
Browser (HTML + CSS + JS) → Spring Boot Controller → Repository (JPA) → Database
                                                                         ↓
Thymeleaf renders HTML ← Controller adds model data ← Repository returns entities
         ↓
Browser JS enhances: live clock, slot grid, table search/filter, QR scanner, dark mode
```

**Key concept:** The server is in full control. Spring Security guards all endpoints declaratively. Controllers pass model data to Thymeleaf for server-side rendering. JavaScript only adds convenience features on top — it never replaces server-side logic.

---

## File Inventory (Every Source File)

### Java Source - `src/main/java/com/qspark/`

| File | Lines | Purpose |
|------|------:|---------|
| `QSparkApplication.java` | 26 | Entry point. `@SpringBootApplication` with `SpringApplication.run()`. |
| `config/AdminDetailsService.java` | 42 | Spring Security `UserDetailsService` — loads admin by username from DB. |
| `config/DataInitializer.java` | 41 | `CommandLineRunner` — seeds admin user with BCrypt-hashed password at startup. |
| `config/SecurityConfig.java` | 85 | Security filter chain, BCrypt encoder, endpoint permissions, CSRF, headers. |
| `controller/LoginController.java` | 31 | Handles landing page (`/`), login page (`GET /login`). |
| `controller/ParkingController.java` | 211 | Dashboard (`GET /dashboard`), create (`POST /save`), edit (`GET /edit/{id}`), update (`POST /update`), mark left (`POST /left/{id}`), delete (`POST /delete/{id}`). |
| `controller/QRCodeController.java` | 101 | QR page (`GET /qrcode`), generate QR PNG (`POST /qrcode/generate`), scan redirect (`POST /qrcode/scan`), JSON list (`GET /qrcode/list`). |
| `model/ParkingSlot.java` | 100 | `@Entity` - id, ownerName, vehicleNumber (with `@Pattern`), vehicleType, slotNumber, mobileNumber (with `@Pattern`), entryTime, exitTime. |
| `model/Admin.java` | 48 | `@Entity` - id, username (unique), password. |
| `model/VehicleRegistration.java` | 57 | `@Entity` - id (Long), vehicleNumber (unique, not null), ownerName, vehicleType, mobileNumber, createdAt. |
| `repository/AdminRepository.java` | 24 | `findByUsername(username)` returns `Optional<Admin>`. |
| `repository/ParkingRepository.java` | 24 | `findBySlotNumber()`, `existsByVehicleNumberAndExitTimeIsNull()`. |
| `repository/VehicleRegistrationRepository.java` | 25 | `findByVehicleNumber()` returns `Optional`, `existsByVehicleNumber()`. |

### Test Source - `src/test/java/com/qspark/`

| File | Lines | Tests | Purpose |
|------|------:|:-----:|---------|
| `QSparkApplicationTests.java` | 26 | 1 | Context loads successfully. |
| `controller/LoginControllerTest.java` | 48 | 2 | WebMvcTest: landing page, login page access. |
| `controller/LoginIntegrationTest.java` | 66 | 4 | Full-stack login flow with H2: valid/invalid login, logout, landing. |
| `controller/ParkingControllerTest.java` | 335 | 24 | Dashboard, save (valid/duplicate/error/invalid-vehicle), delete (valid/invalid), left (valid/invalid/error), edit (valid/invalid), update (valid/invalid/error/occupied-slot), scanned params. |
| `controller/QRCodeControllerTest.java` | 112 | 8 | QR page, generate, scan (unregistered/registered), list with/without auth. |
| `model/ParkingSlotValidationTest.java` | 136 | 14 | Vehicle number regex (4 valid, 4 invalid) + mobile number validation (4 + 1 null). |
| `repository/AdminRepositoryTest.java` | 62 | 4 | Find by username (exists/not found/no match), persist. |
| `repository/ParkingRepositoryTest.java` | 59 | 3 | Find by slot number (exists/null), persist. |
| `repository/VehicleRegistrationRepositoryTest.java` | 66 | 6 | Find by number (exists/empty), exists check, save with createdAt, case sensitivity. |

**Total: 66 tests** - all pass with `mvnw.cmd test`.

### Templates - `src/main/resources/templates/`

| File | Lines | Purpose |
|------|------:|---------|
| `landing.html` | 1074 | Marketing page: full-width hero, floating pill nav (logo left/links center/CTA right), dark mode toggle, feature cards, QR phone mockup, architecture pipeline, developer profiles, FAQ accordion, footer. Inter font, blue #2563EB primary, #EAEAEA bg, IntersectionObserver animations. |
| `login.html` | 307 | Auth card: 2-column layout with logo + form + error shake animation + dark mode toggle. Standalone CSS (dark mode supported via styles.css + app.js). |
| `index.html` | 452 | Dashboard: sidebar + topbar (dark mode, scan, clock, logout), 3-tab layout (Booking form + 4x120 slot grid / Occupied table / History table), search/sort/export/pagination, confirm modals for delete/left. |
| `edit.html` | 200 | Edit booking: cascading basement/slot dropdown, vehicle number formatting, mobile validation, CSRF hidden input. |
| `qrcode.html` | 362 | Two-tab: Generate QR (format selector + AJAX download) / Scan QR (html5-qrcode camera + auto-redirect), registered vehicles table with client-side refresh. |

### Static - `src/main/resources/static/`

| File | Lines | Purpose |
|------|------:|---------|
| `css/styles.css` | 1193 | Full design system: 50+ CSS custom properties (colors, surfaces, text, shadows), 60+ component classes (buttons, inputs, cards, tables, toasts, modals, slot grid, tabs, pagination, alerts), dark mode via `body.dark`, responsive breakpoints at 1024/768/480px. |
| `js/app.js` | 508 | Toast notifications, Modal dialogs, dark mode toggle (`qspark-dark-mode` localStorage), live clock, table filtering, pagination, CSV export, vehicle format helpers (`VEHICLE_FORMATS`/`populateFormatSelect`/`setupVehicleFormat`), slot grid builder, quick-jump. |
| `images/pawan-simha.webp` | — | Developer avatar (landing page). |
| `images/prajwal-r.webp` | — | Developer avatar (landing page). |

### Config Files

| File | Purpose |
|------|---------|
| `application.properties` | MySQL connection (`jdbc:mysql://localhost:3306/qspark`), JPA ddl-auto=update, port 8081, Thymeleaf cache=false. |
| `application-h2.properties` | H2 in-memory (`jdbc:h2:mem:qspark`), H2 console enabled, port 8081. |
| `data.sql` | 0 bytes — seeding handled by DataInitializer.java. |
| `data-h2.sql` | 0 bytes — seeding handled by DataInitializer.java. |
| `pom.xml` | Spring Boot 3.3.1, Java 17, 12 dependencies (data-jpa, thymeleaf, web, mysql-connector-j, h2, validation, zxing 3.5.3, spring-security, test). |
| `QSpark.bat` | Windows launcher: checks port 8081, starts mvnw with H2 via `start /B` in same window, polls for ready, opens browser, cleans up on keypress. |
| `.gitignore` | Ignores HELP.md, backend.log, target/, build/, .DS_Store, Thumbs.db, IDE files, *.local.properties, .opencode/. |
| `.gitattributes` | Auto LF normalization, diff for java/xml/html/css/js/md/properties, mvnw=LF, .bat/.cmd=CRLF, binaries (jar/png/webp/ico/jpg)=binary. |

---

## Request Lifecycle (Detailed)

### Example: Creating a Booking

1. Admin clicks "Confirm Booking" in `index.html`
2. `prepareSubmit()` JS function validates slot is selected, enables disabled form fields
3. Form POST to `/save` with ownerName, vehicleNumber, vehicleType, slotNumber, mobileNumber + CSRF token
4. `ParkingController.save()` checks:
   - Spring Security ensures the user is authenticated before reaching the controller
   - Validation: `@Valid` triggers `@Pattern` regex on vehicleNumber + mobileNumber → returns to index with error if invalid
   - Slot availability: `findBySlotNumber()` with `exitTime == null` → "Slot Already Booked!"
   - Vehicle duplicate: `existsByVehicleNumberAndExitTimeIsNull()` → "Vehicle already parked in another slot!"
5. Sets `entryTime = LocalDateTime.now()`, saves via `repository.save()`
6. Re-fetches all records, splits into active (`exitTime == null`) and history (`exitTime != null`)
7. Returns `"index"` view with model attributes: `parking` (new empty form), `list` (active), `history` (completed), `occupied` (comma-separated occupied slot numbers)
8. Thymeleaf renders `index.html` with the updated data
9. Browser JS rebuilds the slot grid showing newly occupied slot in red

### Example: QR Scan Flow

1. Admin goes to `/qrcode` → clicks Scan tab → clicks "Start Camera"
2. `startScanner()` JS creates `Html5Qrcode` instance with rear camera
3. On successful scan, JS creates a hidden form and submits POST to `/qrcode/scan` with `vehicleNumber`
4. `QRCodeController.scanQR()`:
   - Spring Security ensures the user is authenticated
   - Formats vehicle number to uppercase
   - Looks up `VehicleRegistrationRepository.findByVehicleNumber()`
   - If not found → returns to qrcode view with error "Vehicle not registered"
   - If found → redirects to `/dashboard` with URL parameters (ownerName, vehicleNumber, vehicleType, mobileNumber)
5. `ParkingController.dashboard()` receives URL params, adds them as `scanned*` model attributes
6. `applyScannedVehicle()` JS auto-fills and locks the form fields — admin only needs to pick a slot

### Example: Authentication

1. GET `/login` → `LoginController.loginPage()` returns `login.html`
2. POST `/login` with username + password → Spring Security's form login filter handles auth
3. `AdminDetailsService.loadUserByUsername()` fetches user via `AdminRepository.findByUsername()`
4. `BCryptPasswordEncoder` validates the password against the stored hash
5. If match → Spring Security creates the security context, redirects to `/dashboard`
6. If no match → returns login with `model.addAttribute("error", "Invalid Username or Password")`
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
| `mobile_number` | `VARCHAR` | Validated by `@Pattern` (10 digits) |
| `entry_time` | `DATETIME` | Set server-side via `LocalDateTime.now()` on booking |
| `exit_time` | `DATETIME` | `NULL` while active; set on "Left" action |

### `vehicle_registration` table
| Column | Type | Constraints |
|--------|------|:-----------:|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `vehicle_number` | `VARCHAR` | UNIQUE, NOT NULL |
| `owner_name` | `VARCHAR` | — |
| `vehicle_type` | `VARCHAR` | Same 6 types |
| `mobile_number` | `VARCHAR` | 10 digits |
| `created_at` | `DATETIME` | Set via `LocalDateTime.now()` in constructor |

### Entity Relationships
- No JPA `@OneToMany`/`@ManyToOne` relationships exist — all lookups are explicit via repository methods
- `ParkingSlot` uses `Integer` for ID; `VehicleRegistration` uses `Long` for ID — intentional but inconsistent
- `ParkingSlot.vehicleNumber` and `VehicleRegistration.vehicleNumber` are independent strings (no FK constraint)

---

## Spring Security Architecture

### Filter Chain (SecurityConfig)
- All paths require authentication except `/`, `/login`, `/css/**`, `/js/**`, `/images/**`, `/logo.webp`, `/favicon.ico`, `/hero-banner.webp`
- `/qrcode/generate` and `/qrcode/list` return 401 (for API-like use)
- Form login at `/login` with default success redirect to `/dashboard`
- Logout via POST `/logout`
- CSRF enabled on all state-changing endpoints (POST, PUT, DELETE, PATCH)
- Security headers: XSS protection (`ENABLED_MODE_BLOCK`), content-type sniffing disabled (`nosniff`), frame deny, referrer-policy same-origin

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

### Auth Pattern (Spring Security — No Manual Session Checks)

Auth is managed declaratively through `SecurityConfig.java` filter chain. Controllers never check `session.getAttribute("admin")` — Spring Security handles it before the controller method is reached.

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
- Every POST form must include a CSRF token
- AJAX POST requests need `X-CSRF-TOKEN` header from the meta tag

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

The `VEHICLE_FORMATS` object in `app.js` defines all supported formats. It is populated into `<select id="vehicleFormat">` via `populateFormatSelect()` on index.html and qrcode.html. The `setupVehicleFormat()` function binds the select to the vehicle number input (readonly until format chosen, auto-validates on input).

```javascript
var VEHICLE_FORMATS = {
    'std1': { label: 'Standard (1-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{1} [0-9]{4}$', example: 'KA 04 A 2256' },
    'std2': { label: 'Standard (2-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$', example: 'DL 03 CA 1234' },
    'std3': { label: 'Standard (3-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{3} [0-9]{4}$', example: 'ML 04 ASD 5256' },
    'bhr':  { label: 'Bharat Series', pattern: '^[0-9]{2} BH [0-9]{4} [A-Z]{2}$', example: '26 BH 9876 AB' }
};
```

**Source of truth:** `app.js` only. `index.html` and `qrcode.html` call `populateFormatSelect()` to build the dropdown dynamically. When adding new formats, update `VEHICLE_FORMATS` in `app.js` only.

---

## Testing Conventions

### WebMvcTest (Controller Tests)

```java
@WebMvcTest(YourController.class)
@Import(SecurityConfig.class)
class YourControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private YourRepository yourRepo;
    @MockBean private AdminDetailsService adminDetailsService;

    @Test
    void endpoint_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpoint_WithSession_ShouldWork() throws Exception {
        when(yourRepo.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/endpoint").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("expectedView"))
                .andExpect(model().attributeExists("expectedAttr"));
    }
}
```

**IMPORTANT:** Always include `@Import(SecurityConfig.class)` and `@MockBean AdminDetailsService` for WebMvcTest controller tests. For POST endpoints, include `.with(csrf())`.

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
class ParkingSlotValidationTest {
    private static Validator validator;
    @BeforeAll static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @Test void testValidPattern() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("KA 01 AB 1234");
        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");
        assertThat(violations).isEmpty();
    }
}
```

---

## Frontend Architecture

### CSS Design System (`styles.css` — 1193 lines)

Uses CSS custom properties (design tokens) organized into:
- **Colors:** Gray scale (50-900), blue (50-700: `#2563EB`/`#1d4ed8`), green/red/amber (50-700)
- **Surfaces:** `--bg-body` (#EAEAEA), `--bg-card`, `--bg-sidebar`, `--bg-input`
- **Text:** `--text-primary`, `--text-secondary`, `--text-muted`
- **Borders:** `--border`, `--border-light`, `--border-input`
- **Shadows:** `--shadow-sm` through `--shadow-xl`
- **Dark mode:** `body.dark` overrides 19 custom properties (surfaces, text, borders, shadows)
- **Responsive:** 1024px (2-col), 768px (sidebar→top bar, single column), 480px (compact)

**Component classes:** buttons (9 variants), inputs, cards, stats, tables, actions, toasts (3 types), modals, pagination, slot grid (15-column), basement tabs, legend, badges (4 colors), alerts (2 types), forms, empty states, utilities.

### JS Utilities (`app.js` — 508 lines)

- **Toast:** `Toast.success(msg)`, `Toast.error(msg)`, `Toast.info(msg)` — auto-dismissing notifications
- **Modal:** `Modal.confirm({title, message, confirmText, confirmClass, onConfirm, onCancel})` — trap focus, escape/overlay dismiss
- **Dark mode:** `toggleDarkMode()` / `applyDarkMode()` — persisted in localStorage key `qspark-dark-mode`
- **Table filtering:** `filterTable(tableId, inputId)` — real-time client-side search across all columns
- **Pagination:** `paginateTable(tableId, rowsPerPage)` — with page buttons and info
- **CSV Export:** `exportCSV(tableId, filename)` — exports visible (non-filtered) rows
- **Slot Grid:** `buildSlotGrid(basement, occupiedSlots, gridId, hiddenId, statusId)` — builds clickable 15-column grid
- **Quick Jump:** `quickJumpSlot()` — type a slot number to auto-select or highlight
- **Vehicle Formats:** `VEHICLE_FORMATS` object, `populateFormatSelect()`, `setupVehicleFormat()` — centralized format handling
- **Live Clock:** `updateClock()` — updates `#liveDate` and `#liveTime` every second

### Key Frontend States Every Page Must Handle

1. **Loading** — Skeleton/spinner (CSS classes available but not currently used)
2. **Empty** — Empty state with icon, title, and description (e.g., "No active bookings")
3. **Error** — Error alerts (`.alert-error`) and toast notifications
4. **Success** — Success alerts (`.alert-success`) and toast notifications
5. **Edge Cases** — Mobile responsive breakpoints at 1024px, 768px, 480px; dark mode on all authenticated pages + landing + login

### Tab System (Dashboard & QR pages)

Simple JS tab switching with `display: none` / `display: block` via `.tab-panel.active`. No router. Sidebar and tab-bar both trigger the same `showTab()` function. URL deep-linking: `?tab=occupied`, `?tab=history`, `?scan=1`.

---

## Build & Run Commands

```bash
# Compile
mvnw.cmd compile

# Run with H2 (no MySQL needed) — QUOTE the -D arg
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"

# Run with MySQL (default profile)
mvnw.cmd spring-boot:run

# Run all tests (66 tests)
mvnw.cmd test

# Run a single test class
mvnw.cmd test -Dtest=QRCodeControllerTest

# Clean stale target/ (needed when switching branches/projects)
mvnw.cmd clean

# Quick launcher (Windows) — single terminal window
double-click QSpark.bat
```

---

## Common Tasks — Step by Step

### Adding a New Entity

1. Create model class in `model/` package with `@Entity`, `@Table`, fields, getters/setters
2. Create repository interface in `repository/` extending `JpaRepository<Entity, IdType>`
3. No SQL seed needed — seeding is done exclusively by `DataInitializer.java`
4. Create tests: `@DataJpaTest` for repository, validation tests if `@Pattern` or `@NotNull` used
5. Controller test: `@WebMvcTest(YourController.class)` with `@MockBean` for the new repository + `@MockBean AdminDetailsService` + `@Import(SecurityConfig.class)`

### Adding a New Endpoint

1. Add method to appropriate controller with `@GetMapping`/`@PostMapping`
2. Auth is handled by Spring Security declaratively — no manual session checks needed
3. Add model attributes needed by the view
4. Add test: without auth → returns 401; with auth (`with(user(...).roles("ADMIN"))`) → returns expected view

### Adding a New Template

1. Create in `src/main/resources/templates/`
2. Include Thymeleaf namespace: `<html xmlns:th="http://www.thymeleaf.org">`
3. Reference shared CSS: `<link rel="stylesheet" href="/css/styles.css?v=2">`
4. Reference shared JS: `<script src="/js/app.js"></script>`
5. Include toast container: `<div id="toastContainer" class="toast-container"></div>`
6. Add dark mode toggle button: `<button id="darkModeToggle" onclick="toggleDarkMode()" class="btn btn-ghost btn-icon" title="Toggle dark mode">`
7. Add live clock if needed: `id="liveDate"` and `id="liveTime"`

### Debugging a Failing Test

1. Run the specific test: `mvnw.cmd test -Dtest=FailingTestName`
2. Common failure: missing `@MockBean` for a repository the controller uses
3. Common failure: missing `@Import(SecurityConfig.class)` for WebMvcTest
4. Common failure: missing `@MockBean AdminDetailsService` for WebMvcTest
5. Common failure: missing `.with(csrf())` for POST endpoint tests
6. Check target/surefire-reports/ for detailed XML output

---

## Database Profiles

| Profile | Config File | Seed | Dialect | Console |
|---------|------------|------|---------|---------|
| default (MySQL) | `application.properties` | DataInitializer.java | MySQL | No |
| h2 | `application-h2.properties` | DataInitializer.java | H2 | `/h2-console` |

Both SQL seed files (`data.sql`, `data-h2.sql`) are 0-byte remnants — seed logic was migrated to `DataInitializer.java`. All seeding is done programmatically with BCrypt hashing.

---

## Known Gotchas & Warnings

### Critical (Will Break Things)

- **`-Dspring-boot.run.profiles=h2` MUST be quoted** when passed to `mvnw.cmd` from PowerShell or cmd. Without quotes, Maven parses it as a lifecycle phase name: `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"`
- **Dark mode localStorage key is `qspark-dark-mode`** — `app.js` uses this key consistently across all pages (landing, login, dashboard, edit, QR).
- **Vehicle format map is centralized in `app.js`.** When adding/editing formats, update `VEHICLE_FORMATS` in `app.js` only — `index.html` and `qrcode.html` populate dynamically via `populateFormatSelect()`.
- **ParkingSlot uses `Integer` for ID, VehicleRegistration uses `Long`.** Don't change without a full migration plan.

### Medium (Common Mistakes)

- `target/` may contain stale classes from Maven builds — always `mvn clean` first when switching branches.
- CSRF protection is enabled on all state-changing endpoints. Every form must include a CSRF token. AJAX POST requests need `X-CSRF-TOKEN` header from the meta tag.
- DELETE and LEFT now use POST (not GET). The `index.html` templates use hidden POST forms with CSRF. Confirmation dialogs submit the parent form.
- Camera scanning requires `localhost` or HTTPS — on HTTP-only servers, the camera won't work.
- When testing POST endpoints with `@WebMvcTest`, always include `.with(csrf())` on the request builder.
- Mobile number is validated server-side with `@Pattern(regexp="^[0-9]{10}$")` — both `save()` and `update()` check `BindingResult` for mobile field errors.

---

## History of Recent Fixes

| Date | What Changed | Why |
|------|-------------|-----|
| Jul 2026 | **Audit: DELETE/LEFT GET→POST** | GET endpoints performed state changes — changed to `@PostMapping` with CSRF-protected forms |
| Jul 2026 | **Audit: update() slot availability** | `update()` could move a booking to an already-occupied slot — added occupancy + duplicate-vehicle checks |
| Jul 2026 | **Audit: mobileNumber @Pattern** | No server-side validation for mobile number — added `@Pattern(regexp="^[0-9]{10}$")` on `ParkingSlot.mobileNumber` + field error handling |
| Jul 2026 | **Audit: dead code removed** | Removed unused `parkingRepo` from QRCodeController, unused `findByVehicleNumberContainingIgnoreCase` from ParkingRepository, dead CSS (skeleton, error-state, link, form-grid-3), dead JS (makeSortable, sortTableByColumn) |
| Jul 2026 | **Audit: blue color standardized** | `--blue-500: #2563EB`, `--blue-600: #1d4ed8` — landing/login dashboard now share the same palette |
| Jul 2026 | **Audit: dark mode on landing+login** | Added sun/moon toggle to landing nav and login form — now all pages support dark mode |
| Jul 2026 | **Audit: cache-busting fixed** | `landing.html` loaded styles.css without `?v=2` — added cache-busting query param |
| Jul 2026 | **Audit: documentation updated** | AGENTS.md: test count 62→66, data.sql/h2.sql content corrected, CSRF line fixed, session-check pattern removed, config/ files added to inventory |
| Jul 2026 | **README.md updated** | Spring Boot version (4.0 → 3.3.1), slot count (200 → 480), tech stack cleanup |
| Jul 2026 | **Dark mode key unified** | `fragments.html` used `sppn-dark-mode`, `app.js` used `qpark-dark-mode` — dark mode didn't persist across pages |
| Jul 2026 | **pom.xml cleaned** | Empty `<url/>`, `<licenses/>`, `<developers/>`, `<scm/>` tags removed |
| Jul 2026 | **.gitignore expanded** | Added `.DS_Store`, `Thumbs.db`, `*.log`, `*.swp`, `*.local.properties` |
| Jul 2026 | **.gitattributes rewritten** | Proper Java/Maven line-ending rules, binary file types, diff patterns |
| Jul 2026 | **QRCodeControllerTest added** | 7+1 tests covering session guard, generate, scan, and list endpoints |
| Jul 2026 | **VehicleRegistrationRepositoryTest added** | 6 tests covering CRUD, exists check, case sensitivity |
| Jul 2026 | **Bug fix: @Valid added to update()** | `ParkingController.update()` was missing `@Valid` + `BindingResult` — vehicle number format not enforced on updates |
| Jul 2026 | **Bug fix: delete() existence check** | `delete()` would throw `EmptyResultDataAccessException` on non-existent IDs — added `existsById()` check |
| Jul 2026 | **Bug fix: login redirects to /dashboard** | Login success now redirects to `/dashboard` instead of manually loading model data |
| Jul 2026 | **Bug fix: removed dead form elements** | Non-functional "Remember me" checkbox and "Forgot password" link removed from login.html |
| Jul 2026 | **Code quality: removed fragments.html** | Dead code — no template used `th:include`/`th:replace` with it |
| Jul 2026 | **Code quality: vehicle format centralized** | Format map was in 3 places — now populated dynamically from single `VEHICLE_FORMATS` in app.js |
| Jul 2026 | **Code quality: SecurityConfig dead code** | `contentTypeOptions(customizer -> {})` was a no-op — replaced with `Customizer.withDefaults()` |
| Jul 2026 | **Code quality: nav redesigned** | Logo left, links center, CTA right — wider nav with dark mode toggle and glassmorphism |

---

## Design Principles for Making Changes

1. **Match existing patterns.** Every controller follows the same try/catch → model-add → return-view pattern. Auth is handled declaratively by Spring Security. Don't invent a new pattern.
2. **No frontend frameworks.** All JS is vanilla. All CSS is hand-written with custom properties. No Tailwind, Bootstrap, React, Vue, or Alpine.
3. **Server-rendered first.** JS is progressive enhancement. The app must work (with degraded UX) without JS.
4. **Thymeleaf templates are complete HTML.** Not partials/components. Each page is a full `<!DOCTYPE html>` document.
5. **Dark mode is a design constraint.** Every new UI element needs a dark mode variant (`body.dark .your-class`).
6. **Test every new endpoint.** At minimum: without auth → 401 + with auth → success. Test error/edge cases too. Always include `@Import(SecurityConfig.class)` and `.with(csrf())` for POST endpoint tests.
7. **Both databases must work.** MySQL is production, H2 is dev/testing. Seeding is handled by `DataInitializer.java` — no SQL seed files needed.
8. **No secrets in code.** Database passwords and API keys should be referenced via environment variables.
