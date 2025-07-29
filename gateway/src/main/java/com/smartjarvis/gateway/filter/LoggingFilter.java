package com.smartjarvis.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global Logging Filter for Gateway
 * 
 * Logs all incoming requests and outgoing responses
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String timestamp = LocalDateTime.now().format(formatter);

        // Log incoming request
        logger.info("Incoming request - Method: {}, Path: {}, Remote: {}, User-Agent: {}, Time: {}", 
                   method, path, remoteAddress, userAgent, timestamp);

        // Add request start time to exchange attributes
        exchange.getAttributes().put("requestStartTime", System.currentTimeMillis());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Log response
                    long startTime = (Long) exchange.getAttributes().get("requestStartTime");
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ? 
                        exchange.getResponse().getStatusCode().value() : 0;

                    logger.info("Response - Method: {}, Path: {}, Status: {}, Duration: {}ms, Time: {}", 
                              method, path, statusCode, duration, LocalDateTime.now().format(formatter));
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}