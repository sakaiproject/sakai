/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Static class hosting a couple of utility methods around strings.
 */
public class StringUtils {

	// Private Constructor -----------------------------------------------------

	/**
	 * Private constructor that prevents external instantiation.
	 */
	private StringUtils() {
		// Do nothing.
	}


	// Static Utility Methods --------------------------------------------------

    /**
     * Replaces all occurrences of a pattern within a string by a replacement.
     * @param source  the string that should be searched.
     * @param pattern  the pattern that should be replaced.
     * @param replace  the replacement that should be inserted instead of the
     *        pattern.
     * @return The updated source string.
     */
    public static String replace(String source, String pattern, String replace) {
        if (source == null || source.length() == 0
        		|| pattern == null || pattern.length() == 0) {
            return source;
        }

        int k = source.indexOf(pattern);

        if (k == -1) {
            return source;
        }

        StringBuffer out = new StringBuffer();
        int i = 0, l = pattern.length();

        while (k != -1) {
            out.append(source.substring(i, k));

            if (replace != null) {
                out.append(replace);
            }

            i = k + l;
            k = source.indexOf(pattern, i);
        }
        out.append(source.substring(i));
        return out.toString();
    }

    /**
     * TODO: can't we just use String[].clone()?
     * @param source
     * @return
     */
    public static String[] copy(String[] source) {
        if (source == null) {
            return null;
        }
        int length = source.length;
        String[] result = new String[length];
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    /**
     * Deep-clones a parameter map. The key is the parameter name as a String
     * instance, while the value is a String array (String[]) instance.
     * @param parameters  the parameter map to deep-clone.
     * @return the deep-cloned parameter map.
     */
    public static Map copyParameters(Map parameters) {
        Map result = new HashMap(parameters);
        for (Iterator it = result.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("Parameter map keys "
                		+ "must not be null and of type java.lang.String.");
            }
            try {
                entry.setValue(copy((String[]) entry.getValue()));
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("Parameter map values "
                		+ "must not be null and of type java.lang.String[].");
            }
        }
        return result;
    }

    /**
     * Strips the specified mime type by removing the character encoding
     * specified at the end of the mime type (all characters after the ';').
     * The stripped mime type is trimmed string which contains no white
     * spaces at the beginning and the end.
     * @param mimeType  the mime type to strip.
     * @return the stripped mime type.
     */
    public static String getMimeTypeWithoutEncoding(String mimeType) {
        int index = mimeType.indexOf(';');
        String strippedType = null;
        if (index == -1) {
            strippedType = mimeType;
        } else {
            strippedType = mimeType.substring(0, index);
        }
        return strippedType.trim();
    }

}
