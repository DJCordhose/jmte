package com.floreysoft.jmte.template;

import com.floreysoft.jmte.token.Token;

public interface OutputAppender {
    void append(StringBuilder builder, String text, Token token);
}
