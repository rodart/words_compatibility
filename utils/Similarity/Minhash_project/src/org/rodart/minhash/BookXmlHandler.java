package org.rodart.minhash;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ontos.core.miner.util.ObjectPair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class BookXmlHandler extends DefaultHandler {
    protected static final String BOOK_AUTHOR_FIRST_NAME = "FictionBook.description.title-info.author.first-name";
    protected static final String BOOK_AUTHOR_LAST_NAME = "FictionBook.description.title-info.author.last-name";
    protected static final String BOOK_TITLE = "FictionBook.description.title-info.book-title";
    private static final Pattern BOOK_TEXT = Pattern.compile("FictionBook\\.body(\\.section)+\\.p");

    private StringBuilder data = new StringBuilder();
    private StringBuilder sb = new StringBuilder();
    private Stack<String> attrStack = new Stack<String>();
    private Map<String, String> indexedBooks = new HashMap<String, String>();
    ArrayList<ObjectPair<String, String>> similar_by_key_docs = new ArrayList<ObjectPair<String, String>>();
    private Book book;

    public void startParsingFile(ZipFile zip, ZipEntry entry) throws SAXException {
        attrStack.clear();
        data.setLength(0);
        String path = getBookPath(zip, entry);
        book = new Book(path);
    }

    public void finishParsingFile(ZipFile zip, ZipEntry entry, boolean parsedSuccessfully) {
        if (book.getTitle().length() > 0) {
            if (indexedBooks.containsKey(book.getTitle())) {
                similar_by_key_docs.add(new ObjectPair<String, String>(book.getPath(), indexedBooks.get(book.getTitle())));
            } else {
                indexedBooks.put(book.getTitle(), book.getPath());
            }
        }
        book.clearText();
    }
    
    public Book getBook() {
        return book;
    }

    public void printSimilarBooksTitles(String similarity_by_key_filename) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(similarity_by_key_filename));
            if (similar_by_key_docs.size() == 0) {
                out.println("No similar documents by key.");
            }
            for (ObjectPair<String, String> entry : similar_by_key_docs) {
                out.println(entry.getFirst() + " <--> " + entry.getSecond());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    final public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(ch, start, length);
    }

    @Override
    final public void endElement(String uri, String localName, String qName) throws SAXException {
        String pop = attrStack.lastElement();
        if (!pop.equals(qName))
            throw new SAXException("Expected end of element '" + pop + "' but found end of element '" + qName + "'");

        String elementName = getElementName();
        if (BOOK_TEXT.matcher(elementName).find() && !isBookText(elementName)){
            attrStack.pop();
            return;
        }
        
        if (BOOK_AUTHOR_FIRST_NAME.equals(elementName) || BOOK_AUTHOR_LAST_NAME.equals(elementName) || BOOK_TITLE.equals(elementName)) {
            book.appendTitleElement(elementName, data);
        }
            
        endElement(data, elementName);
        data.setLength(0);
        attrStack.pop();
    }

    protected void endElement(StringBuilder data, String elementName) throws SAXException {
        book.appendParagraph(data);
    }

    @Override
    final public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        attrStack.push(qName);
        String elementName = getElementName();
        //ignore all included tags in BOOK_TEXT and it's content join to BOOK_TEXT (make text plain)
        if (BOOK_TEXT.matcher(elementName).find() && !isBookText(elementName))
            return;

        data.setLength(0);
        startElement();
    }

    protected void startElement() throws SAXException {
        //do nothing
    }

    final protected String getElementName() {
        sb.setLength(0);
        for (String s : attrStack) {
            sb.append(s).append('.');
        }
        if (attrStack.size() > 0)
            sb.deleteCharAt(sb.length()-1); //remove last '.'

        return sb.toString();
    }

    public static String getBookPath(ZipFile zip, ZipEntry entry) {
        return new File(zip.getName()).getName() + "\\" + entry.getName();
    }

    public static boolean isBookText(String elementName) {
        return BOOK_TEXT.matcher(elementName).matches();
    }
}
