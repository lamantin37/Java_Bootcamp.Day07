package edu.school21.models;

import java.util.StringJoiner;

public class Book {
    private final String title;
    private final String author;
    private int pageCount;

    public Book() {
        this.title = "Default title";
        this.author = "Default author";
        this.pageCount = 0;
    }

    public Book(String title, String author, int pageCount) {
        this.title = title;
        this.author = author;
        this.pageCount = pageCount;
    }

    public void increasePageCount(int value) {
        this.pageCount += value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Book.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("author='" + author + "'")
                .add("pageCount=" + pageCount)
                .toString();
    }
}

