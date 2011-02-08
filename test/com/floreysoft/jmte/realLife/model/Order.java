package com.floreysoft.jmte.realLife.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
	public final static BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("20.00");
	public final static BigDecimal SHIPPING_COSTS = new BigDecimal("3.00");
	
	private final Customer customer;
	private final Date orderDate;
	private final List<Item> items = new ArrayList<Item>();

	public Order(Customer customer, Date orderDate) {
		super();
		this.customer = customer;
		this.orderDate = orderDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public List<Item> getItems() {
		return items;
	}

	public BigDecimal getTotal() {
		return getTotalWithoutShipping().add(getShippingCost());
	}
	
	public BigDecimal getShippingCost() {
		if (getTotalWithoutShipping().compareTo(FREE_SHIPPING_THRESHOLD) == -1) {
			return SHIPPING_COSTS;
		} else {
			return new BigDecimal("0");
		}
	}

	public BigDecimal getTotalWithoutShipping() {
		BigDecimal total = new BigDecimal(0);
		for (Item item : items) {
			BigDecimal part = item.getSubTotal();
			total = total.add(part);
		}
		return total;
	}
}
