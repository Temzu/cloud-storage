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
    private ProcessStatus processStatus = ProcessStatus.WAIT_BYTE;

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
            if (processStatus == ProcessStatus.WAIT_BYTE) {
                currentCommand = Command.defineCommand(buf.readByte());
                processStatus = ProcessStatus.defineProcess(currentCommand);

                switch (currentCommand) {
                    case AUTHORIZATION:
                        processStatus = authClient(buf, ctx, processStatus);
                        break;
                    case GET_FILES_LIST:
                        processStatus = getFilesList(ctx, processStatus);
                        break;
                    case DOWNLOAD_FILE:
                        sendFile(buf, ctx);
                        break;
                    case UPLOAD_FILE:
                        System.out.println("Upload: " + processStatus.toString() + " " + currentCommand.toString());
                        processStatus = fileTransfer.readFileParameters(buf, processStatus);
                        break;
                }
            }

            if (currentCommand == Command.UPLOAD_FILE) {
                processStatus = fileTransfer.readFile(buf, processStatus);
                if (processStatus == ProcessStatus.WAIT_BYTE) {
                    processStatus = getFilesList(ctx, processStatus);
                }
            }
        }
        System.out.println(processStatus.toString());
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private ProcessStatus authClient(ByteBuf buf, ChannelHandlerContext ctx, ProcessStatus processStatus) throws IOException {
        LOG.debug("Client tries to log in...");
        processStatus = authUserUtil.handleAuthData(buf, processStatus);
        processStatus = userService.authClient(authUserUtil.getLogin(), authUserUtil.getPassword(), processStatus);

        if (processStatus == ProcessStatus.AUTH_SUCCESS) {
            LOG.debug("Client " + authUserUtil.getLogin() + " logged in!");
            isAuth = true;
            userFolder = Paths.get(rootFolder.toString(), authUserUtil.getLogin());
            if (!Files.exists(userFolder)) {
                Files.createDirectory(userFolder);
            }
            fileTransfer.setCurrentFolder(userFolder.toString());
            ctx.writeAndFlush(authUserUtil.completeAuth());
            processStatus = ProcessStatus.WAIT_BYTE;
        } else {
            LOG.debug("Client " + authUserUtil.getLogin() + " was unable to connect to the server!");
        }
        return processStatus;
    }

    private ProcessStatus getFilesList(ChannelHandlerContext ctx, ProcessStatus processStatus) {
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
            processStatus = ProcessStatus.WAIT_BYTE;
        }
        return processStatus;
    }

    private void sendFile(ByteBuf buf, ChannelHandlerContext ctx) {
        processStatus = fileTransfer.readFileName(buf);
        if (processStatus == ProcessStatus.READ_FILE_READY) {
            ctx.writeAndFlush(fileTransfer.sendFileParameters());
            fileTransfer.sendFile(ctx.channel());
        }
        processStatus = ProcessStatus.WAIT_BYTE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("e = " + cause);
        ctx.close();
    }
}
