import loadbalancer.*;
import loadbalancer.exception.NoServerAvailableException;
import loadbalancer.model.Server;
import loadbalancer.service.HealthChecker;
import loadbalancer.service.LoadBalancerService;
import loadbalancer.service.ServerRegistry;
import loadbalancer.strategy.SimpleRoundRobinStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LoadBalancerTest {

    private LoadBalancerService loadBalancerService;
    private ServerRegistry serverRegistry;

    @BeforeEach
    public void setUp() {
        serverRegistry = new ServerRegistry();
        loadBalancerService = new LoadBalancerService(new SimpleRoundRobinStrategy(), serverRegistry);
    }

    @Test
    public void testRoundRobinSelection() {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://s2.com",true);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        //route to s id 1
        loadBalancerService.routeRequest(new RouteReq());
        //route to s id 2
        loadBalancerService.routeRequest(new RouteReq());
    }

    @Test
    public void testExcludeUnhealthyServers() {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://s2.com",false);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        assertEquals(1, serverRegistry.getHealthyServers().size());
    }

    @Test
    public void testRoundRobinSelectionWithMultipleThreads() throws InterruptedException {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://s2.com",true);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        ExecutorService  executorService = Executors.newFixedThreadPool(5);

        for(int i=0;i<10;i++){
            executorService.execute(() -> loadBalancerService.routeRequest(new RouteReq()));
        }

        executorService.awaitTermination(5, TimeUnit.SECONDS);

//        //route to s id 1
//        loadBalancerService.routeRequest(new RouteReq());
//        //route to s id 2
//        loadBalancerService.routeRequest(new RouteReq());
    }

    @Test
    public void testNoServersAvailableException() {
        Server s1 = createServer("1","http://s1.com",false);
        Server s2 = createServer("2","http://s2.com",false);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        assertThrows(NoServerAvailableException.class,() -> loadBalancerService.routeRequest(new RouteReq()));

    }

    @Test
    public void testHealthCheckerFlow() throws InterruptedException {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://fail-server.com",true);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        HealthChecker healthChecker = new HealthChecker();
        healthChecker.start(serverRegistry);

        Thread.sleep(6000);

        assertTrue(s1.isHealthy());
        assertFalse(s2.isHealthy());

        List<Server> healthyServers = serverRegistry.getHealthyServers();
        assertEquals(1,healthyServers.size());
        assertEquals("1",healthyServers.get(0).getId());

    }

    @Test
    public void testUnhealthyServerAvailable() throws InterruptedException {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://fail-server.com",true);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        HealthChecker healthChecker = new HealthChecker();
        healthChecker.start(serverRegistry);

        Thread.sleep(6000);

        loadBalancerService.routeRequest(new RouteReq());
        loadBalancerService.routeRequest(new RouteReq());

        //change unhealthy server url
        s2.setUrl("http://s2.com");

        Thread.sleep(6000);

        //now the req should be routed to
        //both servers
        loadBalancerService.routeRequest(new RouteReq());
        loadBalancerService.routeRequest(new RouteReq());

//        List<Server> healthyServers = serverRegistry.getHealthyServers();
//        assertEquals(1,healthyServers.size());
//        assertEquals("1",healthyServers.get(0).getId());
    }

    @Test
    public void testServerAdditionUnderHeavyLoad() throws InterruptedException {
        Server s1 = createServer("1","http://s1.com",true);
        Server s2 = createServer("2","http://s2.com",true);

        serverRegistry.addServer(s1);
        serverRegistry.addServer(s2);

        ExecutorService  executorService = Executors.newFixedThreadPool(5);

        for(int i=0;i<15;i++){
            if(i==8){
                //add new server in between
                Server s3 = createServer("3","http://s3.com",true);
                serverRegistry.addServer(s3);
            }
            executorService.execute(() -> loadBalancerService.routeRequest(new RouteReq()));
        }

        executorService.awaitTermination(5, TimeUnit.SECONDS);

    }

    private Server createServer(String id, String url, boolean isAlive) {
        Server server = new Server();
        server.setId(id);
        server.setUrl(url);
        server.setHealthy(isAlive);
        return server;
    }

}
