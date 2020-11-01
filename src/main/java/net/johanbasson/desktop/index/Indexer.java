package net.johanbasson.desktop.index;

import net.johanbasson.desktop.lucene.LuceneIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.yield;


public class Indexer implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Indexer.class);
    private final BlockingQueue<Index> indexQueue;
    private final LuceneIndex luceneIndex;

    public Indexer(BlockingQueue<Index> indexQueue, LuceneIndex luceneIndex) throws IOException {
        this.indexQueue = indexQueue;
        this.luceneIndex = luceneIndex;
    }


    @Override
    public void run() {
        while (true) {
            yield();
            try {
                Index index = indexQueue.take();
                System.out.println("Index: " + index.toString());
                if (Action.MODIFY.equals(index.getAction())) {
                    try {
                        luceneIndex.modify(index);
                    } catch (IOException e) {
                        log.error("Unable to modify index", e);
                    }
                }

                if (Action.DELETE.equals(index.getAction())) {
                    try {
                        luceneIndex.delete(index);
                    } catch (IOException e) {
                        log.error("Unable to delete index", e);
                    }
                }

                if (Action.ADD.equals(index.getAction())) {
                    try {
                        luceneIndex.add(index);
                    } catch (IOException e) {
                        log.error("Unable to add index", e);
                    }
                }

            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        }
    }
}
