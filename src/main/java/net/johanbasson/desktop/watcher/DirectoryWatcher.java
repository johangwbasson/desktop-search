package net.johanbasson.desktop.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.yield;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class DirectoryWatcher implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);
    private final Path path;
    private final BlockingQueue<FileWatchEvent> queue;

    public DirectoryWatcher(Path path, BlockingQueue<FileWatchEvent> queue) {
        this.path = path;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            FileSystem fileSystem = path.getFileSystem();

            try (WatchService service = fileSystem.newWatchService()) {
                // We watch for modification events
                path.register(service, ENTRY_MODIFY);

                // Start the infinite polling loop
                while (true) {
                    yield();
                    WatchKey watchKey = service.take();
                    for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (kind == ENTRY_MODIFY) {
                            Path watchEventPath = (Path) watchEvent.context();
                            Path fullPath = path.resolve(watchEventPath);
                            log.info("Modified FileWatchEvent - " + fullPath.toString());
                            queue.add(new FileWatchEvent(fullPath, Type.MODIFIED));
                        }

                        if (kind == ENTRY_DELETE) {
                            Path watchEventPath = (Path) watchEvent.context();
                            Path fullPath = path.resolve(watchEventPath);
                            log.info("Modified FileWatchEvent - " + fullPath.toString());
                            queue.add(new FileWatchEvent(fullPath, Type.DELETED));
                        }
                    }

                    yield();
                }
            } catch (IOException e) {
                log.error("Unable to allocate watch service", e);
            }
        } catch (Exception e) {
            log.error("Unable to get File System", e);
        }
    }
}
