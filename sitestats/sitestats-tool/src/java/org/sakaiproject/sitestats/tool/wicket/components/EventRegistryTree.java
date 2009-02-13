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

	private WebMarkupContainer	ul					= null;
	private Rows				rows 				= null;

	public EventRegistryTree(String id, List<?> eventRegistry) {
		this(id, eventRegistry, null);
	}
	
	public EventRegistryTree(String id, List<?> eventRegistry, String toolId) {
		super(id);
		ul = new WebMarkupContainer("ul");
		if(toolId != null) {
			ul.add(new AttributeModifier("style", true, new Model("padding: 0 0 0 20px; display: none;")));
			ul.add(new AttributeModifier("class", true, new Model("events")));
		}else{
			ul.add(new AttributeModifier("style", true, new Model("padding: 0px;")));
			ul.add(new AttributeModifier("class", true, new Model("tools")));
		}
		add(ul);
		rows = new Rows("row", eventRegistry, toolId);
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

	private class Rows extends ListView {
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
                
				listItem.setOutputMarkupId(true);
				listItem.setMarkupId(toolId);
				listItem.add(new AttributeModifier("class", true, new Model("tool")));
				
				// nested list: events
				EventRegistryTree nested = new EventRegistryTree("nested", ti.getEvents(), toolId);
				nested.add(new AttributeModifier("class", true, new Model(toolId)));
				nested.setOutputMarkupId(true);
				nested.setRenderBodyOnly(true);
				listItem.add(nested);
				
				// navigating images
				ExternalImage navCollapse = new ExternalImage("navCollapse", "images/nav-minus.gif");
				listItem.add(navCollapse);
				ExternalImage navExpand = new ExternalImage("navExpand", "images/nav-plus.gif");
				listItem.add(navExpand);
				navCollapse.add(new AttributeModifier("onclick", true, new Model("jQuery(this).parent().find('.events').hide(); jQuery('#"+navExpand.getMarkupId()+"').show(); setMainFrameHeightNoScroll( window.name ); jQuery(this).hide(); return false;")));
				navExpand.add(new AttributeModifier("onclick", true, new Model("jQuery(this).parent().find('.events').show(); jQuery('#"+navCollapse.getMarkupId()+"').show(); setMainFrameHeightNoScroll( window.name ); jQuery(this).hide(); return false;")));
				navCollapse.add(new AttributeModifier("style", true, new Model("display: none")));
								
				// image, label, checkbox
				listItem.add(new ExternalImage("image", "images/silk/icons/application_side_boxes.png"));
				listItem.add(new Label("label", new Model(ti.getToolName())));
				CheckBox toolCheckBox = new CheckBox("checkbox", new PropertyModel(ti, "selected"));
				AttributeModifier onclick = new AttributeModifier("onclick", true, new Model("selectUnselectEvents(this); updateToolSelection('#"+toolId+"');"));
				toolCheckBox.add(onclick);
				listItem.add(toolCheckBox);
                
                if(ti.getEvents() == null || ti.getEvents().isEmpty()) {
                	navCollapse.setVisible(false);
                	navExpand.setVisible(false);
                	nested.setVisible(false);
                	toolCheckBox.remove(onclick);
                }
                
			}else if(modelObject instanceof EventInfo){
				EventInfo ei = (EventInfo) modelObject;
                
				listItem.add(new ExternalImage("navCollapse", "images/line-last.gif"));
				listItem.add(new ExternalImage("navExpand", "images/nav-plus.gif").setVisible(false));
				listItem.add(new ExternalImage("image", "images/silk/icons/bullet_feed.png"));
				listItem.add(new Label("label", new Model(ei.getEventName())));
				CheckBox eventCheckBox = new CheckBox("checkbox", new PropertyModel(ei, "selected"));
				eventCheckBox.add(new AttributeModifier("onclick", true, new Model("updateToolSelection('#"+currentToolId+"');")));
				listItem.add(eventCheckBox);
				
				WebMarkupContainer nested = new WebMarkupContainer("nested");
				nested.setVisible(false);
				listItem.add(nested);
                
			}else{
				listItem.setVisible(false);
				listItem.add(new ExternalImage("navCollapse", "images/line-last.gif"));
				listItem.add(new ExternalImage("navExpand", "images/nav-plus.gif").setVisible(false));
				listItem.add(new ExternalImage("image", "images/silk/icons/bullet_feed.png").setVisible(false));
				listItem.add(new Label("label").setVisible(false));
				listItem.add(new CheckBox("checkbox", new Model(Boolean.FALSE)).setVisible(false));
				
				WebMarkupContainer nested = new WebMarkupContainer("nested");
				nested.setVisible(false);
				listItem.add(nested);
			}
		}
	}
}
