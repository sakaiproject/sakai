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
package org.sakaiproject.login.impl.velocity;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.sakaiproject.login.api.LoginRenderContext;
import org.sakaiproject.login.api.LoginRenderEngine;

public class VelocityLoginRenderContext implements LoginRenderContext {
	// Member variables
	
	private Context vcontext = new VelocityContext();

	private boolean debug = false;

	private Map options = null;

	private LoginRenderEngine renderEngine = null;
	
	// Implementation of LoginRenderContext
	
	public String dump() {
		if (debug)
		{
			Object[] keys = vcontext.getKeys();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < keys.length; i++)
			{
				Object o = vcontext.get((String) keys[i]);
				dumpObject(sb, keys[i], o);
			}
			return sb.toString();
		}
		else
		{
			return "";
		}
	}

	public Context getVelocityContext()
	{
		return vcontext;
	}
	
	public void put(String key, Object value) {
		vcontext.put(key, value);
	}

	public boolean uses(String includeOption) {
		if (options == null || includeOption == null)
		{
			return true;
		}
		return "true".equals(options.get(includeOption));
	}

	
	// Accessors
	
	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}
	
	public Map getOptions()
	{
		return options;
	}

	public void setOptions(Map options)
	{
		this.options = options;
	}
	
	public LoginRenderEngine getRenderEngine() {
		return renderEngine;
	}
	
	public void setRenderEngine(LoginRenderEngine renderEngine) {
		this.renderEngine = renderEngine;
	}

	
	// Helper methods
	
	private void dumpObject(StringBuilder sb, Object key, Object o)
	{
		if (o instanceof Map)
		{
			sb.append("Property ").append(key).append(":").append(o).append("\n");
			dumpMap(sb, key, (Map) o);
		}
		else if (o instanceof Collection)
		{
			sb.append("Property ").append(key).append(":").append(o).append("\n");
			dumpCollection(sb, key, (Collection) o);
		}
		else
		{
			sb.append("Property ").append(key).append(":").append(o).append("\n");
		}
	}
	
	private void dumpCollection(StringBuilder sb, Object key, Collection collection)
	{
		int n = 0;
		for (Iterator i = collection.iterator(); i.hasNext();)
		{
			String keyn = key.toString() + "." + String.valueOf(n);
			dumpObject(sb, keyn, i.next());
			n++;
		}
	}

	private void dumpMap(StringBuilder sb, Object key, Map map)
	{
		for (Iterator i = map.keySet().iterator(); i.hasNext();)
		{
			Object keyn = i.next();
			dumpObject(sb, key + "." + keyn, map.get(keyn));
		}
	}

}
