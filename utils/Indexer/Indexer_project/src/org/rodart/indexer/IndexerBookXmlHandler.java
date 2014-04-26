package org.rodart.indexer;

import org.xml.sax.SAXException;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IndexerBookXmlHandler extends BookXmlHandler {
    private boolean add_book;
    private Book book;
    private BookSink sink;
    private String path;
    private boolean eachParagraphAsSeparateDocument;
    private static final int PARAGRAPH_DOC_MIN_LENGTH = 1<<15;

    public IndexerBookXmlHandler(BookSink bookSink, boolean eachParagraphAsSeparateDocument) {
        this.eachParagraphAsSeparateDocument = eachParagraphAsSeparateDocument;
        add_book = false;
        sink = bookSink;
    }

    @Override
    public void startParsingFile(ZipFile zip, ZipEntry entry) throws SAXException {
        super.startParsingFile(zip, entry);
        this.path = getBookPath(zip, entry);
    }

    public void endElement(StringBuilder data, String elementName) {
        if (book == null)
            return; // parsing already indexed book

        try {
            if (isBookText(elementName)) {
                book.appendParagraph(data);
                if(eachParagraphAsSeparateDocument && book.getTextLength() > PARAGRAPH_DOC_MIN_LENGTH)
                    flushToIndex();

            } else if (BOOK_DESCRIPTION.equals(elementName)) {
                if(isIndexed(book))
                    book = null;
            } else if (BOOK_BODY.equals(elementName)) {
                if (book.getTextLength()>0)
                    flushToIndex();

            } else if (BOOK_AUTHOR_FIRST_NAME.equals(elementName) || BOOK_AUTHOR_LAST_NAME.equals(elementName) || BOOK_TITLE.equals(elementName)) {
                book.appendTitleElement(elementName, data);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        } finally {
            data.setLength(0);
        }
    }

    private Set<String> indexedBooks = new HashSet<String>();

    private void flushToIndex() {
        sink.add(book);
        book.clearText();
        if (!add_book) {
            indexedBooks.add(key(book));
            add_book = true;
        }
    }

    private boolean isIndexed(Book book) {
        return indexedBooks.contains(key(book));
    }

    private static String key(Book book) {
        return book.getTitle(); // TODO: make key from author and part of title?
    }

    public void startElement() {
        String elementName = getElementName();
        if (BOOK_DESCRIPTION.equals(elementName)) {
            book = new Book(path);
            add_book = false;
        }
    }

}
