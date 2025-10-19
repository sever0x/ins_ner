package com.sever0x.processor.mcp.service;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.Preprocessor;
import jakarta.annotation.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class McpNerService {
	private final ChatClient chatClient;
	private final Preprocessor preprocessor;
	private final VectorStoreService vectorStoreService;

	public McpNerService(ChatClient chatClient, Preprocessor preprocessor, VectorStoreService vectorStoreService) {
		this.chatClient = chatClient;
		this.preprocessor = preprocessor;
		this.vectorStoreService = vectorStoreService;
	}

	public NERResponse extract(String rawText, @Nullable String filename) {
		String text = preprocessor.normalize(rawText);

		List<Document> top = vectorStoreService.getDocuments(text);
		String ragContext = top.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

		var converter = new BeanOutputConverter<>(NerPayload.class);
		String format = converter.getFormat();

		String system = """
				You are an experienced NER system specializing in insurance documents (insurance policies, letters, reports, invoices).
				Your task is to extract named entities from the input text and return ONLY valid JSON (according to RFC8259),
				which must strictly follow the provided JSON Schema.
				
				The input text language is German or English.
				
				JSON Schema:
				{
				  "entities": {
				    "client_name": string[],
				    "client_number": string[],
				    "contract_number": string[],
				    "client_firma_name": string[],
				    "client_birthdate": string[],
				    "insurer_firma_name": string[]
				  }
				}
				
				Instructions:
				- Return only the values that are explicitly present in the input text (no assumptions or normalization).
				- Use the exact text fragment as it appears in the source.
				- If an entity is missing, return an empty array [].
				- Remove duplicates (even if the same value appears multiple times in the text).
				- "client_firma_name" refers to the client company (the policyholder) as well as its subsidiaries, but only in documents where these subsidiaries are explicitly mentioned.
				- "insurer_firma_name" refers to the insurance company or service organization providing services to the client.
				- "client_name" refers to natural persons representing the client (e.g., CFO, accountant).
				- Do not include representatives of the insurer under “client_name”.
				- "contract_number" includes all sequences resembling insurance contract or policy numbers.
				- RAG context values are for reference only; extract final entities exclusively from the main text.
				- The result must be pure JSON without comments, explanations, or any additional text.
				""";

		String user = """
				 RAG Context:
				 ```\s
				 %s
				 ```
				
				 Text:
				 ```
				 %s
				 ```
				
				 %s
				\s""".formatted(ragContext, text, format);

		try {
			NerPayload payload = chatClient.prompt().system(system).user(user).call().entity(converter);
			Map<String, List<String>> entities = safeEntities(payload);
			return new NERResponse(getFilename(filename), entities);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<String>> safeEntities(NerPayload p) {
		if (p == null || p.entities == null) {
			return Map.of();
		}
		Map<String, List<String>> cleaned = new LinkedHashMap<>();
		p.entities.forEach((k, v) -> {
			if (v != null && !v.isEmpty()) {
				List<String> vals = v.stream()
						.map(s -> s == null ? "" : s.trim())
						.filter(s -> !s.isEmpty())
						.distinct()
						.toList();

				if (!vals.isEmpty()) {
					cleaned.put(k, vals);
				}
			}
		});
		return cleaned;
	}

	private String getFilename(@Nullable String filename) {
		return filename == null ? "" : filename;
	}

	public record NerPayload(Map<String, List<String>> entities) {
	}
}
