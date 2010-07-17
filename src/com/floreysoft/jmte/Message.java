package com.floreysoft.jmte;

import java.util.Locale;

public interface Message {
	public String format();

	public String format(Locale locale);
}
