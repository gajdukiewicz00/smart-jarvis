package com.smartjarvis.nlu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS NLU Service Application
 * 
 * Natural Language Understanding microservice with rule-based intent recognition.
 * Processes transcripts from STT service and extracts intents and entities.
 */
@SpringBootApplication
@EnableKafka
public class NLUServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NLUServiceApplication.class, args);
    }
}
