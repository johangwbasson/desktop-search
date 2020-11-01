package net.johanbasson.desktop.scanner;

import java.io.File;

public class ScannedFile {

    private File file;

    public ScannedFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
