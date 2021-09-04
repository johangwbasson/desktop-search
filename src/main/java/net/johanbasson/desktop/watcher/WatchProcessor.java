package net.johanbasson.desktop.watcher;

import net.johanbasson.desktop.exclusions.Exclusions;
import net.johanbasson.desktop.extract.Extract;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.yield;

public class WatchProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WatchProcessor.class);
    private final Tika tika = new Tika();
    private final BlockingQueue<FileWatchEvent> watchQueue;
    private final BlockingQueue<Extract> extractQueue;

    public WatchProcessor(BlockingQueue<FileWatchEvent> watchQueue, BlockingQueue<Extract> extractQueue) {
        this.watchQueue = watchQueue;
        this.extractQueue = extractQueue;
    }

    @Override
    public void run() {
        while (true) {
            yield();
            try {
                FileWatchEvent event = watchQueue.take();
                log.info("Received: " + event.getFile() + " Type : " + event.getType());

                if (!Exclusions.isExcluded(event.getFile().toFile())) {

                    if (Type.MODIFIED.equals(event.getType())) {
                        File file = event.getFile().toFile();
                        if (file.exists()) {
                            try {
                                String contentType = tika.detect(file);
                                extractQueue.add(new Extract(file.getName(), file.getParentFile().getAbsolutePath(), file.length(), contentType));
                            } catch (IOException ex) {
                                log.error("Could not detect content type", ex);
                            }
                        }
                    }

                } else {
                    log.info("File {} is excluded", event.getFile());
                }
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        }
    }
}
