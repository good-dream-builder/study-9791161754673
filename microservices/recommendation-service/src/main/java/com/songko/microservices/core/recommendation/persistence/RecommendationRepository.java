package com.songko.microservices.core.recommendation.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {
    // List -> Flux로 교체
    Flux<RecommendationEntity> findByProductId(int productId);
}
