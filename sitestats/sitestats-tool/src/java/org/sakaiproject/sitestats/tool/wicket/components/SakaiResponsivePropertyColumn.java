package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import static org.sakaiproject.sitestats.tool.wicket.components.SakaiResponsiveAbstractColumn.DATA_LABEL_ATTR;

/**
 * A responsive PropertyColumn (adds CardTable support)
 * @author plukasew
 */
public class SakaiResponsivePropertyColumn<T,S> extends PropertyColumn<T,S>
{
	/**
	 * A PropertyColumn with CardTable support
	 * @param displayModel display model
	 * @param sortProperty sort property
	 * @param propertyExpression wicket property expression used by PropertyModel
	 */
    public SakaiResponsivePropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression)
    {
        super(displayModel, sortProperty, propertyExpression);
    }

	/**
	 * A PropertyColumn with CardTable support
	 * @param displayModel display model
	 * @param propertyExpression wicket property expression used by PropertyModel
	 */
    public SakaiResponsivePropertyColumn(IModel<String> displayModel, String propertyExpression)
    {
        super(displayModel, propertyExpression);
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel)
    {
        super.populateItem(item, componentId, rowModel);
        item.add(AttributeAppender.append(DATA_LABEL_ATTR, getDisplayModel()));
    }
}
