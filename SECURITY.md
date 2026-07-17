# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ Yes    |

## Reporting a Vulnerability

If you discover a security vulnerability in QSpark, please report it by emailing **iampawansimha.2004@gmail.com**.

Do not publicly disclose the issue until it has been addressed.

We will acknowledge receipt within 48 hours and provide a timeline for the fix.

## Security Features

QSpark implements the following security measures:

- **BCrypt password hashing** (strength 10) — no plaintext password storage
- **CSRF protection** on all state-changing POST endpoints
- **Security headers**: XSS protection (MODE_BLOCK), content-type sniffing disabled (nosniff), frame denial (DENY), referrer policy (SAME_ORIGIN), HSTS (includeSubDomains, 1-year max-age)
- **Form-based authentication** with Spring Security session management
- **Session invalidation** on logout with authentication clearance
- **401 response** on API-style endpoints (`/qrcode/generate`, `/qrcode/list`) instead of redirect
- **Rate limiting** on `/login`: 5 failed attempts per IP triggers a 5-minute cooldown
- **Environment variable secrets**: Admin password and database password are configurable via environment variables, never hardcoded
- **Git history purged**: All historical commits have been rewritten to remove hardcoded credentials ([REDACTED], [REDACTED]) and binary log files

## Known Security Considerations

- The H2 console is enabled in the `h2` development profile only — do not enable in production
- Application runs on HTTP by default — deploy behind a TLS-terminating proxy in production
- Admin credentials are seeded at startup — change immediately after first login in production deployments
