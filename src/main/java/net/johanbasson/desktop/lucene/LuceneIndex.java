package net.johanbasson.desktop.lucene;

import net.johanbasson.desktop.index.Index;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LuceneIndex {

    private Directory dir;
    private Analyzer analyzer = new StandardAnalyzer();
    private IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

    public LuceneIndex(Path path) throws IOException {
        dir = FSDirectory.open(path);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    }

    public void add(Index index) throws IOException {
        Document doc = makeDocument(index);


        IndexWriter writer = new IndexWriter(dir, iwc);
        writer.addDocument(doc);
        writer.commit();
        writer.close();
    }

    @NotNull
    private Document makeDocument(Index index) {
        Document doc = new Document();
        doc.add(new StringField(Fields.FILENAME.name(), index.getFileName(), Field.Store.YES));
        doc.add(new StringField(Fields.DIRECTORY.name(), index.getDirectory(), Field.Store.YES));
        doc.add(new StoredField(Fields.FULL_PATH.name(), index.getFullPath()));
        doc.add(new StringField(Fields.CONTENT_TYPE.name(), index.getContentType(), Field.Store.YES));
        doc.add(new LongPoint(Fields.SIZE.name(), index.getSize()));
        doc.add(new StoredField(Fields.SIZE.name(), index.getSize()));
        if (index.getContent() != null) {
            doc.add(new StringField(Fields.CONTENT.name(), index.getContent(), Field.Store.YES));
        }
        return doc;
    }

    public void modify(Index index) throws IOException {
        Document doc = makeDocument(index);
        IndexWriter writer = new IndexWriter(dir, iwc);
        writer.deleteDocuments(new Term(Fields.FULL_PATH.name(), index.getFullPath()));
        writer.addDocument(doc);
        writer.commit();
        writer.close();

    }

    public void delete(Index index) throws IOException {
        IndexWriter writer = new IndexWriter(dir, iwc);
        writer.deleteDocuments(new Term(Fields.FULL_PATH.name(), index.getFullPath()));
        writer.commit();
        writer.close();
    }

    public boolean contains(File file) throws IOException {
        IndexReader indexReader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(new TermQuery(new Term(Fields.FULL_PATH.name(), file.getAbsolutePath())), 1);
        if (topDocs.totalHits.value == 0) {
            return false;
        } else {
            ScoreDoc scoreDoc = topDocs.scoreDocs[0];
            Document doc = searcher.doc(scoreDoc.doc);
            IndexableField field = doc.getField(Fields.SIZE.name());
            return field.numericValue().longValue() == file.length();
        }
    }
}
