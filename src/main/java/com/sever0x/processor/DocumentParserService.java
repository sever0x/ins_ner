package com.sever0x.processor;

import com.sever0x.processor.api.LayoutApiRetriever;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentParserService {

	private final Preprocessor preprocessor;

	private final LayoutApiRetriever layoutApiRetriever;

	public DocumentParserService(Preprocessor preprocessor, LayoutApiRetriever layoutApiRetriever) {
		this.preprocessor = preprocessor;
		this.layoutApiRetriever = layoutApiRetriever;
	}

	public DocumentResponse parseDocument(MultipartFile file) {
		String filename = file.getOriginalFilename();
		List<DocumentResponse.PageBlock> pages = new ArrayList<>();

		try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
			PDFTextStripper stripper = new PDFTextStripper();
			int totalPages = document.getNumberOfPages();

			for (int p = 1; p <= totalPages; p++) {
				stripper.setStartPage(p);
				stripper.setStartPage(p);
				String raw = stripper.getText(document);
				String text = preprocessor.normalize(raw);
				var sentences = preprocessor.splitIntoSentences(text);
				pages.add(new DocumentResponse.PageBlock(p, text, sentences));
			}

			var layout = layoutApiRetriever.retrieveLayout4Pdf(file);
			return new DocumentResponse(filename, totalPages, pages, layout.pages());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
