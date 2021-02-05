package com.temzu.cloud_storage.server.database.service;

import com.temzu.cloud_storage.operation.ProcessStatus;
import com.temzu.cloud_storage.server.database.entity.User;

public interface UserService {
    ProcessStatus authClient(String login, String password, ProcessStatus status);
    ProcessStatus registerClient();
}
