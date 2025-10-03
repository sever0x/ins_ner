package com.sever0x.processor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class DocumentController {

	private final DocumentParserService documentParserService;

	public DocumentController(DocumentParserService documentParserService) {
		this.documentParserService = documentParserService;
	}

	@PostMapping("/upload")
	public DocumentResponse uploadFile(@RequestParam("file") MultipartFile file) {
		return documentParserService.parseDocument(file);
	}
}
