/**
 * Copyright (c) 2007 The Apereo Foundation
 *
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
 */
package org.sakaiproject.scorm.ui.console.pages;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import org.sakaiproject.wicket.ajax.markup.html.form.SakaiAjaxButton;
import org.sakaiproject.wicket.ajax.markup.html.form.SakaiAjaxCancelButton;

@Slf4j
public class PackageRemovePage extends ConsoleBasePage
{
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	public PackageRemovePage( final PageParameters params )
	{
		add( new FileRemoveForm( "removeForm", params ) );
	}

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

			String title = params.get( "title" ).toString();
			final long contentPackageId = params.get( "contentPackageId" ).toLong();

			ContentPackage contentPackage = new ContentPackage( title, contentPackageId );
			List<ContentPackage> list = new LinkedList<>();
			list.add( contentPackage );

			List<IColumn> columns = new LinkedList<>();
			columns.add( new PropertyColumn( new Model( "Content Package" ), "title", "title" ) );

			DataTable removeTable = new DataTable( "removeTable", columns, new ListDataProvider( list ), 3 );

			final Label alertLabel = new Label( "alert", new ResourceModel( "verify.remove" ) );
			final SakaiAjaxCancelButton btnCancel = new SakaiAjaxCancelButton( "btnCancel", PackageListPage.class );
			btnCancel.setElementsToDisableOnClick( Arrays.asList( new String[] {"btnRemove"} ) );
			SakaiAjaxButton btnRemove = new SakaiAjaxButton( "btnRemove", this )
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit( AjaxRequestTarget target )
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
								boolean on = assessmentSetup.isSynchronizeSCOWithGradebook();
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
						log.warn( "Failed to delete all underlying resources ", rnde );
						alertLabel.setDefaultModel( new ResourceModel( "exception.remove" ) );
						target.add( alertLabel );
						setResponsePage( PackageRemovePage.class, params );
					}
				}
			};
			btnRemove.setElementsToDisableOnClick( Arrays.asList( new String[] {"btnCancel"} ) );

			add( alertLabel );
			add( removeTable );
			add( btnCancel );
			add( btnRemove );
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
					as.setSynchronizeSCOWithGradebook( has );
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
