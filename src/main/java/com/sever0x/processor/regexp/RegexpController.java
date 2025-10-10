package com.sever0x.processor.regexp;

import com.sever0x.processor.BaseNERController;
import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.DocumentParserService;
import com.sever0x.processor.docs.DocumentResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regexp")
public class RegexpController implements BaseNERController {

	private final RegexpNerService regexpNerService;

	private final DocumentParserService documentParserService;

	public RegexpController(RegexpNerService regexpNerService, DocumentParserService documentParserService) {
		this.regexpNerService = regexpNerService;
		this.documentParserService = documentParserService;
	}

	@Override
	@PostMapping("/file")
	public NERResponse getEntities(MultipartFile file) {
		var document = documentParserService.parseDocument(file);
		String allText = document.getPages().stream()
				.map(DocumentResponse.PageBlock::getText)
				.collect(Collectors.joining("\n"));
		var entities = regexpNerService.extractEntities(allText);

		return new RegexpNERResponse(file.getOriginalFilename(), entities);
	}

	@PostMapping("/text")
	public NERResponse getEntities(@RequestBody String raw) {
		return new RegexpNERResponse(regexpNerService.extractEntities(raw));
	}
}
