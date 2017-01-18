/**
 * Copyright (c) 2007 The Apereo Foundation
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

/*
 * This file contains code copied from the Apache Wicket project, from the class
 *  org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
 * Originally authored by Igor Vaynberg (ivaynberg)
 * 
 * Significant modifications have been made to this code, but the original license of that class 
 * is listed below:
 * 
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
package org.sakaiproject.wicket.markup.html.repeater.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.PropertyResolver;

public class SortableListDataProvider implements IDataProvider, ISortStateLocator, IDetachable {

	private static final long serialVersionUID = 1L;

	private List list;
	
	private SingleSortState state = new SingleSortState() {
		public void setPropertySortOrder(String property, int dir)
		{
			super.setPropertySortOrder(property, dir);
			sortList(property, dir);
		}
	};

	public SortableListDataProvider(List list)
	{
		super();
		if (list == null)
		{
			throw new IllegalArgumentException("argument [list] cannot be null");
		}

		this.list = list;
	}

	public final void setSortState(ISortState state)
	{
		if (!(state instanceof SingleSortState))
		{
			throw new IllegalArgumentException(
					"argument [state] must be an instance of SingleSortState, but it is ["
							+ state.getClass().getName() + "]:[" + state.toString() + "]");
		}
		this.state = (SingleSortState)state;
	}
	
	public final ISortState getSortState()
	{
		return state;
	}
	
	public Iterator iterator(final int first, final int count)
	{	
		return list.subList(first, first + count).listIterator();
	}

	public int size()
	{
		return list.size();
	}

	public IModel model(Object object)
	{
		return new Model((Serializable)object);
	}

	public void setSort(SortParam param)
	{
		state.setSort(param);
	}

	public void setSort(String property, boolean ascending)
	{
		setSort(new SortParam(property, ascending));
	}
	
	public SortParam getSort()
	{
		return state.getSort();
	}
	
	private void sortList(final String propertyExpression, int dir) {
	
		Comparator propertyComparator = new Comparator() {
	
			public int compare(Object o1, Object o2) {
					
				Object p1 = PropertyResolver.getValue(propertyExpression, o1);
				Object p2 = PropertyResolver.getValue(propertyExpression, o2);
					
				if (p1 instanceof Comparable)
					return ((Comparable)p1).compareTo((Comparable)p2);

				return 0;
			}
		};
			
		Collections.sort(list, propertyComparator);
		
		if (dir == ISortState.DESCENDING)
			Collections.reverse(list);
	}

	public void detach() {

	}

}
