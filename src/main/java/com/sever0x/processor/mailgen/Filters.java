package com.sever0x.processor.mailgen;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public final class Filters {

	private Filters() {
	}

	public static String apply(String input, List<String> filters) {
		if (input == null) return null;
		String result = input;
		for (String f : filters) {
			result = resolve(f).apply(result);
		}
		return result;
	}

	private static UnaryOperator<String> resolve(String name) {
		String n = name.trim().toLowerCase(Locale.ROOT);
		return switch (n) {
			case "lower" -> s -> s.toLowerCase(Locale.ROOT);
			case "upper" -> s -> s.toUpperCase(Locale.ROOT);
			case "trim" -> String::trim;
			case "cutslash" -> s -> s.replace("/", "");
			case "cap" -> Filters::capitalizeWords;
			default -> s -> s;
		};
	}

	private static String capitalizeWords(String s) {
		String[] parts = s.trim().split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i];
			if (p.isEmpty()) continue;
			sb.append(Character.toUpperCase(p.charAt(0)));
			if (p.length() > 1) sb.append(p.substring(1).toLowerCase(Locale.ROOT));
			if (i < parts.length - 1) sb.append(" ");
		}
		return sb.toString();
	}
}