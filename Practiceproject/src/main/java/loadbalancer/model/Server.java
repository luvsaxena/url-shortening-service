package loadbalancer.model;

import loadbalancer.RouteReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    public String id;
    public String url;
    private int weight;
    private volatile boolean healthy;
    private AtomicInteger activeConnections = new AtomicInteger(0);
    private final Logger logger = LoggerFactory.getLogger(Server.class);

    public boolean isHealthy() {
        return healthy;
    }

    public String getUrl() {
        return url;
    }


    public void setHealthy(boolean isAlive) {
        healthy = isAlive;
    }

    public String getId() {
        return id;
    }

    public void handle(RouteReq routeReq) {
        logger.info("server {} handling req", id);

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
