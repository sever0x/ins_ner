package com.sever0x.processor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class Preprocessor {

	public String normalize(String raw) {
		if (raw == null) {
			return StringUtils.EMPTY;
		}
		String s = raw.replace('\u00A0', ' ')
				.replaceAll("[\\t\\f\\r]+", " ")
				.replaceAll(" +", " ");
		s = s.replaceAll("\\s*\\n\\s*", "\n");
		s = s.replaceAll("[^\\p{Print}\\n]", "");
		return s.trim();
	}

	public List<String> splitIntoSentences(String text) {
		List<String> sentences = new ArrayList<>();
		if (text == null || text.isEmpty()) {
			return sentences;
		}

		BreakIterator it = BreakIterator.getSentenceInstance(Locale.GERMAN);
		it.setText(text);
		int start = it.first();
		for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
			String chunk = text.substring(start, end).trim();
			if (!chunk.isEmpty()) sentences.add(chunk);
		}
		return sentences;
	}
}
