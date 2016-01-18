package com.floreysoft.jmte.template;

public class VariableDescription {
    public enum Context {
        FOR_EACH, IF, TEXT
    }

    public final String name;
    public final String renderer;
    public final String parameters;
    public final Context context;

    public VariableDescription(String name, String renderer, String parameters, Context context) {
        this.name = name;
        this.renderer = renderer;
        this.parameters = parameters;
        this.context = context;
    }

    public VariableDescription(String name, String renderer) {
        this(name, renderer, null, null);
    }

    public VariableDescription(String name) {
        this(name, null, null, null);
    }

    public VariableDescription(String name, Context context) {
        this(name, null, null, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableDescription that = (VariableDescription) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (renderer != null ? !renderer.equals(that.renderer) : that.renderer != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        return context == that.context;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (renderer != null ? renderer.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
