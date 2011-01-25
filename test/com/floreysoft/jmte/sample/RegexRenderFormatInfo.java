package com.floreysoft.jmte.sample;

import com.floreysoft.jmte.RenderFormatInfo;


public class RegexRenderFormatInfo implements RenderFormatInfo {

	private final String regexPatternDescription;

	public RegexRenderFormatInfo(String regexPatternDescription) {
		this.regexPatternDescription = regexPatternDescription;
	}

	public String getRegexPatternDescription() {
		return regexPatternDescription;
	}
	
	

}
