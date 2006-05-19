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

package org.apache.lucene.store.jdbc.lock;

import java.io.IOException;

import org.apache.lucene.store.jdbc.JdbcDirectory;

/**
 * An extension insterface for Lucene <code>Lock</code> class.
 *
 * @author kimchy
 */
public interface JdbcLock {

    /**
     * Configures the lock. Called just after the lock is instantiated.
     *
     * @param jdbcDirectory The directory using the lock
     * @param name The name of the lock
     * @throws IOException
     */
    void configure(JdbcDirectory jdbcDirectory, String name) throws IOException;

    /**
     * Called without configure. Should initialize a creates database.
     *
     * @param jdbcDirectory The directory owning the lock
     */
    void initializeDatabase(JdbcDirectory jdbcDirectory) throws IOException;
}
