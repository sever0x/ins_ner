package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.mcp.service.McpNerService;
import jakarta.annotation.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NerMcpTools {

	private final McpNerService mcpNerService;
	@Nullable
	private final VectorStore vectorStore;

	public NerMcpTools(@Lazy McpNerService mcpNerService, @Nullable VectorStore vectorStore) {
		this.mcpNerService = mcpNerService;
		this.vectorStore = vectorStore;
	}

	@Tool(name = "ner_extract", description = "Extract entities from plain text. Returns a map entity_type -> list of values for the insurance domain.")
	public NERResponse extract(
			@ToolParam(description = "Raw text for analysis") String text,
			@ToolParam(description = "Optional file name", required = false) @Nullable String filename
	) {
		return mcpNerService.extract(text, filename);
	}

	@Tool(name = "rag_upsert", description = "Add documents to the vector store for RAG. Returns the number of upserts.")
	public int ragUpsert(@ToolParam(description = "List of documents with content and metadata")
	                     List<RagItem> items) {
		if (vectorStore == null || items == null || items.isEmpty()) return 0;
		List<Document> docs = new ArrayList<>();
		for (RagItem i : items) {
			Map<String, Object> meta = new LinkedHashMap<>();
			if (i.source != null) {
				meta.put("source", i.source);
			}
			if (i.type != null) {
				meta.put("type", i.type);
			}
			if (i.id != null) {
				meta.put("id", i.id);
			}
			docs.add(new Document(i.content == null ? "" : i.content, meta));
		}
		vectorStore.add(docs);
		return docs.size();
	}

	public record RagItem(
			@ToolParam(description = "Text content of the document") String content,
			@ToolParam(description = "Optional identifier", required = false) @Nullable String id,
			@ToolParam(description = "Optional source", required = false) @Nullable String source,
			@ToolParam(description = "Optional type", required = false) @Nullable String type
	) {
	}
}
