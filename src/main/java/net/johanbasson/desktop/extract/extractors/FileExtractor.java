package net.johanbasson.desktop.extract.extractors;

import java.io.File;
import java.io.IOException;

public interface FileExtractor {

    public String extract(File file) throws IOException;

}
