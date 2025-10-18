package com.sever0x.processor.mcp.controller;

import com.sever0x.processor.BaseNERController;
import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.DocumentParserService;
import com.sever0x.processor.docs.DocumentResponse;
import com.sever0x.processor.mcp.service.McpNerService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mcp")
public class McpNERController implements BaseNERController {

	private final McpNerService nerService;
	private final DocumentParserService documentParserService;

	public McpNERController(McpNerService nerService, DocumentParserService documentParserService) {
		this.nerService = nerService;
		this.documentParserService = documentParserService;
	}

	@Override
	public NERResponse getEntities(@RequestPart("file") MultipartFile file) {
		var document = documentParserService.parseDocument(file);
		String allText = document.getPages().stream()
				.map(DocumentResponse.PageBlock::getText)
				.collect(Collectors.joining("\n"));

		return nerService.extract(allText, file.getOriginalFilename());
	}

	@Override
	public NERResponse getEntities(@RequestBody String raw) {
		return nerService.extract(raw, null);
	}
}
