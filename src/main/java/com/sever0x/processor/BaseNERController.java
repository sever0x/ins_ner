package com.sever0x.processor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

public interface BaseNERController {
	@PostMapping("/file")
	NERResponse getEntities(MultipartFile file);

	@PostMapping("/text")
	NERResponse getEntities(@RequestBody String raw);
}
