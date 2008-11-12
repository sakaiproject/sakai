package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;


/**
 * @author Nuno Fernandes
 */
public class EventRegistryTree extends Panel {
	private static final long	serialVersionUID	= 1L;

	private List<?>		eventRegistry		= null;
	private Rows		rows 				= null;

	public EventRegistryTree(String id, List<?> eventRegistry) {
		this(id, eventRegistry, null);
	}
	
	public EventRegistryTree(String id, List<?> eventRegistry, String toolId) {
		super(id);
		this.eventRegistry = eventRegistry;
		WebMarkupContainer ul = new WebMarkupContainer("ul");
		if(toolId != null) {
			ul.add(new AttributeModifier("style", true, new Model("padding: 0 0 0 20px;")));
		}else{
			ul.add(new AttributeModifier("style", true, new Model("padding: 0px;")));
		}
		add(ul);
		rows = new Rows("rows", eventRegistry, toolId);
		ul.add(rows);
	}

	@Override
	public void renderHead(HtmlHeaderContainer container) {
		container.getHeaderResponse().renderJavascriptReference("/sakai-sitestats-tool/script/prefs.js");
		container.getHeaderResponse().renderOnDomReadyJavascript("updateAllToolsSelection()");
		super.renderHead(container);
	}

	public List<?> getEventRegistry() {
		return rows.getList();
	}
	
	public boolean isToolSuported(ToolInfo toolInfo) {
		return true;
	}

	private static class Rows extends ListView {
		private String		currentToolId		= null;
		/**
		 * Construct.
		 * @param name name of the component
		 * @param list a list where each element is either a string or another
		 *            list
		 */
		public Rows(String name, List list, String toolId) {
			super(name, list);
			this.currentToolId = toolId;
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem listItem) {
			Object modelObject = listItem.getModelObject();

			if(modelObject instanceof ToolInfo){
				final ToolInfo ti = (ToolInfo) modelObject;
				final String toolId = ti.getToolId().replace('.', '_');
                
				WebMarkupContainer row = new WebMarkupContainer("row");
				row.add(new AttributeModifier("class", true, new Model("tool tool_"+toolId)));
				// navigating images
				ExternalImage navCollapse = new ExternalImage("navCollapse", "images/nav-minus.gif");
				row.add(navCollapse);
				ExternalImage navExpand = new ExternalImage("navExpand", "images/nav-plus.gif");
				row.add(navExpand);
				navCollapse.add(new AttributeModifier("onclick", true, new Model("jQuery('."+toolId+"').hide(); jQuery('#"+navExpand.getMarkupId()+"').show(); setMainFrameHeightNoScroll( window.name ); jQuery(this).hide(); return false;")));
				navExpand.add(new AttributeModifier("onclick", true, new Model("jQuery('."+toolId+"').show(); jQuery('#"+navCollapse.getMarkupId()+"').show(); setMainFrameHeightNoScroll( window.name ); jQuery(this).hide(); return false;")));
				navCollapse.add(new AttributeModifier("style", true, new Model("display: none")));
								
				// image, label, checkbox
				row.add(new ExternalImage("image", "images/silk/icons/application_side_boxes.png"));
				row.add(new Label("label", new Model(ti.getToolName())));
				CheckBox toolCheckBox = new CheckBox("checkbox", new PropertyModel(ti, "selected"));
				toolCheckBox.add(new AttributeModifier("onclick", true, new Model("selectUnselectEvents(this); updateToolSelection('.tool_"+toolId+"');")));
				row.add(toolCheckBox);
				listItem.add(row);
				
				EventRegistryTree nested = new EventRegistryTree("nested", ti.getEvents(), toolId);
				nested.add(new AttributeModifier("class", true, new Model(toolId)));
                listItem.add(nested);
                
			}else if(modelObject instanceof EventInfo){
				EventInfo ei = (EventInfo) modelObject;
                
				WebMarkupContainer row = new WebMarkupContainer("row");
				row.add(new ExternalImage("navCollapse", "images/line-last.gif"));
				row.add(new ExternalImage("navExpand", "images/nav-plus.gif").setVisible(false));
				row.add(new ExternalImage("image", "images/silk/icons/bullet_feed.png"));
				row.add(new Label("label", new Model(ei.getEventName())));
				CheckBox eventCheckBox = new CheckBox("checkbox", new PropertyModel(ei, "selected"));
				eventCheckBox.add(new AttributeModifier("onclick", true, new Model("updateToolSelection('.tool_"+currentToolId+"');")));
				row.add(eventCheckBox);
				listItem.add(row);
				
				WebMarkupContainer nested = new WebMarkupContainer("nested");
				nested.setVisible(false);
                listItem.add(nested);
                
			}else{
				WebMarkupContainer row = new WebMarkupContainer("row");
				row.setVisible(false);
				row.add(new ExternalImage("navCollapse", "images/line-last.gif"));
				row.add(new ExternalImage("navExpand", "images/nav-plus.gif").setVisible(false));
				row.add(new ExternalImage("image", "images/silk/icons/bullet_feed.png").setVisible(false));
				row.add(new Label("label").setVisible(false));
				row.add(new CheckBox("checkbox", new Model(Boolean.FALSE)).setVisible(false));
				listItem.add(row);
				
				WebMarkupContainer nested = new WebMarkupContainer("nested");
				nested.setVisible(false);
                listItem.add(nested);
			}
		}
	}
}
