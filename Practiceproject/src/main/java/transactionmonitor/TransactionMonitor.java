package transactionmonitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionMonitor {

    TreeSet<CategoryNode> priorityQueue = new TreeSet<>((c1,c2) -> (c2.count - c1.count));
    Map<String, CategoryNode > categoryMap = new ConcurrentHashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void trackTransaction(String category){
        lock.writeLock().lock();
        try {
            if(categoryMap.containsKey(category)){
                CategoryNode categoryNode = categoryMap.get(category);
                categoryNode.count++;
                return;
            }
            CategoryNode categoryNode = new CategoryNode(category,1);
            categoryMap.put(category,categoryNode);
            priorityQueue.add(categoryNode);
        }
        finally {
            lock.writeLock().unlock();
        }


    }

    List<String> getTopK(int k){
        lock.readLock().lock();
        List<String> topK = new ArrayList<>();
        try{
            //read top k elements
//            List<String> topK = new ArrayList<>();
            for(CategoryNode c : priorityQueue){
                topK.add(c.category);
            }
        }
        finally{
            lock.readLock().unlock();
        }
        return Collections.unmodifiableList(topK);
    }

    private class CategoryNode {
        String category;
        int count;

        public CategoryNode(String category, int count) {
            this.category = category;
            this.count = count;
        }
    }
}
