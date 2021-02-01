package com.temzu.cloud_storage.server.storage;

import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.operation.ProcessStatus;
import com.temzu.cloud_storage.server.database.service.UserService;
import com.temzu.cloud_storage.server.database.service.UserServiceImpl;
import com.temzu.cloud_storage.util.AuthUserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    private boolean isAuth;

    private Path rootFolder;

    private UserService userService;
    private AuthUserUtil authUserUtil;
    private FileTransfer fileTransfer;
    private Path userFolder;

    private Command currentCommand = null;
    private ProcessStatus currentStatus = ProcessStatus.WAIT_BYTE;

    public NettyClientHandler(Path rootFolder) {
        this.rootFolder = rootFolder;
        this.userService = new UserServiceImpl();
        this.authUserUtil = new AuthUserUtil();
        this.fileTransfer = new FileTransfer();
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
        LOG.debug("Client byte");
        while (buf.readableBytes() > 0) {
            if (currentStatus == ProcessStatus.WAIT_BYTE) {
                currentCommand = Command.defineCommand(buf.readByte());
                currentStatus = ProcessStatus.defineProcess(currentCommand);
            }

            switch (currentCommand) {
                case AUTHORIZATION:
                    authClient(buf, ctx);
                    break;
                case GET_FILES_LIST:
                    getFilesList(ctx);
                    break;
                case DOWNLOAD_FILE:
                    sendFile(buf, ctx);
                    break;
            }

            currentStatus = ProcessStatus.WAIT_BYTE;
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void authClient(ByteBuf buf, ChannelHandlerContext ctx) throws IOException {
        LOG.debug("Client tries to log in...");
        currentStatus = authUserUtil.handleAuthData(buf, currentStatus);
        currentStatus = userService.authClient(authUserUtil.getLogin(), authUserUtil.getPassword(), currentStatus);

        if (currentStatus == ProcessStatus.AUTH_SUCCESS) {
            LOG.debug("Client " + authUserUtil.getLogin() + " logged in!");
            isAuth = true;
            userFolder = Paths.get(rootFolder.toString(), authUserUtil.getLogin());
            if (!Files.exists(userFolder)) {
                Files.createDirectory(userFolder);
            }

            ctx.writeAndFlush(authUserUtil.completeAuth());
        } else {
            LOG.debug("Client " + authUserUtil.getLogin() + " was unable to connect to the server!");
        }
    }

    private void getFilesList(ChannelHandlerContext ctx) {
        if (isAuth) {
            LOG.debug("Client tries to get files list...");
            String filesList = null;
            try {
                filesList = Files.list(userFolder)
                        .map((f) -> f.getFileName().toString())
                        .collect(Collectors.joining("/", "", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileTransfer.setCurrentFolder(userFolder.toString());
            ctx.writeAndFlush(fileTransfer.sendSomeMessage(filesList, Command.SEND_FILES_LIST));
            LOG.debug("Files list sent to client!");
        }
    }

    private void sendFile(ByteBuf buf, ChannelHandlerContext ctx) {
        currentStatus = fileTransfer.readFileName(buf);
        if (currentStatus == ProcessStatus.READ_FILE_READY) {
            ctx.writeAndFlush(fileTransfer.sendFileParameters());
            fileTransfer.sendFile(ctx.channel());
        }
    }
}
