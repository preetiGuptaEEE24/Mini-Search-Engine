/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

/**
 * Represents a single document in our search engine.
 * Each document has a unique id, a title, and the body text we index.
 */
public class Document {
    private final int id;
    private final String title;
    private final String content;

    public Document(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + title;
    }
}
