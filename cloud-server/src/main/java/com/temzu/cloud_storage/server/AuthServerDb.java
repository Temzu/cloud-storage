package com.temzu.cloud_storage.server;

import com.temzu.cloud_storage.operation.ProcessStatus;
import com.temzu.cloud_storage.server.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AuthServerDb {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServerDb.class);

    private Session session;
    private final SessionFactory factory;


    public AuthServerDb() {
        factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
        session = null;
        LOG.debug("database connected");
    }

    public void closeConnection() {
        factory.close();
        session.close();
    }

    public ProcessStatus authClient(String login, String password, ProcessStatus status) {
        if (login.isEmpty() || status != ProcessStatus.AUTH_READY) {
            return ProcessStatus.AUTH_NOT_READY;
        }
        session = factory.getCurrentSession();
        session.beginTransaction();
        User user = (User) session
                .createQuery("from User u where u.login = :login and u.password = :password")
                .setParameter("login", login).setParameter("password", password).getSingleResult();
        session.getTransaction().commit();

        return user != null ? ProcessStatus.AUTH_SUCCESS : ProcessStatus.AUTH_ERROR;
    }

//    public static void main(String[] args) {
//        SessionFactory factory = new Configuration()
//                .configure("hibernate.cfg.xml")
//                .addAnnotatedClass(User.class)
//                .buildSessionFactory();
//
//        // CRUD
//        Session session = null;
//
//        try {
////            session = factory.getCurrentSession();
////            User user = new User("User1", "321");
////            session.beginTransaction();
////            session.save(user);
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Reader reader = session.get(Reader.class, 1);
////            Book book = session.get(Book.class, 2);
//////            reader.getBooks().add(book);
////            reader.getBooks().clear();
////            session.getTransaction().commit();
//
//
//            //CREATE
////            session = factory.getCurrentSession();
////            Catalog catalog = new Catalog("Fantasy #15");
////            session.beginTransaction();
////            session.save(catalog);
////            session.getTransaction().commit();
//
////            READ
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Book harryPotterBook = session.get(Book.class, 1);
////            session.getTransaction().commit();
////            System.out.println(harryPotterBook);
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Catalog catalog2 = session.get(Catalog.class, 2L);
////            session.getTransaction().commit();
////            System.out.println(catalog2);
//
////            UPDATE
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Book bookJava1 = session.get(Book.class, 3);
////            bookJava1.setTitle("Java 1 Advanced");
////            session.getTransaction().commit();
////            System.out.println(bookJava1);
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Catalog catalog = session.get(Catalog.class, 1L);
////            catalog.setTitle("Fantasy #8");
////            session.getTransaction().commit();
////            System.out.println(catalog);
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Book bookJava1 = session.get(Book.class, 4);
////            session.delete(bookJava1);
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////             List<Book> allBooks = session.createQuery("from Book").getResultList();
//////             from Book b where b.title = 'Harry Potter' or b.authorName = 'Rowling'
//////             from Book b where b.title LIKE 'Harry%'
//////             from Book b where b.title = :title
////            List<Book> allBooks = session.createQuery("from Book b where b.title = :title").setParameter("title", "Java 1").getResultList();
////            System.out.println(allBooks);
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            session.createQuery("update Book set title = 'A'").executeUpdate();
////            session.createQuery("delete from Book where id = 3").executeUpdate();
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Book book = session.get(Book.class, 1);
////            System.out.println(book);
////            session.getTransaction().commit();
////
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Author author = session.get(Author.class, 1);
////            System.out.println(author);
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            List<Reader> readers = session.createQuery("from Reader").getResultList();
////            System.out.println(readers);
////            session.getTransaction().commit();
//
////            session = factory.getCurrentSession();
////            session.beginTransaction();
////            Author author = session.get(Author.class, 2);
////            session.delete(author);
////            session.getTransaction().commit();
//        } finally {
//            factory.close();
//            session.close();
//        }
//    }
}
