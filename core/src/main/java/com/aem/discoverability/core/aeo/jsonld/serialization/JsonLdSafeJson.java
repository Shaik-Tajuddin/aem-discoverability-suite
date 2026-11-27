package com.aem.discoverability.core.aeo.jsonld.serialization;

import java.util.Collection;
import java.util.Map;

public final class JsonLdSafeJson {
    private JsonLdSafeJson() {
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        JsonLdSafeJson.write(out, value);
        return out.toString();
    }

    private static void write(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            JsonLdSafeJson.writeString(out, (String)value);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                if (!first) {
                    out.append(',');
                }
                first = false;
                JsonLdSafeJson.writeString(out, String.valueOf(e.getKey()));
                out.append(':');
                JsonLdSafeJson.write(out, e.getValue());
            }
            out.append('}');
        } else if (value instanceof Collection) {
            out.append('[');
            boolean first = true;
            for (Object item : (Collection)value) {
                if (item == null) continue;
                if (!first) {
                    out.append(',');
                }
                first = false;
                JsonLdSafeJson.write(out, item);
            }
            out.append(']');
        } else {
            JsonLdSafeJson.writeString(out, String.valueOf(value));
        }
    }

    private static void writeString(StringBuilder out, String raw) {
        out.append('\"');
        block12: for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);
            switch (c) {
                case '\"': {
                    out.append("\\\"");
                    continue block12;
                }
                case '\\': {
                    out.append("\\\\");
                    continue block12;
                }
                case '\b': {
                    out.append("\\b");
                    continue block12;
                }
                case '\f': {
                    out.append("\\f");
                    continue block12;
                }
                case '\n': {
                    out.append("\\n");
                    continue block12;
                }
                case '\r': {
                    out.append("\\r");
                    continue block12;
                }
                case '\t': {
                    out.append("\\t");
                    continue block12;
                }
                case '<': {
                    out.append("\\u003c");
                    continue block12;
                }
                case '>': {
                    out.append("\\u003e");
                    continue block12;
                }
                case '&': {
                    out.append("\\u0026");
                    continue block12;
                }
                default: {
                    if (c < ' ') {
                        out.append(String.format("\\u%04x", c));
                        continue block12;
                    }
                    out.append(c);
                }
            }
        }
        out.append('\"');
    }
}

