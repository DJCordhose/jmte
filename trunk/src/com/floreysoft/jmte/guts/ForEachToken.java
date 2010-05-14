package com.floreysoft.jmte.guts;

import java.util.Iterator;

public class ForEachToken implements Token {
	private final String varName;
	private final Iterator<Object> iterator;
	private String separator;
	private int scanIndex;
	private int offset;

	public ForEachToken(String varName, Iterable<Object> iterable) {
		this.varName = varName;
		this.iterator = iterable.iterator();
	}

	public Iterator<Object> iterator() {
		return iterator;
	}

	public String getVarName() {
		return varName;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getSeparator() {
		return separator;
	}

	public void setScanIndex(int scanIndex) {
		this.scanIndex = scanIndex;
	}

	public int getScanIndex() {
		return scanIndex;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

}
