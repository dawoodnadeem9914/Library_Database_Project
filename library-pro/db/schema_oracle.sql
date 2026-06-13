-- ================================================================
-- CSC3402 / CCS3402 - Enterprise Library Management System v3.0
-- Oracle 21c XE schema: USERS, BOOKS, MEMBERS, LOANS, SETTINGS
-- Run as the LIBRARY user. (As SYSTEM first:
--   CREATE USER LIBRARY IDENTIFIED BY library123;
--   GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO LIBRARY;)
-- Note: the app uses ddl-auto=update, so upgrading an existing v2
-- schema happens automatically on first run. This script is the
-- full reference DDL for a clean install.
-- ================================================================

CREATE SEQUENCE USER_SEQ    START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE BOOK_SEQ    START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE MEMBER_SEQ  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE LOAN_SEQ    START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SETTING_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE USERS (
    USER_ID         NUMBER(19)    PRIMARY KEY,
    FULL_NAME       VARCHAR2(120),
    USERNAME        VARCHAR2(50)  NOT NULL UNIQUE,
    PASSWORD        VARCHAR2(100) NOT NULL,             -- BCrypt hash
    EMAIL           VARCHAR2(120) NOT NULL,
    ROLE            VARCHAR2(20)  NOT NULL
                    CHECK (ROLE IN ('ADMIN','LIBRARIAN','STAFF','MEMBER')),
    ACTIVE          NUMBER(1)     DEFAULT 1 NOT NULL,   -- account enabled?
    LAST_LOGIN      TIMESTAMP,
    AVATAR_URL      VARCHAR2(500),
    CREATED_AT      TIMESTAMP     NOT NULL
);

CREATE TABLE BOOKS (
    BOOK_ID          NUMBER(19)     PRIMARY KEY,
    TITLE            VARCHAR2(200)  NOT NULL,
    AUTHOR           VARCHAR2(120)  NOT NULL,
    ISBN             VARCHAR2(20)   NOT NULL UNIQUE,    -- validated 10/13 digits
    CATEGORY         VARCHAR2(60)   NOT NULL,
    DESCRIPTION      VARCHAR2(1000),
    PUBLISHER        VARCHAR2(120),
    LANGUAGE_CODE    VARCHAR2(30),
    EDITION          VARCHAR2(30),
    SHELF_LOCATION   VARCHAR2(30),
    PUBLICATION_YEAR NUMBER(4),
    TOTAL_COPIES     NUMBER(10)     NOT NULL CHECK (TOTAL_COPIES >= 1),
    AVAILABLE_COPIES NUMBER(10)     NOT NULL CHECK (AVAILABLE_COPIES >= 0),
    IMAGE_URL        VARCHAR2(500),
    CREATED_AT       TIMESTAMP      NOT NULL,
    UPDATED_AT       TIMESTAMP,
    CREATED_BY       VARCHAR2(50),
    UPDATED_BY       VARCHAR2(50)
);

CREATE TABLE MEMBERS (
    MEMBER_ID   NUMBER(19)    PRIMARY KEY,
    FULL_NAME   VARCHAR2(120) NOT NULL,
    EMAIL       VARCHAR2(120) NOT NULL UNIQUE,
    PHONE       VARCHAR2(20)  NOT NULL,
    MATRIC_NO   VARCHAR2(20)  NOT NULL UNIQUE,
    MEMBER_TYPE VARCHAR2(20)  NOT NULL CHECK (MEMBER_TYPE IN ('STUDENT','STAFF')),
    PHOTO_URL   VARCHAR2(500),
    CREATED_AT  TIMESTAMP     NOT NULL,
    UPDATED_AT  TIMESTAMP,
    CREATED_BY  VARCHAR2(50),
    UPDATED_BY  VARCHAR2(50)
);

CREATE TABLE LOANS (
    LOAN_ID     NUMBER(19)   PRIMARY KEY,
    BOOK_ID     NUMBER(19)   NOT NULL,
    MEMBER_ID   NUMBER(19)   NOT NULL,
    LOAN_DATE   DATE         NOT NULL,
    DUE_DATE    DATE         NOT NULL,
    RETURN_DATE DATE,
    STATUS      VARCHAR2(15) DEFAULT 'BORROWED' NOT NULL
                CHECK (STATUS IN ('BORROWED','RETURNED','OVERDUE')),
    CREATED_AT  TIMESTAMP,
    UPDATED_AT  TIMESTAMP,
    CREATED_BY  VARCHAR2(50),
    UPDATED_BY  VARCHAR2(50),
    CONSTRAINT FK_LOAN_BOOK   FOREIGN KEY (BOOK_ID)   REFERENCES BOOKS (BOOK_ID),
    CONSTRAINT FK_LOAN_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBERS (MEMBER_ID)
);

CREATE TABLE SETTINGS (
    SETTING_ID           NUMBER(19)    PRIMARY KEY,
    LIBRARY_NAME         VARCHAR2(120) NOT NULL,
    LIBRARY_ADDRESS      VARCHAR2(250),
    CONTACT_NUMBER       VARCHAR2(25),
    ADMIN_NAME           VARCHAR2(120) NOT NULL,
    ADMIN_EMAIL          VARCHAR2(120) NOT NULL,
    EMAIL_FROM           VARCHAR2(120),
    EMAIL_NOTIFICATIONS  NUMBER(1)     DEFAULT 0 NOT NULL,
    MAX_LOAN_DAYS        NUMBER(3)     NOT NULL CHECK (MAX_LOAN_DAYS BETWEEN 1 AND 90),
    MAX_BOOKS_PER_MEMBER NUMBER(3)     NOT NULL CHECK (MAX_BOOKS_PER_MEMBER BETWEEN 1 AND 20),
    FINE_PER_DAY         NUMBER(8,2)   NOT NULL CHECK (FINE_PER_DAY >= 0),
    THEME_MODE           VARCHAR2(10)  NOT NULL CHECK (THEME_MODE IN ('light','dark','auto'))
);

