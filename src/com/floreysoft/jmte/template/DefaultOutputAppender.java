package com.floreysoft.jmte.template;

import com.floreysoft.jmte.message.ErrorEntry;
import com.floreysoft.jmte.token.InvalidToken;
import com.floreysoft.jmte.token.Token;

public class DefaultOutputAppender implements OutputAppender {

    @Override
    public void append(StringBuilder builder, String text, Token token) {
        if (!(token instanceof InvalidToken) &&
                !(token.getAnnotation() instanceof ErrorEntry)) {
            builder.append(text);
        }
    }
}
