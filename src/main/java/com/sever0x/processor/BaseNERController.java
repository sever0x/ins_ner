package com.sever0x.processor;

import org.springframework.web.multipart.MultipartFile;

public interface BaseNERController {
	NERResponse getEntities(MultipartFile file);
}
