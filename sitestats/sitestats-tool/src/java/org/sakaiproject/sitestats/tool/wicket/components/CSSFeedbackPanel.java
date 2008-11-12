package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class CSSFeedbackPanel extends FeedbackPanel {
	private static final long	serialVersionUID	= 1L;

	public CSSFeedbackPanel(String id) {
		super(id);
		WebMarkupContainer feedbackul = (WebMarkupContainer) get("feedbackul");
		if(feedbackul != null){
			feedbackul.add(new AttributeModifier("class", true, new Model() {
				private static final long	serialVersionUID	= 1L;
				public Object getObject() {
					if(anyErrorMessage()){
						return "alertMessage";
					}else if(anyMessage()){
						return "success";
					}else{
						return "";
					}
				}
			}));
			feedbackul.add(new AttributeModifier("style", true, new Model("list-style-type:none")));
		}
	}
}
