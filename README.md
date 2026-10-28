# Rate limiter

A shared, in-memory Java rate limiter. Allows at most N requests in any rolling second. Requires Java 8 or newer. No dependencies.

```java
RateLimiter limiter = new RateLimiter(100);
if (limiter.allowRequest()) {
    // Call the external API.
}
```

Use the same instance for every thread calling a particular channel. Each channel can have its own instance and limit. The limit must be positive. Rejected requests return false immediately after acquiring the lock; there is no waiting for capacity or retry loop.

## Thread safety

`allowRequest()` is synchronized. Removing expired timestamps, checking the limit, and recording an accepted request all happen under the same instance lock. Two threads cannot both claim the last available slot. The queue is private and accessed only inside this method. The time is also read inside the lock, so timestamps stay ordered.

`System.nanoTime()` measures elapsed time without being affected by wall-clock adjustments. Accepted timestamps are retained for one second; rejected calls are not recorded. Space is O(N), and work per call is amortized O(1), with O(N) cleanup after an idle period.

## Assumption / uncertainty

The wording “current second” could mean fixed one-second buckets or a rolling second. Fixed buckets are simpler, but could allow N calls just before a boundary and another N just after it. I chose a rolling window to avoid that burst given the risk of the channel blocking us. I would confirm the channel's actual policy before integrating.

This coordinates threads in one JVM. Multiple processes would need shared coordination. The caller should make the API call promptly after receiving permission; this class controls admission, not when an external server receives the request.
