package org.rodart.indexer;

public class Book {
    private String path;
    private StringBuilder text = new StringBuilder();
    private StringBuilder title = new StringBuilder();

    public Book(String path) {
        this.path = path;
    }

    public String getText() {
        return text.toString();
    }

    public String getTitle() {
        return title.toString();
    }

    public void appendParagraph(StringBuilder par) {
        text.append(par).append('\n');
    }

    public void appendTitleElement(String tag, StringBuilder val) {
        title.append(tag).append('=').append(val).append(';');
    }

    public String getPath() {
        return path;
    }

    public void clearText() {
        text.setLength(0);
    }

    public int getTextLength() {
        return text.length();
    }
}
