package com.sever0x.processor.mailgen;

import com.sever0x.processor.mailgen.api.PlaceholderInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {

	private static final Pattern PH_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");

	public List<PlaceholderInfo> extractPlaceholders(String templateContent) {
		Matcher m = PH_PATTERN.matcher(templateContent);
		LinkedHashMap<String, PlaceholderInfo> uniq = new LinkedHashMap<>();
		while (m.find()) {
			String inner = m.group(1).trim();
			PlaceholderInfo info = parse(inner);
			uniq.putIfAbsent(inner, info);
		}
		return new ArrayList<>(uniq.values());
	}

	public String render(String templateContent,
	                     Map<String, Object> values,
	                     MailGenProperties.OnMissing onMissing,
	                     List<String> unresolvedOut,
	                     Map<String, Object> usedValuesOut) {

		Matcher m = PH_PATTERN.matcher(templateContent);
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String inner = m.group(1).trim();
			PlaceholderInfo info = parse(inner);

			String baseValue = toString(values.get(info.key()));
			if (baseValue == null) {
				switch (onMissing) {
					case LEAVE -> {
						unresolvedOut.add(inner);
						m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
						continue;
					}
					case EMPTY -> {
						unresolvedOut.add(inner);
						m.appendReplacement(sb, "");
						continue;
					}
					case ERROR -> throw new MailGenException("Missing value for placeholder: " + inner);
				}
			}

			String rendered = Filters.apply(baseValue, info.filters());
			usedValuesOut.put(info.key(), baseValue);
			m.appendReplacement(sb, Matcher.quoteReplacement(rendered));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private PlaceholderInfo parse(String inner) {
		String[] parts = Arrays.stream(inner.split("\\|"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);

		String key = parts.length > 0 ? parts[0] : inner;
		List<String> filters = new ArrayList<>();
		if (parts.length > 1) {
			filters.addAll(Arrays.asList(parts).subList(1, parts.length));
		}
		return new PlaceholderInfo(inner, key, Collections.unmodifiableList(filters));
	}

	private static String toString(Object o) {
		return (o == null) ? null : String.valueOf(o);
	}
}