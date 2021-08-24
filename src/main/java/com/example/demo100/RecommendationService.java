package com.example.demo100;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> getAllBooks();

    public void loadRecommendations();
}
