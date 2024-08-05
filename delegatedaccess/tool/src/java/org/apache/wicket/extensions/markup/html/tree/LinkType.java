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
package org.apache.wicket.extensions.markup.html.tree;

import org.apache.wicket.util.lang.EnumeratedType;

/**
 * The type of junction links and node selection links.
 * <dl>
 * <dt>Regular link</dt>
 * <dd>Non-ajax link, always refreshes the whole page. Works with javascript disabled.</dd>
 * <dt>Ajax link</dt>
 * <dd>Links that supports partial updates. Doesn't work with javascript disabled</dd>
 * <dt>Ajax fallback link</dt>
 * <dd>Link that supports partial updates. With javascript disabled acts like regular link. The
 * drawback is that generated url (thus the entire html) is larger then using the other two</dd>
 * </dl>
 */
public final class LinkType extends EnumeratedType
{

	/** partial updates with no fallback. */
	public static final LinkType AJAX = new LinkType("AJAX");

	/**
	 * partial updates that falls back to a regular link in case the client does not support
	 * javascript.
	 */
	public static final LinkType AJAX_FALLBACK = new LinkType("AJAX_FALLBACK");

	/**
	 * non-ajax version that always re-renders the whole page.
	 */
	public static final LinkType REGULAR = new LinkType("REGULAR");

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name of the type of the link
	 */
	public LinkType(String name)
	{
		super(name);
	}
}
