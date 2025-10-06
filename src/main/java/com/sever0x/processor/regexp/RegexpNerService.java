package com.sever0x.processor.regexp;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A service for extracting named entities from insurance-related text using regular expressions.
 * <p>
 * Supported entities:
 * <ul>
 *     <li><b>contract_number</b> – Insurance policy or contract number</li>
 *     <li><b>client_number</b> – Client or insured person's number</li>
 *     <li><b>insurer</b> – Name of the insurance company</li>
 *     <li><b>client_name</b> – Full name of the client or policyholder</li>
 *     <li><b>birth_date</b> – Date of birth of the client</li>
 * </ul>
 * <p>
 * The service compiles predefined regex patterns on initialization and applies them to input text.
 * Matches are normalized, validated, and returned as a map of entity labels to extracted values.
 */
@Service
public class RegexpNerService {

	private static final List<String> INSURER_SUFFIXES = Arrays.asList(
			"AG", "GmbH", "mbH\\s*&\\s*Co\\.\\s*K[GC]", "SE", "VVaG",
			"S\\.A\\.", "SA", "a\\.s\\.", "N\\.V\\.", "S\\.p\\.A\\.",
			"Versicherung(?:en)?", "Krankenkasse", "Gesellschaft"
	);
	private static final String INSURER_SUFFIX_PATTERN = String.join("|", INSURER_SUFFIXES);

	private final List<CompiledRegexRule> compiledRules;

