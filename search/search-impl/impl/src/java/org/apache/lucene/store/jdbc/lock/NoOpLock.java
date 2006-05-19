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

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.jdbc.JdbcDirectory;

/**
 * A simple no op lock. Performs no locking.
 *
 * @author kimchy
 */
public class NoOpLock extends Lock implements JdbcLock {

    public void configure(JdbcDirectory jdbcDirectory, String name) throws IOException {
        // do nothing
    }

    public void initializeDatabase(JdbcDirectory jdbcDirectory) {
        // do nothing
    }

    public boolean obtain() throws IOException {
        return true;
    }

    public void release() {
        //do nothing
    }

    public boolean isLocked() {
        return false;
    }
}
