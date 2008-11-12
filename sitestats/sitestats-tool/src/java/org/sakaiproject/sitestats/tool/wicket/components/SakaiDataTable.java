package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.model.ResourceModel;

public class SakaiDataTable extends DataTable {
	private static final long	serialVersionUID	= 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            list of columns
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public SakaiDataTable(String id, final List/* <IColumn> */columns,
			ISortableDataProvider dataProvider)
	{
		this(id, (IColumn[])columns.toArray(new IColumn[columns.size()]), dataProvider, 20);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            array of columns
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public SakaiDataTable(String id, final IColumn[] columns, ISortableDataProvider dataProvider,
			int rowsPerPage)
	{
		super(id, columns, dataProvider, rowsPerPage);
		addTopToolbar(new SakaiNavigationToolBar(this));
		addTopToolbar(new HeadersToolbar(this, dataProvider));
		addBottomToolbar(new NoRecordsToolbar(this, new ResourceModel("no_data")));
	}
}
