/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sitestats.api.ServerWideStatsRecord;

public class ServerWideStatsRecordImpl implements ServerWideStatsRecord, Serializable {
	private static final long serialVersionUID	= 1L;

	private List list;
	
	public ServerWideStatsRecordImpl ()
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
