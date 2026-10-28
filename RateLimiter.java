import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

public final class RateLimiter {
    private static final long SECOND = 1_000_000_000L;
    private final int limit;
    private final LongSupplier clock;
    private final Deque<Long> requests = new ArrayDeque<>();

    public RateLimiter(int limit) {
        this(limit, System::nanoTime);
    }

    RateLimiter(int limit, LongSupplier clock) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
        this.clock = clock;
    }

    public synchronized boolean allowRequest() {
        long now = clock.getAsLong();
        while (!requests.isEmpty() && now - requests.peekFirst() >= SECOND) {
            requests.removeFirst();
        }
        if (requests.size() >= limit) {
            return false;
        }
        requests.addLast(now);
        return true;
    }
}