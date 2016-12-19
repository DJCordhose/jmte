package com.floreysoft.jmte.message;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.AbstractToken;
import com.floreysoft.jmte.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JournalingErrorHandler extends AbstractErrorHandler implements ErrorHandler {
    public final List<ErrorEntry> entries = new ArrayList<ErrorEntry>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(ErrorMessage errorMessage, Token token,
                      Map<String, Object> parameters) throws ParseException {
        Message message = new ResourceBundleMessage(errorMessage.key).withModel(
                parameters).onToken(token);
        final ErrorEntry entry = new ErrorEntry(message, errorMessage, token, parameters);
        if (token instanceof AbstractToken) {
            AbstractToken abstractToken = (AbstractToken) token;
            abstractToken.setAnnotation(entry);
        }
        entries.add(entry);
    }
}
