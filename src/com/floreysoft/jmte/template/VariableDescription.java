package com.floreysoft.jmte.template;

public class VariableDescription {
    public final String name;
    public final String renderer;

    public VariableDescription(String name, String renderer) {
        this.name = name;
        this.renderer = renderer;
    }

    public VariableDescription(String name) {
        this(name, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableDescription that = (VariableDescription) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return renderer != null ? renderer.equals(that.renderer) : that.renderer == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (renderer != null ? renderer.hashCode() : 0);
        return result;
    }
}
