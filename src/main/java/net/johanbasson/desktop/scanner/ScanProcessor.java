package net.johanbasson.desktop.scanner;

import net.johanbasson.desktop.extract.Extract;
import net.johanbasson.desktop.lucene.LuceneIndex;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ScanProcessor implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ScanProcessor.class);
    private final LuceneIndex luceneIndex;
    private final BlockingQueue<ScannedFile> scanQueue;
    private final BlockingQueue<Extract> extractQueue;
    private final Tika tika = new Tika();

    public ScanProcessor(LuceneIndex index, BlockingQueue<ScannedFile> scanQueue, BlockingQueue<Extract> extractQueue) {
        this.luceneIndex = index;
        this.extractQueue = extractQueue;
        this.scanQueue = scanQueue;
    }


    @Override
    public void run() {
        while (true) {

            try {
                ScannedFile scannedFile = scanQueue.take();

                try {
                    if (!luceneIndex.contains(scannedFile.getFile())) {
                        String contentType = null;
                        try {
                            contentType = tika.detect(scannedFile.getFile());
                            extractQueue.add(new Extract(scannedFile.getFile().getName(), scannedFile.getFile().getParentFile().getAbsolutePath(), scannedFile.getFile().length(), contentType));
                        } catch (IOException e) {
                            log.error("Could not detect content type", e);
                        }
                    }
                } catch (IOException e) {
                    log.error("Could not query lucene index to see if doc exists", e);
                }

            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }

        }
    }
}
