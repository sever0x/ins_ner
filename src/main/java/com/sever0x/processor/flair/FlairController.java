package com.sever0x.processor.flair;

import com.sever0x.processor.BaseNERController;
import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.DocumentParserService;
import com.sever0x.processor.docs.DocumentResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flair")
public class FlairController implements BaseNERController {

	private final DocumentParserService documentParserService;

	private final FlairService flairService;

	public FlairController(DocumentParserService documentParserService, FlairService flairService) {
		this.documentParserService = documentParserService;
		this.flairService = flairService;
	}

	@Override
	public NERResponse getEntities(MultipartFile file) {
		var document = documentParserService.parseDocument(file);
		String allText = document.getPages().stream()
				.map(DocumentResponse.PageBlock::getText)
				.collect(Collectors.joining("\n"));

		var entities = flairService.getEntities(allText);
		return new NERResponse(file.getOriginalFilename(), entities);
	}

	@Override
	public NERResponse getEntities(String raw) {
		return new NERResponse(flairService.getEntities(raw));
	}
}
