/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.pasystem.impl.handlebars;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.jknack.handlebars.Parser;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.TemplateSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * A simple {@link TemplateCache} built on top of {@link ConcurrentHashMap} that never expires members.
 *
 * @author edgar.espina
 * @author buckett
 * @see com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
 */
@Slf4j
public class ForeverTemplateCache implements TemplateCache {

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
            log.debug("Loading: {}", source);
            entry = Pair.of(source, parser.parse(source));
            cache.put(source, entry);
        } else {
            log.debug("Found in cache: {}", source);
        }
        return entry.getValue();
    }
}
