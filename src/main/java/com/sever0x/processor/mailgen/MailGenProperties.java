package com.sever0x.processor.mailgen;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailgen")
public class MailGenProperties {

	private String templateLocation = "classpath:templates";

	private OnMissing defaultOnMissing = OnMissing.LEAVE;

	private LineEndings defaultLineEndings = LineEndings.LF;

	public enum OnMissing {LEAVE, EMPTY, ERROR}

	public enum LineEndings {LF, CRLF}

	public String getTemplateLocation() {
		return templateLocation;
	}

	public void setTemplateLocation(String templateLocation) {
		this.templateLocation = templateLocation;
	}

	public OnMissing getDefaultOnMissing() {
		return defaultOnMissing;
	}

	public void setDefaultOnMissing(OnMissing defaultOnMissing) {
		this.defaultOnMissing = defaultOnMissing;
	}

	public LineEndings getDefaultLineEndings() {
		return defaultLineEndings;
	}

	public void setDefaultLineEndings(LineEndings defaultLineEndings) {
		this.defaultLineEndings = defaultLineEndings;
	}
}