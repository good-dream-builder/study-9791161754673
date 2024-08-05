package com.songko.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "ProductCompositeService", description = "REST API for composite product information.")
public interface ProductCompositeService {


    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body
     */
    @Operation(
            summary = "${api.product-composite.create-composite-product.summary}",
            description = "${api.product-composite.create-composite-product.description}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
            value = "/product-composite",
            consumes = "application/json")
    Mono<Void> createCompositeProduct(@RequestBody ProductAggregate body);


    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId
     * @return the composite product info, if found, else null
     */
    @Operation(
            summary = "${api.product-composite.get-composite-product.summary}",
            description = "${api.product-composite.get-composite-product.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(responseCode = "404", description = "Not found, the specified id does not exist."),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getCompositeProduct(
            @PathVariable("productId") int productId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
    );


    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productId
     */
    @Operation(
            summary = "${api.product-composite.delete-composite-product.summary}",
            description = "${api.product-composite.delete-composite-product.description}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteCompositeProduct(@PathVariable int productId);
}
