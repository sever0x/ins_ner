package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.api.validation.ValidationClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ValidationTools {

	private final ValidationClient validationClient;

	public ValidationTools(ValidationClient validationClient) {
		this.validationClient = validationClient;
	}

	@Tool(
			name = "validate_entities",
			description = "Validate extracted entities via external stub service. Returns true/false."
	)
	public boolean validate(
			@ToolParam(description = "Original filename", required = false) String filename,
			@ToolParam(description = "Extracted entities map") Map<String, List<String>> entities
	) {
		return validationClient.validate(new NERResponse(filename, entities));
	}
}
