package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.api.validation.ValidationClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ValidationTools {

	private final ValidationClient validationClient;

	public ValidationTools(ValidationClient validationClient) {
		this.validationClient = validationClient;
	}

	@Tool(name = "validate_entities", description = "Validate extracted entities via external stub service. Returns true/false.")
	public boolean validate(NERResponse response) {
		return validationClient.validate(response);
	}
}
