package com.example.demo100;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@RestController
public class RecommendationsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RecommendationRepository recommendationRepository;
    private RecommendationService recommendationService;

    @Autowired
    public RecommendationsController(
            RecommendationRepository recommendationRepository, RecommendationService recommendationService) {
        this.recommendationRepository = recommendationRepository;
        this.recommendationService = recommendationService;
    }

    @RequestMapping(value = "/recommendations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Iterable<Recommendation> getAll() throws ExecutionException, InterruptedException {

        Retry retry = Retry.ofDefaults("test");
        ThreadPoolBulkhead threadPoolBulkhead = ThreadPoolBulkhead
                .ofDefaults("test");
        TimeLimiter timeLimiter = TimeLimiter.of(Duration.ofSeconds(10));
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        Supplier<Iterable<Recommendation>> supplier = () -> recommendationRepository.findAll();
        CompletableFuture<Iterable<Recommendation>> future = Decorators
                .ofSupplier(supplier)
                .withThreadPoolBulkhead(threadPoolBulkhead)
                .withTimeLimiter(timeLimiter, scheduledExecutorService)
                .withRetry(retry, scheduledExecutorService)
                .get()
                .toCompletableFuture();
        return future.get();
    }

    //Delay introduced in recommendationService.getAllBooks()
    //resilience4j will timeout the request after configured timeout
    //resilience4j will retry the request for the configured number of times
    @RequestMapping(value = "/delayedRecommendations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Iterable<Recommendation> getAllWithDelay() throws ExecutionException, InterruptedException {
        Retry retry = Retry.ofDefaults("test");
        ThreadPoolBulkhead threadPoolBulkhead = ThreadPoolBulkhead
                .ofDefaults("test");
        TimeLimiter timeLimiter = TimeLimiter.of(Duration.ofSeconds(8));
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        Supplier<Iterable<Recommendation>> supplier = () -> recommendationService.getAllBooks();
        CompletableFuture<Iterable<Recommendation>> future = Decorators
                .ofSupplier(supplier)
                .withThreadPoolBulkhead(threadPoolBulkhead)
                .withTimeLimiter(timeLimiter, scheduledExecutorService)
                .withRetry(retry, scheduledExecutorService)
                .get()
                .toCompletableFuture();
        return future.get();
    }

    @PostConstruct
    private void loadRecommendations() {
        recommendationService.loadRecommendations();
    }


}
