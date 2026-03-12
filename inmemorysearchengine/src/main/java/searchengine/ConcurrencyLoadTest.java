package searchengine;

import searchengine.strategy.RankingSearchStrategy;

public class ConcurrencyLoadTest {

    public static void main(String[] args) {
        //initialize engine with search strategy
        SearchIndex searchIndex = new SearchIndex(new RankingSearchStrategy());



    }
}
