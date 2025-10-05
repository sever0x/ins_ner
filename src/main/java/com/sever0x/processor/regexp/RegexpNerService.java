package com.sever0x.processor.regexp;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegexpNerService {

	private static final List<String> INSURER_SUFFIXES = List.of(
			"AG", "GmbH", "SE", "VVaG", "S\\.A\\.", "SA", "a\\.s\\.", "N\\.V\\.", "S\\.p\\.A\\."
	);

	private final List<RegexRule> rules = List.of(
			// Contract numbers
			new RegexRule("contract_number", "(?m)(?iu)(?:policenummer|policen?-?\\s*nr\\.?|vertragsnummer|versicherungsschein[-\\s]*nummer|versicherungsschein[-\\s]*nr\\.?|versicherungsnummer)\\s*[:#]?[\\s]*([A-Z0-9][A-Z0-9./\\-]{4,30})"),
			new RegexRule("contract_number", "(?m)(?iu)\\b(?:nr\\.?|no\\.?|police)\\s*[:#]?[\\s]*([A-Z0-9][A-Z0-9./\\-]{4,30})"),
			new RegexRule("contract_number", "(?m)(?iu)\\b([A-Z]{1,3}-\\d{2,5}-\\d{2,5})\\b"),
			new RegexRule("contract_number", "(?m)(?iu)\\b(DE\\d{9,12})\\b"),
			new RegexRule("contract_number", "(?m)(?iu)\\b([A-Z]{2,5}-[A-Z]{2,5}-\\d{4}-\\d{4,5})\\b"),

			// Client number
			new RegexRule("client_number", "(?m)(?iu)(?:kundennummer|kunden[-\\s]*id|versicherten[-\\s]*nummer)\\s*[:#]?[\\s]*([A-Z0-9][A-Z0-9/\\-\\.]{5,20})"),

			// Insurer
			new RegexRule("insurer", "(?m)(?iu)\\b([A-ZÄÖÜ][\\p{L}&\\.\\- ]{2,}?(?:Versicherung(?:en)?|Versicherungsgesellschaft)?\\s+(?:" + String.join("|", INSURER_SUFFIXES) + "))\\b"),

			// Client name
			new RegexRule("client_name", "(?m)(?iu)(?:versicherungsnehmer|name|kunde)\\s*[:\\-]?\\s*([A-ZÄÖÜ][\\p{L}'\\-]+(?:\\s+(?:von|van|zu|de|der|den|la|le))?(?:\\s+[A-ZÄÖÜ][\\p{L}'\\-]+){1,3})"),
			new RegexRule("client_name", "(?m)(?iu)\\b(?:herr|frau)\\s+([A-ZÄÖÜ][\\p{L}'\\-]+(?:\\s+(?:von|van|zu|de|der|den|la|le))?(?:\\s+[A-ZÄÖÜ][\\p{L}'\\-]+){1,3})\\b"),

			// Birth date
			new RegexRule("birth_date", "(?m)(?iu)(?:geburtsdatum|geb\\.?\\s*datum|geboren\\s+am|geb\\.?\\s*am)\\s*[:\\-]?\\s*([0-3]?\\d[.\\-/][01]?\\d[.\\-/](?:19|20)\\d{2})")
	);


	public Map<String, List<String>> extractEntities(String text) {
		Map<String, List<String>> entities = new HashMap<>();

		for (RegexRule rule : rules) {
			Matcher matcher = Pattern.compile(rule.regex()).matcher(text);

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

		if (label.equals("contract_number") || label.equals("client_number")) {
			value = value.replaceAll("\\s+", "");
		}
		if (label.equals("client_name")) {
			// strip after first comma or address keywords
			value = value.replaceAll(",.*", "").replaceAll("\\b(Straße|Strasse|Str\\.|Adresse|Postleitzahl)\\b.*", "").trim();
		}
		return value;
	}

	private boolean isValid(String label, String value) {
		if (label.equals("contract_number")) {
			return value.matches("(?i)[A-Z0-9./\\-]{5,30}") && value.matches(".*\\d.*")
			       && !value.matches("(?i).*(vember|twendig|auskunft|gericht|service|kunde|versicherung).*");
		}

		if (label.equals("client_number")) {
			return value.matches("(?i)[A-Z0-9/\\-\\.]{5,20}") && value.matches(".*\\d.*");
		}

		if (label.equals("insurer")) {
			return INSURER_SUFFIXES.stream().anyMatch(value::contains) && value.split(" ").length <= 6;
		}

		if (label.equals("birth_date")) {
			return value.matches("[0-3]?\\d[.\\-/][01]?\\d[.\\-/](?:19|20)\\d{2}");
		}

		if (label.equals("client_name")) {
			return value.split(" ").length >= 2 && !value.matches(".*\\d.*") && value.length() <= 50;
		}

		return true;
	}

	private record RegexRule(String label, String regex) {}
}
