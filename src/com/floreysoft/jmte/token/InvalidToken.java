package com.floreysoft.jmte.token;

import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.message.ErrorMessage;

public class InvalidToken extends AbstractToken {
	public Object evaluate(TemplateContext context) {
		context.engine.getErrorHandler().error(ErrorMessage.INVALID_EXPRESSION, this);
		return "";
	}
}
