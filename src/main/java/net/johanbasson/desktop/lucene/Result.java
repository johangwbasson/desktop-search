package net.johanbasson.desktop.lucene;

import java.util.List;

public class Result {

    private long hits;
    private List<SearchResult> results;

    public Result(long hits, List<SearchResult> results) {
        this.hits = hits;
        this.results = results;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public long getHits() {
        return hits;
    }
}
