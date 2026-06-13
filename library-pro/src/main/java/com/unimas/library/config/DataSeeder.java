package com.unimas.library.config;

import com.unimas.library.dto.UserForm;
import com.unimas.library.entity.Book;
import com.unimas.library.entity.Member;
import com.unimas.library.repository.BookRepository;
import com.unimas.library.repository.MemberRepository;
import com.unimas.library.service.SettingsService;
import com.unimas.library.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds first-run data:
 *  - default ADMIN (admin / admin123), LIBRARIAN (librarian / lib123)
 *    and STAFF (staff / staff123) accounts
 *  - the single SETTINGS row
 *  - sample books and members so the UI is not empty
 * Change the default passwords after first login!
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserService users, SettingsService settings,
                           BookRepository books, MemberRepository members) {
        return args -> {
            settings.get();   // ensures the settings row exists

            if (!users.exists("admin")) {
                UserForm admin = new UserForm();
                admin.setUsername("admin"); admin.setPassword("admin123");
                admin.setEmail("admin@library.unimas.my"); admin.setRole("ADMIN");
                users.create(admin);
            }
            if (!users.exists("librarian")) {
                UserForm lib = new UserForm();
                lib.setUsername("librarian"); lib.setPassword("lib123");
                lib.setEmail("librarian@library.unimas.my"); lib.setRole("LIBRARIAN");
                users.create(lib);
            }
            if (!users.exists("staff")) {
                UserForm st = new UserForm();
                st.setUsername("staff"); st.setPassword("staff123");
                st.setEmail("staff@library.unimas.my"); st.setRole("STAFF");
                users.create(st);
            }
            if (!users.exists("member")) {
                UserForm me = new UserForm();
                me.setUsername("member"); me.setPassword("member123");
                me.setEmail("dawood@siswa.unimas.my"); me.setRole("MEMBER");
                users.create(me);
            }

            if (books.count() == 0) {
                books.save(book("Database System Concepts", "Silberschatz, Korth, Sudarshan",
                        "9780078022159", "Databases", "McGraw-Hill", 2019, 5,
                        "The classic introduction to database design, SQL and transactions."));
                books.save(book("Spring in Action", "Craig Walls",
                        "9781617297571", "Programming", "Manning", 2022, 3,
                        "Hands-on guide to building applications with the Spring framework."));
                books.save(book("Clean Code", "Robert C. Martin",
                        "9780132350884", "Software Engineering", "Prentice Hall", 2008, 4,
                        "A handbook of agile software craftsmanship."));
                books.save(book("Oracle Database 21c Administration", "Bob Bryla",
                        "9781119787884", "Databases", "Sybex", 2021, 2,
                        "Practical administration of Oracle 21c instances."));
            }

            if (members.count() == 0) {
                // Project team (default members)
                members.save(member("Dawood Nadeem", "dawood@siswa.unimas.my",
                        "011-1112222", "226920", "STUDENT"));   // Team Leader
                members.save(member("Fawzia Moradi", "fawzia@siswa.unimas.my",
                        "011-3334444", "226553", "STUDENT"));
                members.save(member("Ahmad Faiz bin Rahman", "faiz@siswa.unimas.my",
                        "012-3456789", "78234", "STUDENT"));
                members.save(member("Siti Nurhaliza binti Omar", "siti@siswa.unimas.my",
                        "013-9876543", "79112", "STUDENT"));
                members.save(member("Dr. Wong Mei Ling", "mlwong@unimas.my",
                        "019-2223344", "S1045", "STAFF"));
            }
        };
    }

    private static Book book(String title, String author, String isbn, String cat,
                             String pub, int year, int copies, String desc) {
        Book b = new Book();
        b.setTitle(title); b.setAuthor(author); b.setIsbn(isbn); b.setCategory(cat);
        b.setPublisher(pub); b.setPublicationYear(year);
        b.setTotalCopies(copies); b.setAvailableCopies(copies);
        b.setDescription(desc);
        return b;
    }

    private static Member member(String name, String email, String phone, String matric, String type) {
        Member m = new Member();
        m.setFullName(name); m.setEmail(email); m.setPhone(phone);
        m.setMatricNo(matric); m.setMemberType(type);
        return m;
    }
}
