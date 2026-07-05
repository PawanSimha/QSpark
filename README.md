<div align="center">
  <img src="logo.png" alt="Q-Park Logo" width="90"/>
  <h1 align="center">Q-Park</h1>
  <p align="center"><i>Quick &amp; Smart Parking — Quick. Smart. Reliable.</i></p>

  <p align="center">
    <img src="https://img.shields.io/badge/Java-17-%23ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17"/>
    <img src="https://img.shields.io/badge/Spring_Boot-3.3.1-%236DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 3.3.1"/>
    <img src="https://img.shields.io/badge/Thymeleaf-3.1-%23005C0F?style=flat-square&logo=thymeleaf&logoColor=white" alt="Thymeleaf"/>
    <img src="https://img.shields.io/badge/MySQL-8.0-%234479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL 8.0"/>
    <img src="https://img.shields.io/badge/License-MIT-%23ccc?style=flat-square" alt="License MIT"/>
    <img src="https://img.shields.io/badge/Status-Production_Ready-%2322c55e?style=flat-square" alt="Production Ready"/>
  </p>
</div>

---

##  Product Strategy

**Problem:** Parking lots in commercial buildings suffer from manual logbooks, double-booking conflicts, no real-time slot visibility, and zero historical tracking — leading to wasted time, frustrated drivers, and administrative chaos.

**Solution:** Q-Park is a production-grade web application that digitizes parking management with session‑based admin access, a real‑time three‑tab dashboard, cascading slot selection across 200 bays, and full audit history. Built on Spring Boot with MySQL persistence and a clean vanilla‑JS frontend — no frameworks, no bloat.

---

##  Key Features

###  Admin Dashboard
| Feature | Detail |
|---------|--------|
| **Live Clock** | HH:MM:SS display synced to the browser in the header |
| **Three‑Tab Slider** | Booking (new entries), Occupied (active), History (completed) — CSS‑translated pages, zero navigation |
| **Instant Search** | Client‑side `filterTable()` matches across all columns in real time |
| **Column Sorting** | Sort by Vehicle Type, Slot Number, or Entry Time with Asc / Desc toggle |

###  Booking Management
| Feature | Detail |
|---------|--------|
| **200 Slots / 4 Basements** | B1–B4, 50 slots each; cascading dropdowns with occupied bays shown red & disabled |
| **6 Vehicle Types** | Car, Bike, Scooter, Auto Rickshaw, Electric Car, Electric Two Wheeler |
| **Indian Plate Auto‑Format** | Live JS mask enforces `KA 01 AB 1234` format; `@Pattern` backend validation as second layer |
| **10‑Digit Mobile** | `maxlength="10"` + JS strips every non‑numeric keystroke |
| **Auto Entry Time** | `LocalDateTime.now()` recorded server‑side on booking — no user input |
| **Duplicate Protection** | `findBySlotNumber()` checks `exitTime IS NULL` so freed slots can be re‑booked |

###  Operations & History
| Feature | Detail |
|---------|--------|
| **Left Button** | Green action per row calls `GET /left/{id}` → sets `exitTime = now` → record moves to History |
| **Full CRUD** | Create, Read, Update (edit form preserves original entry time), Delete |
| **Audit Trail** | History tab shows every completed parking record with entry time and exit time |

###  Security
| Feature | Detail |
|---------|--------|
| **Session‑Based Auth** | Every guarded endpoint checks `session.getAttribute("admin")` before execution |
| **Unique Admin** | `@Column(unique = true)` on username + `INSERT IGNORE` seed — no duplicate accounts |

---

##  Architecture

