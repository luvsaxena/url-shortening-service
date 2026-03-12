package searchengine.strategy;

import searchengine.model.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiwordRankingStrategy implements SearchStrategy {
    @Override
    public List<Document> getResultDocs(Map<String, Map<String, Integer>> invertedIndex, Map<String, Document> documentStore, String query) {
        String[] tokens = tokenize(query);

        Map<String, Integer> finalScores = new HashMap<>();

        for (String token : tokens) {
            Map<String, Integer> curMap = invertedIndex.get(token);
            if(curMap == null){
                return Collections.emptyList();
            }

            if(finalScores.isEmpty()){
                finalScores.putAll(curMap);
            }
            else {
                finalScores.keySet().retainAll(curMap.keySet());
                for(String id : finalScores.keySet()){
                    finalScores.put(id, finalScores.get(id) + curMap.get(id));
                }
            }
        }

        return finalScores.entrySet()
                .stream()
                .sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(entry -> documentStore.get(entry.getKey()))
                .collect(Collectors.toList());

    }

    private String[] tokenize(String query) {
        String[] tokens = query.toLowerCase().split("\\s+");
        return tokens;
    }
}
