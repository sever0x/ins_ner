package com.sever0x.asi.flair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AutoAnnotationService {

	private static final Logger log = LoggerFactory.getLogger(AutoAnnotationService.class);
	public static final String FLAIR_READY_FOLDER = "./flair_ready";
	public static final String FLAIR_READY_SUBFOLDER = "annotated_data";

	public String saveAnnotatedDocument(String content, InsuranceEntities entities) {
		String conllContent = convertToCoNLLFormat(clean(content), entities);
		String fileName = String.format("doc_%04d.conll", new Random().nextInt(0, Integer.MAX_VALUE));
		Path filePath = Paths.get(FLAIR_READY_FOLDER, FLAIR_READY_SUBFOLDER, fileName);

		try {
			Files.writeString(filePath, conllContent, StandardCharsets.UTF_8);
			log.debug("Saved annotated data: {}", fileName);
			return filePath.toString();
		} catch (IOException e) {
			log.error("Failed to save annotated document {}", fileName, e);
			throw new RuntimeException("Annotation save failed", e);
		}
	}

	public int getTotalCountAnnotatedDocs() {
		Path folderPath = Paths.get(FLAIR_READY_FOLDER, FLAIR_READY_SUBFOLDER);
		long count;
		try (var files = Files.list(folderPath)) {
			count = files.filter(Files::isRegularFile).count();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return (int) count;
	}

	private String convertToCoNLLFormat(String text, InsuranceEntities entities) {
		InsuranceEntities cleanedEntities = cleanEntities(entities);

		List<EntityMention> mentions = new ArrayList<>();
		addMentions(text, cleanedEntities.getContractNumbers(), "CONTRACT_NUMBER", mentions);
		addMentions(text, cleanedEntities.getCustomerNumbers(), "CUSTOMER_NUMBER", mentions);
		addMentions(text, cleanedEntities.getCompanyNames(), "COMPANY_NAME", mentions);
		addMentions(text, cleanedEntities.getPersonNames(), "PERSON_NAME", mentions);
		addMentions(text, cleanedEntities.getDates(), "DATE", mentions);
		addMentions(text, cleanedEntities.getEmails(), "EMAIL", mentions);
		addMentions(text, cleanedEntities.getIbans(), "IBAN", mentions);

		mentions.sort(Comparator.comparingInt(EntityMention::start).thenComparingInt(e -> e.end - e.start));

		StringBuilder conllOutput = new StringBuilder();
		int lastIndex = 0;

		for (EntityMention mention : mentions) {
			if (mention.start < lastIndex) {
				continue;
			}

			if (mention.start > lastIndex) {
				String nonEntityText = text.substring(lastIndex, mention.start);
				appendTokens(nonEntityText, conllOutput);
			}

			appendEntityTokens(mention.text, mention.label, conllOutput);
			lastIndex = mention.end;
		}

		if (lastIndex < text.length()) {
			String remainingText = text.substring(lastIndex);
			appendTokens(remainingText, conllOutput);
		}

		conllOutput.append("\n");

		return conllOutput.toString();
	}

	private void addMentions(String text, List<String> entityList, String label, List<EntityMention> mentions) {
		if (entityList == null) {
			return;
		}
		for (String entityText : entityList) {
			if (entityText == null || entityText.isBlank()) continue;
			Pattern pattern = Pattern.compile(Pattern.quote(entityText));
			pattern.matcher(text).results().forEach(match -> mentions.add(new EntityMention(match.group(), label, match.start(), match.end())));
		}
	}

	private String clean(String text) {
		if (text == null) return "";
		text = text.replaceAll("[*_#]+", "");
		text = text.replaceAll("[-=_]{3,}", "\n");
		text = text.replaceAll("\\|[- ]+\\|", " ");
		text = text.replaceAll("\\|", " ");
		text = text.replaceAll("\\s+([.,:;!?])", "$1");
		text = text.replaceAll("\\s{2,}", " ");
		text = text.replaceAll("(?m)^\\s*$[\n\r]{1,}", "");
		return text.trim();
	}

	private InsuranceEntities cleanEntities(InsuranceEntities rawEntities) {
		InsuranceEntities cleaned = new InsuranceEntities();

		cleaned.setContractNumbers(rawEntities.getContractNumbers());
		cleaned.setCustomerNumbers(rawEntities.getCustomerNumbers());
		cleaned.setCompanyNames(rawEntities.getCompanyNames());

		if (rawEntities.getPersonNames() != null) {
			List<String> cleanedNames = rawEntities.getPersonNames().stream()
					.map(name -> name.replaceAll("(Herrn?|Frau|Dr\\.|Prof\\.)\\s*", "").trim())
					.collect(Collectors.toList());
			cleaned.setPersonNames(cleanedNames);
		}

		return cleaned;
	}

	private void appendTokens(String text, StringBuilder builder) {
		String[] tokens = tokenize(text);
		for (String token : tokens) {
			if (token.isBlank()) continue;
			builder.append(token).append("\tO\n");
		}
	}

	private String[] tokenize(String text) {
		if (text == null || text.isEmpty()) {
			return new String[0];
		}

		text = text.replaceAll("([.,;!?():])", " $1 ");
		text = text.replaceAll("([a-zA-Z0-9])-(?=[a-zA-Z0-9])", "$1 - ");
		text = text.replaceAll("\\s+", " ").trim();

		return text.split("\\s+");
	}

	private void appendEntityTokens(String entityText, String label, StringBuilder builder) {
		String[] tokens = tokenize(entityText);
		if (tokens.length > 0) {
			if (!tokens[0].isBlank()) {
				builder.append(tokens[0]).append("\tB-").append(label).append("\n");
			}
			for (int i = 1; i < tokens.length; i++) {
				if (tokens[i].isBlank()) continue;
				builder.append(tokens[i]).append("\tI-").append(label).append("\n");
			}
		}
	}

	private record EntityMention(String text, String label, int start, int end) {
	}
}
