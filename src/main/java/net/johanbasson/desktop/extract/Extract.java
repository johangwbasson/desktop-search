package net.johanbasson.desktop.extract;

import java.io.File;

public class Extract {

    private String fileName;
    private String directory;
    private long size;
    private String contentType;

    public Extract(String fileName, String directory, long size, String contentType) {
        this.fileName = fileName;
        this.directory = directory;
        this.contentType = contentType;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFullPath() {
        return directory.concat(File.pathSeparator).concat(fileName);
    }

    public long getSize() {
        return size;
    }
}
