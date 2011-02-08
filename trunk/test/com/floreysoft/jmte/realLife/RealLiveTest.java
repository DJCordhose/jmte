package com.floreysoft.jmte.realLife;

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
import com.floreysoft.jmte.realLife.model.Article;
import com.floreysoft.jmte.realLife.model.Customer;
import com.floreysoft.jmte.realLife.model.Item;
import com.floreysoft.jmte.realLife.model.Order;
import com.floreysoft.jmte.util.Util;

public class RealLiveTest {

	public static String template = Util.resourceToString(
			"com/floreysoft/jmte/realLife/template/email.jmte", "UTF-8");

	public static String expected = Util.resourceToString(
			"com/floreysoft/jmte/realLife/template/expected-output.txt",
			"UTF-8");

	Map<String, Object> model = new HashMap<String, Object>();

	{
		Calendar instance = GregorianCalendar.getInstance(Locale.GERMAN);
		instance.set(2011, Calendar.JANUARY, 28);
		Date orderDate = instance.getTime();

		Customer customer = new Customer("Oliver", "Zeigermann",
				"Gaussstrasse 180\n" + "22765 Hamburg\n" + "GERMANY");
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
		assertEquals(Util.unifyNewlines(expected), Util.unifyNewlines(output));

	}

	public String shop(Engine engine) {
		engine.registerRenderer(Date.class, new DateRenderer());
		engine.registerNamedRenderer(new CurrencyRenderer());
		return engine.transform(template, model);

	}
}
