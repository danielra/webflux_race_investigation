package com.example.demo.filter;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class DemoWebFilter implements WebFilter {

    static final Duration TIMEOUT_DURATION = Duration.ofMillis(20000);

    static final HttpStatus RESPONSE_TIMEOUT_HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    static final Logger logger = LoggerFactory.getLogger(DemoWebFilter.class);

    @Override
    public Mono<Void> filter(final ServerWebExchange serverWebExchange, final WebFilterChain webFilterChain) {

        final String path = serverWebExchange.getRequest().getURI().getPath();

        final long requestStartTime = System.nanoTime();

        logger.info(String.format("%sIncoming request observed.", serverWebExchange.getLogPrefix()));

        return webFilterChain.filter(serverWebExchange)
            .timeout(TIMEOUT_DURATION)
            .onErrorResume(TimeoutException.class, (final Throwable unused) -> this.getTimeoutFallback(serverWebExchange, requestStartTime));
    }

    private Mono<Void> getTimeoutFallback(final ServerWebExchange serverWebExchange, final long requestStartTime) {
        final boolean responseStatusCodeAlreadyCommitted = !serverWebExchange.getResponse().setStatusCode(RESPONSE_TIMEOUT_HTTP_STATUS);
        final HttpRequest request = serverWebExchange.getRequest();
        final String requestUri = request.getURI().toString();
        final String requestMethod = request.getMethodValue();
        final String responseStatusMessage = responseStatusCodeAlreadyCommitted ? String.format("Response status code was already committed: '%s'.", serverWebExchange.getResponse().getStatusCode()) : String.format("Set response status code to '%s'.", RESPONSE_TIMEOUT_HTTP_STATUS);
        final long durationMs = Duration.ofNanos(System.nanoTime() - requestStartTime).toMillis();

        logResponseTimeoutDetails(durationMs, requestMethod, requestUri, responseStatusMessage, serverWebExchange.getLogPrefix());
        return Mono.empty();
    }

    private void logResponseTimeoutDetails(final long durationNanoseconds, final String requestMethod, final String requestUri, final String responseStatusMessage, final String requestLogPrefix) {
        logger.error(String.format("%sResponse timeout after %s milliseconds for %s request with uri '%s'. %s", requestLogPrefix, durationNanoseconds, requestMethod, requestUri, responseStatusMessage));
    }
}
