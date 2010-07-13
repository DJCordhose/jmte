package com.floreysoft.jmte.token;

import com.floreysoft.jmte.Token;

public abstract class AbstractToken implements Token {

	private char[] buffer;
	private int start;
	private int end;

	public AbstractToken() {

	}

	public AbstractToken(char[] buffer, int start, int end) {
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
	
	@Override
	public String toString() {
		return getText();
	}
}
