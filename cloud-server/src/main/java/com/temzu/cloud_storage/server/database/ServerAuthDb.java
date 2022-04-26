package com.temzu.cloud_storage.server.database;

import com.temzu.cloud_storage.server.database.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerAuthDb {

  private static final Logger LOG = LoggerFactory.getLogger(ServerAuthDb.class);

  private static ServerAuthDb serverAuthDb;
  private Session session;
  private final SessionFactory factory;

  public static ServerAuthDb getInstance() {
    if (serverAuthDb == null) {
      serverAuthDb = new ServerAuthDb();
    }
    return serverAuthDb;
  }

  private ServerAuthDb() {
    factory =
        new Configuration()
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

  public Session getSession() {
    return factory.getCurrentSession();
  }
}
