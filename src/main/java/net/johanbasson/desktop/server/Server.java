package net.johanbasson.desktop.server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import net.johanbasson.desktop.extract.Extract;
import net.johanbasson.desktop.extract.Extractor;
import net.johanbasson.desktop.index.Index;
import net.johanbasson.desktop.index.Indexer;
import net.johanbasson.desktop.lucene.LuceneIndex;
import net.johanbasson.desktop.lucene.Result;
import net.johanbasson.desktop.lucene.SearchResult;
import net.johanbasson.desktop.scanner.ScanProcessor;
import net.johanbasson.desktop.scanner.ScannedFile;
import net.johanbasson.desktop.scanner.Scanner;
import net.johanbasson.desktop.watcher.DirectoryWatcher;
import net.johanbasson.desktop.watcher.FileWatchEvent;
import net.johanbasson.desktop.watcher.WatchProcessor;
import org.apache.tika.Tika;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final Javalin javalin;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final LuceneIndex luceneIndex;
    private final Tika tika = new Tika();


    public Server() throws IOException {
        luceneIndex = new LuceneIndex(Paths.get("./indexes"));
        javalin = Javalin.create();
        javalin.events(eventListener -> {
            eventListener.serverStopping(() -> {
                log.info("Server stopping");
                executorService.shutdown();
                try {
                    luceneIndex.close();
                } catch (IOException e) {
                    log.error("Unable to close lucene index", e);
                }
            });

            eventListener.serverStopped(() -> {
                log.info("Server stopped");
            });
        });
        javalin.get("/", this::home);
        javalin.get("/search", this::search);
        javalin.get("/get", this::getFile);
    }

    public void start() throws IOException, ParseException {
        BlockingQueue<FileWatchEvent> watchQueue = new LinkedBlockingDeque<>();
        BlockingQueue<Extract> extractQueue = new LinkedBlockingDeque<>();
        BlockingQueue<Index> indexQueue = new LinkedBlockingDeque<>();
        BlockingQueue<ScannedFile> scanQueue = new LinkedBlockingDeque<>();

        executorService.submit(new DirectoryWatcher(Paths.get("/home/johan"), watchQueue));
        executorService.submit(new WatchProcessor(watchQueue, extractQueue));
        executorService.submit(new Extractor(extractQueue, indexQueue));
        executorService.submit(new Indexer(indexQueue, luceneIndex));
        executorService.submit(new Scanner(scanQueue, Paths.get("/home/johan")));
        executorService.submit(new ScanProcessor(luceneIndex, scanQueue, extractQueue));

        javalin.start("127.0.0.1", 7123);

    }

    public void stop() {
        javalin.stop();
    }

    private void home(Context ctx) {
        ctx.render("home.ftl");
    }

    private void search(Context ctx) {
        try {
            Result result = luceneIndex.search(ctx.queryParam("q"));
            Map<String, Object> params = new HashMap<>();
            params.put("result", result);
            ctx.render("results.ftl", params);
        } catch (Exception ex) {
            log.error("Search error", ex);
            Map<String, Object> params = new HashMap<>();
            params.put("error", ex.getMessage());
            ctx.render("500.ftl");

        }
    }

    private void getFile(Context ctx) {
        try {
            String fileName = ctx.queryParam("f");
            File file = new File(fileName);
            ctx.header("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            ctx.contentType(tika.detect(file));
            ctx.result(new BufferedInputStream(new FileInputStream(fileName)));
        } catch (Exception ex) {
            log.error("Search error", ex);
            Map<String, Object> params = new HashMap<>();
            params.put("error", ex.getMessage());
            ctx.render("500.ftl", params);
        }
    }
}
