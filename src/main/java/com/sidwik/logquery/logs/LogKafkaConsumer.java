package com.sidwik.logquery.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidwik.logquery.config.LogQueryProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LogKafkaConsumer {
    private final LogService logService;
    private final ObjectMapper objectMapper;

    public LogKafkaConsumer(LogService logService, ObjectMapper objectMapper) {
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{@logQueryProperties.kafka().topic()}", groupId = "logquery-engine")
    public void consume(String message) throws JsonProcessingException {
        logService.ingest(objectMapper.readValue(message, IngestLogRequest.class));
    }
}
