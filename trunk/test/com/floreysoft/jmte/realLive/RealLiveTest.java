package com.floreysoft.jmte.realLive;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.realLive.model.Article;
import com.floreysoft.jmte.realLive.model.Customer;
import com.floreysoft.jmte.realLive.model.Item;
import com.floreysoft.jmte.realLive.model.Order;
import com.floreysoft.jmte.util.Util;

public class RealLiveTest {

	public static String unifyNewlines(String source) {
		final String regex = "\\r?\\n";
		final String clearedSource = source.replaceAll(regex, "\n");
		return clearedSource;
	}

	public static String template;
	static {
		try {
			template = Util.resourceToString(
					"com/floreysoft/jmte/realLive/template/email.jmte", "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	Map<String, Object> model = new HashMap<String, Object>();

	{
		Calendar instance = GregorianCalendar.getInstance(Locale.GERMAN);
		instance.set(2011, Calendar.JANUARY, 28);
		Date orderDate = instance.getTime();

		Customer customer = new Customer("Oliver", "Zeigermann",
				"Gaußstraße 180\n" + "22765 Hamburg\n" + "GERMANY");
		Order order = new Order(customer, orderDate);

		Article article1 = new Article("How to become famous", new BigDecimal(
				"17.80"));
		order.getItems().add(new Item(1, article1));

		Article article2 = new Article("Cool stuff", new BigDecimal("1.00"));
		order.getItems().add(new Item(2, article2));

		model.put("order", order);
		model.put("separator", "----------------");


	}

	@Test
	public void shop() throws Exception {
		Engine engine = Engine.createDefaultEngine();
		shopTest(engine);
	}
	
	public void shopTest(Engine engine) throws Exception {
		String output = shop(engine);
		String expected = Util.resourceToString(
				"com/floreysoft/jmte/realLive/template/expected-output.txt",
				"UTF-8");
		assertEquals(unifyNewlines(expected), unifyNewlines(output));


	}

	public String shop(Engine engine) {
		engine.registerRenderer(Date.class, new DateRenderer());
		engine.registerNamedRenderer(new CurrencyRenderer());
		return engine.transform(template, model);
		
	}
}
