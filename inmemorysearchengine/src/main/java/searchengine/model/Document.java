package searchengine.model;

import java.util.Map;

public class Document {

    private final String id;
    private final String content;
    private final Map<String, String> metadata;

    public Document(String id, String content, Map<String, String> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }

    public String getId() {return id;}

    public String getContent() {return content;}
}
