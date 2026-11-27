package com.aem.discoverability.core.aeo.jsonld.validation;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class JsonLdValidator {
    private static final Set<String> SUPPORTED = Set.of("Organization", "WebSite", "WebPage", "BreadcrumbList", "Article", "FAQPage");

    private JsonLdValidator() {
    }

    public static boolean isValid(Map<String, Object> node) {
        if (node == null) {
            return false;
        }
        String type = StringUtils.trimToEmpty((String)JsonLdValidator.stringVal(node.get("@type")));
        if (!SUPPORTED.contains(type)) {
            return false;
        }
        switch (type) {
            case "Organization": {
                return JsonLdValidator.hasText(node, "name");
            }
            case "WebSite": {
                return JsonLdValidator.hasText(node, "name") && JsonLdValidator.hasText(node, "url");
            }
            case "WebPage": {
                return JsonLdValidator.hasText(node, "name") || JsonLdValidator.hasText(node, "url");
            }
            case "BreadcrumbList": {
                return JsonLdValidator.hasNonEmptyList(node, "itemListElement");
            }
            case "Article": {
                return JsonLdValidator.hasText(node, "headline");
            }
            case "FAQPage": {
                return JsonLdValidator.hasNonEmptyList(node, "mainEntity");
            }
        }
        return false;
    }

    public static boolean isSupportedType(String type) {
        return SUPPORTED.contains(StringUtils.trimToEmpty((String)type));
    }

    private static boolean hasText(Map<String, Object> node, String key) {
        return StringUtils.isNotBlank((CharSequence)JsonLdValidator.stringVal(node.get(key)));
    }

    private static boolean hasNonEmptyList(Map<String, Object> node, String key) {
        Object value = node.get(key);
        return value instanceof Collection && !((Collection)value).isEmpty();
    }

    private static String stringVal(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static String normalizeType(String raw) {
        return StringUtils.trimToEmpty((String)raw);
    }

    public static String lower(String raw) {
        return JsonLdValidator.normalizeType(raw).toLowerCase(Locale.ROOT);
    }
}

