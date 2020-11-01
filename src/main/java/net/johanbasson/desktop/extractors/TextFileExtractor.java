package net.johanbasson.desktop.extractors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TextFileExtractor implements FileExtractor {
    @Override
    public String extract(File file) throws IOException {
        if (file.exists()) {
            return FileUtils.readFileToString(file, Charsets.UTF_8);
        }

        return null;
    }
}
