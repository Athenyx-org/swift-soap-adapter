package com.swiftTrack.soap_adapter.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/adapter")
public class AdapterController {

    private static final String CMS_RESPONSE_TOPIC = "cms-order-proceeded";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper = new XmlMapper();

    public AdapterController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/response", consumes = "application/xml", produces = "text/plain")
    public ResponseEntity<String> receiveCmsResponse(@RequestBody String cmsResponse) throws Exception {
        System.out.println("--------- adapter received cms response: " + cmsResponse);
        Map<String, Object> payload = xmlMapper.readValue(cmsResponse, Map.class);
        String jsonPayload = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send(CMS_RESPONSE_TOPIC, jsonPayload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.out.println("Failed to send response to CMS: " + ex.getMessage());
                    } else {
                        System.out.println("📤 Published CMS response (JSON) to topic " + CMS_RESPONSE_TOPIC +
                                " (partition=" + result.getRecordMetadata().partition() +
                                ", offset=" + result.getRecordMetadata().offset() + ")"
                        );
                    }
                });
        return ResponseEntity.ok(cmsResponse);
    }


}
