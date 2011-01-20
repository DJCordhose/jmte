package com.floreysoft.jmte;

public interface TemplateExpression<T> {

	T eval(TemplateContext context);
	
}
