package com.floreysoft.jmte.token;

import com.floreysoft.jmte.Token;

public class DefaultToken implements Token {

	private char[] buffer;
	private int start;
	private int end;
	private String sourceName;
	
	public DefaultToken() {

	}

	public DefaultToken(char[] buffer, int start, int end) {
		this(null, buffer, start, end);
	}

	public DefaultToken(String sourceName, char[] buffer, int start, int end) {
		this.setSourceName(sourceName);
		this.setBuffer(buffer);
		this.setStart(start);
		this.setEnd(end);
	}

	public String getText() {
		return new String(getBuffer(), getStart(), getEnd() - getStart());
	}

	public void setBuffer(char[] buffer) {
		this.buffer = buffer;
	}

	public char[] getBuffer() {
		return buffer;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}
	

	public int getLine() {
		int line = 1;
		for (int i = 0; i < start; i++) {
			if (buffer[i] == '\n') {
				line++;
			}
		}
		return line;
	}

	public int getColumn() {
		int column = 0;
		for (int i = start; i >= 0; i--) {
			if (buffer[i] == '\n') {
				break;
			} else {
				column++;
			}
		}
		return column;
	}

	@Override
	public String toString() {
		return getText();
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@Override
	public String getSourceName() {
		return sourceName;
	}
}
