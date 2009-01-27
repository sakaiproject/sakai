package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

//http://cwiki.apache.org/confluence/display/WICKET/Request+Focus+on+a+Specific+Form+Component
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


/* OR
public class FocusOnLoadBehavior extends AbstractBehavior
{
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
		component.setComponentBorder(new IComponentBorder() {
			public void renderBefore(Component component) {
			}

			public void renderAfter(Component component) {
				final Response response = component.getResponse();
				response.write(
						"<script type=\"text/javascript\" language=\"javascript\">document.getElementById(\"" +
						component.getMarkupId() +
						"\").focus()</script>");
			}
		});
	}

	@Override
	public boolean isTemporary() {
		return true;
	}
}
*/