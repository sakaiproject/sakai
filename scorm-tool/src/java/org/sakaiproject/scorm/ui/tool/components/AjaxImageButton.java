package org.sakaiproject.scorm.ui.tool.components;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;

public abstract class AjaxImageButton extends AjaxButton implements IHeaderContributor {
	private static final ResourceReference INACTIVE_BUTTON_IMAGE = new CompressedResourceReference(AjaxImageButton.class, "res/blankButton.gif");
	private static final ResourceReference ACTIVE_BUTTON_IMAGE = new CompressedResourceReference(AjaxImageButton.class, "res/activeButton.gif");
	private static final ResourceReference DISABLED_BUTTON_IMAGE = new CompressedResourceReference(AjaxImageButton.class, "res/disabledButton.gif");

	private static final long serialVersionUID = 1L;
	private boolean isSyncd = true;
	
	public AjaxImageButton(String id) {
		super(id);
	}

	public AjaxImageButton(String id, Form form) {
		super(id, form);
	}

	protected abstract ResourceReference getDisabledBtn();
	
	protected abstract ResourceReference getActiveBtn();
	
	protected abstract ResourceReference getInactiveBtn();
	
	protected String getDisabledSrc() {
		CharSequence url = RequestCycle.get().urlFor(getDisabledBtn());
		return(url.toString());
	}
	
	protected String getInactiveSrc() {
		CharSequence url = RequestCycle.get().urlFor(getInactiveBtn());
		return(url.toString());
	}
	
	protected String getActiveSrc() {
		CharSequence url = RequestCycle.get().urlFor(getActiveBtn());
		return(url.toString());
	}
	
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		String name = tag.getName();

		try
		{
			String value = getModelObjectAsString();
			if (value != null && !"".equals(value))
			{
				tag.put("value", value);
			}
		}
		catch (Exception e)
		{
			// ignore.
		}

		// If the subclass specified javascript, use that
		final String onClickJavaScript = getOnClickScript();
		if (onClickJavaScript != null)
		{
			tag.put("onclick", onClickJavaScript);
		}
		
		if (isEnabled()) {
			tag.put("src", getInactiveSrc());
			tag.put("onmouseover", "activateBtn('" + this.getMarkupId() + "');");
			tag.put("onmouseout", "inactivateBtn('" + this.getMarkupId() + "');");
		} else
			tag.put("src", getDisabledSrc());
	}
	
	
	public void renderHead(IHeaderResponse response) {
		StringBuilder activeSrcListDeclaration = new StringBuilder();
		activeSrcListDeclaration.append("activeSrcList['").append(getMarkupId()).append("']"); 
		StringBuilder inactiveSrcListDeclaration = new StringBuilder();
		inactiveSrcListDeclaration.append("inactiveSrcList['").append(getMarkupId()).append("']"); 
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(activeSrcListDeclaration).append(" = new Image();\n")
			.append(activeSrcListDeclaration).append(".src = '")
			.append(getActiveSrc()).append("';\n")
			.append(inactiveSrcListDeclaration).append(" = new Image();\n")
			.append(inactiveSrcListDeclaration).append(".src = '")
			.append(getInactiveSrc()).append("';\n");
		
		response.renderJavascript(builder.toString(), getId() + "Script");
	}

	public boolean isSyncd() {
		return isSyncd;
	}

	public void setSyncd(boolean isSyncd) {
		this.isSyncd = isSyncd;
	}

}
