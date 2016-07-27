package com.floreysoft.jmte.template;

import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.token.Token;

public class DefaultOutputAppender implements OutputAppender {

    @Override
    public void append(StringBuilder builder, String text, Token token) {
        builder.append(text);
    }
}
