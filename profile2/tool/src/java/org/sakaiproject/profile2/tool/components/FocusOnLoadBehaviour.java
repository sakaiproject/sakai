package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

/*
 * FocusOnLoadBehaviour. 
 * A component you add to another component to make it focused when it is rendered.
 * 
 * use:
 * 
 * component.add(new FocusOnLoadBehaviour);
 * 
 * Steve Swinsburg
 * steve.swinsburg@gmail.com
 * 
 * ref: http://cwiki.apache.org/confluence/display/WICKET/Request+Focus+on+a+Specific+Form+Component
 * 
 */

public class FocusOnLoadBehaviour extends AbstractBehavior {
    
	private static final long serialVersionUID = 1L;
	
	private Component component;

    public void bind( Component component )
    {
        this.component = component;
        component.setOutputMarkupId(true);
    }

    public void renderHead( IHeaderResponse iHeaderResponse )
    {
        super.renderHead(iHeaderResponse);
        iHeaderResponse.renderOnLoadJavascript("document.getElementById('" + component.getMarkupId() + "').focus()");
    }

    public boolean isTemporary()
    {
        // remove the behavior after component has been rendered       
        return true;
    }
}

