package com.temzu.cloud_storage.operation;

import java.util.Arrays;

public enum Command {

    DEFAULT((byte) 0),

    AUTHORIZATION((byte) 1),
    AUTHORIZATION_COMPLETED((byte) 2),

    GET_FILES_LIST((byte) 10),
    SEND_FILES_LIST((byte) 11),

    DOWNLOAD_FILE((byte) 100),
    DOWNLOAD_FILE_SUCCESS((byte) 101),

    UPLOAD_FILE((byte) 150),

    DELETE_FILE((byte) 200),

    RENAME_FILE((byte) 210);

    private byte operationCode;

    Command(byte operationCode) {
        this.operationCode = operationCode;
    }

    public byte getOperationCode() {
        return operationCode;
    }

    public static Command defineCommand(byte operationCode) {
        return Arrays.stream(Command.values())
                .filter(cmd -> cmd.getOperationCode() == operationCode)
                .findAny().orElse(Command.DEFAULT);
    }

}
