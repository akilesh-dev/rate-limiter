# Rate Limiting Algorithms

Try out different rate limiting algorithms! This repository provides implementations of various rate limiting strategies in Java, allowing you to simulate and test how these algorithms handle traffic bursts and maintain request limits.

## Table of Contents
- [Introduction](#introduction)
- [Algorithms Implemented](#algorithms-implemented)

## Introduction

Rate limiting is a crucial technique for controlling the amount of incoming requests to an API or service. By implementing rate limiting, you can prevent abuse, ensure fair usage, and maintain the performance and availability of your services. This repository explores different algorithms used for rate limiting, including fixed window, sliding window, and token bucket strategies.

## Algorithms Implemented

1. **Fixed Window Rate Limiter**: Allows a maximum number of requests in a defined time period (e.g., per minute).
2. **Sliding Window Log Rate Limiter **: A more flexible approach that considers requests over a rolling time window, smoothing out traffic bursts.
3. **Sliding Window Count Rate Limiter**: A hybrid of fixed window sliding window log algorithm.
4. **Token Bucket Rate Limiter**: Controls the amount of data that can be sent over time, allowing for bursts while maintaining an overall average rate.
