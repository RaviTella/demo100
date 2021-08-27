package com.example.demo100;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface RecommendationRepository extends CosmosRepository<Recommendation, String> {

    @Override
    List<Recommendation> findAll();

    @Query(value = "select * from c where c.id=@id and c.author=@author")
    List<Recommendation> getRecommendation(String id, String author);
    List<Recommendation> findByIdAndAuthor(String id, String author);

}