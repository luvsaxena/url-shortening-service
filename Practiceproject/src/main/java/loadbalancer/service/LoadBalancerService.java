package loadbalancer.service;

import loadbalancer.exception.NoServerAvailableException;
import loadbalancer.RouteReq;
import loadbalancer.model.Server;
import loadbalancer.strategy.LoadBalancingStrategy;

import java.util.List;

public class LoadBalancerService {

    LoadBalancingStrategy loadBalancingStrategy;
    ServerRegistry serverRegistry;

    public LoadBalancerService(LoadBalancingStrategy loadBalancingStrategy, ServerRegistry serverRegistry) {
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.serverRegistry = serverRegistry;
    }

    public void routeRequest(RouteReq routeReq) {

        List<Server> healthyServers = serverRegistry.getHealthyServers();
        if(healthyServers == null || healthyServers.isEmpty()) {
            throw new NoServerAvailableException("No servers are available");
        }

        Server s = loadBalancingStrategy.getNextServer(healthyServers);
        s.handle(routeReq);
    }


}
