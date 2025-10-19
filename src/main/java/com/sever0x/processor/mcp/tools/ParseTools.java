package com.sever0x.processor.mcp.tools;

import com.sever0x.processor.docs.DocumentParserService;
import jakarta.annotation.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ParseTools {

	private final DocumentParserService documentParserService;

	public ParseTools(DocumentParserService documentParserService) {
		this.documentParserService = documentParserService;
	}

	public record ParseResult(String filename, String text, int pages){}

	@Tool(name = "parse_pdf", description = "Decode base64 PDF to raw text using PDFBox preprocessor")
	public ParseResult parse(
			@ToolParam(description = "Base64-encoded PDF bytes") String pdfBase64,
			@ToolParam(description = "Original filename", required = false) @Nullable String filename
	) {
		byte[] bytes = Base64.getDecoder().decode(pdfBase64);
		var doc = documentParserService.parseDocument(bytes, filename == null ? "upload.pdf" : filename);
		StringBuilder sb = new StringBuilder();
		doc.getPages().forEach(p -> sb.append(p.getText()).append('\n'));
		return new ParseResult(doc.getFilename(), sb.toString().trim(), doc.getPageCount());
	}
}
