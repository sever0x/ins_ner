package com.sever0x.processor.mailgen.api;

import java.util.List;
import java.util.Map;

public record RenderResponse(
		String template,
		String content,
		List<String> unresolvedPlaceholders,
		Map<String, Object> usedValues
) {}