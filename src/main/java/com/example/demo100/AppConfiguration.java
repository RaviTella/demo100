package com.example.demo100;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableCosmosRepositories
@EnableCosmosAuditing

public class AppConfiguration extends AbstractCosmosConfiguration {



    @Value("${azure.cosmosdb.uri}")
    private String uri;

    @Value("${azure.cosmosdb.key}")
    private String key;

    @Value("${azure.cosmosdb.database}")
    private String dbName;

    private AzureKeyCredential azureKeyCredential;

    @Value("${azure.cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        this.azureKeyCredential = new AzureKeyCredential(key);
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();

        List<String> locations = new ArrayList<>();
        locations.add("Canada East");
        return new CosmosClientBuilder()
                .endpoint(uri)
                .preferredRegions(locations)
                .credential(azureKeyCredential)
                .directMode(directConnectionConfig);
    }

    @Bean
    public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
    }


    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig
                .builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                .build();
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
             logger.info("Response Diagnostics {}", responseDiagnostics);
        }
    }

    @Override
    protected String getDatabaseName() {
        return dbName;
    }
}