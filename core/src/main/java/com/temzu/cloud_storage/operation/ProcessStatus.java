package com.temzu.cloud_storage.operation;

public enum ProcessStatus {
    WAIT_BYTE,

    GET_LOGIN;

    public static ProcessStatus defineProcess(Command cmd) {
        switch (cmd) {
            case AUTHORIZATION:
                return ProcessStatus.GET_LOGIN;
        }
        return ProcessStatus.WAIT_BYTE;
    }
}
