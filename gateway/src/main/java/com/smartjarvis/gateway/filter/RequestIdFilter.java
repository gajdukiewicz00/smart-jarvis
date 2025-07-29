package com.smartjarvis.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request ID Filter for Gateway
 * 
 * Adds a unique request ID to each request for tracing purposes
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check if request ID already exists in headers
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        
        // If no request ID exists, generate a new one
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Store request ID in exchange attributes
        exchange.getAttributes().put(REQUEST_ID_ATTRIBUTE, requestId);
        
        // Add request ID to response headers
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}