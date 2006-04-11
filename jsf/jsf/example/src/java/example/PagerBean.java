/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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




