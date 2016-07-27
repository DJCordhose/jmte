package com.floreysoft.jmte.message;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.AbstractToken;
import com.floreysoft.jmte.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JournalingErrorHandler extends AbstractErrorHandler implements ErrorHandler {
    public final List<Entry> entries = new ArrayList<Entry>();

    public static class Entry {
        public final Message formattedMessage;
        public final String messageKey;
        public final Token token;
        public final Map<String, Object> parameters;

        public Entry(Message formattedMessage, String messageKey, Token token, Map<String, Object> parameters) {
            this.formattedMessage = formattedMessage;
            this.messageKey = messageKey;
            this.token = token;
            this.parameters = parameters;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String messageKey, Token token,
                      Map<String, Object> parameters) throws ParseException {
        Message message = new ResourceBundleMessage(messageKey).withModel(
                parameters).onToken(token);
        final Entry entry = new Entry(message, messageKey, token, parameters);
        if (token instanceof AbstractToken) {
            AbstractToken abstractToken = (AbstractToken) token;
            abstractToken.setAnnotation(entry);
        }
        entries.add(entry);
    }
}
