/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc.index;

import java.io.IOException;

import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;

/**
 * An additional interface that each implementation of <code>IndexInput</code> and <code>IndexOutput</code>
 * must implement. Used to configure newly created <code>IndexInput</code> and <code>IndexOutput</code>
 * Jdbc based implementation.
 *
 * @author kimchy
 */
public interface JdbcIndexConfigurable {

    /**
     * Configures the newly created <code>IndexInput</code> or <code>IndexOutput</code> implementations.
     *
     * @param name The name of the file entry
     * @param jdbcDirectory The jdbc directory instance
     * @param settings The relevant file entry settings
     * @throws IOException
     */
    void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException;
}
