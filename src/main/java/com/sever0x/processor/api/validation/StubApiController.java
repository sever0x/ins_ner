package com.sever0x.processor.api.validation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foreign-service")
public class StubApiController {

	@PostMapping("/stub")
	public boolean stub() {
		return true;
	}
}
