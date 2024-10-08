package com.akilesh.rate_limiter.services;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class InboundServiceTest {

    InboundService inboundService = new InboundService();
    static int count;
    @Test
    void slidingWindowCounter() {

        ExecutorService executorService = Executors.newFixedThreadPool(15); // 15 threads

        // Simulate burst requests
        for (int i = 0; i < 20; i++) { // Simulate 20 requests
            executorService.submit(() -> {
                // Simulate a delay before making a request
                try {
                    Thread.sleep((long) (Math.random() * 100)); // Random delay between 0-100 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Check if the request is allowed or denied
                boolean isDenied = inboundService.slidingWindowCounter();

                if (isDenied) {
                    System.out.println("Request denied: " + Thread.currentThread().getName() +" Request count"+ count++);
                } else {
                    System.out.println("Request allowed: " + Thread.currentThread().getName()+" Request count"+ count++);
                }
            });
        }

        // Shutdown the executor service
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Wait for all threads to finish
        }

        System.out.println("Testing complete.");

    }
}