package org.sakaiproject.pasystem.impl.handlebars;

import com.github.jknack.handlebars.Parser;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.TemplateSource;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * A simple {@link TemplateCache} built on top of {@link ConcurrentHashMap} that never expires members.
 *
 * @author edgar.espina
 * @author buckett
 * @see com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
 */
public class ForeverTemplateCache implements TemplateCache {
    /**
     * The logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The map cache.
     */
    private final ConcurrentMap<TemplateSource, Pair<TemplateSource, Template>> cache;

    /**
     * Creates a new ConcurrentMapTemplateCache.
     *
     * @param cache The concurrent map cache. Required.
     */
    protected ForeverTemplateCache(
            final ConcurrentMap<TemplateSource, Pair<TemplateSource, Template>> cache) {
        this.cache = notNull(cache, "The cache is required.");
    }

    /**
     * Creates a new ConcurrentMapTemplateCache.
     */
    public ForeverTemplateCache() {
        this(new ConcurrentHashMap<TemplateSource, Pair<TemplateSource, Template>>());
    }

    @Override
    public TemplateCache setReload(boolean reload) {
        // Ignored: Forever means forever...
        return this;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void evict(final TemplateSource source) {
        cache.remove(source);
    }

    @Override
    public Template get(final TemplateSource source, final Parser parser) throws IOException {
        notNull(source, "The source is required.");
        notNull(parser, "The parser is required.");

        /**
         * Don't keep duplicated entries, remove old one if a change is detected.
         */
        return cacheGet(source, parser);
    }

    /**
     * Get/Parse a template source.
     *
     * @param source The template source.
     * @param parser The parser.
     * @return A Handlebars template.
     * @throws IOException If we can't read input.
     */
    private Template cacheGet(final TemplateSource source, final Parser parser) throws IOException {
        Pair<TemplateSource, Template> entry = cache.get(source);
        if (entry == null) {
            logger.debug("Loading: {}", source);
            entry = Pair.of(source, parser.parse(source));
            cache.put(source, entry);
        } else {
            logger.debug("Found in cache: {}", source);
        }
        return entry.getValue();
    }
}
