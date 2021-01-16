package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping(value = "/")
public class DemoController {

    static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    private static final int MAX_INT_LIST_SIZE = 100000;

    private static final List<Integer> INTEGER_LIST = new ArrayList<>();
    static {
        for(int i = 0; i < MAX_INT_LIST_SIZE; ++i) {
            INTEGER_LIST.add(i);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<Integer>> get(final ServerWebExchange serverWebExchange) {
        return getResponse(serverWebExchange);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/post_error", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<Integer>> postError(final ServerWebExchange serverWebExchange,
                                    @RequestBody final List<Integer> requestBodyList) {
        return Mono.error(new Exception());
        // return getResponse(serverWebExchange)
        //     .flatMap(responseList -> Mono.error(new Exception("Oh no! " + responseList)));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/post", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<Integer>> post(final ServerWebExchange serverWebExchange,
                                    @RequestBody final List<Integer> requestBodyList) {
        return getResponse(serverWebExchange);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/post_mix", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<Integer>> postMix(final ServerWebExchange serverWebExchange,
                                    @RequestBody final List<Integer> requestBodyList) {
        return getResponse(serverWebExchange)
            .flatMap(responseList -> ThreadLocalRandom.current().nextInt(1000) < 10 ? Mono.error(new Exception()) : Mono.just(responseList));
    }

    private Mono<List<Integer>> getResponse(final ServerWebExchange serverWebExchange) {
        final String logPrefix = serverWebExchange.getLogPrefix();
        final int delayMillis = ThreadLocalRandom.current().nextInt(10);
        final int intListSize = ThreadLocalRandom.current().nextInt(MAX_INT_LIST_SIZE);
        logger.info(String.format("%sController called. Delay: %d. Size: %d", logPrefix, delayMillis, intListSize));
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> this.getIntList(intListSize)))
            .delayElement(Duration.ofMillis(delayMillis))
            .doOnSuccess((unused) -> {
                logger.info(String.format("%sMono returned from Controller doOnSuccess called.", logPrefix));
            })
            .doOnTerminate(() -> {
                logger.info(String.format("%sMono returned from Controller doOnTerminate called.", logPrefix));
            });
    }

    private List<Integer> getIntList(final int size) {
        return INTEGER_LIST.subList(0, size);
    }
}
