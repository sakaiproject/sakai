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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.ui.console.pages.PackageConfigurationPage.AssessmentSetup;
import org.sakaiproject.scorm.ui.console.pages.PackageConfigurationPage.GradebookSetup;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class PackageRemovePage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(PackageRemovePage.class);

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	public PackageRemovePage( final PageParameters params )
	{
		// SCO-98 - disable buttons and add spinner on submit
		add( new FileRemoveForm( "removeForm", params ) );
	}

	/**
	 * SCO-98 - disable buttons and add spinner on submit
	 * 
	 * @author bjones86
	 */
	public class FileRemoveForm extends Form
	{
		private static final long serialVersionUID = 1L;

		@SpringBean(name = "org.sakaiproject.service.gradebook.GradebookExternalAssessmentService")
		GradebookExternalAssessmentService gradebookExternalAssessmentService;

		@SpringBean(name = "org.sakaiproject.scorm.dao.api.ContentPackageManifestDao")
		ContentPackageManifestDao contentPackageManifestDao;

		public FileRemoveForm( String id, final PageParameters params )
		{
			super( id );
			IModel model = new CompoundPropertyModel( this );
			this.setModel( model );

			String title = params.getString( "title" );
			final long contentPackageId = params.getLong( "contentPackageId" );

			ContentPackage contentPackage = new ContentPackage( title, contentPackageId );

			List<ContentPackage> list = new LinkedList<ContentPackage>();
			list.add( contentPackage );

			List<IColumn> columns = new LinkedList<IColumn>();
			columns.add( new PropertyColumn( new Model( "Content Package" ), "title", "title" ) );

			DataTable removeTable = new DataTable( "removeTable", columns.toArray( new IColumn[columns.size()] ), 
					new ListDataProvider( list ), 3 );

			final Label alertLabel = new Label( "alert", new ResourceModel( "verify.remove" ) );
			final CancelButton btnCancel = new CancelButton( "btnCancel", PackageListPage.class );
			IndicatingAjaxButton btnSubmit = new IndicatingAjaxButton( "btnSubmit", this )
			{
				private static final long serialVersionUID = 1L;
				
				@Override
				protected IAjaxCallDecorator getAjaxCallDecorator()
				{
					return new AjaxPostprocessingCallDecorator( super.getAjaxCallDecorator() )
					{
						private static final long serialVersionUID = 1L;
						
						@Override
						public CharSequence postDecorateScript( CharSequence script )
						{
							// Disable the submit and cancel buttons on click
							return script + "this.disabled = true; document.getElementsByName( \"btnCancel\" )[0].disabled = true;";
						}
					};
				}
				
				@Override
				protected void onSubmit( AjaxRequestTarget target, Form<?> form )
				{
					try
					{
						contentService.removeContentPackage( contentPackageId );

						ContentPackage contentPackage = contentService.getContentPackage( contentPackageId );
						GradebookSetup gradebookSetup = getAssessmentSetup( contentPackage );
						String context = getContext();
						if( gradebookSetup.isGradebookDefined() )
						{
							for( AssessmentSetup assessmentSetup : gradebookSetup.getAssessments() )
							{
								String assessmentExternalID = getAssessmentExternalId( gradebookSetup, assessmentSetup );
								boolean on = assessmentSetup.issynchronizeSCOWithGradebook();
								boolean has = gradebookExternalAssessmentService.isExternalAssignmentDefined( context, assessmentExternalID );
								if( has && on )
								{
									gradebookExternalAssessmentService.removeExternalAssessment( context, assessmentExternalID );
								}
							}
						}

						setResponsePage( PackageListPage.class );
					}
					catch( ResourceNotDeletedException rnde )
					{
						LOG.warn( "Failed to delete all underlying resources ", rnde );
						alertLabel.setDefaultModel( new ResourceModel( "exception.remove" ) );
						target.addComponent( alertLabel );
						setResponsePage( PackageRemovePage.class, params );
					}
				}
			};

			add( alertLabel );
			add( removeTable );
			add( btnCancel );
			add( btnSubmit );
		}

		private GradebookSetup getAssessmentSetup( ContentPackage contentPackage )
		{
			String context = getContext();
			final GradebookSetup gradebookSetup = new GradebookSetup();
			boolean isGradebookDefined = gradebookExternalAssessmentService.isGradebookDefined( context );
			gradebookSetup.setGradebookDefined( isGradebookDefined );
			gradebookSetup.setContentPackage( contentPackage );
			if( isGradebookDefined )
			{
				ContentPackageManifest contentPackageManifest = contentPackageManifestDao.load( contentPackage.getManifestId() );
				gradebookSetup.setContentPackageManifest( contentPackageManifest );
				List<AssessmentSetup> assessments = gradebookSetup.getAssessments();
				for( AssessmentSetup as : assessments )
				{
					String assessmentExternalId = getAssessmentExternalId( gradebookSetup, as );
					boolean has = gradebookExternalAssessmentService.isExternalAssignmentDefined( context, assessmentExternalId );
					as.setsynchronizeSCOWithGradebook( has );
				}
			}

			return gradebookSetup;
		}

		private String getContext()
		{
			Placement placement = toolManager.getCurrentPlacement();
			return placement.getContext();
		}

		private String getAssessmentExternalId(final GradebookSetup gradebook, AssessmentSetup assessment) {
			String assessmentExternalId = "" + gradebook.getContentPackageId() + ":" + assessment.getLaunchData().getItemIdentifier();
			return assessmentExternalId;
		}
	}
}
