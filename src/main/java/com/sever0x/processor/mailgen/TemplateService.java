package com.sever0x.processor.mailgen;

import com.sever0x.processor.mailgen.api.PlaceholderInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TemplateService {

	private final MailGenProperties props;
	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private final TemplateEngine engine = new TemplateEngine();

	public TemplateService(MailGenProperties props) {
		this.props = props;
	}

	public List<String> listTemplates() {
		try {
			String base = normalizeLocation(props.getTemplateLocation());
			Resource[] resources = resolver.getResources(base + "/*.md");
			List<String> names = new ArrayList<>();
			for (Resource r : resources) {
				names.add(Objects.requireNonNull(r.getFilename()));
			}
			names.sort(Comparator.naturalOrder());
			return names;
		} catch (Exception e) {
			throw new MailGenException("Failed to list templates from location: " + props.getTemplateLocation(), e);
		}
	}

	public String loadTemplate(String name) {
		try {
			String base = props.getTemplateLocation();
			if (!base.startsWith("classpath:") && !base.startsWith("file:")) {
				base = "classpath:" + base;
			}
			Resource r = resolver.getResource(base + "/" + name);

			if (!r.exists()) {
				throw new MailGenException("Template not found: " + name + " in " + props.getTemplateLocation());
			}
			try (var br = new BufferedReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder sb = new StringBuilder();
				for (String line; (line = br.readLine()) != null; ) {
					sb.append(line).append("\n");
				}
				return sb.toString();
			}
		} catch (MailGenException e) {
			throw e;
		} catch (Exception e) {
			throw new MailGenException("Failed to load template: " + name, e);
		}
	}

	public List<PlaceholderInfo> extractPlaceholders(String name) {
		String content = loadTemplate(name);
		return engine.extractPlaceholders(content);
	}

	public String render(String name, Map<String, Object> values, MailGenProperties.OnMissing onMissing, MailGenProperties.LineEndings le, List<String> unresolvedOut, Map<String, Object> usedValuesOut) {
		String content = loadTemplate(name);
		String rendered = engine.render(content, values, onMissing, unresolvedOut, usedValuesOut);
		return normalizeLineEndings(rendered, le);
	}

	private static String normalizeLocation(String loc) {
		if (loc.startsWith("classpath:")) return "classpath*:" + loc.substring("classpath:".length());
		if (loc.startsWith("file:")) return loc;
		return "classpath*:" + loc;
	}

	private static String normalizeLineEndings(String s, MailGenProperties.LineEndings le) {
		String lf = s.replace("\r\n", "\n").replace("\r", "\n");
		if (le == MailGenProperties.LineEndings.CRLF) {
			return lf.replace("\n", "\r\n");
		}
		return lf;
	}
}