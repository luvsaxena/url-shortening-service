package loadbalancer.service;

import loadbalancer.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthChecker {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    public void start(ServerRegistry serverRegistry) {
        executor.scheduleAtFixedRate(() -> {
            for(Server server : serverRegistry.getServerList()) {
                boolean isAlive = performPing(server.getUrl());
                server.setHealthy(isAlive);
                if(!isAlive){
                    logger.error("Server {} is not healthy", server.getId());
                }
            }

        },0,5, TimeUnit.SECONDS);

    }

    private boolean performPing(String url) {
        //mock logic to return false
        //to test
        if(url.contains("fail")){
            return false;
        }
        return true;
    }
}
