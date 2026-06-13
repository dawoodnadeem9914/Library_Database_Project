# Step-by-Step Integration Guide

Follow these steps in order. Total time: ~20 minutes.

## Step 1 — Prerequisites
- JDK 21+ (`java -version`)
- Maven 3.9+ (`mvn -version`) or use IntelliJ's bundled Maven
- A UPM FSKTM Oracle account (your matric DB account, e.g. D226920)
- Access to the campus network or the UPM VPN (the DB server is on-campus only)
- IntelliJ IDEA (Community is fine)
- (Only if using a local database instead) Oracle 21c XE on port 1521

## Step 2 — Your database account
This project is configured for the **UPM FSKTM Oracle server**. You do **not**
create a user — you already have one (your matric account, e.g. `D226920`).
In SQL Developer create a connection with:

- Username: `D226920`  ·  Password: your matric DB password
- Hostname: `fsktmdbora.upm.edu.my`  ·  Port: `1521`
- **SID:** `fsktm`  (select the SID radio button, not Service name)

Click **Test** → it must say *Success* before the app will connect.
If Test fails you are off-campus — connect the UPM VPN and try again.

*(Using local Oracle XE instead? Then create a LIBRARY user as SYSTEM with
`CREATE USER LIBRARY IDENTIFIED BY library123;` and point the URL at
`@//localhost:1521/XEPDB1` — see the commented note in `application.properties`.)*

## Step 3 — Create the tables (choose ONE option)

**Option A (recommended for the demo):** do nothing.
`spring.jpa.hibernate.ddl-auto=update` lets Hibernate create all five tables
automatically from the entity classes on first run, and the `DataSeeder`
inserts default users, settings and sample data.

**Option B (manual DDL, good for the report):** connect **as your DB account** and run
`db/schema_oracle.sql`. Then change `ddl-auto` to `none` in
`application.properties`. User accounts are still seeded by the app
(passwords must be BCrypt-hashed, so they can't be inserted as plain SQL).

## Step 4 — Configure the connection
Open `src/main/resources/application.properties` and check:

```properties
spring.datasource.url=jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:fsktm
spring.datasource.username=D226920
spring.datasource.password=226920
```

This is already set for the UPM server. The URL ends with `:fsktm` because the
server uses an **SID** (not a service name) — note the colon before `fsktm`,
unlike the local-XE form `/XEPDB1` which uses a slash. Each team member can put
their own matric account here; everyone has a separate schema, so there are no
collisions. For the final demo, point everyone's app at one agreed account (the
team leader's) so you all see the same data.

## Step 5 — Open in IntelliJ
1. File → Open → select the `library-pro` folder (the one with `pom.xml`)
2. Wait for Maven to download dependencies
3. Run `LibraryProApplication`

Or from a terminal: `mvn spring-boot:run`

## Step 6 — Log in
- Open http://localhost:8080 — you are redirected to the login page
- Sign in as `admin` / `admin123` (full access incl. Settings)
- or `librarian` / `lib123` (everything except Settings)
- or `staff` / `staff123` (borrow/return + members, no book editing or deletes)
- or `member` / `member123` (read-only member view: catalogue + own profile)
- Or click **Create account** to register a new Staff/Member user (live password
  strength meter, username availability check, optional avatar upload)

## Step 7 — Smoke test (use this as your testing checklist)
1. Dashboard shows sample data, monthly borrowing chart and quick-action buttons
2. Books → Add Book with a cover image upload → cover shows in the table
3. Books → duplicate ISBN → red toast notification appears top-right
4. Books → click column headers (Title/Author/Year/Availability) → sorting toggles
5. Books → filter by category, then Export PDF and Export Excel → files download
6. Members → register with a profile photo → photo shows on the profile page
7. Member profile shows Active Loans count and Outstanding Fines (RM)
8. Loans → Borrow → copies decrease; Return → increase; over-limit member → blocked
9. Visit a bad URL like /books/99999 → styled 404 page (no Whitelabel page)
10. Log in as `staff` → no Settings menu, no delete buttons; opening /settings → 403 page
11. Settings → Theme → **Dark** → premium black theme; **Auto** → follows your OS theme
12. Login page → eye icon shows/hides the password; Create account → register → sign in
13. Leave the app idle past 30 min → next click returns to login with "session expired"
14. Admin → **Users** → create a user, deactivate it, try logging in with it → "deactivated" message
15. Log in as `member` → sidebar shows only Dashboard, Books, My Profile, About;
    opening `/loans` returns the styled 403 page
16. Books → Export **CSV** (alongside PDF/Excel); add a book with a 12-digit ISBN → validation error
17. Reports → five charts render: status, top books, monthly trend, categories, availability
18. Login → "Forgot password?" → page shows the administrator's email to contact
19. Admin → **Users** → key icon → set a new password for any user (this is the reset flow)

## Step 8 — Push to GitHub
```bash
git init
git add .
git commit -m "Enterprise Library Management System - initial commit"
git branch -M main
git remote add origin https://github.com/<your-team>/library-pro.git
git push -u origin main
```
Each member should commit their own module from their own account —
the commit history is evidence of teamwork for the report.

## Troubleshooting
| Problem | Fix |
|---|---|
| `ORA-12541: no listener` | Start the Oracle service / listener (`lsnrctl start`) |
| `ORA-01017: invalid credentials` | Re-check Step 2 user and Step 4 properties |
| `invalid username/password` at login page | Use seeded accounts: admin/admin123, librarian/lib123 |
| Port 8080 busy | Change `server.port` in application.properties |
| Want to demo without Oracle | `mvn spring-boot:run -Dspring-boot.run.profiles=h2` |
| Upload fails | Image must be JPG/PNG/WEBP/GIF and under 5 MB; check the `uploads/` folder is writable |
| Fresh start | In SQL Developer (as your DB account) drop the BOOKS/LOANS/MEMBERS/USERS/SETTINGS tables, then run the app — the seeder rebuilds them |
| Connection refused / timeout | You're off-campus; connect the UPM VPN. Confirm SQL Developer's **Test** says Success first |
