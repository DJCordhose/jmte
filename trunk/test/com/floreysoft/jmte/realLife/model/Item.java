package com.floreysoft.jmte.realLife.model;

import java.math.BigDecimal;

public class Item {
	public final int amount;
	public final Article article;

	public Item(int amount, Article article) {
		super();
		this.amount = amount;
		this.article = article;
	}

	public BigDecimal getSubTotal() {
		return article.price.multiply(new BigDecimal(amount));
	}
}
