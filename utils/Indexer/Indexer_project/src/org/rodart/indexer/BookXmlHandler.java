package org.rodart.indexer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class BookXmlHandler extends DefaultHandler {
    protected static final String BOOK_DESCRIPTION = "FictionBook.description";
    protected static final String BOOK_AUTHOR_FIRST_NAME = "FictionBook.description.title-info.author.first-name";
    protected static final String BOOK_AUTHOR_LAST_NAME = "FictionBook.description.title-info.author.last-name";
    protected static final String BOOK_TITLE = "FictionBook.description.title-info.book-title";
    protected static final String BOOK_LANG = "FictionBook.description.title-info.lang";
    protected static final String BOOK_GENRE = "FictionBook.description.title-info.genre";

    protected static final String BOOK_BODY = "FictionBook.body";

    private static final Pattern BOOK_TEXT = Pattern.compile("FictionBook\\.body(\\.section)+\\.p");


    private StringBuilder data = new StringBuilder();
    private StringBuilder sb = new StringBuilder();
    private Stack<String> attrStack = new Stack<String>();

    public void startParsingFile(ZipFile zip, ZipEntry entry) throws SAXException {
        attrStack.clear();
        data.setLength(0);
    }

    public void finishParsingFile(ZipFile zip, ZipEntry entry, boolean parsedSuccessfully) {
        // do nothing
    }

    final public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(ch, start, length);
    }

    @Override
    final public void endElement(String uri, String localName, String qName) throws SAXException {
        String pop = attrStack.lastElement();
        if(!pop.equals(qName))
            throw new SAXException("Expected end of element '"+pop+"' but found end of element '"+qName+"'");

        String elementName = getElementName();
        if(BOOK_TEXT.matcher(elementName).find() && !isBookText(elementName)){
            attrStack.pop();
            return;
        }

        endElement(data, elementName);
        data.setLength(0);
        attrStack.pop();
    }

    protected void endElement(StringBuilder data, String elementName) throws SAXException {
        // do nothing
    }

    @Override
    final public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        attrStack.push(qName);
        String elementName = getElementName();
        if (BOOK_TEXT.matcher(elementName).find() && !isBookText(elementName)) {//игнорируем все вложенные в BOOK_TEXT теги, их содержимое присоединяем к BOOK_TEXT (планаризуем текст)
            return;
        }

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
        if(attrStack.size() > 0) {
            sb.deleteCharAt(sb.length()-1); //remove last '.'
        }
        return sb.toString();
    }

    public String getBookPath(ZipFile zip, ZipEntry entry) {
        return new File(zip.getName()).getName()+"\\"+entry.getName();
    }

    public static boolean isBookText(String elementName) {
        return BOOK_TEXT.matcher(elementName).matches();
    }
}

