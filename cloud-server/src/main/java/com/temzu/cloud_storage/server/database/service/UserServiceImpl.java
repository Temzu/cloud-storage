package com.temzu.cloud_storage.server.database.service;

import com.temzu.cloud_storage.operation.ProcessStatus;
import com.temzu.cloud_storage.server.database.dao.UserDao;
import com.temzu.cloud_storage.server.database.dao.UserDaoImpl;
import com.temzu.cloud_storage.server.database.entity.User;

public class UserServiceImpl implements UserService {
  private static UserDao userDao;

  public UserServiceImpl() {
    userDao = new UserDaoImpl();
  }

  @Override
  public ProcessStatus authClient(String login, String password, ProcessStatus status) {
    User user = userDao.getUserById(login);
    if (user == null
        || !user.getPassword().equals(password)
        || status != ProcessStatus.AUTH_READY) {
      return ProcessStatus.AUTH_NOT_READY;
    }
    return ProcessStatus.AUTH_SUCCESS;
  }

  @Override
  public ProcessStatus registerClient() {
    return null;
  }
}
