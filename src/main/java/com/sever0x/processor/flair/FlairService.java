package com.sever0x.processor.flair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlairService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String BASE_URL = "https://sever0x-insurance-de.hf.space/gradio_api/call/predict";

	public Map<String, List<String>> getEntities(String rawText) {
		try {
			String requestBody = objectMapper.writeValueAsString(Map.of("data", List.of(rawText)));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<String> postResponse = restTemplate.postForEntity(BASE_URL, requestEntity, String.class);
			JsonNode postJson = objectMapper.readTree(postResponse.getBody());
			String eventId = postJson.get("event_id").asText();

			String resultUrl = BASE_URL + "/" + eventId;
			JsonNode dataNode = null;
			int retries = 0;
			do {
				Thread.sleep(1000);
				ResponseEntity<String> getResponse = restTemplate.getForEntity(resultUrl, String.class);
				String body = getResponse.getBody();

				if (body != null && body.contains("event: complete")) {
					int idx = body.indexOf("data: [");
					if (idx != -1) {
						String jsonPart = body.substring(idx + 6).trim();
						dataNode = objectMapper.readTree(jsonPart);
						break;
					}
				}
				retries++;
			} while (retries < 10);

			if (dataNode == null) {
				return Map.of();
			}

			Map<String, List<String>> entities = new HashMap<>();
			for (JsonNode entityNode : dataNode.get(0).get("entities")) {
				String text = entityNode.get("text").asText();
				JsonNode labels = entityNode.get("labels");
				if (labels.isArray() && !labels.isEmpty()) {
					for (JsonNode label : labels) {
						String labelName = label.get("value").asText();
						entities.computeIfAbsent(labelName, k -> new ArrayList<>()).add(text);
					}
				}
			}

			return entities;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}