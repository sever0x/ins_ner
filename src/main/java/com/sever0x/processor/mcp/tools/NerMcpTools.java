package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.mcp.service.McpNerService;
import jakarta.annotation.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class NerMcpTools {

	private final McpNerService mcpNerService;

	public NerMcpTools(@Lazy McpNerService mcpNerService) {
		this.mcpNerService = mcpNerService;
	}

	@Tool(name = "ner_extract", description = "Extract entities from plain text. Returns a map entity_type -> list of values for the insurance domain.")
	public NERResponse extract(
			@ToolParam(description = "Raw text for analysis") String text,
			@ToolParam(description = "Optional file name", required = false) @Nullable String filename
	) {
		return mcpNerService.extract(text, filename);
	}
}
