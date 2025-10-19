package com.sever0x.processor.docs;

import com.sever0x.processor.api.LayoutApiRetriever;
import com.sever0x.processor.api.PdfLayoutResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentParserService {

	private final Preprocessor preprocessor;

	private final LayoutApiRetriever layoutApiRetriever;

	@Value("${api.layout.enabled}")
	private boolean isLayoutApiEnabled;

	public DocumentParserService(Preprocessor preprocessor, LayoutApiRetriever layoutApiRetriever) {
		this.preprocessor = preprocessor;
		this.layoutApiRetriever = layoutApiRetriever;
	}

	public DocumentResponse parseDocument(MultipartFile file) {
		String filename = file.getOriginalFilename();
		List<DocumentResponse.PageBlock> pages = new ArrayList<>();

		try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
			int totalPages = getTotalPages(document, pages);

			List<PdfLayoutResponse.PdfLayoutPage> layout = new ArrayList<>();
			if (isLayoutApiEnabled) {
				layout = layoutApiRetriever.retrieveLayout4Pdf(file).pages();
			}
			return new DocumentResponse(filename, totalPages, pages, layout);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public DocumentResponse parseDocument(byte[] bytes, String filename) {
		List<DocumentResponse.PageBlock> pages = new ArrayList<>();
		try (PDDocument document = Loader.loadPDF(bytes)) {
			int totalPages = getTotalPages(document, pages);
			return new DocumentResponse(filename, totalPages, pages, new ArrayList<>());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int getTotalPages(PDDocument document, List<DocumentResponse.PageBlock> pages) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
		int totalPages = document.getNumberOfPages();

		for (int p = 1; p <= totalPages; p++) {
			stripper.setStartPage(p);
			stripper.setEndPage(p);
			String raw = stripper.getText(document);
			String text = preprocessor.normalize(raw);
			var sentences = preprocessor.splitIntoSentences(text);
			pages.add(new DocumentResponse.PageBlock(p, text, sentences));
		}
		return totalPages;
	}
}
