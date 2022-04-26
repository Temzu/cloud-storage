package com.temzu.cloud_storage.server.database.dao;

import com.temzu.cloud_storage.server.database.entity.User;

public interface UserDao {
  User getUserById(String login);
}