```
                    ┌──────────────────────────────────────┐
                    │          BROWSER (Client)             │
                    │  HTML5 · CSS3 · Vanilla JS · Thymeleaf│
                    └──────────────┬───────────────────────┘
                                   │  HTTP / Thymeleaf
                                   ▼
        ┌─────────────────────────────────────────────────────────┐
                 │              SPRING BOOT 3.3 (localhost:8081)            │
        │                                                          │
        │  ┌──────────────────────────────────────────────────┐    │
        │  │          Controller Layer                         │    │
        │  │  LoginController.java  │  ParkingController.java  │    │
        │  │  @GetMapping / @PostMapping · Session Guard       │    │
        │  └──────────────────────┬───────────────────────────┘    │
        │                         ▼                                │
        │  ┌──────────────────────────────────────────────────┐    │
        │  │          Repository Layer (Spring Data JPA)       │    │
        │  │  AdminRepository  │  ParkingRepository             │    │
        │  └──────────────────────┬───────────────────────────┘    │
        │                         ▼                                │
        │  ┌──────────────────────────────────────────────────┐    │
        │  │          Entity Layer (JPA / Hibernate)           │    │
        │  │  Admin · ParkingSlot                              │    │
        │  └──────────────────────┬───────────────────────────┘    │
        └─────────────────────────┼───────────────────────────────┘
                                  │ JDBC / Hibernate ORM
                                  ▼
                    ┌──────────────────────────┐
                    │   MySQL 8.0  /  H2        │
                    │   database: parkease      │
                    └──────────────────────────┘
```

**Request Lifecycle:**
1. Browser sends HTTP request to Spring Boot on `:8081`
2. Controller maps the URL, checks session auth, executes business logic
3. Repository auto‑generates SQL from method names (e.g. `findBySlotNumber()`)
4. Hibernate maps Java entities ↔ database rows
5. Thymeleaf renders HTML; JavaScript powers live clock, cascading dropdowns, instant search, sort, and auto‑format

---

##  Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot (embedded Tomcat) | 3.3.1 |
| **ORM** | Hibernate (via Spring Data JPA) | 6.x |
| **Template Engine** | Thymeleaf | 3.1 |
| **Validation** | Hibernate Validator (`spring-boot-starter-validation`) | — |
| **Primary Database** | MySQL | 8.0 |
| **Demo Database** | H2 (in‑memory, profile: `h2`) | 2.x |
| **Frontend** | HTML5 · CSS3 · Vanilla JavaScript | — |
| **Typography** | Inter (Google Fonts) | — |
| **Icons** | Font Awesome (CDN) | — |
| **Build Tool** | Maven (wrapped: `mvnw.cmd`) | 3.9+ |
| **Auth** | Session‑based (no JWT, no Spring Security) | — |

---

##  Project Structure

```
ParkEase/
├── pom.xml                              # Maven dependencies & build config
├── mvnw.cmd                             # Maven wrapper (Windows)
├── start_app.bat                        # One‑click launcher
├── logo.png                             # Q-Park brand logo
│
├── src/main/java/com/parkease/
│   ├── ParkEaseApplication.java         # @SpringBootApplication entry point
│   ├── controller/
│   │   ├── LoginController.java         # /login, /logout, landing page
│   │   └── ParkingController.java       # /dashboard, /save, /edit, /update, /delete, /left
│   ├── model/
│   │   ├── Admin.java                   # @Entity — id, username, password
│   │   └── ParkingSlot.java             # @Entity — 8 fields, entry/exit timestamps
│   └── repository/
│       ├── AdminRepository.java         # findByUsernameAndPassword()
│       └── ParkingRepository.java       # findBySlotNumber(), findByVehicleNumberContainingIgnoreCase()
│
└── src/main/resources/
    ├── application.properties           # MySQL connection + JPA config
    ├── application-h2.properties        # H2 demo profile override
    ├── data.sql                         # Admin seed (INSERT IGNORE)
    ├── static/
    │   └── logo.png                     # Served at /logo.png
    └── templates/
        ├── landing.html                 # Marketing page (hero, features, architecture)
        ├── login.html                   # Auth form (two‑column layout)
        ├── index.html                   # Dashboard (Booking / Occupied / History tabs)
        └── edit.html                    # Edit booking form
```

---

##  Quick Start

### Prerequisites

