package com.songko.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.songko.api.core.product.Product;
import com.songko.api.core.product.ProductService;
import com.songko.api.core.recommendation.Recommendation;
import com.songko.api.core.recommendation.RecommendationService;
import com.songko.api.core.review.Review;
import com.songko.api.core.review.ReviewService;
import com.songko.api.event.Event;
import com.songko.util.exceptions.InvalidInputException;
import com.songko.util.exceptions.NotFoundException;
import com.songko.util.http.HttpErrorInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;


@Slf4j
// 이벤트를 다른 토픽에 게시하려면 토픽별 MessageChannel을 선언한 자바 인터페이스를 만들고, EnableBinding 애노테이션을 선언해 활성화해야 한다
@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    // URL에 포함된 호스트 이름은 실제 DNS 이름이 아닌,
    // 마이크로서비스가 유레카 서버에 등록할 때 사용한 가상 호스트 이름.
    // spring.application.name 속성 값.
    private final String productServiceUrl = "http://product";
    private final String recommendationServiceUrl = "http://recommendation";
    private final String reviewServiceUrl = "http://review";

    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private final MessageSources messageSources;
    private final int productServiceTimeoutSec;

    public interface MessageSources {
        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }

    @Autowired
    public ProductCompositeIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            MessageSources messageSources,
            @Value("${app.product-service.timeoutSec}") int productServiceTimeoutSec
    ) {
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.productServiceTimeoutSec = productServiceTimeoutSec;
    }

    /**
     * ProductCompositeServiceApplication :: loadBalancedWebClientBuilder 는,
     * 생성자 실행 후 Bean이 생성 되므로, getter를 사용한 늦은 초기화 방식을 사용해야한다.
     */
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }

    // about Product
    @Override
    public Product createProduct(Product body) {
        messageSources.outputProducts()
                .send(MessageBuilder.withPayload(
                        new Event(Event.Type.CREATE, body.getProductId(), body)
                ).build());
        return body;
    }

    @Retry(name = "product")
    @CircuitBreaker(name = "product")
    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
        URI url = UriComponentsBuilder
                .fromUriString(productServiceUrl + "/product/{productId}?delay={delay}&faultPercent={faultPercent}")
                .build(productId, delay, faultPercent);
        log.debug("Will call the getProduct API on URL: {}", url);
        return getWebClient().get().uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                // HTTP 계층에서 발생한 예외를 자체 예외(예: NotFoundException, InvalidInput Exception)로 변경
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex))
                .timeout(Duration.ofSeconds(productServiceTimeoutSec));
    }

    @Override
    public void deleteProduct(int productId) {
        messageSources.outputProducts()
                .send(MessageBuilder.withPayload(
                        new Event(Event.Type.DELETE, productId, null)
                ).build());
    }

    // about Recommendation
    @Override
    public Recommendation createRecommendation(Recommendation body) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(Event.Type.CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        URI url = UriComponentsBuilder
                .fromUriString(recommendationServiceUrl + "/recommendation?productId={productId}")
                .build(productId);
        log.debug("Will call the getRecommendations API on URL: {}", url);

        // product 서비스를 성공적으로 호출하고 review나 recommendation API 호출에 실패했을 때는 전체 요청이 실패한 것으로 처리하지 않는다
        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                // 예외를 전파하는 대신 가능한 많은 정보를 호출자에게 돌려주고자 onErrorResume(error-> empty()) 메서드를 사용
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public void deleteRecommendations(int productId) {
        messageSources.outputRecommendations()
                .send(MessageBuilder.withPayload(
                        new Event(Event.Type.DELETE, productId, null)
                ).build());
    }

    // about Review
    @Override
    public Review createReview(Review body) {
        messageSources.outputReviews()
                .send(MessageBuilder.withPayload(
                        new Event(Event.Type.CREATE, body.getProductId(), body)
                ).build());
        return body;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        URI url = UriComponentsBuilder
                .fromUriString(reviewServiceUrl + "/review?productId={productId}")
                .build(productId);
        log.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get()
                .uri(url)
                .retrieve().bodyToFlux(Review.class).log().onErrorResume(error -> Flux.empty());
    }

    @Override
    public void deleteReviews(int productId) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(Event.Type.DELETE, productId, null)).build());
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
