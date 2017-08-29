/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.authz.impl;

import org.sakaiproject.authz.api.GroupProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map that allows look ups to be done by compound group IDs.
 *
 * @see GroupProvider#unpackId(String)
 * @see GroupProvider#packId(String[])
 */
public class ProviderMap implements Map<String, String>
{
	protected Map<String, String> m_wrapper = null;
	protected GroupProvider m_provider = null;

	/**
	 * Create a new ProviderMap.
	 * @param provider The Group Provider.
	 * @param wrapper The external group ID to role name map.
	 */
	public ProviderMap(GroupProvider provider, Map<String, String> wrapper)
	{
		m_provider = provider;
		m_wrapper = wrapper;
	}

	@Override
	public void clear()
	{
		m_wrapper.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return m_wrapper.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return m_wrapper.containsValue(value);
	}

	@Override
	public Set<Entry<String, String>> entrySet()
	{
		return m_wrapper.entrySet();
	}

	@Override
	public String get(Object key)
	{
		// if we have this key exactly, use it
		String value = m_wrapper.get(key);
		if (value != null) return value;

		// otherwise break up key as a compound id and find what values we have for these
		// the values are roles, and we prefer "maintain" to "access"
		String rv = null;
		String[] ids = m_provider.unpackId((String)key);
		for (int i = 0; i < ids.length; i++)
		{
			// try this one
			value = m_wrapper.get(ids[i]);

			// if we found one already, ask the provider which to keep
			if (value != null)
			{
				rv = m_provider.preferredRole(value, rv);
			}
		}

		return rv;
	}

	@Override
	public boolean isEmpty()
	{
		return m_wrapper.isEmpty();
	}

	@Override
	public Set<String> keySet()
	{
		return m_wrapper.keySet();
	}

	@Override
	public String put(String key, String value)
	{
		return m_wrapper.put(key, value);
	}

	@Override
	public void putAll(Map t)
	{
		m_wrapper.putAll(t);
	}

	@Override
	public String remove(Object key)
	{
		return m_wrapper.remove(key);
	}

	@Override
	public int size()
	{
		return m_wrapper.size();
	}

	@Override
	public Collection<String> values()
	{
		return m_wrapper.values();
	}
}
