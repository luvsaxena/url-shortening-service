package searchengine.strategy;

import searchengine.model.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankingSearchStrategy implements SearchStrategy {
    @Override
    public List<Document> getResultDocs(Map<String, Map<String,Integer>> invertedIndex, Map<String, Document> documentStore, String query) {
        Map<String, Integer> docFreqMap = invertedIndex.getOrDefault(query.toLowerCase(), Collections.emptyMap());

        //sort by freq
        return docFreqMap.entrySet()
                .stream()
                .sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(entry -> documentStore.get(entry.getKey()))
                .collect(Collectors.toList());
    }
}
