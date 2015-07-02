/**
 * $URL$
 * $Id$
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
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;


public class SakaiNavigatorLabel extends Label {

	private static final long	serialVersionUID	= 1L;

	// TODO Factor this interface out and let dataview/datatable implement it
	private static interface PageableComponent extends IClusterable {
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
	 * @param id component id
	 * @param table dataview
	 */
	public SakaiNavigatorLabel(final String id, final DataTable table) {
		this(id, new PageableComponent() {

			private static final long	serialVersionUID	= 1L;

			@Override
			public int getCurrentPage() {
				return (int) table.getCurrentPage();
			}
			
			@Override
			public int getRowCount() {
				return (int) table.getRowCount();
			}
			
			@Override
			public int getRowsPerPage() {
				return (int) table.getItemsPerPage();
			}

		});

	}

	/**
	 * @param id component id
	 * @param table pageable view
	 */
	public SakaiNavigatorLabel(final String id, final DataView table) {
		this(id, new PageableComponent() {

			private static final long	serialVersionUID	= 1L;

			@Override
			public int getCurrentPage() {
				return (int) table.getCurrentPage();
			}

			@Override
			public int getRowCount() {
				return (int) table.getRowCount();
			}

			@Override
			public int getRowsPerPage() {
				return (int) table.getItemsPerPage();
			}

		});

	}

	private SakaiNavigatorLabel(final String id, final PageableComponent table) {
		super(id);
		Model model = new Model(new LabelModelObject(table)); 
		setDefaultModel(
				new StringResourceModel(
						"pager_textStatus", 
						this, 
						model,
						"Viewing {0} - {1} of {2} {3}",
						new Object[] {
							new PropertyModel(model, "from"),
							new PropertyModel(model, "to"),
							new PropertyModel(model, "of"),
							new ResourceModel("pager_textItem"),
						})
		);
	}

	private static class LabelModelObject implements IClusterable {
		private static final long		serialVersionUID	= 1L;
		private final PageableComponent	table;

		/**
		 * Construct.
		 * @param table
		 */
		public LabelModelObject(PageableComponent table) {
			this.table = table;
		}

		/**
		 * @return "z" in "Showing x to y of z"
		 */
		public int getOf() {
			return table.getRowCount();
		}

		/**
		 * @return "x" in "Showing x to y of z"
		 */
		public int getFrom() {
			if(getOf() == 0){
				return 0;
			}
			return (table.getCurrentPage() * table.getRowsPerPage()) + 1;
		}

		/**
		 * @return "y" in "Showing x to y of z"
		 */
		public int getTo() {
			if(getOf() == 0){
				return 0;
			}
			return Math.min(getOf(), getFrom() + table.getRowsPerPage() - 1);
		}

	}
}
