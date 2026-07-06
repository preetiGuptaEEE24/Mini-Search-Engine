
package search.engine;


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
