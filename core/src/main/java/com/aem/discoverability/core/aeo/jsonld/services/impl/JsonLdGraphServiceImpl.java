package com.aem.discoverability.core.aeo.jsonld.services.impl;

import com.aem.discoverability.core.aeo.jsonld.registry.JsonLdEntityRegistry;
import com.aem.discoverability.core.aeo.jsonld.serialization.JsonLdSafeJson;
import com.aem.discoverability.core.aeo.jsonld.services.JsonLdGraphService;
import com.aem.discoverability.core.aeo.jsonld.validation.JsonLdValidator;
import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.ProjectResolverImpl;
import com.day.cq.wcm.api.Page;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service={JsonLdGraphService.class})
public class JsonLdGraphServiceImpl
implements JsonLdGraphService {
    static final String COMPONENT_RT = "discoverability/components/jsonld";
    @Reference
    private PageEligibilityService pageEligibilityService;
    @Reference
    private CanonicalUrlService canonicalUrlService;
    @Reference
    private DomainPolicy domainPolicy;

    @Override
    public Optional<String> buildJson(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null || !configuration.isJsonLdEnabled()) {
            return Optional.empty();
        }
        if (!this.pageEligibilityService.evaluate(page, configuration).isEligible()) {
            return Optional.empty();
        }
        Optional pageUrl = this.canonicalUrlService.resolve(page, configuration);
        if (pageUrl.isEmpty()) {
            return Optional.empty();
        }
        Optional origin = this.domainPolicy.publicOrigin(configuration);
        if (origin.isEmpty()) {
            return Optional.empty();
        }
        JsonLdEntityRegistry registry = new JsonLdEntityRegistry();
        this.collectPageComponents(page, configuration, (String)pageUrl.get(), (String)origin.get(), registry);
        if (configuration.isJsonLdAutoWebPage()) {
            JsonLdGraphServiceImpl.accept(registry, this.buildWebPage(page, (String)pageUrl.get()));
        }
        if (configuration.isJsonLdAutoBreadcrumb()) {
            JsonLdGraphServiceImpl.accept(registry, this.buildBreadcrumb(page, configuration));
        }
        if (configuration.isJsonLdIncludeOrganization()) {
            JsonLdGraphServiceImpl.accept(registry, JsonLdGraphServiceImpl.buildOrganization(configuration, (String)origin.get()));
        }
        if (configuration.isJsonLdIncludeWebSite()) {
            JsonLdGraphServiceImpl.accept(registry, JsonLdGraphServiceImpl.buildWebSite(configuration, (String)origin.get()));
        }
        if (registry.isEmpty()) {
            return Optional.empty();
        }
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("@context", "https://schema.org");
        root.put("@graph", registry.nodes());
        return Optional.of(JsonLdSafeJson.stringify(root));
    }

    private void collectPageComponents(Page page, ResolvedConfiguration configuration, String pageUrl, String origin, JsonLdEntityRegistry registry) {
        Resource content = page.getContentResource();
        if (content == null) {
            return;
        }
        this.walk(content, configuration, page, pageUrl, origin, registry);
    }

    private void walk(Resource resource, ResolvedConfiguration configuration, Page page, String pageUrl, String origin, JsonLdEntityRegistry registry) {
        if (COMPONENT_RT.equals(resource.getResourceType())) {
            JsonLdGraphServiceImpl.accept(registry, this.fromComponent(resource, page, pageUrl, origin));
        }
        for (Resource child : resource.getChildren()) {
            this.walk(child, configuration, page, pageUrl, origin, registry);
        }
    }

    private Map<String, Object> fromComponent(Resource resource, Page page, String pageUrl, String origin) {
        ValueMap props = resource.getValueMap();
        String type = JsonLdValidator.normalizeType((String)props.get("schemaType", (Object)"Article"));
        if (!JsonLdValidator.isSupportedType(type)) {
            return null;
        }
        LinkedHashMap<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("@type", type);
        switch (type) {
            case "Article": {
                JsonLdGraphServiceImpl.fillArticle(node, props, page, pageUrl, origin);
                break;
            }
            case "FAQPage": {
                JsonLdGraphServiceImpl.fillFaq(node, resource, pageUrl);
                break;
            }
            case "WebPage": {
                JsonLdGraphServiceImpl.fillWebPageOverride(node, props, page, pageUrl);
                break;
            }
            case "Organization": {
                JsonLdGraphServiceImpl.fillOrganizationOverride(node, props, origin);
                break;
            }
            case "WebSite": {
                JsonLdGraphServiceImpl.fillWebSiteOverride(node, props, origin);
                break;
            }
            case "BreadcrumbList": {
                return null;
            }
            default: {
                return null;
            }
        }
        return node;
    }

    private static void fillArticle(Map<String, Object> node, ValueMap props, Page page, String pageUrl, String origin) {
        String headline = JsonLdGraphServiceImpl.first((String)props.get("headline", (Object)""), page.getTitle(), page.getName());
        node.put("@id", pageUrl + "#article");
        node.put("headline", headline);
        JsonLdGraphServiceImpl.putIf(node, "description", JsonLdGraphServiceImpl.first((String)props.get("description", (Object)""), page.getDescription()));
        JsonLdGraphServiceImpl.putIf(node, "image", JsonLdGraphServiceImpl.toAbsoluteUrl((String)props.get("image", (Object)""), origin));
        JsonLdGraphServiceImpl.putIf(node, "datePublished", (String)props.get("datePublished", (Object)""));
        JsonLdGraphServiceImpl.putIf(node, "dateModified", (String)props.get("dateModified", (Object)""));
        String author = StringUtils.trimToEmpty((String)((String)props.get("authorName", (Object)"")));
        if (!author.isEmpty()) {
            LinkedHashMap<String, String> person = new LinkedHashMap<String, String>();
            person.put("@type", "Person");
            person.put("name", author);
            node.put("author", person);
        }
        LinkedHashMap<String, String> main = new LinkedHashMap<String, String>();
        main.put("@type", "WebPage");
        main.put("@id", pageUrl);
        node.put("mainEntityOfPage", main);
    }

    private static void fillFaq(Map<String, Object> node, Resource resource, String pageUrl) {
        node.put("@id", pageUrl + "#faq");
        ArrayList entities = new ArrayList();
        Resource questions = resource.getChild("questions");
        if (questions != null) {
            for (Resource q : questions.getChildren()) {
                ValueMap vm = q.getValueMap();
                String question = StringUtils.trimToEmpty((String)((String)vm.get("question", (Object)"")));
                String answer = StringUtils.trimToEmpty((String)((String)vm.get("answer", (Object)"")));
                if (question.isEmpty() || answer.isEmpty()) continue;
                LinkedHashMap<String, String> accepted = new LinkedHashMap<String, String>();
                accepted.put("@type", "Answer");
                accepted.put("text", answer);
                LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("@type", "Question");
                item.put("name", question);
                item.put("acceptedAnswer", accepted);
                entities.add(item);
            }
        }
        node.put("mainEntity", entities);
        node.put("url", pageUrl);
    }

    private static void fillWebPageOverride(Map<String, Object> node, ValueMap props, Page page, String pageUrl) {
        node.put("@id", pageUrl);
        node.put("url", pageUrl);
        node.put("name", JsonLdGraphServiceImpl.first((String)props.get("headline", (Object)""), page.getTitle(), page.getName()));
        JsonLdGraphServiceImpl.putIf(node, "description", JsonLdGraphServiceImpl.first((String)props.get("description", (Object)""), page.getDescription()));
    }

    private static void fillOrganizationOverride(Map<String, Object> node, ValueMap props, String origin) {
        String name = StringUtils.trimToEmpty((String)((String)props.get("headline", (Object)"")));
        if (name.isEmpty()) {
            return;
        }
        node.put("@id", origin + "/#organization");
        node.put("name", name);
        JsonLdGraphServiceImpl.putIf(node, "url", origin);
        JsonLdGraphServiceImpl.putIf(node, "logo", (String)props.get("image", (Object)""));
    }

    private static void fillWebSiteOverride(Map<String, Object> node, ValueMap props, String origin) {
        String name = StringUtils.trimToEmpty((String)((String)props.get("headline", (Object)"")));
        if (name.isEmpty()) {
            return;
        }
        node.put("@id", origin + "/#website");
        node.put("name", name);
        node.put("url", origin);
    }

    private Map<String, Object> buildWebPage(Page page, String pageUrl) {
        LinkedHashMap<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("@type", "WebPage");
        node.put("@id", pageUrl);
        node.put("url", pageUrl);
        node.put("name", JsonLdGraphServiceImpl.first(page.getTitle(), page.getName()));
        JsonLdGraphServiceImpl.putIf(node, "description", page.getDescription());
        Calendar modified = page.getLastModified();
        if (modified != null) {
            node.put("dateModified", modified.toInstant().toString());
        }
        return node;
    }

    private Map<String, Object> buildBreadcrumb(Page page, ResolvedConfiguration configuration) {
        String root = ProjectResolverImpl.normalizeRoot((String)configuration.getProjectRoot());
        ArrayList elements = new ArrayList();
        ArrayList<Page> chain = new ArrayList<Page>();
        for (Page current = page; current != null && ProjectResolverImpl.isUnder((String)current.getPath(), (String)root); current = current.getParent()) {
            chain.add(0, current);
            if (current.getPath().equals(root)) break;
        }
        int position = 1;
        for (Page crumb : chain) {
            Optional loc = this.canonicalUrlService.resolve(crumb, configuration);
            if (loc.isEmpty()) continue;
            LinkedHashMap<String, Object> listItem = new LinkedHashMap<String, Object>();
            listItem.put("@type", "ListItem");
            listItem.put("position", position++);
            listItem.put("name", JsonLdGraphServiceImpl.first(crumb.getNavigationTitle(), crumb.getTitle(), crumb.getName()));
            listItem.put("item", loc.get());
            elements.add(listItem);
        }
        if (elements.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("@type", "BreadcrumbList");
        node.put("@id", String.valueOf(((Map)elements.get(elements.size() - 1)).get("item")) + "#breadcrumb");
        node.put("itemListElement", elements);
        return node;
    }

    private static Map<String, Object> buildOrganization(ResolvedConfiguration configuration, String origin) {
        String name = StringUtils.trimToEmpty((String)configuration.getJsonLdOrganizationName());
        if (name.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("@type", "Organization");
        node.put("@id", origin + "/#organization");
        node.put("name", name);
        node.put("url", origin);
        JsonLdGraphServiceImpl.putIf(node, "logo", configuration.getJsonLdOrganizationLogoUrl());
        return node;
    }

    private static Map<String, Object> buildWebSite(ResolvedConfiguration configuration, String origin) {
        String name = StringUtils.trimToEmpty((String)configuration.getJsonLdWebsiteName());
        if (name.isEmpty()) {
            name = StringUtils.trimToEmpty((String)configuration.getJsonLdOrganizationName());
        }
        if (name.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("@type", "WebSite");
        node.put("@id", origin + "/#website");
        node.put("name", name);
        node.put("url", origin);
        LinkedHashMap<String, Object> publisher = new LinkedHashMap<>();
        publisher.put("@id", origin + "/#organization");
        node.put("publisher", publisher);
        return node;
    }

    private static void accept(JsonLdEntityRegistry registry, Map<String, Object> node) {
        if (node == null || !JsonLdValidator.isValid(node)) {
            return;
        }
        registry.register(node);
    }

    private static void putIf(Map<String, Object> node, String key, String value) {
        String trimmed = StringUtils.trimToEmpty((String)value);
        if (!trimmed.isEmpty()) {
            node.put(key, trimmed);
        }
    }

    private static String first(String ... values) {
        for (String value : values) {
            String trimmed = StringUtils.trimToEmpty((String)value);
            if (trimmed.isEmpty()) continue;
            return trimmed;
        }
        return "";
    }

    static String toAbsoluteUrl(String raw, String origin) {
        String value = StringUtils.trimToEmpty((String)raw);
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("https://") || value.startsWith("http://")) {
            return value;
        }
        if (value.startsWith("/") && StringUtils.isNotBlank((CharSequence)origin)) {
            return origin + value;
        }
        return "";
    }
}

