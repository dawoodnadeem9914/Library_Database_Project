# Library Pro — Enterprise Library Management System (v3.0)

CSC3402 / CCS3402 Database Application Development group project.
Spring Boot 3 · Java 21 · Oracle 21c XE · Spring Security · Thymeleaf · Bootstrap 5 · Maven

**Highlights:** pure-black enterprise theme, full User Management module (admin
creates/edits users, resets passwords, assigns roles), MEMBER role with a personal
profile page, role-based access control, instant overdue calculation, 8-card
analytics dashboard, reports with Chart.js, PDF / Excel / CSV export, ISBN validation,
cover & photo uploads, and styled error pages. Passwords are reset by the
administrator (no email service required).

## Modules
| Module    | URL          | Features |
|-----------|--------------|----------|
| Login     | `/login`     | BCrypt, remember-me, show/hide password, loading spinner, lockout/disabled messages |
| Register  | `/register`  | Live password strength meter + rules, username availability check, avatar upload (Staff / Member) |
| Reset     | `/forgot-password` | Shows the administrator's contact email to request a password reset |
| Users     | `/users`     | **ADMIN only** — create/edit/delete, activate/deactivate, reset password, assign roles, search + filter |
| Profile   | `/profile`   | Own account details + own loans and fines (links by email to a member record) |
| Dashboard | `/dashboard` | 5 stat cards, recent loans, latest members, most borrowed books |
| Books     | `/books`     | CRUD, search (title/author/ISBN/category), sortable columns, pagination, cover **upload**, availability badges, **PDF/Excel export** |
| Members   | `/members`   | CRUD, search, profile **photo upload**, borrowing history, active-loan count, fine summary |
| Loans     | `/loans`     | Borrow/return, auto availability update, loan limits, overdue tracking, fines |
| Reports   | `/reports`   | Statistics cards + Chart.js charts, most borrowed books, most active members |
| Settings  | `/settings`  | **ADMIN only** — library info + address/contact, borrowing rules, fines, email settings, light/dark/**auto** theme |
| About     | `/about`     | Project, team and technology overview |

## Default accounts (seeded on first run — change after first login!)
| Username    | Password   | Role      | Access |
|-------------|------------|-----------|--------|
| `admin`     | `admin123` | ADMIN     | Everything incl. Settings |
| `librarian` | `lib123`   | LIBRARIAN | Everything except Settings |
| `staff`     | `staff123` | STAFF     | Borrow/return + members + reports; no book editing, no deletes |
| `member`    | `member123`| MEMBER    | Read-only: dashboard, catalogue, own profile & loans |

## Quick start
See **INTEGRATION_GUIDE.md** for full step-by-step setup. Short version:

```bash
# 1. Create the Oracle user (as SYSTEM):
#    CREATE USER LIBRARY IDENTIFIED BY library123;
#    GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO LIBRARY;
# 2. Adjust src/main/resources/application.properties if needed
mvn spring-boot:run
# open http://localhost:8080  →  login: admin / admin123
```

No Oracle handy? Demo with in-memory H2:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## Database
Five tables: `USERS`, `BOOKS`, `MEMBERS`, `LOANS`, `SETTINGS`
(see `db/schema_oracle.sql` — Hibernate can also auto-create them with `ddl-auto=update`).

Business rules enforced in the service layer:
- Borrowing decreases `AVAILABLE_COPIES`; returning increases it (single transaction)
- Due date = loan date + `MAX_LOAN_DAYS` from SETTINGS
- A member may hold at most `MAX_BOOKS_PER_MEMBER` active loans
- Overdue status recalculates instantly on every dashboard/loans/reports load
  (due date < today AND no return date → OVERDUE), plus an hourly scheduler
- Fine = days overdue × `FINE_PER_DAY`
- Records carry `created_by` / `updated_by` stamped from the logged-in user

## Security
- BCrypt password hashing; CSRF protection enabled (Thymeleaf injects tokens)
- Password policy: 8+ chars with uppercase, lowercase, number and special character
- Role-based access control enforced at route level, with menus hidden per role
- Forgot a password? The page shows the administrator's email; the admin resets it
  from Users → reset password (no external email service needed)

## Error handling
`GlobalExceptionHandler` (@ControllerAdvice) + a custom `ErrorPageController` replace
the Whitelabel Error Page with styled 404 / 403 / 500 / database-error pages. All
exceptions are logged. Access denied requests land on a friendly 403 page.

## File uploads
Book covers and member photos are stored under `./uploads/` (configurable via
`app.upload-dir`) and served at `/uploads/**`. Max 5 MB, image types only.
