package com.aem.discoverability.core.aeo.jsonld.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonLdEntityRegistry {
    private final Map<String, Map<String, Object>> byId = new LinkedHashMap<String, Map<String, Object>>();
    private final List<Map<String, Object>> anonymous = new ArrayList<Map<String, Object>>();

    public boolean register(Map<String, Object> node) {
        if (node == null || node.isEmpty()) {
            return false;
        }
        Object id = node.get("@id");
        if (id instanceof String && !((String)id).isEmpty()) {
            String key = (String)id;
            if (this.byId.containsKey(key)) {
                return false;
            }
            this.byId.put(key, node);
            return true;
        }
        this.anonymous.add(node);
        return true;
    }

    public List<Map<String, Object>> nodes() {
        ArrayList<Map<String, Object>> all = new ArrayList<Map<String, Object>>(this.byId.values());
        all.addAll(this.anonymous);
        return all;
    }

    public boolean isEmpty() {
        return this.byId.isEmpty() && this.anonymous.isEmpty();
    }
}

