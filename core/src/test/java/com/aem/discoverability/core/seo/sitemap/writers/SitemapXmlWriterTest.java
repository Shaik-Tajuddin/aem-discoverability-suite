package com.aem.discoverability.core.seo.sitemap.writers;

import com.aem.discoverability.core.seo.sitemap.api.LanguageAlternate;
import com.aem.discoverability.core.seo.sitemap.models.SitemapEntry;
import com.aem.discoverability.core.seo.sitemap.writers.SitemapXmlWriter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SitemapXmlWriterTest {
    SitemapXmlWriterTest() {
    }

    @Test
    void writesIndexAndEscapes() {
        String xml = SitemapXmlWriter.writeIndex(List.of("https://www.example.com/sitemap-0.xml"));
        Assertions.assertTrue((boolean)xml.contains("<sitemapindex"));
        Assertions.assertTrue((boolean)xml.contains("<loc>https://www.example.com/sitemap-0.xml</loc>"));
    }

    @Test
    void writesUrlsetWithLastmodAndHreflang() {
        SitemapEntry entry = new SitemapEntry("/content/site/en/about", "https://www.example.com/en/about", Instant.parse("2024-01-15T10:15:30Z"), List.of(new LanguageAlternate("/content/site/en/about", "en", "https://www.example.com/en/about")));
        String xml = SitemapXmlWriter.writeUrlset(List.of(entry), (boolean)true, (boolean)true);
        Assertions.assertTrue((boolean)xml.contains("xmlns:xhtml="));
        Assertions.assertTrue((boolean)xml.contains("<lastmod>2024-01-15T10:15:30Z</lastmod>"));
        Assertions.assertTrue((boolean)xml.contains("hreflang=\"en\""));
        Assertions.assertTrue((boolean)xml.contains("href=\"https://www.example.com/en/about\""));
    }

    @Test
    void escapesXmlSpecialCharacters() {
        Assertions.assertTrue((boolean)SitemapXmlWriter.escape((String)"a&b<c>\"d'").contains("&amp;"));
        Assertions.assertTrue((boolean)SitemapXmlWriter.escape((String)"a&b<c>\"d'").contains("&lt;"));
    }
}

