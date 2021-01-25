package com.temzu.cloud_storage.client;

import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.util.AuthUserUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CloudStorageServerHandler extends ChannelInboundHandlerAdapter {
    private FileTransfer fileTransfer;
    private AuthUserUtil authUserUtil;
    private Command currentCommand = Command.DEFAULT;

    public CloudStorageServerHandler(FileTransfer fileTransfer, AuthUserUtil authUserUtil) {
        this.fileTransfer = fileTransfer;
        this.authUserUtil = authUserUtil;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
