package com.sever0x.processor.api;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * This component provide layout from Python service to extract data via LayoutLMv3
 */
@Component
public class LayoutApiRetriever {
	private static final String API_URL = "http://localhost:8001/extract-layout";

	private static final RestTemplate restTemplate = new RestTemplate();

	public PdfLayoutResponse retrieveLayout4Pdf(MultipartFile file) throws IOException {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		ResponseEntity<PdfLayoutResponse> response = restTemplate.postForEntity(API_URL, requestEntity, PdfLayoutResponse.class);
		return response.getBody();
	}
}
