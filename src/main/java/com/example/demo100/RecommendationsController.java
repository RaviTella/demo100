package com.example.demo100;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

@RestController
public class RecommendationsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RecommendationRepository recommendationRepository;
    private RecommendationService recommendationService;
    private CosmosAsyncClient cosmosAsyncClient;

    @Autowired
    public RecommendationsController(
            RecommendationRepository recommendationRepository, RecommendationService recommendationService, CosmosAsyncClient cosmosAsyncClient) {
        this.recommendationRepository = recommendationRepository;
        this.recommendationService = recommendationService;
        this.cosmosAsyncClient = cosmosAsyncClient;
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

        //supplier run asynchronously in a ThreadPoolBulkhead
        ThreadPoolBulkheadConfig threadPoolBulkheadconfig = ThreadPoolBulkheadConfig
                .custom()
                .maxThreadPoolSize(4)
                .coreThreadPoolSize(2)
                .queueCapacity(20)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig
                .custom()
                .cancelRunningFuture(true)
                .timeoutDuration(Duration.ofSeconds(8))
                .build();

        RetryConfig retryConfig = RetryConfig
                .custom()
                .maxAttempts(4)
                .retryExceptions(TimeoutException.class)
                .build();
        //Scheduler to schedule a timeout on a CompletableFuture and used for retries after the specified timeout
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
        Supplier<Iterable<Recommendation>> supplier = () -> recommendationService.getAllBooks();
        CompletableFuture<Iterable<Recommendation>> future = Decorators
                .ofSupplier(supplier)
                .withThreadPoolBulkhead(ThreadPoolBulkhead.of("config", threadPoolBulkheadconfig))
                .withTimeLimiter(TimeLimiter.of(timeLimiterConfig), scheduledExecutorService)
                .withRetry(Retry.of("config", retryConfig), scheduledExecutorService)
                .get()
                .toCompletableFuture();
        return future.get();
    }

    @PostConstruct
    private void loadRecommendations() {
        recommendationService.loadRecommendations();
    }


    @RequestMapping(value = "/recommendationsCF", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<List<Recommendation>> getAllWithCF() throws ExecutionException, InterruptedException {
        return CompletableFuture
                .supplyAsync(() -> recommendationService.getAllBooks())
                .orTimeout(11, TimeUnit.SECONDS);
    }


    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Recommendation> query() {
        System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "true");
        logger.info("COSMOS.QUERYPLAN_CACHING_ENABLED=" + System
                .getenv()
                .get("COSMOS.QUERYPLAN_CACHING_ENABLED"));
        return recommendationRepository.findByIdAndAuthor("5", "Aleksandar Prokopec");
    }

    @RequestMapping(value = "/queryjava", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Recommendation> Queryjava() {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setQueryMetricsEnabled(true);
        String sql = "SELECT * FROM root r WHERE r.author = 'Aleksandar Prokopec'";
        SqlQuerySpec querySpec = new SqlQuerySpec(sql);
    return cosmosAsyncClient
                .getDatabase("bstore")
                .getContainer("Recommendation")
                .queryItems(querySpec,Recommendation.class).byPage().blockLast().getResults();

    }


}

