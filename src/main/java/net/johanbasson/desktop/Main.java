package net.johanbasson.desktop;

import io.javalin.Javalin;
import net.johanbasson.desktop.extract.Extract;
import net.johanbasson.desktop.extract.Extractor;
import net.johanbasson.desktop.index.Index;
import net.johanbasson.desktop.index.Indexer;
import net.johanbasson.desktop.lucene.LuceneIndex;
import net.johanbasson.desktop.scanner.ScanProcessor;
import net.johanbasson.desktop.scanner.ScannedFile;
import net.johanbasson.desktop.scanner.Scanner;
import net.johanbasson.desktop.watcher.DirectoryWatcher;
import net.johanbasson.desktop.watcher.FileWatchEvent;
import net.johanbasson.desktop.watcher.WatchProcessor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ParseException {
        BlockingQueue<FileWatchEvent> watchQueue = new LinkedBlockingDeque<>();
        BlockingQueue<Extract> extractQueue = new LinkedBlockingDeque<>();
        BlockingQueue<Index> indexQueue = new LinkedBlockingDeque<>();
        BlockingQueue<ScannedFile> scanQueue = new LinkedBlockingDeque<>();

        LuceneIndex luceneIndex = new LuceneIndex(Paths.get("./indexes"));


        ExecutorService executorService = Executors.newFixedThreadPool(5);

        executorService.submit(new DirectoryWatcher(Paths.get("/home/johan"), watchQueue));
        executorService.submit(new WatchProcessor(watchQueue, extractQueue));
        executorService.submit(new Extractor(extractQueue, indexQueue));
        executorService.submit(new Indexer(indexQueue, luceneIndex));
        executorService.submit(new Scanner(scanQueue, Paths.get("/home/johan")));
        executorService.submit(new ScanProcessor(luceneIndex, scanQueue, extractQueue));


        log.info("Threads started");

        Javalin javalin = Javalin.create();
        javalin.start(7123);

        log.info("Server started");

    }
}
