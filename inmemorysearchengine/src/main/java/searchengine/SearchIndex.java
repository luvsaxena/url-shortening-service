package searchengine;

import searchengine.model.Document;
import searchengine.strategy.SearchStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SearchIndex {

    private final Map<String, Map<String, Integer>> invertedIndex = new ConcurrentHashMap<>();
    private final Map<String, Document> documentStore = new ConcurrentHashMap<>();
    private final SearchStrategy searchStrategy;

    public SearchIndex(SearchStrategy searchStrategy) {
        //can be used for search
        this.searchStrategy = searchStrategy;
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void indexDocument(Document doc) {
        lock.writeLock().lock();
        try{
            documentStore.put(doc.getId(),doc);
            String[] tokens = tokenize(doc.getContent());
            for(String token : tokens){
                Map<String, Integer> freqMap = invertedIndex.computeIfAbsent(token, m -> new HashMap<>());
                freqMap.put(doc.getId(), freqMap.getOrDefault(doc.getId(), 0) + 1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Document> searchDocuments(String query) {
        lock.readLock().lock();
        try {
            return this.searchStrategy.getResultDocs(invertedIndex, documentStore, query);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    private String[] tokenize(String content) {
        return content.toLowerCase().split("\\s+");
    }

}
