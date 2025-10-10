package com.sever0x.processor.regexp;

import com.sever0x.processor.NERResponse;

import java.util.List;
import java.util.Map;

public record RegexpNERResponse(
		String filename,
		Map<String, List<String>> entities
) implements NERResponse {
	public RegexpNERResponse(Map<String, List<String>> entities) {
		this("", entities);
	}
}
