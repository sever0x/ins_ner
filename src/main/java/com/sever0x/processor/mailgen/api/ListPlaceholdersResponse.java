package com.sever0x.processor.mailgen.api;

import java.util.List;

public record ListPlaceholdersResponse(String template, List<PlaceholderInfo> placeholders) {
}