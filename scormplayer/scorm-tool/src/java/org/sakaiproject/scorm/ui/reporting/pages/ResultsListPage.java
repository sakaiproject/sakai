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
package org.sakaiproject.scorm.ui.reporting.pages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.file.Files;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.LearnerExperience;
import org.sakaiproject.scorm.model.api.Objective;
import org.sakaiproject.scorm.model.api.comparator.LearnerExperienceComparator;
import org.sakaiproject.scorm.model.api.comparator.LearnerExperienceComparator.CompType;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.NameValuePair;
import org.sakaiproject.scorm.ui.console.components.AccessStatusColumn;
import org.sakaiproject.scorm.ui.console.components.AttemptNumberAction;
import org.sakaiproject.scorm.ui.console.components.ContentPackageDetailPanel;
import org.sakaiproject.scorm.ui.console.components.DecoratedDatePropertyColumn;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;
import org.sakaiproject.wicket.markup.html.repeater.data.table.ActionColumn;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

@Slf4j
public class ResultsListPage extends ConsoleBasePage
{
	private static final long serialVersionUID = 1L;

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResultService")
	ScormResultService resultService;

	private static final String NEW_LINE			= "\n";
	private static final String DOUBLE_QUOTE		= "\"";
	private static final String CSV_DELIMITER		= ",";
	private static final String EMPTY_CELL			= DOUBLE_QUOTE + DOUBLE_QUOTE + CSV_DELIMITER;
	private static final String SUMMARY_INDENT		= EMPTY_CELL + EMPTY_CELL + EMPTY_CELL + EMPTY_CELL;
	private static final String INTERACTION_INDENT	= SUMMARY_INDENT + SUMMARY_INDENT;
	private static final String OBJECTIVE_INDENT	= INTERACTION_INDENT + INTERACTION_INDENT + EMPTY_CELL;
	private static		 String CSV_HEADERS;

	public ResultsListPage(PageParameters pageParams)
	{
		super(pageParams);

		final long contentPackageId = pageParams.get("contentPackageId").toLong();
		final ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);

		// SCO-94 - deny users who do not have scorm.view.results permission
		String context = lms.currentContext();
		boolean canViewResults = lms.canViewResults( context );
		Label heading = new Label( "heading", new ResourceModel( "page.heading.notAllowed" ) );
		add( heading );

		final AttemptDataProvider dataProvider = new AttemptDataProvider(contentPackageId);
		dataProvider.setFilterConfigurerVisible(true);
		dataProvider.setTableTitle(getLocalizer().getString("table.title", this));

		// SCO-127
		buildExportHeaders();
		IModel<File> fileModel = new IModel<File>()
		{
			@Override
			public File getObject()
			{
				File tempFile = null;
				try
				{
					tempFile = File.createTempFile( contentPackage.getTitle() + "_results", ".csv" );
					InputStream data = new ByteArrayInputStream( generateExportCSV( dataProvider ).getBytes() );
					Files.writeTo( tempFile, data );
				}
				catch( IOException ex )
				{
					log.error( "Could not generate results export: {}", ex );
				}

				return tempFile;
			}
		};
		DownloadLink btnExport = new DownloadLink( "btnExport", fileModel, contentPackage.getTitle() + "_results.csv" );
		btnExport.setDeleteAfterDownload( true );
		add( btnExport );

