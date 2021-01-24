package com.temzu.cloud_storage.server;

import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.operation.ProcessStatus;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    private boolean isAuth;

    private Path rootFolder;
    private AuthServerDb authServerDb;

    private Command currentCommand = null;
    private ProcessStatus currentStatus = ProcessStatus.WAIT_BYTE;

    public NettyClientHandler(Path rootFolder, AuthServerDb authServerDb) {
        this.rootFolder = rootFolder;
        this.authServerDb = authServerDb;
        isAuth = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client accepted!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client disconnected!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        while (buf.readableBytes() > 0) {
            if (currentStatus == ProcessStatus.WAIT_BYTE) {
                currentCommand = Command.defineCommand(buf.readByte());
                currentStatus = ProcessStatus.defineProcess(currentCommand);
            }

            if (currentCommand == Command.AUTHORIZATION) {

            }

        }
    }
}
