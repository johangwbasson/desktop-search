package net.johanbasson.desktop.lucene;

public class SearchResult {

    private String fullPath;
    private String filename;
    private String directory;
    private String extension;
    private long size;
    private String contentType;
    private float score;
    private String[] frags;


    public SearchResult(String fullPath, String filename, String directory, String extension, long size, String contentType, float score, String[] frags) {
        this.filename = filename;
        this.directory = directory;
        this.size = size;
        this.contentType = contentType;
        this.score = score;
        this.fullPath = fullPath;
        this.extension = extension;
        this.frags = frags;
    }

    public String getFilename() {
        return filename;
    }

    public String getDirectory() {
        return directory;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public double getScore() {
        return score;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getExtension() {
        return extension;
    }

    public String[] getFrags() {
        return frags;
    }
}
