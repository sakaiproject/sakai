/*
 * Copyright 2008 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.oauth.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/** A decorator that retains a copy of the first few bytes of data. */
public class ExcerptInputStream extends BufferedInputStream
{
    /**
     * A marker that's appended to the excerpt if it's less than the complete
     * stream.
     */
    public static final byte[] ELLIPSIS = " ...".getBytes();

    public ExcerptInputStream(InputStream in) throws IOException {
        super(in);
        mark(LIMIT);
        int total = 0;
        int read;
        while ((read = read(excerpt, total, LIMIT - total)) != -1 && ((total += read) < LIMIT));
        if (total == LIMIT) {
            // Only add the ellipsis if there are at least LIMIT bytes
            System.arraycopy(ELLIPSIS, 0, excerpt, total, ELLIPSIS.length);
        } else {
            byte[] tmp = new byte[total];
            System.arraycopy(excerpt, 0, tmp, 0, total);
            excerpt = tmp;
        }
        reset();
    }

    private static final int LIMIT = 1024;
    private byte[] excerpt = new byte[LIMIT + ELLIPSIS.length];

    /** The first few bytes of data, plus ELLIPSIS if there are more bytes. */
    public byte[] getExcerpt()
    {
        return excerpt;
    }

}
