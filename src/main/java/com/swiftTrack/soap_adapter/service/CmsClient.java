package com.swiftTrack.soap_adapter.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CmsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final XmlMapper xmlMapper = new XmlMapper();

    public Map<String, Object> sendOrder(Map<String, Object> orderEvent) throws Exception {
        String xmlRequest = xmlMapper.writeValueAsString(orderEvent);
        System.out.println("Sending order event: " + xmlRequest);

        String xmlResponse = restTemplate.postForObject(
            "http://localhost:8082/cms/orders",
                xmlRequest,
                String.class
        );
        System.out.println("⬅️ Received XML from CMS:\n" + xmlResponse);
        return xmlMapper.readValue(xmlResponse, HashMap.class);
    }

}