		if( !canViewResults )
		{
			btnExport.setVisibilityAllowed( false );
			heading.setVisibilityAllowed( true );
			add( new WebMarkupContainer( "attemptPresenter" ) );
			add( new WebMarkupContainer( "details" ) );
		}
		else
		{
			// SCO-94
			heading.setVisibilityAllowed( false );

			addBreadcrumb(new Model(contentPackage.getTitle()), ResultsListPage.class, new PageParameters(), false);

			SakaiDataTable table = new SakaiDataTable("resultsTable", getColumns(), dataProvider, true);
			add(table);

			add(new ContentPackageDetailPanel("details", contentPackage));
		}
	}

	/**
	 * Instantiate the header row for the export, if it hasn't already been instantiated
	 */
	private void buildExportHeaders()
	{
		if( StringUtils.isBlank( CSV_HEADERS ) )
		{
			CSV_HEADERS = DOUBLE_QUOTE + getLocalizer().getString( "export.headers.learner", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.lastAttempt", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.status", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.attempts", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.title", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.score", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.completionStatus", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.successStatus", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.identifier", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.type", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.weighting", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.latency", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.time", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.result", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.description", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.correctResponse", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.learnerResponse", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.objectiveIdentifier", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.objectiveDescription", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.objectiveCompletionStatus", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.objectiveSuccessStatus", this ) + DOUBLE_QUOTE + CSV_DELIMITER + 
						DOUBLE_QUOTE + getLocalizer().getString( "export.headers.objectiveScore", this ) + DOUBLE_QUOTE + NEW_LINE;
		}
	}

	/**
	 * Export all results for the current module to a CSV file.
	 * 
	 * @param dataProvider used to respect current sort order of the UI
	 * @return string representation of the CSV file
	 */
	private String generateExportCSV( AttemptDataProvider attemptProvider )
	{
		// Create the column headers
		StringBuilder csv = new StringBuilder();
		csv.append( CSV_HEADERS );

		Iterator<LearnerExperience> itr = attemptProvider.iterator( 0, attemptProvider.size() );
		while( itr.hasNext() )
		{
			// Learner info
			LearnerExperience learner = itr.next();
			csv.append( DOUBLE_QUOTE ).append( learner.getLearnerName() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
			csv.append( DOUBLE_QUOTE ).append( Objects.toString( learner.getLastAttemptDate(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
			csv.append( DOUBLE_QUOTE ).append( getStatusLabel( learner.getStatus() ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
			csv.append( DOUBLE_QUOTE ).append( learner.getNumberOfAttempts() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );

			// Get the summaries for all attempts for the current user
			List<ActivitySummary> summaries = new ArrayList<>();
			for( int i = 1; i <= learner.getNumberOfAttempts(); i++ )
			{
				summaries.addAll( resultService.getActivitySummaries( learner.getContentPackageId(), learner.getLearnerId(), i ) );
			}

			if( summaries.isEmpty() )
			{
				csv.append( NEW_LINE );
			}

			for( int i = 0; i < summaries.size(); i++ )
			{
				if( i != 0 )
				{
					csv.append( SUMMARY_INDENT );
				}

				// Summary info
				ActivitySummary summary = summaries.get( i );
				csv.append( DOUBLE_QUOTE ).append( summary.getTitle() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
				csv.append( DOUBLE_QUOTE ).append( summary.getScaled() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
				csv.append( DOUBLE_QUOTE ).append( Objects.toString( summary.getCompletionStatus(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
				csv.append( DOUBLE_QUOTE ).append( Objects.toString( summary.getSuccessStatus(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );

				// Get the interactions
				ActivityReport report = resultService.getActivityReport( summary.getContentPackageId(), summary.getLearnerId(), summary.getAttemptNumber(), summary.getScoId() );
				if( report != null )
				{
					List<Interaction> interactions = report.getInteractions();
					if( interactions.isEmpty() )
					{
						csv.append( NEW_LINE );
					}

					for( int j = 0; j < interactions.size(); j++ )
					{
						if( j != 0 )
						{
							csv.append( INTERACTION_INDENT );
						}

						// Interaction info
						Interaction interaction = interactions.get( j );
						StringBuilder correctResponses = new StringBuilder();
						for( String correctResponse : interaction.getCorrectResponses() )
						{
							if( StringUtils.isNotEmpty( correctResponse ) )
							{
								correctResponses.append( correctResponse );
							}
						}
						csv.append( DOUBLE_QUOTE ).append( interaction.getInteractionId() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getType() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getWeighting() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getLatency() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getTimestamp() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getResult() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getDescription() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( correctResponses.toString() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
						csv.append( DOUBLE_QUOTE ).append( interaction.getLearnerResponse() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );

						// Objective info
						List<Objective> objectives = interaction.getObjectives();
						for( int k = 0; k < objectives.size(); k++ )
						{
							if( k != 0 )
							{
								csv.append( OBJECTIVE_INDENT );
							}

							Objective objective = objectives.get( k );
							csv.append( DOUBLE_QUOTE ).append( objective.getId() ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
							csv.append( DOUBLE_QUOTE ).append( Objects.toString( objective.getDescription(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
							csv.append( DOUBLE_QUOTE ).append( Objects.toString( objective.getCompletionStatus(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
							csv.append( DOUBLE_QUOTE ).append( Objects.toString( objective.getSuccessStatus(), "" ) ).append( DOUBLE_QUOTE ).append( CSV_DELIMITER );
							csv.append( DOUBLE_QUOTE ).append( Objects.toString( objective.getScore().getScaled(), "" ) ).append( DOUBLE_QUOTE ).append( NEW_LINE );
						}

						if( objectives.isEmpty() )
						{
							csv.append( NEW_LINE );
						}
					}
				}
				else
				{
					csv.append( NEW_LINE );
				}
			}
		}

		return csv.toString();
	}

	/**
	 * Determine the proper status label for the give status integer
	 * @param status status integer
	 * @return the String status label corresponding to the status integer given
	 */
	private String getStatusLabel( int status )
	{
		switch( status )
		{
			case NOT_ACCESSED:
			{
				return getLocalizer().getString( "access.status.not.accessed", this );
			}
			case INCOMPLETE:
			{
				return getLocalizer().getString( "access.status.incomplete", this );
			}
			case COMPLETED:
			{
				return getLocalizer().getString( "access.status.completed", this );
			}
			case GRADED:
			{
				return getLocalizer().getString( "access.status.graded", this );
			}
		}

		return "";
	}

	private List<IColumn> getColumns()
	{
		IModel learnerNameHeader = new ResourceModel("column.header.learner.name");
		IModel attemptedHeader = new ResourceModel("column.header.attempted");
		IModel statusHeader = new ResourceModel("column.header.status");
		IModel numberOfAttemptsHeader = new ResourceModel("column.header.attempt.number");

		List<IColumn> columns = new ArrayList<>(4);

		ActionColumn actionColumn = new ActionColumn(learnerNameHeader, "learnerName", "learnerName");

		String[] paramPropertyExpressions = {"contentPackageId", "learnerId"};

		Action summaryAction = new Action("learnerName", LearnerResultsPage.class, paramPropertyExpressions);
		actionColumn.addAction(summaryAction);
		columns.add(actionColumn);

		columns.add(new DecoratedDatePropertyColumn(attemptedHeader, "lastAttemptDate", "lastAttemptDate"));
		columns.add(new AccessStatusColumn(statusHeader, "status"));

		ActionColumn attemptNumberActionColumn = new ActionColumn(numberOfAttemptsHeader, "numberOfAttempts", "numberOfAttempts");
		attemptNumberActionColumn.addAction(new AttemptNumberAction("numberOfAttempts", LearnerResultsPage.class, paramPropertyExpressions));
		columns.add(attemptNumberActionColumn);

		return columns;
	}

	public class AttemptDataProvider extends EnhancedDataProvider
	{
		private static final long serialVersionUID = 1L;
		private final List<LearnerExperience> learnerExperiences;
		private final LearnerExperienceComparator comp = new LearnerExperienceComparator();

		public AttemptDataProvider(long contentPackageId)
		{
			this.learnerExperiences = resultService.getLearnerExperiences(contentPackageId);
			setSort( "learnerName", SortOrder.ASCENDING );
		}

		@Override
		public Iterator<LearnerExperience> iterator(long first, long count)
		{
			// Get the sort type
			SortParam sort = getSort();
			String sortProp = (String) sort.getProperty();
			boolean sortAsc = sort.isAscending();

			// Set the sort type in the comparator
			if( StringUtils.equals( sortProp, "lastAttemptDate" ) )
			{
				comp.setCompType( CompType.AttemptDate );
			}
			else if( StringUtils.equals( sortProp, "status" ) )
			{
				comp.setCompType( CompType.Status );
			}
			else if( StringUtils.equals( sortProp, "numberOfAttempts" ) )
			{
				comp.setCompType( CompType.NumberOfAttempts );
			}
			else
			{
				comp.setCompType( CompType.Learner );
			}

			// Sort using the comparator in the direction requested
			if( sortAsc )
			{
				Collections.sort( learnerExperiences, comp );
			}
			else
			{
				Collections.sort( learnerExperiences, Collections.reverseOrder( comp ) );
			}

			// Return sub list of sorted collection
			return learnerExperiences.subList((int) first, (int) first + (int) count).iterator();
		}

		@Override
		public long size()
		{
			return learnerExperiences.size();
		}

		@Override
		public List<NameValuePair> getFilterList()
		{
			List<NameValuePair> list = new LinkedList<>();
			list.add(new NameValuePair(getString( "filter.all" ), "ALL_GROUPS"));
			return list;
		}
	}
}
