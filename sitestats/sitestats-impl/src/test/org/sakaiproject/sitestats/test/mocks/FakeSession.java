package org.sakaiproject.sitestats.test.mocks;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;

public class FakeSession implements Session {
	private Map<Object,Object> attrs = new HashMap<Object, Object>();

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void clearExcept(Collection arg0) {
		// TODO Auto-generated method stub

	}

	public Object getAttribute(String attr) {
		return attrs.get(attr);
	}

	public Enumeration getAttributeNames() {
		return null;
	}

	public ContextSession getContextSession(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ToolSession getToolSession(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserEid() {
		return "userEid";
	}

	public String getUserId() {
		return "userId";
	}

	public void invalidate() {
		// TODO Auto-generated method stub

	}

	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setActive() {
		// TODO Auto-generated method stub

	}

	public void setAttribute(String key, Object value) {
		attrs.put(key, value);
	}

	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setUserEid(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setUserId(String arg0) {
		// TODO Auto-generated method stub

	}

}
