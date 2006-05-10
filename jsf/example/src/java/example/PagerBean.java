/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/


package example;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
/**
 * Example backing bean for the pager widget
 */
public class PagerBean
{
	private static final PrintStream out = System.out;
	private int totalItems;
	private int firstItem;
	private int pageSize;
	private ArrayList data;


	public PagerBean()
	{
		out.println("PagerBean()");
		totalItems = 211;
		firstItem = 50;
		pageSize = 5;
		initData();
	}

	public int getFirstItem()
	{
		out.println("PagerBean.getFirstItem()");
		return firstItem;
	}
	public void setFirstItem(int firstItem)
	{
		out.println("PagerBean.setFirstItem()");
		this.firstItem = firstItem;
	}
	public int getPageSize()
	{
		out.println("PagerBean.getPageSize()");
		return pageSize;
	}
	public void setPageSize(int pageSize)
	{
		out.println("PagerBean.setPageSize()");
		this.pageSize = pageSize;
	}
	public int getTotalItems()
	{
		out.println("PagerBean.getTotalItems()");
		return totalItems;
	}
	public void setTotalItems(int totalItems)
	{
		out.println("PagerBean.setTotalItems()");
		this.totalItems = totalItems;
	}

	public void handleValueChange(ValueChangeEvent event)
		throws AbortProcessingException
	{
		out.println("PagerBean.processValueChange(): old value: " + event.getOldValue() + " new value: " + event.getNewValue());
	}

	private void initData()
	{
		data = new ArrayList();
		for (int i=0; i<totalItems; i++)
		{
			data.add("Item #" + (i+1));
		}
	}

	public ArrayList getData()
	{
		out.println("PagerBean.getData()");
		return data;
	}
}




