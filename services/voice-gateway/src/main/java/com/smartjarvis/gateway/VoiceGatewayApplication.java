package com.smartjarvis.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Voice Gateway Application
 * 
 * WebSocket gateway for voice communication with full microservices architecture.
 * Handles real-time audio streaming and publishes events to Kafka.
 */
@SpringBootApplication
@EnableKafka
public class VoiceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceGatewayApplication.class, args);
    }
}
