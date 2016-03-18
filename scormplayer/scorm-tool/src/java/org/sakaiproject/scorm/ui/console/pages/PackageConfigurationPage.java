/*
 * #%L
 * SCORM Tool
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sakaiproject.scorm.ui.console.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.adl.validator.contentpackage.LaunchData;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.wicket.markup.html.form.CancelButton;
import org.sakaiproject.wicket.model.DecoratedPropertyModel;
import org.sakaiproject.wicket.model.SimpleDateFormatPropertyModel;

public class PackageConfigurationPage extends ConsoleBasePage {

	static class AssessmentSetup implements Serializable{

		private static final long serialVersionUID = 1L;
		LaunchData launchData;
		Double numberOffPoints = 100d;
		boolean synchronizeSCOWithGradebook;

		public AssessmentSetup() {
			super();
		}

		public AssessmentSetup(LaunchData launchData) {
			super();
			this.launchData = launchData;
		}

		public String getItemIdentifier() {
			return launchData.getItemIdentifier();
		}
		public String getItemTitle() {
			return launchData.getItemTitle();
		}

		public LaunchData getLaunchData() {
			return launchData;
		}

		public Double getNumberOffPoints() {
			return numberOffPoints;
		}

		public boolean issynchronizeSCOWithGradebook() {
			return synchronizeSCOWithGradebook;
		}

		public void setLaunchData(LaunchData launchData) {
			this.launchData = launchData;
		}

		public void setNumberOffPoints(Double numberOffPoints) {
			this.numberOffPoints = numberOffPoints;
		}

		public void setsynchronizeSCOWithGradebook(boolean synchronizeSCOWithGradebook) {
			this.synchronizeSCOWithGradebook = synchronizeSCOWithGradebook;
		}
	}

	public class DisplayNamePropertyModel extends DecoratedPropertyModel implements Serializable{

		private static final long serialVersionUID = 1L;

		public DisplayNamePropertyModel(Object modelObject, String expression) {
			super(modelObject, expression);
		}

		@Override
		public Object convertObject(Object object) {
			String userId = String.valueOf(object);

			return lms.getLearnerName(userId);
		}
	}

	static class GradebookSetup implements Serializable{
		private static final long serialVersionUID = 1L;
		boolean isGradebookDefined;
		ContentPackage contentPackage;

		public ContentPackage getContentPackage() {
			return contentPackage;
		}

		public void setContentPackage(ContentPackage contentPackage) {
			this.contentPackage = contentPackage;
		}

		ContentPackageManifest contentPackageManifest;
		List<AssessmentSetup> assessments = new ArrayList<PackageConfigurationPage.AssessmentSetup>();

		public List<AssessmentSetup> getAssessments() {
			return assessments;
		}

		public String getContentPackageId() {
			return "" + contentPackage.getContentPackageId();
		}

		public ContentPackageManifest getContentPackageManifest() {
			return contentPackageManifest;
		}

		public boolean isGradebookDefined() {
			return isGradebookDefined;
		}

		public void setContentPackageManifest(ContentPackageManifest contentPackageManifest) {
			this.contentPackageManifest = contentPackageManifest;
			assessments.clear();
			@SuppressWarnings("unchecked")
			List<LaunchData> launchDatas = contentPackageManifest.getLaunchData();
			for (LaunchData launchData : launchDatas) {
				String scormType = launchData.getSCORMType();
				if ("sco".equalsIgnoreCase(scormType)) {
					AssessmentSetup assessment = buildAssessmentSetup(launchData);
					assessments.add(assessment);
				}
			}
		}

		protected AssessmentSetup buildAssessmentSetup(LaunchData launchData) {
			AssessmentSetup assessment = new AssessmentSetup(launchData);
			return assessment;
		}

		public void setGradebookDefined(boolean isGradebookDefined) {
			this.isGradebookDefined = isGradebookDefined;
		}
	}

	public class TryChoiceRenderer extends ChoiceRenderer implements Serializable{
		private static final long serialVersionUID = 1L;

		public TryChoiceRenderer() {
			super();
		}

		@Override
		public Object getDisplayValue(Object object) {
			Integer n = (Integer) object;

			if (n == -1)
			{
				return unlimitedMessage;
			}

			return object;
		}
	}

	private static final long serialVersionUID = 1L;
	private static ResourceReference PAGE_ICON = new ResourceReference(PackageConfigurationPage.class, "res/table_edit.png");
	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	@SpringBean(name = "org.sakaiproject.service.gradebook.GradebookExternalAssessmentService")
	GradebookExternalAssessmentService gradebookExternalAssessmentService;

	@SpringBean(name = "org.sakaiproject.scorm.dao.api.ContentPackageManifestDao")
	ContentPackageManifestDao contentPackageManifestDao;

	private String unlimitedMessage;

	public PackageConfigurationPage(PageParameters params) {
		super(params);
		long contentPackageId = params.getLong("contentPackageId");

		final ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		final GradebookSetup gradebookSetup = getAssessmentSetup(contentPackage);

		final Class pageSubmit;
		final Class pageCancel;

		// @NOTE this is a hack that allows us to change the destination we
		// are redirected to after form submission depending on where we come from
		// I'm sure there's a more reliable way to do this is Wicket but it's not trivial to figure it out.
		if(params.getBoolean("no-toolbar")) {
			pageSubmit = DisplayDesignatedPackage.class;
			pageCancel = DisplayDesignatedPackage.class;
		} else {
			pageSubmit = PackageListPage.class;
			pageCancel = PackageListPage.class;
		}

		Form form = new Form("configurationForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				if (gradebookSetup.isGradebookDefined()) {
					List<AssessmentSetup> assessments = gradebookSetup.getAssessments();
					for (AssessmentSetup assessmentSetup : assessments) {
						boolean on = assessmentSetup.issynchronizeSCOWithGradebook();
						String assessmentExternalId = getAssessmentExternalId(gradebookSetup, assessmentSetup);
						String context = getContext();
						boolean has = gradebookExternalAssessmentService.isExternalAssignmentDefined(context, assessmentExternalId);
						String fixedTitle = getItemTitle(assessmentSetup, context);
						if (has && on) { 
							gradebookExternalAssessmentService.updateExternalAssessment(context, assessmentExternalId, null, fixedTitle, assessmentSetup.numberOffPoints, gradebookSetup.getContentPackage().getDueOn()); 
							} else if (!has && on) { 
								gradebookExternalAssessmentService.addExternalAssessment(context, assessmentExternalId, null, fixedTitle, assessmentSetup.numberOffPoints, gradebookSetup.getContentPackage().getDueOn(), "SCORM player"); 
							} else if (has && !on) { 
								gradebookExternalAssessmentService.removeExternalAssessment(context, assessmentExternalId);
							}
						
					}
				}
				contentService.updateContentPackage(contentPackage);
				setResponsePage(pageSubmit);
			}

			protected String getItemTitle(AssessmentSetup assessmentSetup, String context) {
				String fixedTitle = assessmentSetup.getItemTitle();
				int count = 1;
				while (gradebookExternalAssessmentService.isAssignmentDefined(context, fixedTitle)) {
					fixedTitle = assessmentSetup.getItemTitle() + " (" + count++ + ")";
				}
				return fixedTitle;
			}
		};

		List<Integer> tryList = new LinkedList<Integer>();

		tryList.add(-1);
		for (int i = 1; i <= 10; i++) {
			tryList.add(i);
		}

		this.unlimitedMessage = getLocalizer().getString("unlimited", this);

		TextField nameField = new TextField("packageName", new PropertyModel(contentPackage, "title"));
		nameField.setRequired(true);
		form.add(nameField);
		DateTimeField releaseOnDTF = new DateTimeField("releaseOnDTF", new PropertyModel(contentPackage, "releaseOn"));
		releaseOnDTF.setRequired(true);

		form.add(releaseOnDTF);
		form.add(new DateTimeField("dueOnDTF", new PropertyModel(contentPackage, "dueOn")));
		form.add(new DateTimeField("acceptUntilDTF", new PropertyModel(contentPackage, "acceptUntil")));
		form.add(new DropDownChoice("numberOfTries", new PropertyModel(contentPackage, "numberOfTries"), tryList, new TryChoiceRenderer()));
		form.add(new Label("createdBy", new DisplayNamePropertyModel(contentPackage, "createdBy")));
		form.add(new Label("createdOn", new SimpleDateFormatPropertyModel(contentPackage, "createdOn")));
		form.add(new Label("modifiedBy", new DisplayNamePropertyModel(contentPackage, "modifiedBy")));
		form.add(new Label("modifiedOn", new SimpleDateFormatPropertyModel(contentPackage, "modifiedOn")));

		ListView scos;
		form.add(scos = new ListView("scos", gradebookSetup.getAssessments()) {

			private static final long serialVersionUID = 965550162166385688L;

			@Override
			protected void populateItem(final ListItem item) {
				Label label = new Label("itemTitle", new PropertyModel(item.getModelObject(), "itemTitle"));
				item.add(label);
				final WebMarkupContainer verifySyncWithGradebook = new WebMarkupContainer( "verifySyncWithGradebook" );
				verifySyncWithGradebook.setOutputMarkupId( true );
				verifySyncWithGradebook.setOutputMarkupPlaceholderTag( true );
				verifySyncWithGradebook.setVisible( false );
				item.add( verifySyncWithGradebook );

				AjaxCheckBox synchronizeSCOWithGradebook = new AjaxCheckBox("synchronizeSCOWithGradebook", new PropertyModel(item.getModelObject(), 
						"synchronizeSCOWithGradebook") )
				{
					@Override
					protected void onUpdate( AjaxRequestTarget target )
					{
						AssessmentSetup as = (AssessmentSetup) item.getModelObject();
						String assessmentExternalId = getAssessmentExternalId( gradebookSetup, as );
						boolean hasGradebookSync = gradebookExternalAssessmentService.isExternalAssignmentDefined( getContext(), assessmentExternalId );
						boolean isChecked = this.getModelObject();
						verifySyncWithGradebook.setVisible( hasGradebookSync && !isChecked );
						target.addComponent( verifySyncWithGradebook );
					}
				};

				item.add(synchronizeSCOWithGradebook);
			}
		});
		scos.setVisible(gradebookSetup.isGradebookDefined() && !gradebookSetup.getAssessments().isEmpty());

		form.add(new CancelButton("cancel", pageCancel));
		add(form);
	}

	private GradebookSetup getAssessmentSetup(ContentPackage contentPackage) {
		final GradebookSetup gradebookSetup = new GradebookSetup();
		String context = getContext();
		boolean isGradebookDefined = gradebookExternalAssessmentService.isGradebookDefined(context);
		gradebookSetup.setGradebookDefined(isGradebookDefined);
		gradebookSetup.setContentPackage(contentPackage);
		if (isGradebookDefined) {
			ContentPackageManifest contentPackageManifest = contentPackageManifestDao.load(contentPackage.getManifestId());
			gradebookSetup.setContentPackageManifest(contentPackageManifest);
			List<AssessmentSetup> assessments = gradebookSetup.getAssessments();
			for (AssessmentSetup as : assessments) {
				String assessmentExternalId = getAssessmentExternalId(gradebookSetup, as);
				boolean has = gradebookExternalAssessmentService.isExternalAssignmentDefined(getContext(), assessmentExternalId);
				as.setsynchronizeSCOWithGradebook(has);
			}
		}
		return gradebookSetup;
	}

	protected String getContext() {
		Placement placement = ToolManager.getCurrentPlacement();
		String context = placement.getContext();
		return context;
	}

	@Override
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}

	private static String getAssessmentExternalId(final GradebookSetup gradebook, AssessmentSetup assessment) {
		String assessmentExternalId = "" + gradebook.getContentPackageId() + ":" + assessment.getLaunchData().getItemIdentifier();
		return assessmentExternalId;
	}
}
