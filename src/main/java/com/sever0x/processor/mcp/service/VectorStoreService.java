package com.sever0x.processor.mcp.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.id.JdkSha256HexIdGenerator;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class VectorStoreService {

	private final VectorStore vectorStore;
	private static final int WINDOW_CHARS = 320;
	private static final Pattern SPACE = Pattern.compile("\\s+");
	private static final JdkSha256HexIdGenerator ID = new JdkSha256HexIdGenerator();

	public VectorStoreService(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	public List<Document> getDocuments(String text) {
		return vectorStore.similaritySearch(
				SearchRequest.builder()
						.query(text)
						.topK(5)
						.similarityThreshold(0.65)
						.filterExpression("ns == 'ner_ctx'")
						.build()
		);
	}

	public int enrich(String filename, String fullText, Map<String, List<String>> entities) {
		if (vectorStore == null || entities == null || entities.isEmpty() || fullText == null) {
			return 0;
		}

		List<Document> docs = new ArrayList<>();
		entities.forEach((type, values) -> {
			if (values == null) {
				return;
			}
			for (String v : values) {
				if (v == null || v.isBlank()) {
					continue;
				}
				String window = extractWindow(fullText, v);
				Map<String, Object> meta = new LinkedHashMap<>();
				meta.put("ns", "ner_ctx");
				meta.put("source", filename == null ? "" : filename);
				meta.put("entity_type", type);
				meta.put("value", v);

				String idSeed = (filename == null ? "" : filename) + "|" + type + "|" + v + "|" + norm(window);
				String id = ID.generateId(idSeed);

				docs.add(new Document(id, window, meta));
			}
		});

		if (!docs.isEmpty()) {
			vectorStore.delete(docs.stream().map(Document::getId).toList());
			vectorStore.add(docs);
		}
		return docs.size();
	}

	private String extractWindow(String text, String value) {
		int idx = text.indexOf(value);
		if (idx < 0) {
			return text.substring(0, Math.min(text.length(), VectorStoreService.WINDOW_CHARS * 2));
		}
		int start = Math.max(0, idx - VectorStoreService.WINDOW_CHARS);
		int end = Math.min(text.length(), idx + value.length() + VectorStoreService.WINDOW_CHARS);
		return text.substring(start, end).trim();
	}

	private String norm(String s) {
		return SPACE.matcher(s == null ? "" : s).replaceAll(" ").trim();
	}
}
