package searchengine.strategy;

import searchengine.model.Document;

import java.util.List;
import java.util.Map;

public interface SearchStrategy {

    List<Document> getResultDocs(Map<String, Map<String,Integer>> invertedIndex, Map<String, Document> documentStore, String query);

}
