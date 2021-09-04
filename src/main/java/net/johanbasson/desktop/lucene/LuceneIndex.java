package net.johanbasson.desktop.lucene;

import net.johanbasson.desktop.highlight.Highlighter;
import net.johanbasson.desktop.index.Index;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
//import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LuceneIndex {

    private static final Logger log = LoggerFactory.getLogger(LuceneIndex.class);
    private final Directory dir;
    private final Analyzer analyzer = new StandardAnalyzer();
    private final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    private final IndexWriter indexWriter;
    private boolean open = true;

    public LuceneIndex(Path path) throws IOException {
        dir = FSDirectory.open(path);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(dir, iwc);
    }

    public void close() throws IOException {
        open = false;
        indexWriter.close();
    }

    public void add(Index index) throws IOException {
        if (open) {
            Document doc = makeDocument(index);
            indexWriter.addDocument(doc);
            indexWriter.commit();
        }
    }

    @NotNull
    private Document makeDocument(Index index) {
        Document doc = new Document();
        doc.add(new TextField(Fields.FILENAME.name(), index.getFileName(), Field.Store.YES));
        doc.add(new TextField(Fields.DIRECTORY.name(), index.getDirectory(), Field.Store.YES));
        doc.add(new TextField(Fields.EXTENSTION.name(), FilenameUtils.getExtension(index.getFileName()), Field.Store.YES));
        doc.add(new TextField(Fields.FULL_PATH.name(), index.getFullPath(), Field.Store.YES));
        doc.add(new TextField(Fields.CONTENT_TYPE.name(), index.getContentType(), Field.Store.YES));
        doc.add(new TextField(Fields.HASH.name(), DigestUtils.md5Hex(index.getFullPath()), Field.Store.YES));
        doc.add(new LongPoint(Fields.SIZE.name(), index.getSize()));
        doc.add(new StoredField(Fields.SIZE.name(), index.getSize()));
        if (index.getContent() != null) {
            doc.add(new TextField(Fields.CONTENT.name(), index.getContent(), Field.Store.YES));
        }
        return doc;
    }

    public void modify(Index index) throws IOException {
        if (open) {
            Document doc = makeDocument(index);
            indexWriter.deleteDocuments(new Term(Fields.FULL_PATH.name(), index.getFullPath()));
            indexWriter.addDocument(doc);
            indexWriter.commit();
        }
    }

    public void delete(Index index) throws IOException {
        if (open) {
//        try (IndexWriter writer = new IndexWriter(dir, iwc)) {
            indexWriter.deleteDocuments(new Term(Fields.FULL_PATH.name(), index.getFullPath()));
            indexWriter.commit();
//        }
        }
    }

    public boolean contains(File file) throws IOException {
        try {
            IndexReader indexReader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            String hash = DigestUtils.md5Hex(file.getAbsolutePath());
            TopDocs topDocs = searcher.search(new TermQuery(new Term(Fields.HASH.name(), hash)), 1);
            if (topDocs.totalHits.value == 0) {
                return false;
            } else {
                ScoreDoc scoreDoc = topDocs.scoreDocs[0];
                Document doc = searcher.doc(scoreDoc.doc);
                IndexableField field = doc.getField(Fields.SIZE.name());
                return field.numericValue().longValue() == file.length();
            }
            // TODO - Handle gracefull
        } catch (IndexNotFoundException ex) {
            return false;
        }
    }

    public Result search(String text) throws IOException, ParseException {
        if (text == null || text.trim().length() == 0) {
            log.warn("Text is empty");
            return new Result(0, new ArrayList<>());
        }
        List<SearchResult> results = new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(indexReader);

        QueryParser fileNameQP = new QueryParser(Fields.FILENAME.name(), new StandardAnalyzer());
        Query fileNameQuery = fileNameQP.parse(text);

        QueryParser contentQP = new QueryParser(Fields.CONTENT.name(), new StandardAnalyzer());
        Query contentQuery = contentQP.parse(text);

        BooleanQuery qry = new BooleanQuery.Builder().add(fileNameQuery, BooleanClause.Occur.SHOULD).add(contentQuery, BooleanClause.Occur.SHOULD).build();
        TopDocs hits = searcher.search(qry, 50);

//        Formatter formatter = new SimpleHTMLFormatter();
//        QueryScorer scorer = new QueryScorer(fileNameQuery);
//        Highlighter highlighter = new Highlighter(formatter, scorer);
//        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);
        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
        //Fragmenter fragmenter = new SimpleFragmenter(10);

//        highlighter.setTextFragmenter(fragmenter);


        for (int t = 0; t < hits.scoreDocs.length; t++) {
            int id = hits.scoreDocs[t].doc;
            ScoreDoc scoreDoc = hits.scoreDocs[t];
            Document doc = searcher.doc(scoreDoc.doc);
            String content = doc.getField(Fields.CONTENT.name()).stringValue();

            List<String> frags = Highlighter.highlight(content, text, 10);

            results.add(new SearchResult(doc.getField(Fields.FULL_PATH.name()).stringValue(),
                    doc.getField(Fields.FILENAME.name()).stringValue(),
                    doc.getField(Fields.DIRECTORY.name()).stringValue(),
                    doc.getField(Fields.EXTENSTION.name()).stringValue(),
                    doc.getField(Fields.SIZE.name()).numericValue().longValue(),
                    doc.getField(Fields.CONTENT_TYPE.name()).stringValue(),
                    scoreDoc.score, frags.toArray(new String[frags.size()])));

//            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, id, Fields.CONTENT.name(), analyzer);
//            try {
//                String[] frags = highlighter.getBestFragments(tokenStream, content, 10);
//                log.info("Query: {}", text );
//                log.info("Frags: {}", frags);
//                results.add(new SearchResult(doc.getField(Fields.FULL_PATH.name()).stringValue(),
//                        doc.getField(Fields.FILENAME.name()).stringValue(),
//                        doc.getField(Fields.DIRECTORY.name()).stringValue(),
//                        doc.getField(Fields.EXTENSTION.name()).stringValue(),
//                        doc.getField(Fields.SIZE.name()).numericValue().longValue(),
//                        doc.getField(Fields.CONTENT_TYPE.name()).stringValue(),
//                        scoreDoc.score, frags));
//            } catch (InvalidTokenOffsetsException e) {
//                log.error("Unable to tokenize", e);
//                results.add(new SearchResult(doc.getField(Fields.FULL_PATH.name()).stringValue(),
//                        doc.getField(Fields.FILENAME.name()).stringValue(),
//                        doc.getField(Fields.DIRECTORY.name()).stringValue(),
//                        doc.getField(Fields.EXTENSTION.name()).stringValue(),
//                        doc.getField(Fields.SIZE.name()).numericValue().longValue(),
//                        doc.getField(Fields.CONTENT_TYPE.name()).stringValue(),
//                        scoreDoc.score, new String[]{}));
//            }

        }
        return new Result(hits.totalHits.value, results);
    }


}
