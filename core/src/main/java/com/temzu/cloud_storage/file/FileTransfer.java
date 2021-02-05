package com.temzu.cloud_storage.file;

import com.temzu.cloud_storage.callback.CallBackProgress;
import com.temzu.cloud_storage.callback.Callback;
import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.operation.ProcessStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(FileTransfer.class);

    private long fileLength;

    private final int CMD_BYTE_LENGTH = 1;
    private final int FILE_NAME_LENGTH = 4;
    private final int FILE_LENGTH = 8;


    private String currentFolder;

    private String currentFilename;
    private String newFileName;

    private Path downloadFile;
    private Path uploadFile;
    private RandomAccessFile raf;
    private FileChannel fChannel;
    private long countBytes;
    private int tempCount;
    private Callback getFileListCallBack;
    private Callback downloadFileCallback;
    private Callback deleteFileCallback;
    private CallBackProgress progressBarCallback;

    public ByteBuf sendSomeMessage(String message, Command command) {
        LOG.debug("Send message: " + command.toString() + " " + message);
        byte[] bytesMessage = message.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(CMD_BYTE_LENGTH + FILE_NAME_LENGTH + bytesMessage.length);
        buf.writeByte(command.getOperationCode());
        buf.writeInt(bytesMessage.length);
        buf.writeBytes(bytesMessage);
        return buf;
    }

    public ByteBuf requestFileList(String directoryName){
        return sendSomeMessage(directoryName, Command.GET_FILES_LIST);
    }

    public ProcessStatus takeFileList(ByteBuf buf, ProcessStatus status) {
        if (status == ProcessStatus.GET_FILES_LIST) {
            LOG.debug("Take files list from server");
            int len = buf.readInt();
            byte[] filesByte = new byte[len];
            buf.readBytes(filesByte);
            List<String> filesList = Arrays.stream(new String(filesByte, StandardCharsets.UTF_8).split("/"))
                    .collect(Collectors.toCollection(ArrayList::new));
            getFileListCallBack.call(filesList);
            LOG.debug("Files list: " + filesList);
            status = ProcessStatus.WAIT_BYTE;
        }
        System.out.println(status.toString());
        return status;
    }

    public ProcessStatus readFileName(ByteBuf buf) {
        ProcessStatus status;
        try {
            LOG.debug("Read file name...");
            int len = buf.readInt();
            byte[] filesByte = new byte[len];
            buf.readBytes(filesByte);
            currentFilename = new String(filesByte, StandardCharsets.UTF_8);
            downloadFile = Paths.get(currentFolder, currentFilename);
            fileLength = Files.size(downloadFile);
            status =  ProcessStatus.READ_FILE_READY;
            LOG.debug("File name is \"" + currentFilename + "\"" );

        } catch (IOException e) {
            LOG.error("e = " + e);
            status =  ProcessStatus.READ_FILE_ERROR;
        }
        return status;
    }

    public ByteBuf sendFileParameters() {
        byte[] fileNameBytes = downloadFile.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buff = ByteBufAllocator.DEFAULT.directBuffer(CMD_BYTE_LENGTH + FILE_NAME_LENGTH + fileNameBytes.length + FILE_LENGTH);
        buff.writeByte(Command.DOWNLOAD_FILE_SUCCESS.getOperationCode());
        buff.writeInt(fileNameBytes.length);
        buff.writeBytes(fileNameBytes);
        buff.writeLong(fileLength);
        try {
            countBytes = 0;
            raf = new RandomAccessFile(downloadFile.toFile(), "r");
            fChannel = raf.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return buff;
    }

    public ProcessStatus readFileParameters(ByteBuf buf, ProcessStatus processStatus) {
        if (processStatus == ProcessStatus.START_DOWNLOAD_PROCESS) {
            System.out.println("Start download process");
            downloadFile = null;
            raf = null;
            fChannel = null;

            int len = buf.readInt();
            byte[] filesByte = new byte[len];
            buf.readBytes(filesByte);
            currentFilename = new String(filesByte, StandardCharsets.UTF_8);
            fileLength = buf.readLong();
            processStatus = ProcessStatus.DOWNLOAD_PROCESS;
        }
        return processStatus;
    }

    public void sendFile(Channel channel) {
        try {
            ByteBuf answer;
            ByteBuffer bufRead = ByteBuffer.allocate(32000);
            int bytesRead = fChannel.read(bufRead);
            countBytes = countBytes + bytesRead;
            while (bytesRead != -1 && countBytes <= fileLength) {
                answer = ByteBufAllocator.DEFAULT.directBuffer(32000);
                bufRead.flip();
                while(bufRead.hasRemaining()){
                    byte[] fileBytes = new byte[bytesRead];
                    bufRead.get(fileBytes);
                    answer.writeBytes(fileBytes);
                    channel.writeAndFlush(answer);
                }
                bufRead.clear();
                bytesRead = fChannel.read(bufRead);
                countBytes = countBytes + bytesRead;
                callProgressBar(countBytes, fileLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ProcessStatus readFile(ByteBuf buf, ProcessStatus processStatus) throws IOException {
        if(processStatus == ProcessStatus.DOWNLOAD_PROCESS) {
            countBytes = 0;
            tempCount = 0;
            downloadFile = Paths.get(currentFolder, currentFilename);
            raf = new RandomAccessFile(downloadFile.toFile(), "rw");
            fChannel = raf.getChannel();
            processStatus = ProcessStatus.START_DOWNLOAD_FILE;
        }

        if (processStatus == ProcessStatus.START_DOWNLOAD_FILE) {
            while (buf.readableBytes() > 0 && countBytes < fileLength) {
                tempCount = fChannel.write(buf.nioBuffer());
                countBytes = countBytes + tempCount;
                buf.readerIndex(buf.readerIndex() + tempCount);
                callProgressBar(countBytes, fileLength);
            }
            if (countBytes == fileLength) {
                if (downloadFileCallback != null) {
                    downloadFileCallback.call();
                }
                processStatus = ProcessStatus.WAIT_BYTE;
                raf.close();
            }
        }
        return processStatus;
    }
    private void callProgressBar(long current, long size){
        if(progressBarCallback != null && size != 0){
            progressBarCallback.call((double) current/size);
        }
    }

    public ByteBuf requestUploadFile(String fileName) {
        currentFilename = fileName;
        uploadFile = Paths.get(currentFolder, currentFilename);
        System.out.println(uploadFile.toString());
        try {
            fileLength = Files.size(uploadFile);
        } catch (IOException e) {
            LOG.error("Error determining file size: e = " + e);
            return null;
        }
        byte[] fileNameBytes = uploadFile.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buff = ByteBufAllocator.DEFAULT.directBuffer(CMD_BYTE_LENGTH + FILE_NAME_LENGTH + fileNameBytes.length + FILE_LENGTH);
        buff.writeByte(Command.UPLOAD_FILE.getOperationCode());
        buff.writeInt(fileNameBytes.length);
        buff.writeBytes(fileNameBytes);
        buff.writeLong(fileLength);
        try {
            raf = new RandomAccessFile(uploadFile.toFile(), "r");
            fChannel = raf.getChannel();
            countBytes = 0l;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return buff;
    }

    public ProcessStatus renameFile(ByteBuf buf) {
        int len = buf.readInt();
        byte[] filesByte = new byte[len];
        buf.readBytes(filesByte);
        String str = new String(filesByte, StandardCharsets.UTF_8);
        String[] strings = str.split("\\s");
        currentFilename = strings[0];
        newFileName = strings[1];
        return ProcessStatus.RENAME_FILE_READY;
    }

    public void setDeleteFileCallback(Callback deleteFileCallback) {
        this.deleteFileCallback = deleteFileCallback;
    }

    public String getCurrentFilename() {
        return currentFilename;
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    public void setGetFileListCallBack(Callback getFileListCallBack) {
        this.getFileListCallBack = getFileListCallBack;
    }

    public void setDownloadFileCallback(Callback downloadFileCallback) {
        this.downloadFileCallback = downloadFileCallback;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setProgressBarCallback(CallBackProgress progressBarCallback) {
        this.progressBarCallback = progressBarCallback;
    }
}
