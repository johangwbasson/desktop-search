package net.johanbasson.desktop.extract;

import net.johanbasson.desktop.extract.extractors.ExtractorMapping;
import net.johanbasson.desktop.extract.extractors.Extractors;
import net.johanbasson.desktop.extract.extractors.FileExtractor;
import net.johanbasson.desktop.index.Action;
import net.johanbasson.desktop.index.Index;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.yield;

public class Extractor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Extractor.class);
    private final BlockingQueue<Extract> extractQueue;
    private final BlockingQueue<Index> indexQueue;
    private final List<ExtractorMapping> mappings;

    public Extractor(BlockingQueue<Extract> extractQueue, BlockingQueue<Index> indexQueue) throws IOException, ParseException {
        this.extractQueue = extractQueue;
        this.indexQueue = indexQueue;
        this.mappings = Extractors.load();
    }

    @Override
    public void run() {
        while (true) {
            yield();
            try {
                Extract extract = extractQueue.take();
                log.debug("Received extract request: {}", extract.toString());
                FileExtractor fileExtractor = getFileExtractor(extract);
                if (fileExtractor != null) {
                    log.debug("Extracting text from file");
                    File file = new File(extract.getFullPath());
                    try {
                        log.debug("Queuing for indexing");
                        indexQueue.add(new Index(Action.ADD, extract.getFileName(), extract.getDirectory(), extract.getSize(), extract.getContentType(), fileExtractor.extract(file)));
                    } catch (IOException e) {
                        log.error("Could not extract text from file", e);
                    }
                } else {
                    log.info("Queuing for indexing");
                    indexQueue.add(new Index(Action.ADD, extract.getFileName(), extract.getDirectory(), extract.getSize(), extract.getContentType(), null));
                }

            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }

        }
    }

    private FileExtractor getFileExtractor(Extract extract) {
        for (ExtractorMapping mapping : mappings) {
            if (mapping.getContentTypes().contains(extract.getContentType().trim().toLowerCase())) {
                return mapping.getFileExtractor();
            }
        }

        log.info("Content Type: " + extract.getContentType());
        return null;
    }

}
