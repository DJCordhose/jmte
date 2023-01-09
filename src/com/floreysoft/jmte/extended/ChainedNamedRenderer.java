/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.floreysoft.jmte.extended;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.util.Util;

import java.util.*;

/**
 * This {@link NamedRenderer} can be used to chain other {@link NamedRenderer}s.
 * <p>
 * This renderer is able to pass the rendered {@link String} across several different
 * {@link NamedRenderer}s. All renderers are able to use their specific format parameters -
 * they just need to be separated by a semicolon (;). To make this work, you need to register
 * this renderer <b>after</b> all other renderer in the {@link com.floreysoft.jmte.Engine}. E.g.:
 * <pre>engine.registerNamedRenderer(new ChainedNamedRenderer(engine.getAllNamedRenderers()));</pre>
 * </p>
 * <p/>
 * <p>
 * Example: <pre>${token;chain(renderer1(options1);renderer2)}</pre>
 * </p>
 */
public class ChainedNamedRenderer implements NamedRenderer {

    private final Map<String, NamedRenderer> namedRenderers;

    public ChainedNamedRenderer(Collection<NamedRenderer> namedRenderers) {
        this.namedRenderers = new HashMap<>();
        namedRenderers.forEach(namedRenderer -> this.namedRenderers.put(namedRenderer.getName(), namedRenderer));
    }

    @Override
    public String render(Object o, String format, Locale locale, Map<String, Object> model) {
        Object result = o;

        List<String> subRenderers = Util.RAW_OUTPUT_MINI_PARSER.split(format, ';', Integer.MAX_VALUE);

        for (String subRenderer : subRenderers) {
            List<String> strings = Util.MINI_PARSER.greedyScan(subRenderer, "(", ")");

            String rendererName = getSafe(strings, 0);
            String rendererParams = getSafe(strings, 1);

            if (rendererName == null || rendererName.isEmpty()) {
                continue;
            }

            NamedRenderer namedRenderer = namedRenderers.get(rendererName);
            if (namedRenderer != null) {
                result = namedRenderer.render(result, rendererParams, locale, model);
            }
        }

        return String.valueOf(result);
    }

    private String getSafe(List<String> list, int index) {
        if (index < list.size()) {
            return list.get(index);
        }
        return "";
    }

    @Override
    public String getName() {
        return "chain";
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    @Override
    public Class<?>[] getSupportedClasses() {
        return new Class[0];
    }
}
