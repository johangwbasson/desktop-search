package net.johanbasson.desktop.index;

import net.johanbasson.desktop.exclusions.Exclusions;
import net.johanbasson.desktop.lucene.LuceneIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
                log.debug("Received index request: {}", index.toString());

                if (!Exclusions.isExcluded(index.getFullPath())) {
                    if (Action.MODIFY.equals(index.getAction())) {
                        try {
                            log.debug("MODIFY index : {}", index.toString());
                            luceneIndex.modify(index);
                        } catch (IOException e) {
                            log.error("Unable to modify index", e);
                        }
                    }

                    if (Action.DELETE.equals(index.getAction())) {
                        try {
                            log.debug("DELETE index : {}", index.toString());
                            luceneIndex.delete(index);
                        } catch (IOException e) {
                            log.error("Unable to delete index", e);
                        }
                    }

                    if (Action.ADD.equals(index.getAction())) {
                        try {
                            log.debug("ADD index : {}", index.toString());
                            log.info("> {}", index.getFullPath());
                            luceneIndex.add(index);
                        } catch (IOException e) {
                            log.error("Unable to add index", e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        }
    }
}
