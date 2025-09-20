package com.swiftTrack.soap_adapter.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CmsClient {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final String cmsBaseUrl;

    public CmsClient(RestTemplate restTemplate,
                     @Value("${cms.base-url}") String cmsBaseUrl) {
        this.restTemplate = restTemplate;
        this.cmsBaseUrl = cmsBaseUrl;
    }

    public Map<String, Object> sendOrder(Map<String, Object> orderEvent) throws Exception {
        String xmlRequest = xmlMapper.writeValueAsString(orderEvent);
        System.out.println("Sending order event: " + xmlRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, headers);

        String url = cmsBaseUrl + "/cms/orders";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Unexpected response from CMS: status=" + response.getStatusCode());
            }
            String xmlResponse = response.getBody();
            System.out.println("⬅️ Received XML from CMS:\n" + xmlResponse);
            return xmlMapper.readValue(xmlResponse, HashMap.class);
        } catch (ResourceAccessException rae) {
            // Connection issues (refused, timeout, DNS)
            throw new IllegalStateException("Failed to reach CMS at " + url + ". " +
                    "If running in Docker, do not use 'localhost'; use the CMS service hostname and ensure the port is exposed. " +
                    "Original error: " + rae.getMessage(), rae);
        }
    }

}