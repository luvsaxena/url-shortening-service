package loadbalancer.strategy;

import loadbalancer.model.Server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleRoundRobinStrategy implements LoadBalancingStrategy {
    private final AtomicInteger index  = new AtomicInteger(-1);

    @Override
    public Server getNextServer(List<Server> serverList) {

        if(serverList == null || serverList.isEmpty()) {
            throw new RuntimeException("Server list is null or empty");
        }

        int i = index.incrementAndGet() % serverList.size();
        Server server = serverList.get(i);
        return server;
    }
}
