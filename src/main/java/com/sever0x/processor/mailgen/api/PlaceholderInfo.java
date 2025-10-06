package com.sever0x.processor.mailgen.api;

import java.util.List;

public record PlaceholderInfo(
		String raw,
		String key,
		List<String> filters
) {
}