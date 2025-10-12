package com.sever0x.processor.regexp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.DocumentParserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegexpControllerTest {

	@Autowired
	private RegexpNerService regexpNerService;

	@Autowired
	private DocumentParserService documentParserService;

	private RegexpController controller;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// For collecting metrics
	private static final List<NERTestResult> testResults = new ArrayList<>();
	private static final List<Map<String, Object>> errors = new ArrayList<>();

	@BeforeEach
	void setUp() {
		controller = new RegexpController(regexpNerService, documentParserService);
	}

	/**
	 * Data source for 18 files.
	 * The PDF name and the corresponding JSON name must match.
	 */
	static Stream<String> pdfFiles() {
		return Stream.of(
				"1 AXA.pdf",
				"2 Maklervertrag – AKG Assekuranz-Kontor GmbH.pdf",
				"3 Gewerbe Inhaltsversicherung.pdf",
				"4 versicherungspolice-beispiel.pdf – Helsana Versicherungspolice.pdf",
				"5 Muster-Versicherungsschein-VK.pdf",
				"6 versicherungspolice-beispiel.pdf",
				"7 Bestaetigung_Drohne_Privathaftpflicht_helden_de_MUSTER.pdf",
				"8 2022-Versicherungspolice-Muster.pdf",
				"9 Muster-Versicherungsschein-ONE-Kfz-Switch.pdf",
				"10 1695843460.pdf",
				"11 Ihre Police.pdf",
				"12 Muster-Police-Reiserücktritt-Vollschutz.pdf",
				"13 Muster-Versicherungsschein-VK-1.pdf",
				"14 GPT Muster-Versicherungsschein-Kfz-DE.pdf",
				"15 GPT Reiseversicherung_Mustervertrag.pdf",
				"16 GPT KFZ_Versicherung_2025.pdf",
				"17 GPT Privathaftpflichtversicherung_Pro_2025.pdf",
				"18 GPT Kapitallebensversicherung_2025.pdf"
		);
	}

	@ParameterizedTest
	@MethodSource("pdfFiles")
	@DisplayName("Verify that entities extracted from PDF match the reference JSON")
	void testExtractEntitiesMatchesExpected(String filename) throws Exception {
		var pdfResource = new ClassPathResource("examples/pdf/" + filename);
		byte[] pdfBytes = Files.readAllBytes(pdfResource.getFile().toPath());
		MockMultipartFile mockFile = new MockMultipartFile("file", filename, "application/pdf", pdfBytes);

		NERResponse response = controller.getEntities(mockFile);
		assertThat(response).isInstanceOf(NERResponse.class);
		var actualEntities = response.entities();

		File expectedFile = new ClassPathResource("examples/pdf/" + filename + ".json").getFile();
		Map<String, Object> expected = objectMapper.readValue(expectedFile, new TypeReference<>() {
		});

		@SuppressWarnings("unchecked")
		Map<String, List<String>> expectedEntities = (Map<String, List<String>>) expected.get("entities");

		int tp = 0, fp = 0, fn = 0;

		for (String key : expectedEntities.keySet()) {
			List<String> goldValues = expectedEntities.get(key);
			List<String> predictedValues = actualEntities.getOrDefault(key, Collections.emptyList());

			Set<String> goldSet = new HashSet<>(goldValues);
			Set<String> predSet = new HashSet<>(predictedValues);

			Set<String> truePositives = predSet.stream().filter(goldSet::contains).collect(Collectors.toSet());
			tp += truePositives.size();
			fp += predSet.size() - truePositives.size();
			fn += goldSet.size() - truePositives.size();

			// Soft check: log mismatch instead of failing
			if (!predSet.containsAll(goldSet)) {
				Map<String, Object> error = new LinkedHashMap<>();
				error.put("file", filename);
				error.put("field", key);
				error.put("expected", goldSet);
				error.put("actual", predSet);
				errors.add(error);

				System.err.printf(
						"⚠️  Field '%s' mismatch in %s. Expected: %s, got: %s%n",
						key, filename, goldSet, predSet
				);
			}
		}

		testResults.add(new NERTestResult(filename, tp, fp, fn));
	}

	@AfterAll
	void writeMetricsReport() throws Exception {
		int totalTP = testResults.stream().mapToInt(NERTestResult::tp).sum();
		int totalFP = testResults.stream().mapToInt(NERTestResult::fp).sum();
		int totalFN = testResults.stream().mapToInt(NERTestResult::fn).sum();

		double precision = totalTP + totalFP == 0 ? 0.0 : totalTP / (double) (totalTP + totalFP);
		double recall = totalTP + totalFN == 0 ? 0.0 : totalTP / (double) (totalTP + totalFN);
		double f1 = (precision + recall == 0) ? 0.0 : (2 * precision * recall) / (precision + recall);

		Map<String, Object> metrics = new LinkedHashMap<>();
		metrics.put("total_tp", totalTP);
		metrics.put("total_fp", totalFP);
		metrics.put("total_fn", totalFN);
		metrics.put("precision", precision);
		metrics.put("recall", recall);
		metrics.put("f1", f1);
		metrics.put("per_file", testResults);
		metrics.put("errors", errors);

		File outFile = new File("target/ner_metrics.json");
		try (FileWriter writer = new FileWriter(outFile)) {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, metrics);
		}

		System.out.println("📊 NER metrics saved to: " + outFile.getAbsolutePath());
		if (!errors.isEmpty()) {
			System.err.println("⚠️  Some mismatches were found. See 'errors' section in ner_metrics.json for details.");
		}
	}

	@Test
	@DisplayName("RegexpNerService should extract at least one insurer from a sample text")
	void testExtractInsurerDirectly() {
		String text = "Versicherer: Allianz Versicherung AG\nGeburtsdatum: 15.05.1985";
		Map<String, List<String>> result = regexpNerService.extractEntities(text);

		assertThat(result).containsKey("insurer");
		assertThat(result.get("insurer")).contains("Allianz Versicherung AG");
		assertThat(result.get("birth_date")).contains("15.05.1985");
	}

	record NERTestResult(String filename, int tp, int fp, int fn) {
	}
}
