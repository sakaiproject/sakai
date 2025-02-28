package org.sakaiproject.tool.assessment.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextFormatTest {

    @Test
    public void testConvertPlaintextToFormattedTextNoHighUnicode() {
        // Test HTML special characters
        assertEquals("&lt;div&gt;", TextFormat.convertPlaintextToFormattedTextNoHighUnicode("<div>"));
        assertEquals("&amp;nbsp;", TextFormat.convertPlaintextToFormattedTextNoHighUnicode("&nbsp;"));
        assertEquals("Test &amp; Test", TextFormat.convertPlaintextToFormattedTextNoHighUnicode("Test & Test"));
        assertEquals("&lt;script&gt;alert(&quot;test&quot;);&lt;/script&gt;", 
            TextFormat.convertPlaintextToFormattedTextNoHighUnicode("<script>alert(\"test\");</script>"));
    }

    @Test
    public void testConvertFormattedTextToPlaintext() {
        // Test unescaping HTML entities
        assertEquals("<div>", TextFormat.convertFormattedTextToPlaintext("&lt;div&gt;"));
        assertEquals("&nbsp;", TextFormat.convertFormattedTextToPlaintext("&amp;nbsp;"));
        assertEquals("Test & Test", TextFormat.convertFormattedTextToPlaintext("Test &amp; Test"));
        assertEquals("<script>alert(\"test\");</script>", 
            TextFormat.convertFormattedTextToPlaintext("&lt;script&gt;alert(&quot;test&quot;);&lt;/script&gt;"));
    }

    @Test
    public void testNullAndEmptyInputs() {
        assertEquals("", TextFormat.convertPlaintextToFormattedTextNoHighUnicode(null));
        assertEquals("", TextFormat.convertPlaintextToFormattedTextNoHighUnicode(""));
        assertEquals("", TextFormat.convertFormattedTextToPlaintext(null));
        assertEquals("", TextFormat.convertFormattedTextToPlaintext(""));
    }
}