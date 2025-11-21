package org.sakaiproject.entitybroker.rest;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.serialization.MapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityEncodingManagerJsonTest {

    private static class TestEncodingManager extends EntityEncodingManager {
        TestEncodingManager() {
            super(null, null);
        }
    }

    private static class Outer {
        private final String label;

        Outer(String label) {
            this.label = label;
        }

        class Inner {
            private final String title;

            Inner(String title) {
                this.title = title + "-" + label;
            }

            public String getTitle() {
                return title;
            }
        }
    }

    @Test
    public void serializeNonStaticInnerClassAsObject() throws Exception {
        ObjectMapper mapper = MapperFactory.jsonBuilder().build();
        TestEncodingManager manager = new TestEncodingManager();

        Outer outer = new Outer("outer");
        Outer.Inner inner = outer.new Inner("assignment");

        String json = manager.encodeData(inner, Formats.JSON, null, Collections.emptyMap());

        Map<String, Object> parsed = mapper.readValue(json, new TypeReference<Map<String, Object>>() { });
        assertEquals("assignment-outer", parsed.get("title"));
    }
}
