package com.aem.discoverability.core.seo.sitemap.writers;

import com.aem.discoverability.core.seo.sitemap.api.LanguageAlternate;
import com.aem.discoverability.core.seo.sitemap.models.SitemapEntry;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class SitemapXmlWriter {
    private static final DateTimeFormatter LASTMOD = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private SitemapXmlWriter() {
    }

    public static String writeIndex(List<String> partitionLocs) {
        StringBuilder xml = new StringBuilder(256 + partitionLocs.size() * 128);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String loc : partitionLocs) {
            xml.append("  <sitemap>\n");
            xml.append("    <loc>").append(SitemapXmlWriter.escape(loc)).append("</loc>\n");
            xml.append("  </sitemap>\n");
        }
        xml.append("</sitemapindex>\n");
        return xml.toString();
    }

    public static String writeUrlset(List<SitemapEntry> entries, boolean includeLastmod, boolean includeHreflang) {
        StringBuilder xml = new StringBuilder(512 + entries.size() * 256);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (includeHreflang) {
            xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ");
            xml.append("xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");
        } else {
            xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        }
        for (SitemapEntry entry : entries) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(SitemapXmlWriter.escape(entry.getLoc())).append("</loc>\n");
            if (includeLastmod) {
                entry.getLastModified().ifPresent(instant -> xml.append("    <lastmod>").append(SitemapXmlWriter.formatLastmod(instant)).append("</lastmod>\n"));
            }
            if (includeHreflang) {
                for (LanguageAlternate alt : entry.getAlternates()) {
                    xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(SitemapXmlWriter.escape(alt.getHreflang())).append("\" href=\"").append(SitemapXmlWriter.escape(alt.getAbsoluteUrl())).append("\"/>\n");
                }
            }
            xml.append("  </url>\n");
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    static String formatLastmod(Instant instant) {
        return LASTMOD.format(instant);
    }

    static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        block7: for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '&': {
                    out.append("&amp;");
                    continue block7;
                }
                case '<': {
                    out.append("&lt;");
                    continue block7;
                }
                case '>': {
                    out.append("&gt;");
                    continue block7;
                }
                case '\"': {
                    out.append("&quot;");
                    continue block7;
                }
                case '\'': {
                    out.append("&apos;");
                    continue block7;
                }
                default: {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }
}

