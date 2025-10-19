package com.sever0x.processor;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

public record NERResponse(
		@ToolParam(description = "Original filename") String filename,
		@ToolParam(description = "Extracted entities map") Map<String, List<String>> entities
) {
	public NERResponse(Map<String, List<String>> entities) {
		this("", entities);
	}
}
