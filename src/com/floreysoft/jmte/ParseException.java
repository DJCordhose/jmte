package com.floreysoft.jmte;

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
	public final Message message;

	public ParseException(Message message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message.format();
	}
}
