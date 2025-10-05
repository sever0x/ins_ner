package com.sever0x.processor.regexp;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegexpNerService {

	private final List<RegexRule> rules = List.of(
			// Vertrags-Nr
			new RegexRule("contract_number", "(?iu)(?:versicherungsschein[-\\s]*nummer|versicherungsschein[-\\s]*nr\\.?|policen?-?\\s*nr\\.?|policenummer|police\\s*nr\\.?|vertragsnr\\.?|vertragsnummer|versicherungs-?\\s*nr\\.?|versicherungsnummer|kfz[-\\s]*versicherung\\s*nr\\.?)\\s*[:#]?\\s*([A-Z0-9][A-Z0-9./\\- ]{3,})"),
			new RegexRule("contract_number", "(?iu)\\b(?:nr\\.?|no\\.?)\\s*[:#]?\\s*([A-Z0-9][A-Z0-9./\\- ]{3,})"),

			// Kunder-Nr
			new RegexRule("client_number", "(?iu)(?:kunden(?:nummer|[-\\s]*nr\\.?)|kunden[-\\s]*id|versicherten[-\\s]*nr\\.?|versichertennummer|versicherten[-\\s]*nummer)\\s*[:#]?\\s*([A-Z0-9][A-Z0-9 /\\-\\.]{4,})"),

			// Firma Name
			new RegexRule("insurer", "(?imu)\\bversicherer(?:\\s+(?:ist|sind))?\\s*:?\\s*(.+?)(?:(?:,|\\bund\\b|\\.)\\s|$)"),
			new RegexRule("insurer", "(?imu)^(?<name>[A-ZÄÖÜ][\\p{L}&\\.\\- ]{2,}?(?:Versicherung(?:en)?|Versicherungsgesellschaft)?\\s+(?:AG|GmbH(?:\\s*&\\s*Co\\.\\s*KG)?|SE|VVaG|S\\.A\\.|SA|a\\.s\\.|N\\.V\\.|S\\.p\\.A\\.))\\b"),

			// Kunde Name
			new RegexRule("client_name", "(?is)versicherungsnehmer[^:\\n]*[:\\n]\\s*(?:name\\s*:\\s*)?([A-ZÄÖÜ][\\p{L}'\\-]+(?:\\s+(?:von|van|zu|de|der|den|la|le))?(?:\\s+[A-ZÄÖÜ][\\p{L}'\\-]+){1,3})"),
			new RegexRule("client_name", "(?iu)\\b(?:herr|frau)\\s+([A-ZÄÖÜ][\\p{L}'\\-]+(?:\\s+(?:von|van|zu|de|der|den|la|le))?(?:\\s+[A-ZÄÖÜ][\\p{L}'\\-]+){1,3})\\b"),
			new RegexRule("client_name", "(?imu)^\\s*name\\s*:\\s*([A-ZÄÖÜ][\\p{L}'\\-]+(?:\\s+(?:von|van|zu|de|der|den|la|le))?(?:\\s+[A-ZÄÖÜ][\\p{L}'\\-]+){1,3})\\s*$"),

			// Geburtsdatum
			new RegexRule("birth_date", "(?iu)(?:geburtsdatum|geb\\.?\\s*datum|geboren\\s+am|geb\\.?\\s*am)\\s*[:\\-]?\\s*([0-3]?\\d[.\\-/][01]?\\d[.\\-/](?:19|20)\\d{2}|[0-3]?\\d\\.\\s*(?:januar|februar|märz|maerz|april|mai|juni|juli|august|september|oktober|november|dezember)\\s*\\d{4})"),
			new RegexRule("birth_date", "(?iu)\\b([0-3]?\\d[.\\-/][01]?\\d[.\\-/](?:19|20)\\d{2})\\b(?=,?\\s*(?:versicherten[-\\s]*nr|policen?-?nr|familien[-\\s]*nr|$))")
	);

	public Map<String, List<String>> extractEntities(String text) {
		Map<String, List<String>> entities = new HashMap<>();

		for (RegexRule rule : rules) {
			Pattern pattern = Pattern.compile(rule.regex());
			Matcher matcher = pattern.matcher(text);

			while (matcher.find()) {
				String value = matcher.group(1).trim();
				value = normalize(rule.label(), value);

				entities.computeIfAbsent(rule.label(), k -> new ArrayList<>());
				if (!entities.get(rule.label()).contains(value)) {
					entities.get(rule.label()).add(value);
				}
			}
		}
		return entities;
	}

	private String normalize(String label, String value) {
		if (label.equals("client_number") || label.equals("contract_number")) {
			return value.replaceAll("\\s+", "");
		}
		if (label.equals("birth_date")) {
			return value.replaceAll("\\s+", "");
		}
		return value;
	}

	private record RegexRule(String label, String regex) {}
}
