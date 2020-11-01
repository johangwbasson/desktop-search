package net.johanbasson.desktop.extractors;

import java.io.File;
import java.io.IOException;

public interface FileExtractor {

    public String extract(File file) throws IOException;

}
