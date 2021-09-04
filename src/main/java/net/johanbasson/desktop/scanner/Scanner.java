package net.johanbasson.desktop.scanner;

import net.johanbasson.desktop.watcher.FileWatchEvent;
import net.johanbasson.desktop.watcher.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Scanner implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(Scanner.class);
    private final BlockingQueue<ScannedFile> scanQueue;
    private final Path path;
    private final Random random = new Random();

    public Scanner(BlockingQueue<ScannedFile> scannedQueue, Path path) {
        this.scanQueue = scannedQueue;
        this.path = path;
    }

    @Override
    public void run() {
        scan(path.toFile());
    }

    private void scan(File file) {
        Thread.yield();
        if (file.isDirectory()) {
            Thread.yield();
            sleep();
            Arrays.asList(Objects.requireNonNull(file.listFiles())).forEach(this::scan);
        } else {
            log.debug("Detected file {}", file.getAbsolutePath());
            scanQueue.add(new ScannedFile(file));
            checkQueueSize();
            Thread.yield();
        }
    }

    private void checkQueueSize() {
        if (scanQueue.size() > 20) {
            do {
                log.info("Waiting for queue to be processed");
                try {
                    Thread.sleep(1000);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.yield();
            } while (scanQueue.size() > 20);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
