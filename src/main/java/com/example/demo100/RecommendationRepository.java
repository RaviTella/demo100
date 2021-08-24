package com.example.demo100;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface RecommendationRepository extends CosmosRepository<Recommendation, String> {

    @Override
    List<Recommendation> findAll();
}