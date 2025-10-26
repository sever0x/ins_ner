package com.sever0x.processor.regexp;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts only IBAN, dates and email addresses from text using regex.
 * <p>
 * Labels:
 * - iban
 * - date
 * - email
 */
@Service
public class RegexpNerService {

	private final List<CompiledRegexRule> compiledRules;

	public RegexpNerService() {
		this.compiledRules = createRules().stream()
				.map(rule -> new CompiledRegexRule(
						rule.label(),
						Pattern.compile(rule.regex(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CHARACTER_CLASS)
				))
				.collect(Collectors.toList());
	}

	private List<RegexRule> createRules() {
		return List.of(
				// IBAN: allow spaces between groups; capture whole IBAN
				new RegexRule(
						"iban",
						"(?iu)\\b([A-Z]{2}\\d{2}(?:\\s*[A-Z0-9]){11,30})\\b"
				),
				// Dates: DD.MM.YYYY, DD-MM-YYYY, DD/MM/YYYY (1-2 digit day/month); also YYYY-MM-DD
				new RegexRule("date",
						"(?iu)\\b(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4})\\b"),
				new RegexRule("date",
						"(?iu)\\b(\\d{4}[./-]\\d{1,2}[./-]\\d{1,2})\\b"),
				// Emails (simple robust)
				new RegexRule("email",
						"(?iu)\\b([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,})\\b")
		);
	}

	public Map<String, List<String>> extractEntities(String text) {
		Map<String, List<String>> entities = new HashMap<>();

		for (CompiledRegexRule rule : compiledRules) {
			Matcher matcher = rule.pattern().matcher(text);

			while (matcher.find()) {
				String value = matcher.group(1).trim();
				value = normalize(rule.label(), value);
				if (!isValid(rule.label(), value)) continue;

				entities.computeIfAbsent(rule.label(), k -> new ArrayList<>());
				if (!entities.get(rule.label()).contains(value)) {
					entities.get(rule.label()).add(value);
				}
			}
		}
		return entities;
	}

	private String normalize(String label, String value) {
		value = value.replaceAll("[\\n\\r]+", " ").trim();

		switch (label) {
			case "iban":
				// remove spaces, uppercase
				value = value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
				break;

			case "email":
				// normalize to lowercase
				value = value.toLowerCase(Locale.ROOT);
				break;

			case "date":
				// unify separators to dot and zero-pad day/month; expand 2-digit year
				String[] parts;
				if (value.matches("\\d{4}[./-]\\d{1,2}[./-]\\d{1,2}")) {
					parts = value.split("[./-]");
					int year = Integer.parseInt(parts[0]);
					int month = Integer.parseInt(parts[1]);
					int day = Integer.parseInt(parts[2]);
					value = String.format("%02d.%02d.%04d", day, month, year);
				} else {
					parts = value.split("[./-]");
					int day = Integer.parseInt(parts[0]);
					int month = Integer.parseInt(parts[1]);
					int year = Integer.parseInt(parts[2]);
					if (parts[2].length() == 2) {
						// 00-49 -> 2000-2049, 50-99 -> 1950-1999
						year = (year <= 49) ? (2000 + year) : (1900 + year);
					}
					value = String.format("%02d.%02d.%04d", day, month, year);
				}
				break;
		}
		return value;
	}

	private boolean isValid(String label, String value) {
		if (value == null || value.isEmpty()) return false;

		switch (label) {
			case "iban":
				// length 15..34 alnum and checksum
				if (!value.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$")) return false;
				return isValidIbanChecksum(value);

			case "email":
				// basic sanity (already matched by regex)
				return value.length() <= 254;

			case "date":
				if (!value.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) return false;
				String[] p = value.split("\\.");
				int d = Integer.parseInt(p[0]);
				int m = Integer.parseInt(p[1]);
				int y = Integer.parseInt(p[2]);
				if (y < 1900 || y > 2100) return false;
				if (m < 1 || m > 12) return false;
				int[] mdays = {31, isLeap(y) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
				return d >= 1 && d <= mdays[m - 1];

			default:
				return false;
		}
	}

	private boolean isLeap(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	// IBAN checksum per ISO 13616 (mod-97 == 1)
	private boolean isValidIbanChecksum(String iban) {
		String rearranged = iban.substring(4) + iban.substring(0, 4);
		StringBuilder sb = new StringBuilder(rearranged.length() * 2);
		for (char c : rearranged.toCharArray()) {
			if (Character.isDigit(c)) {
				sb.append(c);
			} else if (Character.isLetter(c)) {
				sb.append((int) (Character.toUpperCase(c) - 'A') + 10);
			} else {
				return false;
			}
		}
		// compute mod 97 on long string
		String num = sb.toString();
		int mod = 0;
		for (int i = 0; i < num.length(); i++) {
			mod = (mod * 10 + (num.charAt(i) - '0')) % 97;
		}
		return mod == 1;
	}

	private record RegexRule(String label, String regex) {
	}

	private record CompiledRegexRule(String label, Pattern pattern) {
	}
}