package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sitestats.api.StatsRecord;

public class StatsRecordImpl implements StatsRecord, Serializable {
	private static final long serialVersionUID	= 1L;

	private List list;
	
	public StatsRecordImpl ()
	{
	    list = new ArrayList ();
	}

	public void add (Object e)
	{
	    list.add (e);
	}

	public Object get (int index)
	{
	    return list.get (index);
	}
	
	


}
