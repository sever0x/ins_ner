package com.sever0x.processor.api;

import java.util.List;

public record PdfLayoutResponse(
		String filename,
		List<PdfLayoutPage> pages
) {
	public record PdfLayoutPage(
			int page_number,
			double width,
			double height,
			List<Block> blocks
	) {
		public record Block(
				String text,
				double x0,
				double y0,
				double x1,
				double y1
		) {}
	}
}