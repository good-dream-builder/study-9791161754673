package com.songko.microservices.core.product.services;

import com.songko.api.core.product.Product;
import com.songko.api.core.product.ProductService;
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
    private final ProductService productService;

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Product> event) {

        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Product product = event.getData();
                log.info("Create product with ID: {}", product.getProductId());
                productService.createProduct(product);
                break;

            case DELETE:
                int productId = event.getKey();
                log.info("Delete recommendations with ProductID: {}", productId);
                productService.deleteProduct(productId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
