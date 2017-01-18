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
package org.sakaiproject.wicket.markup.html.repeater.data.table;

import org.apache.wicket.IClusterable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

public class ClassicNavigatorLabel extends Label {
	private static final long serialVersionUID = 1L;

	// TODO Factor this interface out and let dataview/datatable implement it
	private static interface PageableComponent extends IClusterable
	{
		/**
		 * @return total number of rows across all pages
		 */
		int getRowCount();

		/**
		 * @return current page
		 */
		int getCurrentPage();

		/**
		 * @return rows per page
		 */
		int getRowsPerPage();
	}

	/**
	 * @param id
	 *            component id
	 * @param table
	 *            dataview
	 */
	public ClassicNavigatorLabel(final String id, final DataTable table)
	{
		this(id, new PageableComponent()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public int getCurrentPage()
			{
				return table.getCurrentPage();
			}

			public int getRowCount()
			{
				return table.getRowCount();
			}

			public int getRowsPerPage()
			{
				return table.getRowsPerPage();
			}

		});

	}

	/**
	 * @param id
	 *            component id
	 * @param table
	 *            pageable view
	 */
	public ClassicNavigatorLabel(final String id, final DataView table)
	{
		this(id, new PageableComponent()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public int getCurrentPage()
			{
				return table.getCurrentPage();
			}

			public int getRowCount()
			{
				return table.getRowCount();
			}

			public int getRowsPerPage()
			{
				return table.getItemsPerPage();
			}

		});

	}

	private ClassicNavigatorLabel(final String id, final PageableComponent table)
	{
		super(id);
		setDefaultModel(new StringResourceModel("NavigatorLabel", this, new Model(new LabelModelObject(table)), "${showingText} ${from} ${toText} ${to} ${ofText} ${of} ${suffixText}"));
	}

	private class LabelModelObject implements IClusterable
	{
		private static final long serialVersionUID = 1L;
		private PageableComponent table;
		private ResourceModel showingModel, ofModel, toModel, suffixModel;
		
		
		/**
		 * Construct.
		 * @param table
		 */
		public LabelModelObject(PageableComponent table)
		{
			this.table = table;
			this.showingModel = new ResourceModel("paging.display.viewing");
			this.ofModel = new ResourceModel("paging.display.of");
			this.toModel = new ResourceModel("paging.display.to");
			this.suffixModel = new ResourceModel("paging.display.suffix");
		}
		
		public String getShowingText() {
			return String.valueOf(showingModel.getObject());
		}
		
		public String getOfText() {
			return String.valueOf(ofModel.getObject());
		}
		
		public String getToText() {
			return String.valueOf(toModel.getObject());
		}
		
		public String getSuffixText() {
			return String.valueOf(suffixModel.getObject());
		}
		
		/**
		 * @return "z" in "Showing x to y of z"
		 */
		public int getOf()
		{
			return table.getRowCount();
		}
		
		/**
		 * @return "x" in "Showing x to y of z"
		 */
		public int getFrom()
		{
			if (getOf() == 0)
			{
				return 0;
			}
			return (table.getCurrentPage() * table.getRowsPerPage()) + 1;
		}
		
		/**
		 * @return "y" in "Showing x to y of z"
		 */
		public int getTo()
		{
			if (getOf() == 0)
			{
				return 0;
			}
			return Math.min(getOf(), getFrom() + table.getRowsPerPage()-1);
		}
		
	}
}
