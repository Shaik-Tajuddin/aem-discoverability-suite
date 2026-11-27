package com.aem.discoverability.core.geo.markdown.util;

import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtractorSupportTest {
    ExtractorSupportTest() {
    }

    @Test
    void matchesDiscoverabilityAndCoreTypes() {
        Assertions.assertTrue((boolean)ExtractorSupport.matches((String)"discoverability/components/title", (String)"title"));
        Assertions.assertTrue((boolean)ExtractorSupport.matches((String)"core/wcm/components/title/v3/title", (String)"title"));
        Assertions.assertTrue((boolean)ExtractorSupport.matches((String)"wknd/components/text", (String)"text"));
        Assertions.assertTrue((boolean)ExtractorSupport.matches((String)"wknd/components/accordion", (String)"accordion"));
        Assertions.assertFalse((boolean)ExtractorSupport.matches((String)"discoverability/components/text", (String)"title"));
    }

    @Test
    void stripsHtml() {
        Assertions.assertEquals((Object)"Hello world", (Object)ExtractorSupport.plainText((String)"<p>Hello <b>world</b></p>"));
    }
}

