package com.floreysoft.jmte.template;

import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.message.JournalingErrorHandler;
import com.floreysoft.jmte.token.InvalidToken;
import com.floreysoft.jmte.token.Token;

public class DefaultOutputAppender implements OutputAppender {

    @Override
    public void append(StringBuilder builder, String text, Token token) {
        if (!(token instanceof InvalidToken) &&
                !(token.getAnnotation() instanceof JournalingErrorHandler.Entry)) {
            builder.append(text);
        }
    }
}
