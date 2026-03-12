package loadbalancer.strategy;

import loadbalancer.model.Server;

import java.util.List;

public interface LoadBalancingStrategy {

    Server getNextServer(List<Server> serverList);
}
