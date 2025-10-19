package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.mcp.service.VectorStoreService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EnrichmentTools {

	private final VectorStoreService vectorStoreService;

	public EnrichmentTools(VectorStoreService vectorStoreService) {
		this.vectorStoreService = vectorStoreService;
	}

	public record EnrichRequest(
			@ToolParam(description = "Original filename") String filename,
			@ToolParam(description = "Raw text") String text,
			@ToolParam(description = "Extracted entities") Map<String, List<String>> entities) {}

	@Tool(name = "enrich_entities", description = "Upsert entity-centric context windows into VectorStore. Returns upsert count.")
	public int enrich(EnrichRequest req) {
		return vectorStoreService.enrich(req.filename(), req.text(), req.entities());
	}
}
