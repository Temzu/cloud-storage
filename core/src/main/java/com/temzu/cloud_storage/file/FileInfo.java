package com.temzu.cloud_storage.file;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileInfo {
    public static String UP_TOKEN = "[..]";

    private String fileName;
    private long length;

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.length = -1;
            } else {
                this.length = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with file: " + path.toAbsolutePath().toString());
        }
    }

    public FileInfo(String fileName, long length) {
        this.fileName = fileName;
        this.length = -2L;
    }

    public boolean isUpDirectory() {
        return length == -2L;
    }

    public boolean isDirectory() {
        return length == -1L;
    }

}

