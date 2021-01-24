package com.temzu.cloud_storage.util;


import com.temzu.cloud_storage.operation.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

public class AuthUserUtil {
    private String login;
    private String password;

    private int cmdLen = 1;
    private int otherLen = 4;


    public ByteBuf singIn(String login, String password) {
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(cmdLen + otherLen + loginBytes.length + otherLen + passwordBytes.length);
        buf.writeByte(Command.AUTHORIZATION.getOperationCode());
        buf.writeInt(loginBytes.length);
        buf.writeBytes(loginBytes);
        buf.writeInt(passwordBytes.length);
        buf.writeBytes(passwordBytes);
        return buf;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
