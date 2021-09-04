package net.johanbasson.desktop.extract.extractors;

import java.util.Set;

public class ExtractorMapping {
    private FileExtractor fileExtractor;
    private Set<String> contentTypes ;

    public ExtractorMapping(FileExtractor fileExtractor, Set<String> contentTypes) {
        this.fileExtractor = fileExtractor;
        this.contentTypes = contentTypes;
    }

    public FileExtractor getFileExtractor() {
        return fileExtractor;
    }

    public Set<String> getContentTypes() {
        return contentTypes;
    }
}
