package com.songko.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {


    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json")
    Product createProduct(@RequestBody Product body);

    /**
     * Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId
     * @return the product, if found, else null
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Mono<Product> getProduct(@PathVariable("productId") int productId,
                             @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
                             @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId
     */
    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable("productId") int productId);
}
