package com.sever0x.processor.mailgen;

import com.sever0x.processor.mailgen.api.ListPlaceholdersResponse;
import com.sever0x.processor.mailgen.api.ListTemplatesResponse;
import com.sever0x.processor.mailgen.api.RenderRequest;
import com.sever0x.processor.mailgen.api.RenderResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mailgen")
public class MailGenController {

	private final TemplateService service;
	private final MailGenProperties props;

	public MailGenController(TemplateService service, MailGenProperties props) {
		this.service = service;
		this.props = props;
	}

	@GetMapping("/templates")
	public ListTemplatesResponse listTemplates() {
		return new ListTemplatesResponse(service.listTemplates());
	}

	@GetMapping("/templates/{name}/placeholders")
	public ListPlaceholdersResponse listPlaceholders(@PathVariable("name") String name) {
		return new ListPlaceholdersResponse(name, service.extractPlaceholders(name));
	}

	@PostMapping(value = "/render", consumes = MediaType.APPLICATION_JSON_VALUE)
	public RenderResponse render(@RequestBody RenderRequest req) {
		if (req.getTemplate() == null || req.getTemplate().isBlank()) {
			throw new MailGenException("Field 'template' is required");
		}
		Map<String, Object> values = Optional.ofNullable(req.getValues()).orElseGet(Map::of);

		var effectiveOnMissing = Optional.ofNullable(req.getOnMissing()).orElse(props.getDefaultOnMissing());
		var effectiveLE = Optional.ofNullable(req.getLineEndings()).orElse(props.getDefaultLineEndings());

		List<String> unresolved = new ArrayList<>();
		Map<String, Object> usedValues = new LinkedHashMap<>();

		String content = service.render(req.getTemplate(), values, effectiveOnMissing, effectiveLE, unresolved, usedValues);

		return new RenderResponse(req.getTemplate(), content, unresolved, usedValues);
	}
}