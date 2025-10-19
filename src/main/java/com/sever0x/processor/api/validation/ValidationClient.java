package com.sever0x.processor.api.validation;

import com.sever0x.processor.NERResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ValidationClient {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${api.validation.url:http://localhost:9099/validate}")
	private String url;

	public boolean validate(NERResponse resp) {
		try {
			ResponseEntity<Boolean> r = restTemplate.postForEntity(url, resp, Boolean.class);
			return r.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(r.getBody());
		} catch (Exception e) {
			return false;
		}
	}
}
