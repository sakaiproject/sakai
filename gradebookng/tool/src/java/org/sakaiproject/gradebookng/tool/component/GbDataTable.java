package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import java.util.List;

public class GbDataTable<T, S> extends DataTable<T, S> {
	public GbDataTable(final String id, final List<? extends IColumn<T, S>> columns,
										 final IDataProvider<T> dataProvider, final long rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	}

	@Override
	protected Item<IColumn<T, S>> newCellItem(final String id, final int index,
														 final IModel<IColumn<T, S>> model) {
		return new Item(id, index, model) {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Object modelObject = model.getObject();

				if (modelObject instanceof AbstractColumn &&
						"studentColumn".equals(((AbstractColumn) modelObject).getDisplayModel().getObject())) {
					tag.setName("th");
					tag.getAttributes().put("role", "rowheader");
					tag.getAttributes().put("scope", "row");
				} else {
					tag.getAttributes().put("role", "gridcell");
				}
			}
		};
	}
}
