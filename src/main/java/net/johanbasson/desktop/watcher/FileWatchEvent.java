package net.johanbasson.desktop.watcher;

import java.nio.file.Path;

public class FileWatchEvent {

    private Path file;
    private Type type;

    public FileWatchEvent(Path file, Type type) {
        this.file = file;
        this.type = type;
    }

    public Path getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }
}