	/**
	 * Creates a new {@code RegexpNerService} and precompiles all regex rules.
	 */
	public RegexpNerService() {
		this.compiledRules = createRules().stream()
				.map(rule -> new CompiledRegexRule(
						rule.label(),
						Pattern.compile(rule.regex(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CHARACTER_CLASS)
				))
				.collect(Collectors.toList());
	}

	/**
	 * Defines the list of regex rules used to extract entities from text.
	 *
	 * @return a list of regex rules for entity extraction
	 */
	private List<RegexRule> createRules() {
		return List.of(
				new RegexRule("contract_number", "(?iu)Vertragsnr\\.\\s*([A-Z0-9][A-Z0-9./\\-]{4,30})"),
				new RegexRule("contract_number", "(?iu)Versicherungsschein-Nummer:\\s*([A-Z0-9/\\-]{8,30})"),
				new RegexRule("contract_number", "(?iu)Police[-\\s]*Nr\\.?\\s*([A-Z0-9][A-Z0-9./\\-]{4,30})"),
				new RegexRule("contract_number", "(?iu)Versicherungsschein\\s+zur\\s+Kfz-Versicherung\\s+Nr\\.\\s*(\\d{5,10})"),
				new RegexRule("contract_number", "(?iu)VERSICHERUNGS-NACHWEIS\\s+FÜR\\s+DIE\\s+VERTRAGS-NR:\\s*([A-Z0-9]{8,12})"),
				new RegexRule("contract_number", "(?iu)(?:Police-Nr\\.|Versicherungsschein\\s+Nr\\.|Vertragsnummer:|Pol.-Nr.:)\\s*([A-Z]{2,5}-[A-Z]{2,5}-\\d{4}-\\d{4,5})"),
				new RegexRule("contract_number", "(?iu)(?:Police\\s*Nr\\.|Vertragsnummer:|Pol.-Nr.:|Contract Nr.:)\\s*([A-Z0-9_]{5,15})"),
				new RegexRule("contract_number", "(?iu)Police\\s*Nr\\.:\\s*(KPT\\d{7,10}|\\d{7,10})"),

				new RegexRule("client_number", "(?iu)^[A-ZÄÖÜ][a-zäöüß]+\\s+[A-ZÄÖÜ][a-zäöüß]+,\\s*\\d{2}\\.\\d{2}\\.\\d{4},\\s*Versicherten-Nr\\.\\s*([\\d\\s]{3}\\s[\\d\\s]{3}\\s[\\d\\s]{3})"),
				new RegexRule("client_number", "(?iu)^\\s*Versicherten-Nr\\.\\s*([\\d]{3}\\s[\\d]{3}\\s[\\d]{3})"),

				new RegexRule("insurer", "(?iu)(?:^|\\n)\\s*(?:Versicherer:|Versicherer\\s+ist\\s+die)\\s*([A-ZÄÖÜ][A-Za-zÄÖÜäöüß&\\.\\- ]{5,80}?(?:" + INSURER_SUFFIX_PATTERN + "))\\b"),
				new RegexRule("insurer", "(?iu)(AXA\\s+Versicherung\\s+AG|AKG\\s+Assekuranz-Kontor\\s+GmbH|Helvetia|Helsana\\s+Versicherungen\\s+AG|R\\+V\\s+Lebensversicherung\\s+AG|ONE\\s+Versicherung\\s+AG|Schweizerische\\s+Mobiliar\\s+Versicherungsgesellschaft\\s+AG|KPT\\s+(?:Krankenkasse|Versicherungen)\\s+AG|AWP\\s+P&C\\s+S\\.A\\.|Allianz\\s+Versicherung\\s+AG|GlobalTravel\\s+Versicherung\\s+AG|Nordstern\\s+Versicherung\\s+AG|SilberKlar\\s+Versicherung\\s+AG|HanseLife\\s+Versicherung\\s+AG)\\b"),
				new RegexRule("insurer", "(?iu)([A-ZÄÖÜ]{3,}\\s+(?:Versicherung|Versicherungen|Assekuranz|Kontor|Krankenkasse|Mobiliar)\\s+[A-Za-zÄÖÜäöüß]*\\s*(?:AG|GmbH|S\\.A\\.))\\b"),

				new RegexRule("client_name", "(?iu)(?:Versicherungsnehmer:|Name:|Versicherte:|Kunde:|Policenhalter:)\\s*([A-ZÄÖÜ][a-zäöüß]+(?:\\s+(?:von|van|zu|de|der))?(?:\\s+[A-ZÄÖÜ][a-zäöüß]+){0,3})"),
				new RegexRule("client_name", "(?iu)Versicherungsnehmer\\s*\\n\\s*([A-ZÄÖÜ][a-zäöüß]+\\s+[A-ZÄÖÜ][a-zäöüß]+)"),

				new RegexRule("birth_date", "(?iu)Geburtsdatum:\\s*(\\d{2}\\.\\d{2}\\.\\d{4})"),
				new RegexRule("birth_date", "(?iu)(?:Geb\\.:|Geboren\\s+am:|geb\\.\\s*)(\\d{2}\\.\\d{2}\\.\\d{4})"),
				new RegexRule("birth_date", "(?iu)[A-ZÄÖÜ][a-zäöüß]+\\s+[A-ZÄÖÜ][a-zäöüß]+,\\s*(\\d{2}\\.\\d{2}\\.\\d{4})")
		);
	}

	/**
	 * Extracts entities from the given text based on predefined regex patterns.
	 *
	 * @param text the input text to process
	 * @return a map where keys are entity labels and values are lists of matched entity values
	 */
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

	/**
	 * Normalizes extracted values based on their label.
	 * Removes unwanted characters and enforces consistent formatting.
	 *
	 * @param label the entity label
	 * @param value the raw extracted value
	 * @return the normalized value
	 */
	private String normalize(String label, String value) {
		value = value.replaceAll("[\\n\\r]+", " ").trim();

		if ("contract_number".equals(label) || "client_number".equals(label)) {
			value = value.replaceAll("[^A-Z0-9/\\-]", "");
		}
		if ("client_name".equals(label)) {
			value = value.replaceAll(",.*", "")
					.replaceAll("\\b(Straße|Strasse|Str\\.|Adresse|Postleitzahl|PLZ|Hausnummer|Hausnr\\.|Wohnort|Geburtsdatum)\\b.*", "")
					.trim()
					.replaceAll("\\s+", " ");
		}
		if ("birth_date".equals(label)) {
			if (value.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
				String[] parts = value.split("\\.");
				String day = String.format("%02d", Integer.parseInt(parts[0]));
				String month = String.format("%02d", Integer.parseInt(parts[1]));
				value = day + "." + month + "." + parts[2];
			}
		}
		return value;
	}

	/**
	 * Validates the extracted value based on entity-specific rules.
	 *
	 * @param label the entity label
	 * @param value the normalized value
	 * @return true if the value is considered valid, false otherwise
	 */
	private boolean isValid(String label, String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}

		switch (label) {
			case "contract_number":
				return value.length() >= 5 && value.length() <= 30 &&
				       value.matches(".*\\d.*") &&
				       !value.matches("(?i).*(bitte|angeben|ausstellungstag|vertragsbeginn|vertragsende|service|kunde|versicherung|widerruf|allgemein|bedingung|information|belehrung|hinweis|besondere|hinweise|legende|avb|zvb|uvb|bb|vb|agb|vvg|kvg|svg|vstg|vvg-infov|vvg-informationspflichtenverordnung|bdsG|dsgvo|eu|ewr|schweiz|deutschland|österreich|luxemburg|frankreich|usa|kanada|europa|weltweit|geltungsbereich|reiseart|reisedauer|leistungsübersicht|maximale|versicherungssumme|selbstbeteiligung|service-leistung|ohne|kostenübernahme|unser|versprechen|antworten|stornoberatung|umfangreiche|informationen|thema|reise|reiseversicherung|online|www|chatbot|kontaktformulare|anliegen|versicherungs-leistungen|notfall|service|schnelle|fachkundige|hilfe|weltweit|genaue|anschrift|telefonnummer|aufenthaltsortes|ansprechpartner|arzt|krankenhaus|polizei|sachverhalt|reisebeginn|ende|veranstalter|versicherungsschein-nummer|ziel|zielort|transitland|abschlussprüfung|klassenziel|vorrücken|veranstaltung|hauptzweck|reiseveranstalter|gewerblicher|anbieter|storniert|kosten|zusätzlich|gebucht|unterkunft|beförderung|ersatzdokumente|reiseplanung|geldtransfer|unverzüglich|familie|freunde|rechtlicher|beistand|konsulat|adresse|erreichbarkeit|wichtige|nachricht|heimat|allgemeine|ausschlüsse|schäden|umstände|ereignisse|bekannt|vorhersehbar|beabsichtigt|erwartet|vorerkrankungen|absichtlich|selbstverletzung|selbstmordversuch|selbstmord|schwangerschaften|geburten|normal|frei|komplikationen|fruchtbarkeitsbehandlungen|abbruch|medizinisch|indiziert|psychische|erkrankungen|alkohol|drogen|körperliche|symptome|medikamente|ärztlich|verschrieben|vorschrift|eingenommen|vorsätzlich|herbeigeführt|besatzung|flugzeug|nutzfahrzeug|gewerbliches|wasserfahrzeug|trainee|auszubildender|professioneller|semi-professioneller|sportwettbewerb|training|extremsportarten|risikoreiche|sport|freizeit|aktivitäten|fallschirmspringen|base-jumping|gleitschirm|drachenfliegen|bungee-springen|höhlenklettern|abseilen|höhlenwandern|skifahren|snowboarden|markierte|pisten|hubschrauber|klettersport|freies|klettern|aktivität|große|höhe|kampfsportarten|selbstverteidigung|rennsport|motorisierte|fahrzeuge|wasserfahrzeuge|apnoetauchen|gerätetauchen|20|metern|tauchlehrer|strafbare|handlung|verurteilung|opfer|epidemie|pandemie|naturkatastrophen|luft|wasser|verschmutzungen|thermische|biologische|chemische|verseuchung|gefahr|freisetzung|schadstoffe|kernreaktionen|kernstrahlung|radioaktive|krieg|bürgerkrieg|kriegsähnliche|ereignisse|militärdienst|zivile|unruhen|aufstand|terroristische|politische|risiken|cyber-risiko|maßnahmen|staatsgewalt|reisewarnungen|verbote|regierung|behörde|reiseanbieter|geschäftstätigkeit|finanzsituation|insolvenz|gepäck|medizinischer|bedarf|ausrüstung|abnutzung|normaler|gebrauch|fehlerhafte|materialien|mangelhafte|verarbeitung|medizinische|versorgung|behandlung|anlass|reise|absichtlich|herbeiführen|aktivitäten|geltende|gesetze|vorschriften|verstoßen|wirtschafts|handels|finanzsanktionen|embargos).*");

			case "client_number":
				String digitsOnly = value.replaceAll("\\D", "");
				return digitsOnly.length() >= 6 && digitsOnly.length() <= 12;

			case "insurer":
				boolean hasSuffix = INSURER_SUFFIXES.stream().anyMatch(suffix -> value.matches("(?i).*" + suffix + ".*"));
				boolean hasBadWords = value.matches(".*\\b(ist|hat|kann|wird|sind|eine|einer|dem|den|der|die|das|ein|eine|diese|dieser|dieses|diesen|diesem|jene|jener|jenes|jenen|jenem|solche|solcher|solches|solchen|solchem|welche|welcher|welches|welchen|welchem|meine|meiner|meines|meinen|meinem|deine|deiner|deines|deinen|deinem|seine|seiner|seines|seinen|seinem|ihre|ihrer|ihres|ihren|ihrem|unsere|unserer|unseres|unseren|unserem|eure|eurer|eures|euren|eurem|ihre|ihrer|ihres|ihren|ihrem)\\b.*");
				return hasSuffix && !hasBadWords && value.split("\\s+").length <= 8 && value.length() <= 80;

			case "birth_date":
				if (!value.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) return false;
				String[] parts = value.split("\\.");
				int day = Integer.parseInt(parts[0]);
				int month = Integer.parseInt(parts[1]);
				int year = Integer.parseInt(parts[2]);
				return day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 1900 && year <= 2025;

			case "client_name":
				String[] nameParts = value.split("\\s+");
				return nameParts.length >= 2 && nameParts.length <= 4 &&
				       !value.matches(".*\\d.*") &&
				       value.length() >= 5 && value.length() <= 50 &&
				       Arrays.stream(nameParts).allMatch(part -> !part.isEmpty() && Character.isUpperCase(part.charAt(0)));
			default:
				return true;
		}
	}

	private record RegexRule(String label, String regex) {}
	private record CompiledRegexRule(String label, Pattern pattern) {}
}
