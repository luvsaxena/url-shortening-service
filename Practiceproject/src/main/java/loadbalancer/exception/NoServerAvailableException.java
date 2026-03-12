package loadbalancer.exception;

public class NoServerAvailableException extends RuntimeException  {

    public NoServerAvailableException(String message) {
        super(message);
    }
}
