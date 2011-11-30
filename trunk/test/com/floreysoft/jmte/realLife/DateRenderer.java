package com.floreysoft.jmte.realLife;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.floreysoft.jmte.Renderer;

public class DateRenderer implements Renderer<Date>{
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
//		DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM, ''yy", Locale.GERMAN);

	@Override
	public String render(Date date, Locale locale) {
		String rendered = dateFormat.format(date);
		return rendered;
	}

}
