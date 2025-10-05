package com.sever0x.processor.regexp;

import com.sever0x.processor.BaseNERController;
import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.DocumentParserService;
import com.sever0x.processor.docs.DocumentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RegexpController implements BaseNERController {

	private final RegexpNerService regexpNerService;

	private final DocumentParserService documentParserService;

	public RegexpController(RegexpNerService regexpNerService, DocumentParserService documentParserService) {
		this.regexpNerService = regexpNerService;
		this.documentParserService = documentParserService;
	}

	@Override
	@PostMapping("/regexp")
	public NERResponse getEntities(MultipartFile file) {
		var document = documentParserService.parseDocument(file);
		String allText = document.getPages().stream()
				.map(DocumentResponse.PageBlock::getText)
				.collect(Collectors.joining("\n"));
		var entities = regexpNerService.extractEntities(allText);

		return new RegexpNERResponse(file.getOriginalFilename(), entities);
	}
}
