package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;

public class GbHeadersToolbar extends HeadersToolbar {
	public GbHeadersToolbar(final DataTable table, final ISortStateLocator stateLocator) {
		super(table, stateLocator);
	}
}
