package com.temzu.cloud_storage.operation;

public enum ProcessStatus {
  WAIT_BYTE,

  GET_LOGIN,
  GET_FILES_LIST,
  FILES_LIST_READY,

  READ_FILE,
  READ_FILE_READY,
  READ_FILE_ERROR,

  START_DOWNLOAD_PROCESS,
  DOWNLOAD_PROCESS,
  START_DOWNLOAD_FILE,
  DOWNLOAD_FILE,
  DOWNLOAD_PROCESS_ERROR,

  AUTH_READY,
  AUTH_NOT_READY,
  AUTH_SUCCESS,
  AUTH_ERROR,

  RENAME_FILE_READY;

  public static ProcessStatus defineProcess(Requests cmd) {
    switch (cmd) {
      case AUTHORIZATION:
        return ProcessStatus.GET_LOGIN;
      case SEND_FILES_LIST:
        return ProcessStatus.GET_FILES_LIST;
      case DOWNLOAD_FILE:
        return ProcessStatus.READ_FILE;
      case DOWNLOAD_FILE_SUCCESS:
      case UPLOAD_FILE:
        return ProcessStatus.START_DOWNLOAD_PROCESS;
    }
    return ProcessStatus.WAIT_BYTE;
  }
}
