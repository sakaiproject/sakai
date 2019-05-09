package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * Abstract base class for responsive columns (adds CardTable support)
 * @author plukasew
 */
public abstract class SakaiResponsiveAbstractColumn<T,S> extends AbstractColumn<T,S>
{
	protected static final String DATA_LABEL_ATTR = "data-label";

	/**
	 * An AbstractColumn with CardTable support
	 * @param displayModel display model
	 * @param sortProperty sort property
	 */
	public SakaiResponsiveAbstractColumn(IModel<String> displayModel, S sortProperty)
	{
		super(displayModel, sortProperty);
	}

	/**
	 * A PropertyColumn with CardTable support
	 * @param displayModel display model
	 */
	public SakaiResponsiveAbstractColumn(IModel<String> displayModel)
	{
		super(displayModel);
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel)
	{
		item.add(AttributeAppender.append(DATA_LABEL_ATTR, getDisplayModel()));
		populateItemContribution(item, componentId, rowModel);
	}

	public abstract void populateItemContribution(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel);
}
