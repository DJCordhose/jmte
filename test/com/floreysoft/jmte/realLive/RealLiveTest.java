package com.floreysoft.jmte.realLive;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.realLive.model.Article;
import com.floreysoft.jmte.realLive.model.Customer;
import com.floreysoft.jmte.realLive.model.Item;
import com.floreysoft.jmte.realLive.model.Order;
import com.floreysoft.jmte.util.Util;

public class RealLiveTest {
	@Test
	public void shop() throws Exception {
		Date orderDate = new Date();
		Customer customer = new Customer("Oliver", "Zeigermann",
				"Gaußstraße 180\n" + "22765 Hamburg\n" + "GERMANY");
		Order order = new Order(customer, orderDate);

		Article article1 = new Article("How to become famous", new BigDecimal(
				"17.80"));
		order.getItems().add(new Item(1, article1));

		Article article2 = new Article("Cool stuff", new BigDecimal("1.00"));
		order.getItems().add(new Item(2, article2));

		String template = Util.resourceToString(
				"com/floreysoft/jmte/realLive/template/email.jmte", "UTF-8");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("order", order);

		String output = Engine.createDefaultEngine().transform(template, model);
		String expected = Util.resourceToString(
				"com/floreysoft/jmte/realLive/template/expected-output.txt",
				"UTF-8");
		assertEquals(expected, output);

	}
}
