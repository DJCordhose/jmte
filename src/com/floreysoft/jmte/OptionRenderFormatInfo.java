package com.floreysoft.jmte;

public class OptionRenderFormatInfo implements RenderFormatInfo {

	private final String[] options;

	public OptionRenderFormatInfo(String[] options) {
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}

}
