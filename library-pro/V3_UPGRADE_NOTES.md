# Library Pro — Feature Overview & Role Matrix

A Spring Boot + Oracle web application demonstrating full CRUD with role-based
access control, built for CSC3402 / CCS3402.

## Core CRUD modules
- **Books** — create, read (list + detail), update, delete; search, category
  filter, sortable columns, cover image upload, ISBN validation, PDF/Excel/CSV export
- **Members** — full CRUD, profile photo upload, borrowing history, fine summary
- **Loans** — borrow, return, edit (admin), delete (admin); instant overdue calculation

## Supporting modules
- **Dashboard** — 8 statistic cards + monthly borrowing chart + recent lists
- **Reports** — Chart.js: loan status, most borrowed, monthly trend, books by
  category, availability; most active members
- **User Management (admin)** — create / edit / delete users, activate / deactivate,
  **reset passwords**, assign roles, search + filter
- **Settings (admin)** — library info, borrowing rules, fines, theme
- **Profile** — every user sees their own account; members see their own loans & fines

## Roles

| Capability | ADMIN | LIBRARIAN | STAFF | MEMBER |
|------------|:-----:|:---------:|:-----:|:------:|
| Dashboard | ✔ | ✔ | ✔ | ✔ |
| Browse catalogue | ✔ | ✔ | ✔ | ✔ (read-only) |
| Add/edit books | ✔ | ✔ | — | — |
| Add/edit members | ✔ | ✔ | ✔ | — |
| Borrow / return | ✔ | ✔ | ✔ | — |
| Edit loan dates | ✔ | — | — | — |
| Delete records | ✔ | — | — | — |
| Reports + exports | ✔ | ✔ | ✔ | — |
| User management | ✔ | — | — | — |
| Settings | ✔ | — | — | — |
| Own profile & loans | ✔ | ✔ | ✔ | ✔ |

## Password reset
There is no email service. If a user forgets their password they contact the
administrator (the forgot-password page shows the admin's email), and the admin
sets a new password from **Users → reset password**. Oracle-only, nothing external.

## Security
BCrypt password hashing, CSRF protection, role-based route protection, a strong
password policy (8+ chars, mixed case, number, special character), and `created_by`
/ `updated_by` auditing columns on the main tables.

## Database (Oracle 21c XE)
Five tables: USERS, BOOKS, MEMBERS, LOANS, SETTINGS. The app uses `ddl-auto=update`
so tables are created automatically on first run; `db/schema_oracle.sql` is the
full reference DDL. Default accounts (seeded): admin/admin123, librarian/lib123,
staff/staff123, member/member123.
