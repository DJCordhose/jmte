package com.floreysoft.jmte.template;

import com.floreysoft.jmte.message.JournalingErrorHandler;
import com.floreysoft.jmte.token.Token;

public class ErrorReportingOutputAppender implements OutputAppender {

    private final static String ERROR_PATTERN = "[!!%s|%s|%s!!]";
    @Override
    public void append(StringBuilder builder, String text, Token token) {
        final String textToAppend;
        final Object annotation = token.getAnnotation();
        if (annotation instanceof JournalingErrorHandler.Entry) {
            final JournalingErrorHandler.Entry entry = (JournalingErrorHandler.Entry) annotation;
            final String message = entry.formattedMessage.formatPlain();
            textToAppend = String.format(ERROR_PATTERN, entry.messageKey, message, text);
        } else {
            textToAppend = text;
        }
        builder.append(textToAppend);
    }
}