- **Java 17+** ([Download](https://adoptium.net/))
- **Maven 3.9+** (bundled as `mvnw.cmd` — no separate install needed)
- **MySQL 8.0** (optional — use H2 profile to skip)

### 1. Clone & Enter

```bash
cd ParkEase
```

### 2. Create MySQL Database (skip for H2)

```sql
CREATE DATABASE parkease;
```

Update your MySQL password in `src/main/resources/application.properties`:

```properties
spring.datasource.password=your_password
```

### 3. Run the Application

**Option A — One‑Click (Windows)**

Double‑click `start_app.bat`:
- Detects port 8081 (opens browser if already running)
- Launches Maven in a minimized window
- Polls every 3 seconds until the server is ready
- Opens `http://localhost:8081/` automatically
- Press any key to cleanly kill the server

**Option B — Terminal (MySQL)**

```bash
mvnw.cmd spring-boot:run
```

**Option C — Terminal (H2, no MySQL)**

```bash
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

### 4. Login

| Field | Value |
|-------|-------|
| **URL** | `http://localhost:8081/` |
| **Username** | `SPPN` |
| **Password** | `[REDACTED]` |

---

##  API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|:---:|
| `GET` | `/` | Landing / marketing page | ✗ |
| `GET` | `/login` | Login form | ✗ |
| `POST` | `/login` | Authenticate admin | ✗ |
| `GET` | `/logout` | Invalidate session | ✓ |
| `GET` | `/dashboard` | Admin dashboard (active + history) | ✓ |
| `POST` | `/save` | Create a new parking booking | ✓ |
| `GET` | `/edit/{id}` | Edit booking form | ✓ |
| `POST` | `/update` | Update existing booking | ✓ |
| `GET` | `/delete/{id}` | Delete a booking record | ✓ |
| `GET` | `/left/{id}` | Mark vehicle as exited (sets `exit_time`) | ✓ |

### Database Schema

**`admin`** table:
| Column | Type | Constraints |
|--------|------|:-----------:|
| `id` | `INT` | PK, AUTO_INCREMENT |
| `username` | `VARCHAR` | UNIQUE |
| `password` | `VARCHAR` | NOT NULL |

**`parking_slot`** table:
| Column | Type | Notes |
|--------|------|-------|
| `id` | `INT` | PK, AUTO_INCREMENT |
| `owner_name` | `VARCHAR` | — |
| `vehicle_number` | `VARCHAR` | Format: `KA 01 AB 1234` |
| `vehicle_type` | `VARCHAR` | 6 vehicle types |
| `slot_number` | `VARCHAR` | `B1-01` … `B4-50` |
| `mobile_number` | `VARCHAR` | 10 digits |
| `entry_time` | `DATETIME` | Set server‑side on booking |
| `exit_time` | `DATETIME` | `NULL` while active; set on "Left" action |

---

##  Product Roadmap

- [ ] **QR Code Entry / Exit** — Generate QR on booking, scan to mark exit
- [ ] **Receipt / Invoice Generation** — PDF download for completed parking records
- [ ] **Multi‑Admin Support** — Role‑based access (operator vs. owner)
- [ ] **Real‑Time Dashboard Push** — WebSocket integration for live slot updates across open browser tabs

---

##  Developer Experience

### Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `500` on login after restart | Duplicate admin rows | Run `DELETE FROM admin WHERE id > 1; ALTER TABLE admin ADD UNIQUE (username);` in MySQL |
| `404` on page refresh inside dashboard | Client‑side routing | Always navigate via buttons — the app uses server‑side rendering |
| `Port 8081 already in use` | Another instance running | Close the old CMD window or run `taskkill /f /im java.exe` |
| MySQL connection refused | MySQL service stopped | Start MySQL: `net start MySQL80` |

### Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-idea`).
3. Commit your changes.
4. Push to your branch and open a Pull Request.

All contributions are welcome — bug fixes, new features, UI polish, or documentation improvements.

---

##  Author & License

**Built by** — Pawan Simha, Sangeetha, Prajwal R, Nithin ML  
*Academic project for Q-Park system demonstration.*

This project is licensed under the **MIT License**. See the `LICENSE` file for details.

---

<div align="center">
  <sub>Made with ❤️ by the Q-Park Team</sub>
  <br/>
  <sub>© 2026 Q-Park — Quick &amp; Smart Parking</sub>
</div>
