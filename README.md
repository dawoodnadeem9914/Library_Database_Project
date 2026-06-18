<div align="center">

# 🏛️ Enterprise Library Management System

### *A production-ready full-stack web application built with the Spring ecosystem*

> 🌐 **[Try it live on Railway → https://librarydatabaseproject-production.up.railway.app](https://librarydatabaseproject-production.up.railway.app)**

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](#)
[![Oracle DB](https://img.shields.io/badge/Oracle_DB-F80000?style=for-the-badge&logo=oracle&logoColor=white)](#)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](#)
[![Bootstrap 5](https://img.shields.io/badge/Bootstrap_5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](#)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)

<br/>

> A secure, role-based enterprise web application for managing library operations — built with a production-grade Spring Boot stack, Oracle database, and a fully responsive frontend.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies Used](#-technologies-used)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Security Model](#-security-model)
- [Screenshots](#-screenshots)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## 🌟 Overview

The **Enterprise Library Management System** is a full-stack web application that digitises and streamlines the operations of a library. Built as part of the CSC3402 Enterprise Application Development course at UPM, it demonstrates a comprehensive understanding of enterprise software architecture.

The system supports multiple user roles (Admin, Librarian, Member), provides secure authentication and authorisation via Spring Security, and manages books, members, and borrowing records through a fully relational Oracle database.

---

## ✨ Features

### 🔐 Security & Authentication
- Secure login and session management powered by **Spring Security**
- **Role-Based Access Control (RBAC)** — Admins, Librarians, and Members see different views and have different permissions
- Password encryption using BCrypt hashing
- CSRF protection enabled by default

### 📚 Library Operations
- Complete **book catalogue management** (add, update, delete, search)
- **Member management** — register, update, and deactivate library members
- **Borrowing and returns** — track which member has which book and when it's due
- Search and filter books by title, author, ISBN, or category

### 🖥️ Frontend
- Fully **responsive UI** built with Bootstrap 5 — works on desktop, tablet, and mobile
- Dynamic server-side rendering with **Thymeleaf** templates
- Clean, accessible design with intuitive navigation

### 🗃️ Database
- Fully relational **Oracle Database** schema
- JDBC / Spring Data integration for clean data access
- Proper use of foreign keys, constraints, and indexing

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION ARCHITECTURE                      │
└─────────────────────────────────────────────────────────────────┘

  ┌──────────────────┐       ┌──────────────────────────────────┐
  │   FRONTEND LAYER │       │         BACKEND LAYER            │
  │                  │       │                                  │
  │  Thymeleaf HTML  │──────►│   Spring Boot Controllers        │
  │  Bootstrap 5 CSS │       │   Spring Security Filter Chain   │
  │  JavaScript      │       │   Service Layer (Business Logic) │
  └──────────────────┘       │   Repository Layer (Data Access) │
                             └──────────────┬───────────────────┘
                                            │
                                            ▼
                             ┌──────────────────────────────────┐
                             │         DATA LAYER               │
                             │                                  │
                             │         Oracle Database          │
                             │    (Books, Members, Loans,       │
                             │     Users, Roles tables)         │
                             └──────────────────────────────────┘

  Architecture Pattern: MVC (Model-View-Controller)
  Security Pattern:     Filter-Based Authentication + RBAC
```

---

## 🛠️ Technologies Used

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 17+ |
| **Framework** | Spring Boot | 3.x |
| **Security** | Spring Security | 6.x |
| **Template Engine** | Thymeleaf | 3.x |
| **Database** | Oracle Database | 19c+ |
| **Data Access** | Spring Data / JDBC | — |
| **Frontend** | Bootstrap 5 | 5.3 |
| **Build Tool** | Maven | 3.x |
| **IDE** | IntelliJ IDEA | — |

---

## 🗃️ Database Schema

```sql
-- Core tables (simplified)

BOOKS
├── book_id       (PK)
├── title
├── author
├── isbn          (UNIQUE)
├── category
├── quantity
└── available_qty

MEMBERS
├── member_id     (PK)
├── full_name
├── email         (UNIQUE)
├── phone
└── joined_date

LOANS
├── loan_id       (PK)
├── book_id       (FK → BOOKS)
├── member_id     (FK → MEMBERS)
├── borrow_date
├── due_date
└── return_date

USERS
├── user_id       (PK)
├── username      (UNIQUE)
├── password      (BCrypt hashed)
└── role          (ADMIN / LIBRARIAN / MEMBER)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Oracle Database 19c (or Oracle XE for local dev)
- IntelliJ IDEA (recommended)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/dawoodnadeem9914/Library_Database_Project.git
cd Library_Database_Project

# 2. Configure database connection
# Edit src/main/resources/application.properties:
# spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
# spring.datasource.username=YOUR_DB_USER
# spring.datasource.password=YOUR_DB_PASSWORD

# 3. Run the SQL schema
# Execute the SQL scripts in /db/ folder on your Oracle instance

# 4. Build and run
mvn clean install
mvn spring-boot:run

# 5. Access the application
# http://localhost:8080
```

### Default Login Credentials (Development)

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Librarian | `librarian` | `lib123` |
| Member | `member` | `member123` |

> ⚠️ Change all credentials before any deployment.

---

## 🔒 Security Model

```
PUBLIC ACCESS          MEMBER ACCESS         ADMIN/LIBRARIAN ACCESS
───────────────        ────────────────       ─────────────────────────
/login                 /dashboard             /books/add
/register              /books (view)          /books/edit
/                      /profile               /members/manage
                       /search                /loans/manage
                                             /reports
                                             /users/manage
```

---

## 🔮 Future Improvements

- [ ] 📧 Email notifications for due-date reminders
- [ ] 📊 Admin dashboard with analytics and charts
- [ ] 🔍 Advanced search with filters (genre, publication year, availability)
- [ ] 📱 REST API layer for future mobile app integration
- [ ] ☁️ Cloud deployment (AWS / Azure)
- [ ] 🧪 Unit and integration tests (JUnit 5, Mockito)
- [ ] 🐳 Docker containerisation

---

## 👨‍💻 Author

**Dawood Nadeem**  
BSc Computer Science @ University Putra Malaysia (UPM)  
📧 [Captaindawood12@gmail.com](mailto:Captaindawood12@gmail.com)  
🔗 [LinkedIn](https://linkedin.com/in/dawood-nadeem) · [GitHub](https://github.com/dawoodnadeem9914)

---

<div align="center">

*Built as part of CSC3402 — Enterprise Application Development @ UPM*  
*⭐ Star this repo if you found it helpful!*

</div>
