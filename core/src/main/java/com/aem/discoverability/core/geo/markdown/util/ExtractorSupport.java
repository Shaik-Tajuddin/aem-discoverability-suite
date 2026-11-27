package com.aem.discoverability.core.geo.markdown.util;

import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public final class ExtractorSupport {
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private ExtractorSupport() {
    }

    public static boolean matches(String resourceType, String componentName) {
        if (StringUtils.isBlank((CharSequence)resourceType) || StringUtils.isBlank((CharSequence)componentName)) {
            return false;
        }
        String rt = resourceType;
        // Proxied site types (wknd/components/text), suite types, and Core Component versioned paths.
        return rt.equals("discoverability/components/" + componentName)
                || rt.endsWith("/components/" + componentName)
                || rt.endsWith("/components/" + componentName + "/v1/" + componentName)
                || rt.endsWith("/components/" + componentName + "/v2/" + componentName)
                || rt.endsWith("/components/" + componentName + "/v3/" + componentName);
    }

    public static String plainText(String htmlOrText) {
        if (StringUtils.isBlank((CharSequence)htmlOrText)) {
            return "";
        }
        String noTags = TAGS.matcher(htmlOrText).replaceAll(" ");
        return WHITESPACE.matcher(noTags).replaceAll(" ").trim().replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
    }

    public static String escapeMd(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
    }

    public static String heading(int level, String text) {
        int capped = Math.min(6, Math.max(1, level));
        String body = ExtractorSupport.plainText(text);
        if (body.isEmpty()) {
            return "";
        }
        return "#".repeat(capped) + " " + body + "\n\n";
    }

    public static String normalizeType(String type) {
        return StringUtils.trimToEmpty((String)type).toLowerCase(Locale.ROOT);
    }

    public static String firstNonBlank(String ... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!StringUtils.isNotBlank((CharSequence)value)) continue;
            return value;
        }
        return "";
    }
}

