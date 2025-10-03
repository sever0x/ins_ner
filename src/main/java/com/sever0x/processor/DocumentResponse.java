package com.sever0x.processor;

import java.util.List;

public class DocumentResponse {
	private String filename;
	private int pageCount;
	private List<PageBlock> pages;

	public DocumentResponse() {
	}

	public DocumentResponse(String filename, int pageCount, List<PageBlock> pages) {
		this.filename = filename;
		this.pageCount = pageCount;
		this.pages = pages;
	}

	public String getFilename() {
		return filename;
	}

	public int getPageCount() {
		return pageCount;
	}

	public List<PageBlock> getPages() {
		return pages;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public void setPages(List<PageBlock> pages) {
		this.pages = pages;
	}

	public static class PageBlock {
		private int pageNumber;
		private String text;
		private List<String> sentences;

		public PageBlock() {
		}

		public PageBlock(int pageNumber, String text, List<String> sentences) {
			this.pageNumber = pageNumber;
			this.text = text;
			this.sentences = sentences;
		}

		public int getPageNumber() {
			return pageNumber;
		}

		public String getText() {
			return text;
		}

		public List<String> getSentences() {
			return sentences;
		}

		public void setPageNumber(int pageNumber) {
			this.pageNumber = pageNumber;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setSentences(List<String> sentences) {
			this.sentences = sentences;
		}
	}
}