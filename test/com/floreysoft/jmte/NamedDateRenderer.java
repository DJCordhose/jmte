package com.floreysoft.jmte;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NamedDateRenderer implements NamedRenderer {

	private final String regexPatternDescription = "Was weiß ich denn?";

	Date convert(Object o, DateFormat dateFormat) {
		if (o instanceof Date) {
			return (Date) o;
		} else if (o instanceof Number) {
			long longValue = ((Number) o).longValue();
			return new Date(longValue);
		} else if (o instanceof String) {
			try {
				return dateFormat.parse((String) o);
			} catch (ParseException e) {
			}
		}
		return null;
	}

	@Override
	public RenderFormatInfo getFormatInfo() {
		return new RegexRenderFormatInfo(regexPatternDescription);
	}

	@Override
	public String getName() {
		return "date";
	}

	@Override
	public Class[] getSupportedClasses() {
		return new Class[] { Date.class, String.class, Integer.class,
				Long.class };
	}

	@Override
	public String render(Object o, String pattern) {
		try {
			DateFormat dateFormat =  new SimpleDateFormat(pattern);
			Date value = convert(o, dateFormat);
			if (value != null) {
				String format = dateFormat.format(value);
				return format;
			}
		} catch (IllegalArgumentException iae) {
		} catch (NullPointerException npe) {
		}
		return null;

	}
}
