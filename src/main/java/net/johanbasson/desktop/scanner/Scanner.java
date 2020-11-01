package net.johanbasson.desktop.scanner;

import net.johanbasson.desktop.watcher.FileWatchEvent;
import net.johanbasson.desktop.watcher.Type;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Scanner implements Runnable{

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
        sleep();
        Thread.yield();
        if (file.isDirectory()) {
            Thread.yield();
            Arrays.asList(Objects.requireNonNull(file.listFiles())).forEach(this::scan);
        } else {
            scanQueue.add(new ScannedFile(file));
            Thread.yield();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(1000 + random.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
