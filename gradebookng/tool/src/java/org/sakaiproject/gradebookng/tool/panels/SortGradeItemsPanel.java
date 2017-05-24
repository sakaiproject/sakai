package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortGradeItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final ModalWindow window;

	public SortGradeItemsPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final List tabs = new ArrayList();

		final Map<String, Object> model = (Map<String, Object>)getDefaultModelObject();
		final boolean categoriesEnabled = (boolean) model.get("categoriesEnabled");

		final Form<Void> form = new Form<>("form");

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
				String[] ids = request.getParameterValues("id");

				Map<Long, Integer> updates = new HashMap<>();
				boolean error = false;

				for (int i=0; i<ids.length; i++) {
					String id = ids[i];
					String order = request.getParameter(String.format("item_%s[order]", id));
					String current = request.getParameter(String.format("item_%s[current_order]", id));

					if (current != order) {
						updates.put(Long.valueOf(id), Integer.valueOf(order));
					}
				}

				String orderToUpdate = request.getParameter("orderby");
				boolean byCategory = "category".equals(orderToUpdate);
				boolean byItem = "item".equals(orderToUpdate);
				try {
					for (Long assignmentId : updates.keySet()) {
						Integer order = updates.get(assignmentId);

						if (byCategory) {
							businessService.updateAssignmentCategorizedOrder(assignmentId, order);
						} else if (byItem) {
							businessService.updateAssignmentOrder(assignmentId, order);
						}
					}
				} catch (IdUnusedException e) {
					System.err.println(e);
					e.printStackTrace();
					error = true;
				} catch (PermissionException e) {
					System.err.println(e);
					e.printStackTrace();
					error = true;
				}

				if (error) {
					getSession().error(getString("sortgradeitems.error"));
				} else if (byCategory){
					getSession().info(getString("sortgradeitems.success.bycategory"));
				} else if (byItem) {
					getSession().info(getString("sortgradeitems.success.byitem"));
				}

				setResponsePage(getPage().getPageClass());
			}
		};

		if (categoriesEnabled) {
			tabs.add(new AbstractTab(new Model<String>(getString("sortgradeitems.bycategory"))) {
				@Override
				public Panel getPanel(final String panelId) {
					return new SortGradeItemsByCategoryPanel(panelId, (IModel<Map<String, Object>>)getDefaultModel());
				}
			});

			submit.add(new Label("label", getString("sortgradeitems.submitbycategory")));
		} else {
			submit.add(new Label("label", getString("sortgradeitems.submit")));
		}
		tabs.add(new AbstractTab(new Model<String>(getString("sortgradeitems.bygradeitem"))) {
			@Override
			public Panel getPanel(final String panelId) {
				return new SortGradeItemsByGradeItemPanel(panelId);
			}
		});

		form.add(new AjaxBootstrapTabbedPanel("tabs", tabs) {
			@Override
			protected String getTabContainerCssClass() {
				return "nav nav-tabs";
			}

			@Override
			protected void onAjaxUpdate(final AjaxRequestTarget target) {
				// ensure the submit button reflects the currently selected tab
				// as form will only submit the status on that tab (not both!)
				if (getSelectedTab() == 1) {
					submit.replace(new Label("label", getString("sortgradeitems.submit")));
					target.add(submit);
				} else if (getTabs().size() > 1) {
					submit.replace(new Label("label", getString("sortgradeitems.submitbycategory")));
					target.add(submit);
				}
				super.onAjaxUpdate(target);
			}
		});

		form.add(submit);
		add(form);
	}
}