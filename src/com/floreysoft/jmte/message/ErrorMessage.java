package com.floreysoft.jmte.message;

public enum ErrorMessage {
    NOT_ARRAY("not-array-error", false),
    INDEX_OUT_OF_BOUNDS("index-out-of-bounds-error", false),
    NO_CALL_ON_STRING("no-call-on-string", false),
    PROPERTY_ACCESS("property-access-error", false),
    INVALID_INDEX("invalid-index-error"),
    FOR_EACH_UNDEFINED_VARNAME("foreach-undefined-varname"),
    MISSING_END("missing-end"),
    UNMATCHED_END("unmatched-end"),
    ELSE_OUT_OF_SCOPE("else-out-of-scope"),
    INVALID_EXPRESSION("invalid-expression"),
    INVALID_ARRAY_SYNTAX("invalid-array-syntax");

    public final String key;
    public final boolean isStatic;
    private final boolean isSevere;

    ErrorMessage(String key) {
        this(key, true,true);
    }

    ErrorMessage(String key, boolean isStatic) {
        this(key, isStatic,true);
    }

    ErrorMessage(String key, boolean isStatic, boolean isSevere) {
        this.key = key;
        this.isStatic = isStatic;
        this.isSevere = isSevere;
    }

    public final boolean isSevere() {
        return this.isSevere;
    }
}
