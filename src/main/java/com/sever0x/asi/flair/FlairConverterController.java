package com.sever0x.asi.flair;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/converter")
public class FlairConverterController {

	private final AutoAnnotationService autoAnnotationService;

	public FlairConverterController(AutoAnnotationService autoAnnotationService) {
		this.autoAnnotationService = autoAnnotationService;
	}

	@PostMapping
	public ConverterResponse convert2ConllFormat(@RequestBody ConverterRequest converterRequest) {
		String result = autoAnnotationService.saveAnnotatedDocument(converterRequest.raw(), convert2InsuranceEntities(converterRequest));
		boolean converterResult = !"".equals(result);

		return new ConverterResponse(converterResult, autoAnnotationService.getTotalCountAnnotatedDocs());
	}

	private InsuranceEntities convert2InsuranceEntities(ConverterRequest request) {
		InsuranceEntities insuranceEntities = new InsuranceEntities();
		insuranceEntities.setPersonNames(Collections.singletonList(request.clientName()));
		insuranceEntities.setCompanyNames(Collections.singletonList(request.firmaName()));
		insuranceEntities.setContractNumbers(Collections.singletonList(request.contractNumber()));
		insuranceEntities.setCustomerNumbers(Collections.singletonList(request.clientNumber()));
		insuranceEntities.setDates(request.dates());
		insuranceEntities.setEmails(request.emails());
		insuranceEntities.setIbans(request.ibans());
		return insuranceEntities;
	}

	public record ConverterRequest(
			@JsonProperty("original_raw") String raw,
			@JsonProperty("contract_number") String contractNumber,
			@JsonProperty("client_number") String clientNumber,
			@JsonProperty("firma_name") String firmaName,
			@JsonProperty("client_name") String clientName,
			@JsonProperty("dates") List<String> dates,
			@JsonProperty("ibans") List<String> ibans,
			@JsonProperty("emails") List<String> emails
	) {}

	public record ConverterResponse(
			boolean converted,
			int totalDocs
	) {}
}
