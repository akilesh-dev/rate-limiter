package com.akilesh.rate_limiter.services;


import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InboundService {

    //token bucket
    private final HashMap<String, Long> bucket = new HashMap<>();
    private final HashMap<String, LocalDateTime> lastSeen = new HashMap<>();
    private final Long capacity = 1L; //1 request allowed every 20 seconds (for test purposes)
    private final Long refillTime = 20L;

    //fixed window
    private final ConcurrentHashMap<Long, AtomicLong> window = new ConcurrentHashMap<>();
    private final Long maxRequests = 5L;

    //sliding window log
    private final Deque<LocalDateTime> slidingWindow = new ArrayDeque<>();
    private final long discardTimeInSeconds = 60L;   //10 requests per minute


    public boolean tokenBucket(String ip) {
        LocalDateTime currTime = LocalDateTime.now();
        if (!bucket.containsKey(ip)) {
            bucket.put(ip, capacity);
            lastSeen.put(ip, currTime);
        }
        long currCapacity = bucket.get(ip);
        long timeSinceLastSeen = Duration.between(lastSeen.get(ip), currTime).toSeconds();

        currCapacity = Math.min(capacity, timeSinceLastSeen / refillTime + currCapacity);
        if (currCapacity <= 0) {
            lastSeen.put(ip, currTime);
            return true;
        }
        currCapacity--;
        bucket.put(ip, currCapacity);
        return false;
    }

    public boolean fixedWindow() {
        LocalDateTime currTime = LocalDateTime.now();
        long key = currTime.getMinute();
        if (!window.containsKey(key)) {
            window.clear();
            window.put(key, new AtomicLong(maxRequests));
        }
        long currCount = window.get(key).get();
        if (currCount <= 0) {
            return true;
        }
        currCount--;
        window.put(key, new AtomicLong(currCount));
        return false;
    }

    public boolean slidingWindowLog() {
        LocalDateTime currTime = LocalDateTime.now();
        while (!slidingWindow.isEmpty() && Duration.between(slidingWindow.getFirst(), currTime).toSeconds() > discardTimeInSeconds) {
            slidingWindow.removeFirst();
        }
        slidingWindow.addLast(currTime);
        return slidingWindow.size() > maxRequests;
    }

    public boolean slidingWindowCounter() {
        LocalDateTime currTime = LocalDateTime.now();
        long currKey = currTime.getMinute();
        if (!window.containsKey(currKey)) {
            // Cleanup old windows
            long thresholdTime = currTime.getMinute() - 1; // delete all windows other than prev window
            window.keySet().removeIf(key -> key < thresholdTime);

            window.put(currKey, new AtomicLong(maxRequests));
        }
        long currCount = window.get(currKey).get();
        if (currCount <= 0) {
            return true;
        }
        currCount--;
        double currentWindowProgress = (double) (currTime.getSecond()) / 60;
        if (currentWindowProgress >= 0.4) {     //if 40% is progressed in this window, add 60% count from prev window
            long prevKey = (currTime.getMinute() - 1); //prev minute window
            long prevCount = window.getOrDefault(prevKey, new AtomicLong(0)).get();
            currCount -= (long) Math.floor(prevCount * 0.6); //take 60% from the prev window and add it to the curr window
        }
        if (currCount <= 0) {
            return true;
        }
        window.put(currKey, new AtomicLong(currCount));
        return false;
    }
}
