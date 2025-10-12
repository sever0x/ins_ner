package com.sever0x.processor;

import java.util.List;
import java.util.Map;

public record NERResponse(
		String filename,
		Map<String, List<String>> entities
) {
	public NERResponse(Map<String, List<String>> entities) {
		this("", entities);
	}
}
