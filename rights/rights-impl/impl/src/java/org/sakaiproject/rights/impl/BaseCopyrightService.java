package org.sakaiproject.rights.impl;

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.rights.api.CopyrightEdit;
import org.sakaiproject.rights.api.CopyrightService;
import org.sakaiproject.rights.api.RightsAssignment;
import org.sakaiproject.rights.api.RightsAssignmentEdit;


public class BaseCopyrightService implements CopyrightService {

	public void commitRightsAssignment(RightsAssignmentEdit edit) 
	{
		// TODO Auto-generated method stub

	}

	public RightsAssignmentEdit createRightsAssignment(String entityRef) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public RightsAssignment getRightsAssignment(String entityRef) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setRightsAssignment(String entityRef, RightsAssignment rights) 
	{
		// TODO Auto-generated method stub

	}
	
	public class BasicCopyright implements CopyrightEdit 
	{

		public void setOwner(String owner) 
		{
			// TODO Auto-generated method stub

		}

		public void setYear(String year) 
		{
			// TODO Auto-generated method stub

		}

		public String getOwner() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getYear() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Element toXml(Document doc, Stack stack) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}	// class BasicCopyright

}	// class BaseCopyrightService
