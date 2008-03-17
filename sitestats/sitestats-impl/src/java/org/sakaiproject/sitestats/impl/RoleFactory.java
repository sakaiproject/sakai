package org.sakaiproject.sitestats.impl;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;


public class RoleFactory implements ObjectCreationFactory {

	public Object createObject(Attributes attributes) throws Exception {
		String id = attributes.getValue("id");

		if(id == null){ throw new Exception("Mandatory id attribute not present on role tag."); }
		String role = new String(id);
		return role;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester digester) {
		// TODO Auto-generated method stub

	}

}
