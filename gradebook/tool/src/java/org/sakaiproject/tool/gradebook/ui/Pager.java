/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/ui/Pager.java,v 1.3 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A stopgap class to mimic the functionality of our eventual pager JSF
 * component. When such a component becomes available, we can take these
 * objects out of the backing beans, and refer to the backing bean methods
 * directly from the component tag.
 *
 * @deprecated Replaced by sakaix:pager component.
 */
public class Pager implements Serializable {
	private static final Log logger = LogFactory.getLog(Pager.class);

	private String selectedDisplayedRows;
	private List displayedRowsSelectItems;
	private Paging pagingBean;

	public Pager() {
		if (logger.isDebugEnabled()) logger.debug("PagerBean()");
		displayedRowsSelectItems = new ArrayList();
		displayedRowsSelectItems.add(new SelectItem(new Integer(10), "Show 10"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(20), "Show 20"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(50), "Show 50"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(0), "Show all"));
	}

	public void pagePrevious(ActionEvent event) {
		setFirstRow(Math.max(getFirstRow() - getInternalMaxDisplayedRows(), 0));
	}

	public void pageNext(ActionEvent event) {
		setFirstRow(Math.min(getFirstRow() + getInternalMaxDisplayedRows(), getDataRows() - 1));
	}

	public void pageFirst(ActionEvent event) {
		setFirstRow(0);
	}

	public void pageLast(ActionEvent event) {
		int displayed = getInternalMaxDisplayedRows();
		int lastPage = (getDataRows() - 1) / displayed;
		setFirstRow(lastPage * displayed);
	}

	public void pageChangeRange(ValueChangeEvent event) {
		if (logger.isDebugEnabled()) logger.debug("pageChangeRange");
	}

	public Paging getPagingBean() {
		return pagingBean;
	}
	public void setPagingBean(Paging pagingBean) {
		this.pagingBean = pagingBean;

		// Initialize the backing bean's paging position.
		// (The assumption here is that user preferences for the paging
		// components will be loaded outside the specific backing bean.)
		setFirstRow(0);
		setMaxDisplayedRows(new Integer(20));
	}

	public boolean isFirstPage() {
		return (getFirstRow() == 0);
	}
	public boolean isLastPage() {
		int maxDisplayedRows = getInternalMaxDisplayedRows();
		int dataRows = getDataRows();
		return ((maxDisplayedRows > dataRows) || ((getFirstRow() + maxDisplayedRows) >= dataRows));
	}

	public int getFirstRow() {
		return pagingBean.getFirstRow();
	}
	public void setFirstRow(int firstRow) {
		pagingBean.setFirstRow(firstRow);
	}

	int getInternalMaxDisplayedRows() {
		if (logger.isDebugEnabled()) logger.debug("getInternalMaxDisplayedRows " + pagingBean.getMaxDisplayedRows());
		return pagingBean.getMaxDisplayedRows();
	}

	public Integer getMaxDisplayedRows() {
		int returnInt = getInternalMaxDisplayedRows();
		if (returnInt == Integer.MAX_VALUE) {
			returnInt = 0;
		}
		if (logger.isDebugEnabled()) logger.debug("getMaxDisplayedRows " + returnInt);
		return new Integer(returnInt);
	}
	public void setMaxDisplayedRows(Integer iMaxDisplayedRows) {
		if (logger.isDebugEnabled()) logger.debug("setMaxDisplayedRows " + iMaxDisplayedRows);
		int maxDisplayedRows = getInternalMaxDisplayedRows();
		if (maxDisplayedRows != iMaxDisplayedRows.intValue()) {
			maxDisplayedRows = iMaxDisplayedRows.intValue();
			if (maxDisplayedRows == 0) {
				// "Show all" was selected.
				maxDisplayedRows = Integer.MAX_VALUE;
			}
			pagingBean.setMaxDisplayedRows(maxDisplayedRows);
			if (getFirstRow() < maxDisplayedRows) {
				setFirstRow(0);
			}
		}
	}

	public List getDisplayedRowsSelectItems() {
		return displayedRowsSelectItems;
	}
	public int getDataRows() {
		return pagingBean.getDataRows();
	}

	/**

	 * Since we don't know how the eventual paging control component
	 * will package this informational message, I'm implementing it
	 * in a very primitive way right now.
	 */
	public String getPageContext() {
		int dataRows = getDataRows();
		int lastDisplayedRow = getInternalMaxDisplayedRows();
		if (lastDisplayedRow != Integer.MAX_VALUE) {
			lastDisplayedRow += getFirstRow();
		}
		if (lastDisplayedRow > dataRows) {
			lastDisplayedRow = dataRows;
		}
		return "Viewing " + (getFirstRow() + 1) + " - " + lastDisplayedRow + " of " + dataRows + " students";
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/ui/Pager.java,v 1.3 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
