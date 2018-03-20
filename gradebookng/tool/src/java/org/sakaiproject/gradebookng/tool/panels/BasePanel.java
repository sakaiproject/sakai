package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.pages.AccessDeniedPage;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel extension to abstract away some common functionality that many GBNG panels share.
 * Classes extending {@link BasePanel} do not need to inject the {@link GradebookNgBusinessService} as it is in here.
 */
public abstract class BasePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public BasePanel(final String id) {
		super(id);
	}

	public BasePanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	/**
	 * Helper to get the user role, via the business service. Handles the Access Denied scenario.
	 * @return
	 */
	public GbRole getUserRole() {

		GbRole role;
		try {
			role = this.businessService.getUserRole();
		} catch (final GbAccessDeniedException e) {
			final PageParameters params = new PageParameters();
			params.add("message", MessageHelper.getString("error.role"));
			throw new RestartResponseException(AccessDeniedPage.class, params);
		}
		return role;
	}

	/**
	 * Get the current user, via the business service
	 * @return
	 */
	public String getCurrentUserId() {
		return this.businessService.getCurrentUser().getId();
	}

	/**
	 * Get the Gradebook for the panel, via the business service
	 * @return
	 */
	public Gradebook getGradebook() {
		return this.businessService.getGradebook();
	}

}

