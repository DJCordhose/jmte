package com.floreysoft.jmte;

public interface Processor<T> {

	T eval(TemplateContext context);
	
}
