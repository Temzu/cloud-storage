package com.temzu.cloud_storage.server.database.service;

import com.temzu.cloud_storage.operation.ProcessStatus;

public interface UserService {
  ProcessStatus authClient(String login, String password, ProcessStatus status);

  ProcessStatus registerClient();
}
