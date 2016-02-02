package com.floreysoft.jmte.renderer;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

public abstract class SimpleNamedRenderer implements NamedRenderer {
    private final String name;

    public SimpleNamedRenderer(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    @Override
    public Class<?>[] getSupportedClasses() {
        return new Class<?>[0];
    }
}
