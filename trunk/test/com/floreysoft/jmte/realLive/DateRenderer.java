package com.floreysoft.jmte.realLive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.floreysoft.jmte.Renderer;

public class DateRenderer implements Renderer<Date>{

	@Override
	public String render(Date date) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
//		DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM, ''yy", Locale.GERMAN);
		String rendered = dateFormat.format(date);
		return rendered;
	}

}
