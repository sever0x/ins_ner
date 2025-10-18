package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.mcp.service.McpNerService;
import jakarta.annotation.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
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

	public NerMcpTools(McpNerService mcpNerService, @Nullable VectorStore vectorStore) {
		this.mcpNerService = mcpNerService;
		this.vectorStore = vectorStore;
	}

	@Tool(name = "ner_extract", description = "Витягти сутності з plain text. Повертає map entity_type -> list значень для страхового домену.")
	public NERResponse extract(
			@ToolParam(description = "Сирий текст для аналізу") String text,
			@ToolParam(description = "Опціональна назва файла", required = false) @Nullable String filename
	) {
		return mcpNerService.extract(text, filename);
	}

	@Tool(name = "rag_upsert", description = "Додати документи до векторного сховища для RAG. Повертає кількість upsert.")
	public int ragUpsert(@ToolParam(description = "Список документів з контентом та метаданими")
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
			@ToolParam(description = "Текстовий контент документа") String content,
			@ToolParam(description = "Опціональний ідентифікатор", required = false) @Nullable String id,
			@ToolParam(description = "Опціональне джерело", required = false) @Nullable String source,
			@ToolParam(description = "Опціональний тип", required = false) @Nullable String type
	) {
	}
}
