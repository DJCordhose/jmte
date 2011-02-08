package com.floreysoft.jmte.realLife.model;

import java.math.BigDecimal;

public class Article {
	public final String name;
	public final BigDecimal price;

	public Article(String name, BigDecimal price) {
		super();
		this.name = name;
		this.price = price;
	}

}
