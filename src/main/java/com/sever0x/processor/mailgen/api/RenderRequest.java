package com.sever0x.processor.mailgen.api;

import com.sever0x.processor.mailgen.MailGenProperties;

import java.util.Map;

public class RenderRequest {
	private String template;
	private Map<String, Object> values;
	private MailGenProperties.OnMissing onMissing;
	private MailGenProperties.LineEndings lineEndings;

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public MailGenProperties.OnMissing getOnMissing() {
		return onMissing;
	}

	public void setOnMissing(MailGenProperties.OnMissing onMissing) {
		this.onMissing = onMissing;
	}

	public MailGenProperties.LineEndings getLineEndings() {
		return lineEndings;
	}

	public void setLineEndings(MailGenProperties.LineEndings lineEndings) {
		this.lineEndings = lineEndings;
	}
}