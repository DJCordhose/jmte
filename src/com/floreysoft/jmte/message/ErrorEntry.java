package com.floreysoft.jmte.message;

import com.floreysoft.jmte.token.Token;

import java.util.Map;

public class ErrorEntry {
    public final Message formattedMessage;
    public final ErrorMessage errorMessage;
    public final Token token;
    public final Map<String, Object> parameters;

    public ErrorEntry(Message formattedMessage, ErrorMessage errorMessage, Token token, Map<String, Object> parameters) {
        this.formattedMessage = formattedMessage;
        this.errorMessage = errorMessage;
        this.token = token;
        this.parameters = parameters;
    }
}
