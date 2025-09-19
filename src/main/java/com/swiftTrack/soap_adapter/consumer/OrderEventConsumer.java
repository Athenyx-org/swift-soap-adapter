package com.swiftTrack.soap_adapter.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftTrack.soap_adapter.service.CmsClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final CmsClient cmsClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventConsumer(ObjectMapper objectMapper, CmsClient cmsClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.cmsClient = cmsClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-create", groupId = "cms-adapter")
    public void consume(String message) throws Exception {
        Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
        System.out.println("Adapter received order event: " + orderEvent);

        Map<String, Object> cmsResponse = cmsClient.sendOrder(orderEvent);

        String responseJson = objectMapper.writeValueAsString(cmsResponse);
        kafkaTemplate.send("cms-order-processed", responseJson);
        System.out.println("📤 Published CMS response to topic cms-order-processed: " + responseJson);
    }

}
