package com.sever0x.processor.mailgen;

public class MailGenException extends RuntimeException {
	public MailGenException(String message) {
		super(message);
	}

	public MailGenException(String message, Throwable cause) {
		super(message, cause);
	}
}
