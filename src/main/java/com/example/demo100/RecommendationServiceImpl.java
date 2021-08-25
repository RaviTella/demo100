package com.example.demo100;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RecommendationRepository recommendationRepository;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public List<Recommendation> getAllBooks() {
        List<Long> timeList = Arrays.asList(10000L, 6000L);
        Random rand = new Random();
        Long randomTime = timeList.get(rand.nextInt(timeList.size()));
        logger.info("!!!!!Introducing a Delay of!!!!: " + randomTime + "ms" );
        logger.info("#####Calling Cosmos####");
        try {
            Thread.sleep(randomTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return recommendationRepository.findAll();
    }

    public void loadRecommendations() {
        List<Recommendation> recommendations = new ArrayList<Recommendation>();
        recommendations.add(new Recommendation("1", "01234", "Getting Started with kubernetes", "Jonathan Baier", "Learn Kubernetes the right way", "images/Kubernetes.jpg"));
        recommendations.add(new Recommendation("2", "95201", "Learning Docker Networking", "Rajdeep Das", "Docker networking deep dive", "images/DockerNetworking.jpg"));
        recommendations.add(new Recommendation("6", "95298", "Spring Microservices", "Rajesh RV", "Build scalable microservices with Spring and Docker", "images/SpringMicroServices.jpg"));
        recommendationRepository.saveAll(recommendations);
    }
}