-- Default settings row (premium black theme is the default)
INSERT INTO SETTINGS (SETTING_ID, LIBRARY_NAME, LIBRARY_ADDRESS, CONTACT_NUMBER, ADMIN_NAME,
    ADMIN_EMAIL, EMAIL_FROM, EMAIL_NOTIFICATIONS, MAX_LOAN_DAYS, MAX_BOOKS_PER_MEMBER,
    FINE_PER_DAY, THEME_MODE)
VALUES (SETTING_SEQ.NEXTVAL, 'UNIMAS Library',
    'Universiti Malaysia Sarawak, 94300 Kota Samarahan, Sarawak', '+60 82-581 000',
    'System Administrator', 'admin@library.unimas.my', 'noreply@library.unimas.my', 0,
    14, 3, 0.50, 'dark');

-- NOTE: user accounts are seeded by the application (DataSeeder) because
-- passwords must be BCrypt-hashed. Defaults:
--   admin/admin123 (ADMIN) · librarian/lib123 (LIBRARIAN)
--   staff/staff123 (STAFF) · member/member123 (MEMBER)

-- Sample books
INSERT INTO BOOKS VALUES (BOOK_SEQ.NEXTVAL, 'Database System Concepts', 'Silberschatz, Korth, Sudarshan',
    '9780078022159', 'Databases', 'The classic introduction to database design, SQL and transactions.',
    'McGraw-Hill', 'English', '7th Edition', 'A1-01', 2019, 5, 5, NULL,
    SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
INSERT INTO BOOKS VALUES (BOOK_SEQ.NEXTVAL, 'Spring in Action', 'Craig Walls',
    '9781617297571', 'Programming', 'Hands-on guide to building applications with the Spring framework.',
    'Manning', 'English', '6th Edition', 'B2-14', 2022, 3, 3, NULL,
    SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
INSERT INTO BOOKS VALUES (BOOK_SEQ.NEXTVAL, 'Clean Code', 'Robert C. Martin',
    '9780132350884', 'Software Engineering', 'A handbook of agile software craftsmanship.',
    'Prentice Hall', 'English', '1st Edition', 'B3-07', 2008, 4, 4, NULL,
    SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');

-- Default members (project team) + samples
INSERT INTO MEMBERS VALUES (MEMBER_SEQ.NEXTVAL, 'Dawood Nadeem', 'dawood@siswa.unimas.my',
    '011-1112222', '226920', 'STUDENT', NULL, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system'); -- Leader
INSERT INTO MEMBERS VALUES (MEMBER_SEQ.NEXTVAL, 'Fawzia Moradi', 'fawzia@siswa.unimas.my',
    '011-3334444', '226553', 'STUDENT', NULL, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
INSERT INTO MEMBERS VALUES (MEMBER_SEQ.NEXTVAL, 'Ahmad Faiz bin Rahman', 'faiz@siswa.unimas.my',
    '012-3456789', '78234', 'STUDENT', NULL, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
INSERT INTO MEMBERS VALUES (MEMBER_SEQ.NEXTVAL, 'Siti Nurhaliza binti Omar', 'siti@siswa.unimas.my',
    '013-9876543', '79112', 'STUDENT', NULL, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
INSERT INTO MEMBERS VALUES (MEMBER_SEQ.NEXTVAL, 'Dr. Wong Mei Ling', 'mlwong@unimas.my',
    '019-2223344', 'S1045', 'STAFF', NULL, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');

-- Sample loan (book 2 borrowed by member 1)
INSERT INTO LOANS VALUES (LOAN_SEQ.NEXTVAL, 2, 1, DATE '2026-06-01', DATE '2026-06-15', NULL,
    'BORROWED', SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
UPDATE BOOKS SET AVAILABLE_COPIES = AVAILABLE_COPIES - 1 WHERE BOOK_ID = 2;

COMMIT;

-- ================================================================
-- UPGRADE SCRIPT: run this instead on an existing v2.x database
-- (or simply let ddl-auto=update do it automatically)
-- ================================================================
-- ALTER TABLE USERS ADD (ACTIVE NUMBER(1) DEFAULT 1 NOT NULL,
--     LAST_LOGIN TIMESTAMP, AVATAR_URL VARCHAR2(500));
-- ALTER TABLE USERS DROP CONSTRAINT <role_check_name>;
-- ALTER TABLE USERS ADD CHECK (ROLE IN ('ADMIN','LIBRARIAN','STAFF','MEMBER'));
-- ALTER TABLE BOOKS ADD (LANGUAGE_CODE VARCHAR2(30), EDITION VARCHAR2(30),
--     SHELF_LOCATION VARCHAR2(30), CREATED_BY VARCHAR2(50), UPDATED_BY VARCHAR2(50));
-- ALTER TABLE MEMBERS ADD (CREATED_BY VARCHAR2(50), UPDATED_BY VARCHAR2(50));
-- ALTER TABLE LOANS ADD (CREATED_BY VARCHAR2(50), UPDATED_BY VARCHAR2(50));

