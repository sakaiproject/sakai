package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;


/**
 * @author Nuno Fernandes
 */
public abstract class AjaxLazyLoadFragment extends Panel {
	private static final long				serialVersionUID	= 1L;
	private MyAbstractDefaultAjaxBehavior	behavior			= null;
	private Component						loadingComponent	= null;
	private boolean							doneAjaxUpdate		= false;

	/**
	 * @param id
	 */
	public AjaxLazyLoadFragment(String id) {
		super(id);
		setOutputMarkupId(true);
		loadingComponent = getLoadingComponent("content");
		add(loadingComponent.setRenderBodyOnly(true));

		behavior = new MyAbstractDefaultAjaxBehavior(Duration.ONE_SECOND);
		add(behavior);
	}

	/**
	 * @param markupId The frgment markupid.
	 * @return The fragment that must be lazy created.
	 */
	public abstract Fragment getLazyLoadFragment(String markupId);

	/**
	 * @param markupId The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId) {
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", true, new Model("...")));
		return indicator;
	}

	@SuppressWarnings("serial")
	class MyAbstractDefaultAjaxBehavior extends AbstractAjaxTimerBehavior {
		
		public MyAbstractDefaultAjaxBehavior(Duration updateInterval) {
			super(updateInterval);
		}

		@Override
		protected void onTimer(AjaxRequestTarget target) {
			if(!doneAjaxUpdate){
				Fragment fragment = getLazyLoadFragment("content");
				AjaxLazyLoadFragment.this.replace(fragment.setRenderBodyOnly(true));
				target.addComponent(AjaxLazyLoadFragment.this);
				doneAjaxUpdate = true;
				stop();
			}
		}

		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			response.renderOnDomReadyJavascript(getCallbackScript(false).toString());
		}

		public boolean isEnabled(Component component) {
			return get("content") == loadingComponent;
		}
	}
}
