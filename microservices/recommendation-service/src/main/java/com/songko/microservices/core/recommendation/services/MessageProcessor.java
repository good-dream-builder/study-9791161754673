package com.songko.microservices.core.recommendation.services;

import com.songko.api.core.recommendation.Recommendation;
import com.songko.api.core.recommendation.RecommendationService;
import com.songko.api.event.Event;
import com.songko.util.exceptions.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@Slf4j
@RequiredArgsConstructor
@EnableBinding(Sink.class)
public class MessageProcessor {
    private final RecommendationService recommendationService;


    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Recommendation> event) {

        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Recommendation recommendation = event.getData();
                log.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                recommendationService.createRecommendation(recommendation);
                break;

            case DELETE:
                int productId = event.getKey();
                log.info("Delete recommendations with ProductID: {}", productId);
                recommendationService.deleteRecommendations(productId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
