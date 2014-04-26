package org.rodart.indexer;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class BookIndexer implements BookSink {
  private IndexWriter writer;

  public BookIndexer(IndexWriter indexWriter) {
    writer = indexWriter;
  }

  public void add(Book book) {
    if (book == null) {
      return;
    }
    try {
      Document document = fromBook(book);
      if (document != null) {
        writer.addDocument(document);
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static Document fromBook(Book book) {
    String title = book.getTitle();
    String text = book.getText();
    text = text.replace('\n', ' '); //TODO:???
    text = text.replace("  ", " "); //TODO:???
    
    if (title == null || text == null) {
      return null;
    } else {
      Document document = new Document();
      document.add(new Field(BookConstants.FIELD_PATH, book.getPath(), Field.Store.YES, Field.Index.NO));
      document.add(new Field(BookConstants.FIELD_TITLE, title, Field.Store.YES, Field.Index.NO));
      document.add(new Field(BookConstants.FIELD_TEXT, text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
      return document;
    }
  }
}
