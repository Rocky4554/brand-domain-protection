package com.example.BrandProtection.domainiq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DomainIqRateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(DomainIqRateLimiter.class);

    private final int maxPerMinute;
    private long windowStartMillis;
    private int callsInWindow;

    DomainIqRateLimiter(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
        this.windowStartMillis = System.currentTimeMillis();
        this.callsInWindow = 0;
    }

    synchronized void acquire() {
        long now = System.currentTimeMillis();
        long elapsed = now - windowStartMillis;
        if (elapsed >= 60_000L) {
            windowStartMillis = now;
            callsInWindow = 0;
        }

        if (callsInWindow >= maxPerMinute) {
            long sleepMillis = 60_000L - elapsed;
            if (sleepMillis > 0) {
                logger.warn("DomainIQ rate limit exceeded. Blocking for {} ms.", sleepMillis);
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new DomainIqException("Interrupted while waiting for DomainIQ rate limit reset.", ex);
                }
                windowStartMillis = System.currentTimeMillis();
                callsInWindow = 0;
            }
        }

        callsInWindow++;
    }
}
