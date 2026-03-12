package loadbalancer.service;

import loadbalancer.model.Server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ServerRegistry {
    private List<Server> serverList = new CopyOnWriteArrayList<>();

    public List<Server> getHealthyServers() {
        return  serverList
                .stream()
                .filter(Server::isHealthy)
                .collect(Collectors.toList());
    }

    public void addServer(Server server) {
        serverList.add(server);
    }

    public List<Server> getServerList() {
        return serverList;
    }
}
