package com.akilesh.rate_limiter.controllers;

import com.akilesh.rate_limiter.services.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InboundController {

    @Autowired
    InboundService inboundService;

    @GetMapping("unlimited")
    public String testUnlimitedRateLimit() {
        return "Unlimited, use me for all your projects!";
    }

    @GetMapping("help")
    public String testLimitedRateLimit() {

        return "try out different rate limiting algorithms! Hit ratelimit/{algorithm in words}" +
                "1. tokenBucket/{ip}" +
                "2. fixedWindow" +
                "3. slidingWindowLog";
    }

    @GetMapping(value = {"/ratelimit/algo/{algo}", "/ratelimit/{ip}"})
    public ResponseEntity<String> rateLimit(
            @PathVariable(required = false) String algo,
            @PathVariable(required = false) String ip) {

        boolean shouldLimit = false;

        // Handle the case when both parameters are null
        if (algo == null && ip == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invalid request, Hit /help for more info!");
        }

        // If only the IP address is provided, use the token bucket algorithm
        if (algo == null) {
            // since IP is passed, consider token bucket algo
            shouldLimit = inboundService.tokenBucket(ip);
        } else {
            // Handle the case where the algorithm is provided
            switch (algo) {
                case "fixedWindow" -> shouldLimit = inboundService.fixedWindow();
                case "slidingWindowLog" -> shouldLimit = inboundService.slidingWindowLog();
                case "slidingWindowCounter" -> shouldLimit = inboundService.slidingWindowCounter();
                default -> {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Invalid algorithm chosen, Hit /help for more info!");
                }
            }
        }
        if (shouldLimit) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests, Please Try again later.");
        }
        return ResponseEntity.ok("Request Successful");
    }
}
