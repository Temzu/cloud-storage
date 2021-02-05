package com.temzu.cloud_storage.server.database.dao;

import com.temzu.cloud_storage.server.database.ServerAuthDb;
import com.temzu.cloud_storage.server.database.entity.User;
import org.hibernate.Session;

public class UserDaoImpl implements UserDao{
    private Session session;

    @Override
    public User getUserById(String login) {
        session = ServerAuthDb.getInstance().getSession();
        session.beginTransaction();
        User user = session
                .byNaturalId(User.class)
                .using("login", login)
                .load();
        session.detach(user);
        session.getTransaction().commit();
        return user;
    }

}
