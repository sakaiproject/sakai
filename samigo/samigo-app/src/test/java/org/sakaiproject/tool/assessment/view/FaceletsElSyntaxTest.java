package org.sakaiproject.tool.assessment.view;

import org.junit.Test;

import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

/**
 * Scans Facelets for rendered attribute EL and attempts to parse it.
 * This catches tokenization typos like "ortotalScores" that break EL parsing at runtime.
 */
public class FaceletsElSyntaxTest {

    private static final Pattern RENDERED_ATTR =
            Pattern.compile("\\brendered\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EL_PATTERN = Pattern.compile("#\\{[^}]+\\}");

    @Test
    public void parseRenderedElExpressions() throws Exception {
        Path base = Paths.get("src/webapp/jsf");
        if (!Files.exists(base)) {
            return; // module without Facelets; nothing to validate
        }

        ExpressionFactory ef = ExpressionFactory.newInstance();
        StandardELContext ctx = new StandardELContext(ef);

        List<String> errors = new ArrayList<>();

        Files.walk(base)
                .filter(p -> p.toString().endsWith(".xhtml"))
                .forEach(p -> {
                    try {
                        String content = Files.readString(p, StandardCharsets.UTF_8);
                        Matcher attrMatcher = RENDERED_ATTR.matcher(content);
                        while (attrMatcher.find()) {
                            String attrValue = attrMatcher.group(2);
                            Matcher elMatcher = EL_PATTERN.matcher(attrValue);
                            while (elMatcher.find()) {
                                String expr = elMatcher.group();
                                String exprUnescaped = expr
                                        .replace("&lt;", "<")
                                        .replace("&gt;", ">")
                                        .replace("&amp;", "&");
                                int absoluteStart = attrMatcher.start(2) + elMatcher.start();
                                int line = 1;
                                for (int i = 0; i < absoluteStart && i < content.length(); i++) {
                                    if (content.charAt(i) == '\n') line++;
                                }
                                try {
                                    ef.createValueExpression(ctx, exprUnescaped, Object.class);
                                } catch (Exception e) {
                                    errors.add(p + ":" + line + " -> " + expr + " => " + e.getClass().getSimpleName() + ": " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException ioe) {
                        errors.add(p + " -> IO error: " + ioe.getMessage());
                    }
                });

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("EL parse errors found in rendered attributes:\n");
            for (String e : errors) sb.append(" - ").append(e).append('\n');
            fail(sb.toString());
        }
    }
}
