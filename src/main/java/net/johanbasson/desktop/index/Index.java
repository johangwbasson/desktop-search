package net.johanbasson.desktop.index;

import java.io.File;

public class Index {

    private Action action;
    private String fileName;
    private String directory;
    private long size;
    private String contentType;
    private String content;

    public Index(Action action, String fileName, String directory, long size, String contentType, String content) {
        this.action = action;
        this.fileName = fileName;
        this.directory = directory;
        this.contentType = contentType;
        this.content = content;
        this.size = size;
    }

    public Action getAction() {
        return action;
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

    public String getContent() {
        return content;
    }

    public String getFullPath() {
        return directory.concat(File.pathSeparator).concat(fileName);
    }


    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Index{" +
                "action=" + action +
                ", fileName='" + fileName + '\'' +
                ", directory='" + directory + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
