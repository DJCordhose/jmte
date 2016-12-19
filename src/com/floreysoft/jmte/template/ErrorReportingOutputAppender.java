package com.floreysoft.jmte.template;

import com.floreysoft.jmte.message.ErrorEntry;
import com.floreysoft.jmte.token.Token;

public class ErrorReportingOutputAppender implements OutputAppender {

    private final static String ERROR_PATTERN = "[!!%s|%s|%s!!]";
    private String prefix = "${";
    private String suffix = "}";

    @Override
    public void append(StringBuilder builder, String text, Token token) {
        final String textToAppend;
        final Object annotation = token.getAnnotation();
        if (annotation instanceof ErrorEntry) {
            final ErrorEntry entry = (ErrorEntry) annotation;
            final String message = entry.formattedMessage.formatPlain();
            final String expressionText;
            if (text == null) {
                expressionText = "";
            } else {
                expressionText = prefix + token.getText() + suffix;
            }
            textToAppend = String.format(ERROR_PATTERN, entry.errorMessage.key, message, expressionText);
        } else {
            textToAppend = text;
        }
        builder.append(textToAppend);
    }
}
