package com.temzu.cloud_storage.client.network;

import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.operation.ProcessStatus;
import com.temzu.cloud_storage.util.AuthUserUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

    private FileTransfer fileTransfer;
    private AuthUserUtil authUserUtil;
    private Command currentCommand = Command.DEFAULT;
    private ProcessStatus processStatus = ProcessStatus.WAIT_BYTE;

    public ServerHandler(AuthUserUtil authUserUtil, FileTransfer fileTransfer) {
        this.authUserUtil = authUserUtil;
        this.fileTransfer = fileTransfer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if (processStatus == ProcessStatus.WAIT_BYTE) {
                currentCommand = Command.defineCommand(buf.readByte());
                processStatus = ProcessStatus.defineProcess(currentCommand);
                switch (currentCommand) {
                    case AUTHORIZATION_COMPLETED:
                        authUserUtil.callLogIn();
                        LOG.debug("Authorization successful!");
                        break;
                    case SEND_FILES_LIST:
                        processStatus = fileTransfer.takeFileList(buf, processStatus);
                        break;
                    case DOWNLOAD_FILE_SUCCESS:
                        processStatus = fileTransfer.readFileParameters(buf, processStatus);
                        break;
                }
            }
            if (currentCommand == Command.DOWNLOAD_FILE_SUCCESS) {
                processStatus = fileTransfer.readFile(buf, processStatus);
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
