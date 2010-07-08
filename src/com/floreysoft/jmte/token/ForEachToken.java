package com.floreysoft.jmte.token;

import java.util.Iterator;

public class ForEachToken extends AbstractToken {
	private final String varName;
	private final Iterator<Object> iterator;
	private String separator;
	private int scanIndex;
	private int offset;
	private boolean last;
	private boolean first;
	private int index;

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

	public void setLast(boolean last) {
		this.last = last;
	}

	public boolean isLast() {
		return last;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isFirst() {
		return first;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
