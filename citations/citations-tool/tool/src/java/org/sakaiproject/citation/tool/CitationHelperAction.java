/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.tool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.citation.api.ActiveSearch;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.CitationIterator;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.SearchCategory;
import org.sakaiproject.citation.api.SearchDatabaseHierarchy;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.citation.cover.ConfigurationService;
import org.sakaiproject.citation.cover.SearchManager;
import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.citation.util.api.SearchQuery;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 *
 */
public class CitationHelperAction extends VelocityPortletPaneledAction
{

	/**
	 * This class contains constants and utility methods to maintain state of
	 * the advanced search form UI and process submitted data
	 *
	 * @author gbhatnag
	 */
	protected static class AdvancedSearchHelper
	{
		/* ids for fields */
		public static final String KEYWORD_ID = "keyword";
		public static final String AUTHOR_ID = "author";
		public static final String TITLE_ID = "title";
		public static final String SUBJECT_ID = "subject";
		public static final String YEAR_ID = "year";

		/* keys to hold state information */
		public static final String STATE_FIELD1 = CitationHelper.CITATION_PREFIX + "advField1";
		public static final String STATE_FIELD2 = CitationHelper.CITATION_PREFIX + "advField2";
		public static final String STATE_FIELD3 = CitationHelper.CITATION_PREFIX + "advField3";
		public static final String STATE_FIELD4 = CitationHelper.CITATION_PREFIX + "advField4";
		public static final String STATE_FIELD5 = CitationHelper.CITATION_PREFIX + "advField5";
		public static final String STATE_CRITERIA1 = CitationHelper.CITATION_PREFIX + "advCriteria1";
		public static final String STATE_CRITERIA2 = CitationHelper.CITATION_PREFIX + "advCriteria2";
		public static final String STATE_CRITERIA3 = CitationHelper.CITATION_PREFIX + "advCriteria3";
		public static final String STATE_CRITERIA4 = CitationHelper.CITATION_PREFIX + "advCriteria4";
		public static final String STATE_CRITERIA5 = CitationHelper.CITATION_PREFIX + "advCriteria5";

		/**
		 * Puts field selections stored in session state (using setFieldSelections())
		 * into the given context
		 *
		 * @param context context to put field selections into
		 * @param state SessionState to pull field selections from
		 */
		public static void putFieldSelections( Context context, SessionState state )
		{
			/*
			context.put( "advField1", ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD1 ) );
			context.put( "advField2", ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD2 ) );
			context.put( "advField3", ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD3 ) );
			context.put( "advField4", ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD4 ) );
			context.put( "advField5", ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD5 ) );
			*/

			String advField1 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD1 );
			String advField2 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD2 );
			String advField3 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD3 );
			String advField4 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD4 );
			String advField5 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD5 );

			if( advField1 != null && !advField1.trim().equals("") )
			{
				context.put( "advField1", advField1 );
			}
			else
			{
				context.put( "advField1", KEYWORD_ID );
			}

			if( advField2 != null && !advField2.trim().equals("") )
			{
				context.put( "advField2", advField2 );
			}
			else
			{
				context.put( "advField2", AUTHOR_ID );
			}

			if( advField3 != null && !advField3.trim().equals("") )
			{
				context.put( "advField3", advField3 );
			}
			else
			{
				context.put( "advField3", TITLE_ID );
			}

			if( advField4 != null && !advField4.trim().equals("") )
			{
				context.put( "advField4", advField4 );
			}
			else
			{
				context.put( "advField4", SUBJECT_ID );
			}

			if( advField5 != null && !advField5.trim().equals("") )
			{
				context.put( "advField5", advField5 );
			}
			else
			{
				context.put( "advField5", YEAR_ID );
			}
		}

		/**
		 * Puts field criteria stored in session state (using setFieldCriteria())
		 * into the given context
		 *
		 * @param context context to put field criteria into
		 * @param state SessionState to pull field criteria from
		 */
		public static void putFieldCriteria( Context context, SessionState state )
		{
			context.put( "advCriteria1", ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA1 ) );
			context.put( "advCriteria2", ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA2 ) );
			context.put( "advCriteria3", ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA3 ) );
			context.put( "advCriteria4", ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA4 ) );
			context.put( "advCriteria5", ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA5 ) );
		}

		/**
		 * Sets user-selected fields in session state from request parameters
		 *
		 * @param params  request parameters from doBeginSearch
		 * @param state   SessionState to store field selections
		 */
		public static void setFieldSelections( ParameterParser params, SessionState state )
		{
			String advField1 = params.getString( "advField1" );
			if( advField1 != null && !advField1.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_FIELD1, advField1 );
			}

			String advField2 = params.getString( "advField2" );
			if( advField2 != null && !advField2.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_FIELD2, advField2 );
			}

			String advField3 = params.getString( "advField3" );
			if( advField3 != null && !advField3.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_FIELD3, advField3 );
			}

			String advField4 = params.getString( "advField4" );
			if( advField4 != null && !advField4.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_FIELD4, advField4 );
			}

			String advField5 = params.getString( "advField5" );
			if( advField5 != null && !advField5.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_FIELD5, advField5 );
			}
		}

		/**
		 * Sets user-entered search field criteria in session state
		 * from request parameters
		 *
		 * @param params  request parameters from doBeginSearch
		 * @param state   SessionState to store field criteria
		 */
		public static void setFieldCriteria( ParameterParser params, SessionState state )
		{
			String advCriteria1 = params.getString( "advCriteria1" );
			if( advCriteria1 != null && !advCriteria1.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_CRITERIA1, advCriteria1 );
			}

			String advCriteria2 = params.getString( "advCriteria2" );
			if( advCriteria2 != null && !advCriteria2.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_CRITERIA2, advCriteria2 );
			}

			String advCriteria3 = params.getString( "advCriteria3" );
			if( advCriteria3 != null && !advCriteria3.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_CRITERIA3, advCriteria3 );
			}

			String advCriteria4 = params.getString( "advCriteria4" );
			if( advCriteria4 != null && !advCriteria4.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_CRITERIA4, advCriteria4 );
			}

			String advCriteria5 = params.getString( "advCriteria5" );
			if( advCriteria5 != null && !advCriteria5.trim().equals("") )
			{
				state.setAttribute( AdvancedSearchHelper.STATE_CRITERIA5, advCriteria5 );
			}
		}

		public static SearchQuery getAdvancedCriteria( SessionState state )
		{
			String advField1 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD1 );
			String advField2 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD2 );
			String advField3 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD3 );
			String advField4 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD4 );
			String advField5 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_FIELD5 );

			String advCriteria1 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA1 );
			String advCriteria2 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA2 );
			String advCriteria3 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA3 );
			String advCriteria4 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA4 );
			String advCriteria5 = ( String )state.getAttribute( AdvancedSearchHelper.STATE_CRITERIA5 );

			SearchQuery searchQuery = new org.sakaiproject.citation.util.impl.SearchQuery();

			/*
			 *  put fielded, non-null criteria into the searchQuery
			 */
			if( advField1 != null && advCriteria1 != null )
			{
				if( advField1.equalsIgnoreCase( KEYWORD_ID ) )
				{
					searchQuery.addKeywords( advCriteria1 );
				}
				else if( advField1.equalsIgnoreCase( AUTHOR_ID ) )
				{
					searchQuery.addAuthor( advCriteria1 );
				}
				else if( advField1.equalsIgnoreCase( TITLE_ID ) )
				{
					searchQuery.addTitle( advCriteria1 );
				}
				else if( advField1.equalsIgnoreCase( SUBJECT_ID ) )
				{
					searchQuery.addSubject( advCriteria1 );
				}
				else if( advField1.equalsIgnoreCase( YEAR_ID ) )
				{
					searchQuery.addYear( advCriteria1 );
				}
			}

			if( advField2 != null && advCriteria2 != null )
			{
				if( advField2.equalsIgnoreCase( KEYWORD_ID ) )
				{
					searchQuery.addKeywords( advCriteria2 );
				}
				else if( advField2.equalsIgnoreCase( AUTHOR_ID ) )
				{
					searchQuery.addAuthor( advCriteria2 );
				}
				else if( advField2.equalsIgnoreCase( TITLE_ID ) )
				{
					searchQuery.addTitle( advCriteria2 );
				}
				else if( advField2.equalsIgnoreCase( SUBJECT_ID ) )
				{
					searchQuery.addSubject( advCriteria2 );
				}
				else if( advField2.equalsIgnoreCase( YEAR_ID ) )
				{
					searchQuery.addYear( advCriteria2 );
				}
			}

			if( advField3 != null && advCriteria3 != null )
			{
				if( advField3.equalsIgnoreCase( KEYWORD_ID ) )
				{
					searchQuery.addKeywords( advCriteria3 );
				}
				else if( advField3.equalsIgnoreCase( AUTHOR_ID ) )
				{
					searchQuery.addAuthor( advCriteria3 );
				}
				else if( advField3.equalsIgnoreCase( TITLE_ID ) )
				{
					searchQuery.addTitle( advCriteria3 );
				}
				else if( advField3.equalsIgnoreCase( SUBJECT_ID ) )
				{
					searchQuery.addSubject( advCriteria3 );
				}
				else if( advField3.equalsIgnoreCase( YEAR_ID ) )
				{
					searchQuery.addYear( advCriteria3 );
				}
			}

			if( advField4 != null && advCriteria4 != null )
			{
				if( advField4.equalsIgnoreCase( KEYWORD_ID ) )
				{
					searchQuery.addKeywords( advCriteria4 );
				}
				else if( advField4.equalsIgnoreCase( AUTHOR_ID ) )
				{
					searchQuery.addAuthor( advCriteria4 );
				}
				else if( advField4.equalsIgnoreCase( TITLE_ID ) )
				{
					searchQuery.addTitle( advCriteria4 );
				}
				else if( advField4.equalsIgnoreCase( SUBJECT_ID ) )
				{
					searchQuery.addSubject( advCriteria4 );
				}
				else if( advField4.equalsIgnoreCase( YEAR_ID ) )
				{
					searchQuery.addYear( advCriteria4 );
				}
			}

			if( advField5 != null && advCriteria5 != null )
			{
				if( advField5.equalsIgnoreCase( KEYWORD_ID ) )
				{
					searchQuery.addKeywords( advCriteria5 );
				}
				else if( advField5.equalsIgnoreCase( AUTHOR_ID ) )
				{
					searchQuery.addAuthor( advCriteria5 );
				}
				else if( advField5.equalsIgnoreCase( TITLE_ID ) )
				{
					searchQuery.addTitle( advCriteria5 );
				}
				else if( advField5.equalsIgnoreCase( SUBJECT_ID ) )
				{
					searchQuery.addSubject( advCriteria5 );
				}
				else if( advField5.equalsIgnoreCase( YEAR_ID ) )
				{
					searchQuery.addYear( advCriteria5 );
				}
			}

			return searchQuery;
		}

		public static void clearAdvancedFormState( SessionState state )
		{
			state.removeAttribute( AdvancedSearchHelper.STATE_FIELD1 );
			state.removeAttribute( AdvancedSearchHelper.STATE_FIELD2 );
			state.removeAttribute( AdvancedSearchHelper.STATE_FIELD3 );
			state.removeAttribute( AdvancedSearchHelper.STATE_FIELD4 );
			state.removeAttribute( AdvancedSearchHelper.STATE_FIELD5 );

			state.removeAttribute( AdvancedSearchHelper.STATE_CRITERIA1 );
			state.removeAttribute( AdvancedSearchHelper.STATE_CRITERIA2 );
			state.removeAttribute( AdvancedSearchHelper.STATE_CRITERIA3 );
			state.removeAttribute( AdvancedSearchHelper.STATE_CRITERIA4 );
			state.removeAttribute( AdvancedSearchHelper.STATE_CRITERIA5 );
		}
	}

	protected final static Log logger = LogFactory.getLog(CitationHelperAction.class);

	public static ResourceLoader rb = new ResourceLoader("citations");

	public static final Integer DEFAULT_RESULTS_PAGE_SIZE = new Integer(10);
	public static final Integer DEFAULT_LIST_PAGE_SIZE = new Integer(10);

	protected static final String ELEMENT_ID_CREATE_FORM = "createForm";
	protected static final String ELEMENT_ID_EDIT_FORM = "editForm";
	protected static final String ELEMENT_ID_LIST_FORM = "listForm";
	protected static final String ELEMENT_ID_SEARCH_FORM = "searchForm";
	protected static final String ELEMENT_ID_RESULTS_FORM = "resultsForm";
	protected static final String ELEMENT_ID_VIEW_FORM = "viewForm";

	/**
	 * Mode defines a complete set of values describing the user's navigation intentions
	 */
	public enum Mode
	{
		DATABASE,
		CREATE,
		EDIT,
		ERROR,
		LIST,
		ADD_CITATIONS,
		IMPORT_CITATIONS,
		MESSAGE,
		SEARCH,
		RESULTS,
		VIEW;
	}

	/*
	 * define a set of "fake" Modes (asynchronous calls) to maintain proper
	 * back-button stack state
	 */
	protected static Set<Mode> ignoreModes = new java.util.HashSet<Mode>();
	static {
		ignoreModes.add( Mode.DATABASE );
		ignoreModes.add( Mode.MESSAGE );
	}

	protected static final String PARAM_FORM_NAME = "FORM_NAME";

	protected static final String STATE_RESOURCES_ADD = CitationHelper.CITATION_PREFIX + "resources_add";
	protected static final String STATE_CURRENT_DATABASES = CitationHelper.CITATION_PREFIX + "current_databases";
	protected static final String STATE_BACK_BUTTON_STACK = CitationHelper.CITATION_PREFIX + "back-button_stack";
	protected static final String STATE_COLLECTION_ID = CitationHelper.CITATION_PREFIX + "collection_id";
	protected static final String STATE_COLLECTION = CitationHelper.CITATION_PREFIX + "collection"; 
	protected static final String STATE_CITATION_ID = CitationHelper.CITATION_PREFIX + "citation_id";
	protected static final String STATE_COLLECTION_TITLE = CitationHelper.CITATION_PREFIX + "collection_name";
	protected static final String STATE_CURRENT_REPOSITORY = CitationHelper.CITATION_PREFIX + "current_repository";
	protected static final String STATE_CURRENT_RESULTS = CitationHelper.CITATION_PREFIX + "current_results";
	protected static final String STATE_LIST_ITERATOR = CitationHelper.CITATION_PREFIX + "list_iterator";
	protected static final String STATE_LIST_PAGE = CitationHelper.CITATION_PREFIX + "list_page";
	protected static final String STATE_LIST_PAGE_SIZE = CitationHelper.CITATION_PREFIX + "list_page_size";
	protected static final String STATE_LIST_NO_SCROLL = CitationHelper.CITATION_PREFIX + "list_no_scroll";
	protected static final String STATE_NO_KEYWORDS = CitationHelper.CITATION_PREFIX + "no_search_criteria";
	protected static final String STATE_NO_DATABASES = CitationHelper.CITATION_PREFIX + "no_databases";
	protected static final String STATE_NO_RESULTS = CitationHelper.CITATION_PREFIX + "no_results";
	protected static final String STATE_SEARCH_HIERARCHY = CitationHelper.CITATION_PREFIX + "search_hierarchy";
	protected static final String STATE_SELECTED_CATEGORY = CitationHelper.CITATION_PREFIX + "selected_category";
	protected static final String STATE_DEFAULT_CATEGORY = CitationHelper.CITATION_PREFIX + "default_category";
	protected static final String STATE_UNAUTHORIZED_DB = CitationHelper.CITATION_PREFIX + "unauthorized_database";
	protected static final String STATE_REPOSITORY_MAP = CitationHelper.CITATION_PREFIX + "repository_map";
	protected static final String STATE_RESULTS_PAGE_SIZE = CitationHelper.CITATION_PREFIX + "results_page_size";
	protected static final String STATE_KEYWORDS = CitationHelper.CITATION_PREFIX + "search_criteria";
	protected static final String STATE_SEARCH_INFO = CitationHelper.CITATION_PREFIX + "search_info";
	protected static final String STATE_BASIC_SEARCH = CitationHelper.CITATION_PREFIX + "basic_search";
	protected static final String STATE_SEARCH_RESULTS = CitationHelper.CITATION_PREFIX + "search_results";

	protected static final String TEMPLATE_CREATE = "citation/create";
	protected static final String TEMPLATE_EDIT = "citation/edit";
	protected static final String TEMPLATE_ERROR = "citation/error";
	protected static final String TEMPLATE_LIST = "citation/list";
	protected static final String TEMPLATE_ADD_CITATIONS = "citation/add_citations";
	protected static final String TEMPLATE_IMPORT_CITATIONS = "citation/import_citations";
	protected static final String TEMPLATE_MESSAGE = "citation/_message";
	protected static final String TEMPLATE_SEARCH = "citation/search";
	protected static final String TEMPLATE_RESULTS = "citation/results";
	protected static final String TEMPLATE_VIEW = "citation/view";
	protected static final String TEMPLATE_DATABASE = "citation/_databases";


	/**
	 * Check for the helper-done case locally and handle it before letting the VPPA.toolModeDispatch() handle the actual dispatch.
	 * @see org.sakaiproject.cheftool.VelocityPortletPaneledAction#toolModeDispatch(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		//SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);

		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = ToolManager.getCurrentTool();

			String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			try
			{
				res.sendRedirect(url);  // TODO
			}
			catch (IOException e)
			{
				logger.warn("IOException", e);
				// Log.warn("chef", this + " : ", e);
			}
			return;
		}

		super.toolModeDispatch(methodBase, methodExt, req, res);
	}

	/**
	 * build the context in helper mode.
	 *
	 * @return The name of the template to use.
	 */
	public String buildCitationsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		initHelper(portlet, context, rundata, state);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
		context.put("xilator", new Validator());

		// set me as the helper class
		// state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, CitationHelperAction.class.getName());

		String alertMessages = (String) state.getAttribute(STATE_MESSAGE);
		if(alertMessages != null)
		{
			context.put("alertMessages", alertMessages);
			state.removeAttribute(STATE_MESSAGE);
		}

		if(showBackButton(state))
		{
			context.put("showBackButton", Boolean.TRUE);
		}

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);

		String template = "";
		Mode mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null)
		{
			mode = Mode.ADD_CITATIONS;
			setMode(state, mode);
		}

		switch(mode)
		{
			case ADD_CITATIONS:
				template = buildAddCitationsPanelContext(portlet, context, rundata, state);
				break;
			case CREATE:
				template = buildCreatePanelContext(portlet, context, rundata, state);
				break;
			case EDIT:
				template = buildEditPanelContext(portlet, context, rundata, state);
				break;
			case ERROR:
				template = buildErrorPanelContext(portlet, context, rundata, state);
				break;
			case LIST:
				template = buildListPanelContext(portlet, context, rundata, state);
				break;
			case MESSAGE:
				template = buildMessagePanelContext(portlet, context, rundata, state);
				break;
			case SEARCH:
				template = buildSearchPanelContext(portlet, context, rundata, state);
				break;
			case VIEW:
				template = buildViewPanelContext(portlet, context, rundata, state);
				break;
		}

		return template;

	}	// buildCitationsPanelContext

	/**
     * @param state
     * @return
     */
    private boolean showBackButton(SessionState state)
    {
	    Stack<Mode> stack = (Stack<Mode>) state.getAttribute( STATE_BACK_BUTTON_STACK );

	    if( stack == null )
	    {
	    	logger.warn( "showBackButton getting null back-button stack from state" );
	    	return false;
	    }

	    // only show the button if you have somewhere to go back to
	    return ( stack.size() > 1 );
    }

    protected void putCitationCollectionDetails( Context context, SessionState state )
    {
		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		String refStr = contentService.getReference(resourceId);
		Reference ref = EntityManager.newReference(refStr);
		String collectionTitle = null;
		if( ref != null )
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", collectionTitle );
		}
		else
		{
			context.put( "collectionTitle", (String)state.getAttribute( STATE_COLLECTION_TITLE ) );
		}

		// get the collection we're now working on
		String collectionId = (String)state.getAttribute(STATE_COLLECTION_ID);
		context.put( "collectionId", collectionId );

		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null)
		{
			logger.warn( "buildAddCitationsPanelContext unable to access citationCollection " + collectionId );
		}

		// get the size of the list
		context.put( "collectionSize", new Integer( collection.size() ) );
    }

	public String buildImportCitationsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validator
		context.put("xilator", new Validator());

		return TEMPLATE_IMPORT_CITATIONS;

	}	// buildImportPanelContext
	
	/**
     *
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public String buildAddCitationsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// body onload handler
		context.put("sakai_onload", "setMainFrameHeight( window.name )");

		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		String refStr = contentService.getReference(resourceId);
		Reference ref = EntityManager.newReference(refStr);
		String collectionTitle = null;
		if( ref != null )
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}

		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", collectionTitle );
		}
		else
		{
			context.put( "collectionTitle", (String)state.getAttribute( STATE_COLLECTION_TITLE ) );
		}

		// get the collection we're now working on
		String collectionId = (String)state.getAttribute(STATE_COLLECTION_ID);
		context.put( "collectionId", collectionId );

		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null)
		{
			logger.warn( "buildAddCitationsPanelContext unable to access citationCollection " + collectionId );
		}

		// get the size of the list
		context.put( "collectionSize", new Integer( collection.size() ) );

		// determine which features to display
		if( ConfigurationService.isGoogleScholarEnabled() )
		{
			String googleUrl = SearchManager.getGoogleScholarUrl(contentService.getUuid(resourceId));
			context.put( "googleUrl", googleUrl );

			// object array for formatted messages
			Object[] googleArgs = { rb.getString( "linkLabel.google" ) };
			context.put( "googleArgs", googleArgs );
		}

		if( ConfigurationService.isLibrarySearchEnabled() &&
				ConfigurationService.isConfigurationXmlAvailable() &&
				ConfigurationService.isDatabaseHierarchyXmlAvailable() )
		{
			context.put( "searchLibrary", Boolean.TRUE );
		}

		// form name
		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);

		return TEMPLATE_ADD_CITATIONS;

    } // buildAddCitationsPanelContext

	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildCreatePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validator
		context.put("xilator", new Validator());

		//context.put("sakai_onload", "setPopupHeight('create');checkinWithOpener('create');");
		//context.put("sakai_onunload", "window.opener.parent.popups['create']=null;");

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);

		List schemas = CitationService.getSchemas();
		context.put("TEMPLATES", schemas);

		Schema defaultSchema = CitationService.getDefaultSchema();
		context.put("DEFAULT_TEMPLATE", defaultSchema);

		// Object array for instruction message
		Object[] instrArgs = { rb.getString( "submit.create" ) };
		context.put( "instrArgs", instrArgs );

		return TEMPLATE_CREATE;

	}	// buildCreatePanelContext

	/**
	 * @param portlet
	 * @param context
	 * @param rundata
	 * @param state
	 * @return
	 */
	public String buildDatabasePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// get hierarchy
		SearchDatabaseHierarchy hierarchy = ( SearchDatabaseHierarchy )
		state.getAttribute(STATE_SEARCH_HIERARCHY);

		// get selected category
		SearchCategory category = ( SearchCategory ) state.getAttribute(
				STATE_SELECTED_CATEGORY );

		if( category == null )
		{
			// bad...
			logger.warn( "buildDatabasePanelContext getting null selected " +
					"category from state." );
		}

		// put selected category into context
		context.put( "category", category );

		// maxDbNum
		Integer maxDbNum = new Integer(hierarchy.getNumMaxSearchableDb());
		context.put( "maxDbNum", maxDbNum );

		// object array for formatted messages
		Object[] maxDbArgs = { maxDbNum };
		context.put( "maxDbArgs", maxDbArgs );

		// validator
		context.put("xilator", new Validator());

		return TEMPLATE_DATABASE;
	}  // buildDatabasePanelContext

	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public String buildEditPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validator
		context.put("xilator", new Validator());

		context.put("sakai_onload", "setMainFrameHeight( window.name ); heavyResize();");
		//context.put("sakai_onunload", "window.opener.parent.popups['edit']=null;");

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);

		Citation citation = (Citation) state.getAttribute(CitationHelper.CITATION_EDIT_ITEM);
		if(citation == null)
		{
			doEdit(rundata);
			citation = (Citation) state.getAttribute(CitationHelper.CITATION_EDIT_ITEM);
		}
		context.put("citation", citation);

		String citationId = (String) state.getAttribute(CitationHelper.CITATION_EDIT_ID);
		String collectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
		context.put("citationId", citationId);
		context.put("collectionId", collectionId);

		List schemas = CitationService.getSchemas();
		context.put("TEMPLATES", schemas);

		context.put("DEFAULT_TEMPLATE", citation.getSchema());

		// Object array for formatted instruction
		Object[] instrArgs = { rb.getString( "submit.edit" ) };
		context.put( "instrArgs", instrArgs );

	    return TEMPLATE_EDIT;
    }

	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    private String buildErrorPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		//context.put("sakai_onload", "setPopupHeight('error');");
		//context.put("sakai_onunload", "window.opener.parent.popups['error']=null;");

	    return TEMPLATE_ERROR;
    }

	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validator
		context.put("xilator", new Validator());

		if( state.removeAttribute( STATE_LIST_NO_SCROLL ) == null )
		{
			context.put("sakai_onload", "setMainFrameHeight( window.name )");
		}
		else
		{
			context.put("sakai_onload", "resizeFrame()");
		}

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		String refStr = contentService.getReference(resourceId);
		Reference ref = EntityManager.newReference(refStr);
		String collectionTitle = null;
		if( ref != null )
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", collectionTitle );
		}
		else
		{
			context.put( "collectionTitle", (String)state.getAttribute( STATE_COLLECTION_TITLE ) );
		}

		context.put("openUrlLabel", ConfigurationService.getSiteConfigOpenUrlLabel());

		context.put(PARAM_FORM_NAME, ELEMENT_ID_LIST_FORM);

		CitationCollection collection = getCitationCollection(state, true);

		// collection size
		context.put( "collectionSize", new Integer( collection.size() ) );

		// export URLs
		String exportUrlSel = collection.getUrl(CitationService.REF_TYPE_EXPORT_RIS_SEL);
		String exportUrlAll = collection.getUrl(CitationService.REF_TYPE_EXPORT_RIS_ALL);
		context.put("exportUrlSel", exportUrlSel);
		context.put("exportUrlAll", exportUrlAll);

		Integer listPageSize = (Integer) state.getAttribute(STATE_LIST_PAGE_SIZE);
		if(listPageSize == null)
		{
			listPageSize = DEFAULT_LIST_PAGE_SIZE;
			state.setAttribute(STATE_LIST_PAGE_SIZE, listPageSize);
		}
		context.put("listPageSize", listPageSize);

		CitationIterator newIterator = collection.iterator();
		CitationIterator oldIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(oldIterator != null)
		{
			newIterator.setPageSize(listPageSize.intValue());
			newIterator.setPage(oldIterator.getPage());
		}
		context.put("citations", newIterator);
		context.put("collectionId", collection.getId());
		if(! collection.isEmpty())
		{
			context.put("show_citations", Boolean.TRUE);

			int page = newIterator.getPage();
			int pageSize = newIterator.getPageSize();
			int totalSize = collection.size();

			int start = page * pageSize + 1;
			int end = Math.min((page + 1) * pageSize, totalSize);

			Integer[] position = { new Integer(start) , new Integer(end), new Integer(totalSize)};
			String showing = (String) rb.getFormattedMessage("showing.results", position);
			context.put("showing", showing);
		}
		state.setAttribute(STATE_LIST_ITERATOR, newIterator);

		// back to search results button control
		context.put("searchResults", state.getAttribute(STATE_SEARCH_RESULTS) );

		// constant schema identifier
		context.put( "titleProperty", Schema.TITLE );

		/*
		 * Object arrays for formatted messages
		 */
		Object[] instrMainArgs = { ConfigurationService.getSiteConfigOpenUrlLabel() };
		context.put( "instrMainArgs", instrMainArgs );

		Object[] instrSubArgs = { rb.getString( "label.finish" ) };
		context.put( "instrSubArgs", instrSubArgs );

		Object[] emptyListArgs = { rb.getString( "label.menu" ) };
		context.put( "emptyListArgs", emptyListArgs );

		return TEMPLATE_LIST;

	}	// buildListPanelContext

	/**
	 * This method retrieves the CitationCollection for the current session.  
	 * If the CitationCollection is already in session-state and has not been
	 * updated in the persistent storage since it was last accessed, the copy 
	 * in session-state will be returned.  If it has been updated in storage,
	 * the copy in session-state will be updated and returned. If the 
	 * CitationCollection has not yet been created in storage and the second 
	 * parameter is true, this method will create the collection and return it.
	 * In that case, values will be added to session-state for attributes named 
	 * STATE_COLLECTION_ID and STATE_COLLECTION. If the CitationCollection has 
	 * not yet been created in storage and the second parameter is false, the
	 * method will return null.
	 * @param state The SessionState object for the current session.
	 * @param create A flag indicating whether the collection should be created
	 * 	if it does not already exist.
	 * @return The CitationCollection for the current session, or null. 
	 */
	protected CitationCollection getCitationCollection(SessionState state, boolean create) 
	{
		CitationCollection collection = (CitationCollection) state.getAttribute(STATE_COLLECTION);
		if(collection == null)
		{
			String collectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
			if(collectionId == null && create)
			{
				collection = CitationService.addCollection();
				state.setAttribute(STATE_COLLECTION_ID, collection.getId());
			}
			else
			{
				try
	            {
		            collection = CitationService.getCollection(collectionId);
	            }
	            catch (IdUnusedException e)
	            {
		            logger.warn("IdUnusedException: CitationHelperAction.getCitationCollection() unable to access citationCollection " + collectionId);
	            }
				if(collection == null && create)
				{
					collection = CitationService.addCollection();
					state.setAttribute(STATE_COLLECTION_ID, collection.getId());
				}
			}
			if(collection != null)
			{
				state.setAttribute(STATE_COLLECTION, collection);
			}
		}
		return collection;
	}

	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		initHelper(portlet, context, rundata, state);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		// always put whether this is a Resources Add or Revise operation
		if( state.getAttribute( STATE_RESOURCES_ADD ) != null )
		{
			context.put( "resourcesAddAction", Boolean.TRUE );
		}

		if(showBackButton(state))
		{
			context.put("showBackButton", Boolean.TRUE);
		}

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);

		String template = "";
		Mode mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null)
		{
			// mode really shouldn't be null here
			logger.warn( "buildMainPanelContext() getting null Mode from state" );
			mode = Mode.ADD_CITATIONS;
			setMode(state, mode);
		}

		// add mode to the template
		context.put( "citationsHelperMode", mode );

		switch(mode)
		{
			case IMPORT_CITATIONS:
				template = buildImportCitationsPanelContext(portlet, context, rundata, state);
				break;
			case ADD_CITATIONS:
				template = buildAddCitationsPanelContext(portlet, context, rundata, state);
				break;
			case CREATE:
				template = buildCreatePanelContext(portlet, context, rundata, state);
				break;
			case DATABASE:
				template = buildDatabasePanelContext(portlet, context, rundata, state);
				break;
			case EDIT:
				template = buildEditPanelContext(portlet, context, rundata, state);
				break;
			case ERROR:
				template = buildErrorPanelContext(portlet, context, rundata, state);
				break;
			case LIST:
				template = buildListPanelContext(portlet, context, rundata, state);
				break;
			case MESSAGE:
				template = buildMessagePanelContext(portlet, context, rundata, state);
				break;
			case SEARCH:
				template = buildSearchPanelContext(portlet, context, rundata, state);
				break;
			case RESULTS:
				template = buildResultsPanelContext(portlet, context, rundata, state);
				break;
			case VIEW:
				template = buildViewPanelContext(portlet, context, rundata, state);
				break;
		}

		return template;

	}	// buildMainPanelContext

	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public String buildMessagePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
	    context.put("sakai_onload", "");
	    //context.put("FORM_NAME", "messageForm");

	    context.put( "citationId", state.getAttribute( STATE_CITATION_ID ) );

		// get the collection we're now working on
		String collectionId = (String)state.getAttribute(STATE_COLLECTION_ID);
		context.put( "collectionId", collectionId );

		int size = 0;
		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null)
		{
			logger.warn( "buildMessagePanelContext unable to access citationCollection " + collectionId );
		}
		else
		{
			size = collection.size();
		}

		// get the size of the list
		context.put( "citationCount", new Integer( size ) );

	    return TEMPLATE_MESSAGE;
    }

	/**
     *
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public String buildResultsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validators
		context.put("TextValidator", new QuotedTextValidator());
		context.put("xilator", new Validator());

		// javascript to run on page load
		context.put("sakai_onload", "setMainFrameHeight( window.name ); highlightButtonSelections( '" + rb.getString("remove.results") + "' )");

		// put the citation list title and size
		putCitationCollectionDetails(context, state);

		// signal basic/advanced search
		Object basicSearch = state.getAttribute( STATE_BASIC_SEARCH );
		context.put( "basicSearch", basicSearch );
		if( basicSearch != null )
		{
			context.put( "searchType", ActiveSearch.BASIC_SEARCH_TYPE );
		}
		else
		{
			context.put( "searchType", ActiveSearch.ADVANCED_SEARCH_TYPE );
		}

		/*
		 * SEARCH RESULTS
		 */
		ActiveSearch searchResults = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(searchResults != null)
		{
			context.put("searchResults", searchResults);
			List currentResults = (List) state.getAttribute(STATE_CURRENT_RESULTS);
			context.put("currentResults", currentResults);

			Integer[] position = { new Integer(searchResults.getFirstRecordIndex() + 1) , new Integer(searchResults.getLastRecordIndex()), searchResults.getNumRecordsFound()};
			String showing = (String) rb.getFormattedMessage("showing.results", position);
			context.put("showing", showing);
		}

		// selected databases
		String[] databaseIds = (String[])state.getAttribute( STATE_CURRENT_DATABASES );
		context.put( "selectedDatabases", databaseIds );

		// load basic/advanced search form state
		loadSearchFormState( context, state );

		/*
		 * OTHER CONTEXT PARAMS
		 */
		// searchInfo
		ActiveSearch searchInfo = (ActiveSearch) state.getAttribute(STATE_SEARCH_INFO);
		context.put("searchInfo", searchInfo);

		// form name
		context.put(PARAM_FORM_NAME, ELEMENT_ID_RESULTS_FORM);

		// OpenURL Label
		context.put( "openUrlLabel", ConfigurationService.getSiteConfigOpenUrlLabel() );

		// object arrays for formatted messages
		Object[] instrMainArgs = { rb.getString( "add.results" ) };
		context.put( "instrMainArgs", instrMainArgs );

		Object[] instrSubArgs = { rb.getString( "label.new.search" ) };
		context.put( "instrSubArgs", instrSubArgs );

		/*
		 * ERROR CHECKING
		 */
		String alertMessages = (String) state.removeAttribute(STATE_MESSAGE);
		if(alertMessages != null)
		{
			context.put("alertMessages", alertMessages);
		}

		Object noResults = state.removeAttribute( STATE_NO_RESULTS );
		if( noResults != null )
		{
			context.put( "noResults", noResults );
		}

		Object noSearch = state.removeAttribute(STATE_NO_KEYWORDS);
		if(noSearch != null)
		{
			context.put("noSearch", noSearch);
		}

		Object noDatabases = state.removeAttribute( STATE_NO_DATABASES );
		if( noDatabases != null )
		{
			context.put( "noDatabases", noDatabases );
		}

    	return TEMPLATE_RESULTS;

    }  // buildResultsPanelContext

	/**
	 * @param portlet
	 * @param context
	 * @param rundata
	 * @param state
	 * @return
	 */
	public String buildSearchPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validators
		context.put("TextValidator", new QuotedTextValidator());
		context.put("xilator", new Validator());

		// javascript to run on page load
		context.put("sakai_onload", "setMainFrameHeight( window.name ); showTopCategory()");

		// put citation list title/size
		putCitationCollectionDetails(context, state);

		// resource-related
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		String guid = contentService.getUuid(resourceId);
		context.put("RESOURCE_ID", guid);

		// category information from hierarchy
		SearchDatabaseHierarchy hierarchy = (SearchDatabaseHierarchy) state.getAttribute(STATE_SEARCH_HIERARCHY);
		context.put( "defaultCategory", hierarchy.getDefaultCategory() );
		context.put( "categoryListing", hierarchy.getCategoryListing() );

		// load basic/advanced search form state
		loadSearchFormState( context, state );

		/*
		 * MISCELLANEOUS CONTEXT PARAMS
		 */
		// default to basicSearch
		context.put( "basicSearch", state.getAttribute( STATE_BASIC_SEARCH ) );

		// searchInfo
		ActiveSearch searchInfo = (ActiveSearch) state.getAttribute(STATE_SEARCH_INFO);
		context.put("searchInfo", searchInfo);

		// max number of searchable databases
		Integer maxDbNum = new Integer(hierarchy.getNumMaxSearchableDb());
		context.put( "maxDbNum", maxDbNum );

		// form name
		context.put(PARAM_FORM_NAME, ELEMENT_ID_SEARCH_FORM);

		// OpenURL Label
		context.put( "openUrlLabel", ConfigurationService.getSiteConfigOpenUrlLabel() );

		// object arrays for formatted messages
		Object[] instrArgs = { rb.getString( "submit.search" ) };
		context.put( "instrArgs", instrArgs );

	    return TEMPLATE_SEARCH;

	}	// buildSearchPanelContext

    /**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public String buildViewPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);

		// validator
		context.put("xilator", new Validator());

		//context.put("sakai_onload", "setMainFrameHeight('" + CitationHelper.CITATION_FRAME_ID + "');");

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		context.put(PARAM_FORM_NAME, ELEMENT_ID_VIEW_FORM);

		Citation citation = (Citation) state.getAttribute(CitationHelper.CITATION_VIEW_ITEM);
		if(citation == null)
		{
			doEdit(rundata);
			citation = (Citation) state.getAttribute(CitationHelper.CITATION_VIEW_ITEM);
		}
		context.put("citation", citation);

		String citationId = (String) state.getAttribute(CitationHelper.CITATION_VIEW_ID);
		String collectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
		context.put("citationId", citationId);
		context.put("collectionId", collectionId);

		List schemas = CitationService.getSchemas();
		context.put("TEMPLATES", schemas);

		context.put("DEFAULT_TEMPLATE", citation.getSchema());

	    return TEMPLATE_VIEW;

    }	// buildViewPanelContext


	/**
	 *
	 * @param context
	 * @param state
	 */
	protected void loadSearchFormState( Context context, SessionState state )
	{
		// remember data previously entered
		if( state.getAttribute( STATE_BASIC_SEARCH ) != null )
		{
			/* basic search */
			context.put( "keywords", ( String )state.getAttribute( STATE_KEYWORDS ) );

			// default advanced search selections
			context.put( "advField1", AdvancedSearchHelper.KEYWORD_ID );
			context.put( "advField2", AdvancedSearchHelper.AUTHOR_ID );
			context.put( "advField3", AdvancedSearchHelper.TITLE_ID );
			context.put( "advField4", AdvancedSearchHelper.SUBJECT_ID );
			context.put( "advField5", AdvancedSearchHelper.YEAR_ID );
		}
		else
		{
			/* advanced search */

			// field selections
			AdvancedSearchHelper.putFieldSelections( context, state );

			// field criteria
			AdvancedSearchHelper.putFieldCriteria( context, state );
		}

		// basic/advanced search types
		context.put( "basicSearchType", ActiveSearch.BASIC_SEARCH_TYPE );
		context.put( "advancedSearchType", ActiveSearch.ADVANCED_SEARCH_TYPE );
	}

	/**
	*
	*/
	public void doFinish ( RunData data)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		int citationCount = 0;

		if(pipe.getAction().getActionType() == ResourceToolAction.ActionType.CREATE)
		{
			SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

	    	// delete the temporary resource
			String temporaryResourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
			ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
			ContentResource tempResource = null;
			try
            {
				// get the temp resource
	            tempResource = contentService.getResource(temporaryResourceId);

	            // use the temp resource to 'create' the real resource
	            pipe.setRevisedContent(tempResource.getContent());

	            // remove the temp resource
	            if( CitationService.allowRemoveCitationList( temporaryResourceId ) )
	            {
	            	// setup a SecurityAdvisor
		            SecurityService.pushAdvisor( new CitationListSecurityAdviser(
		            		SessionManager.getCurrentSessionUserId(),
		            		ContentHostingService.AUTH_RESOURCE_REMOVE_ANY,
		            		tempResource.getReference() ) );

		            // remove temp resource
		            contentService.removeResource(temporaryResourceId);

		            // clear advisors
		            SecurityService.clearAdvisors();

		            tempResource = null;
	            }
            }
            catch (PermissionException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("PermissionException ", e);
            }
            catch (IdUnusedException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("IdUnusedException ", e);
            }
            catch (TypeException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("TypeException ", e);
            }
            catch (InUseException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("InUseException ", e);
            }
            catch (ServerOverloadException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("ServerOverloadException ", e);
            }
		}

		// set content (mime) type
		pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
        pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_TYPE, ResourceType.MIME_TYPE_HTML);

		// set the alternative_reference to point to reference_root for CitationService
		pipe.setRevisedResourceProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, org.sakaiproject.citation.api.CitationService.REFERENCE_ROOT);

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// get the collection we're now working on
		CitationCollection collection = getCitationCollection(state, true);

		String collectionId = (String) state.getAttribute(STATE_COLLECTION_ID);

		String[] args = new String[]{ Integer.toString(collection.size()) };
		String size_str =rb.getFormattedMessage("citation.count",  args);
    	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_LENGTH, size_str);

    	// leave helper mode
		pipe.setActionCanceled(false);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);

		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

		cleanup(toolSession, CitationHelper.CITATION_PREFIX);

	}	// doFinish

    /**
     * Cancel the action for which the helper was launched.
     */
    public void doCancel(RunData data)
    {
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if(pipe.getAction().getActionType() == ResourceToolAction.ActionType.CREATE)
		{
			SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
			// TODO: delete the citation collection and all citations

	    	// TODO: delete the temporary resource
			String temporaryResourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
			ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
			ContentResourceEdit edit = null;
			try
            {
	            edit = contentService.editResource(temporaryResourceId);
	            contentService.removeResource(edit);
	            edit = null;
            }
            catch (PermissionException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("PermissionException ", e);
            }
            catch (IdUnusedException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("IdUnusedException ", e);
            }
            catch (TypeException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("TypeException ", e);
            }
            catch (InUseException e)
            {
	            // TODO Auto-generated catch block
	            logger.warn("InUseException ", e);
            }

            if(edit != null)
            {
            	contentService.cancelResource(edit);
            }
		}

    	// leave helper mode
		pipe.setActionCanceled(true);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);

		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

		cleanup(toolSession, CitationHelper.CITATION_PREFIX);

    }

	/**
	* Adds a citation to the current citation collection.  Called from the search-results popup.
	*/
	public void doAddCitation ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// get the citation from search results, add it to the citation collection, and rebuild the context
		String[] citationIds = params.getStrings("citationId");
		String collectionId = params.getString("collectionId");

		Integer page = (Integer) state.getAttribute(STATE_LIST_PAGE);
		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		CitationCollection tempCollection = search.getSearchResults();
		Map index = search.getIndex();
		if(index == null)
		{
			index = new Hashtable();
			search.setIndex(index);
		}

		CitationCollection permCollection = getCitationCollection(state, true);
		for(int i = 0; i < citationIds.length; i++)
		{
			try
			{
				Citation citation = tempCollection.getCitation(citationIds[i]);
				citation.setAdded(true);
				permCollection.add(citation);
			}
			catch(IdUnusedException ex)
			{
		        logger.info("doAdd: unable to add citation " + citationIds[i] + " to collection " + collectionId);
			}
		}
        CitationService.save(permCollection);
        // setMode(state, Mode.LIST);
 	}

	/**
	 * @param data
	 */
	public void doBack(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		popMode( state );
	}

	/**
     * @param state
     */
    protected void popMode( SessionState state )
    {
		Stack<Mode> stack = (Stack<Mode>) state.getAttribute( STATE_BACK_BUTTON_STACK );

		if( stack != null && stack.size() > 1 )
		{
			// pop the Mode currently being viewed
			stack.pop();

			// pop the previous Mode - the one to go back to
			Mode mode = stack.pop();
			logger.debug( "popMode popped " + mode );

			state.setAttribute( CitationHelper.STATE_HELPER_MODE, mode );
		}
		else
		{
			// stack should not be null or < 2
			logger.warn( "popMode() getting null or < 2 back-button stack from state." );
		}
    }

	/**
	* Removes a citation from the current citation collection.  Called from the search-results popup.
	*/
	public void doRemove ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// get the citation number from search results, remove it from the citation collection, and rebuild the context
		// get the citation from search results, add it to the citation collection, and rebuild the context
		String[] citationIds = params.getStrings("citationId");
		String collectionId = params.getString("collectionId");

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		CitationCollection tempCollection = search.getSearchResults();
		Map index = search.getIndex();
		if(index == null)
		{
			index = new Hashtable();
			search.setIndex(index);
		}

		CitationCollection permCollection = getCitationCollection(state, true);
		for(int i = 0; i < citationIds.length; i++)
		{
			try
			{
				Citation citation = tempCollection.getCitation(citationIds[i]);
				citation.setAdded(false);
				permCollection.remove(citation);
			}
			catch(IdUnusedException ex)
			{
		        logger.info("doAdd: unable to add citation " + citationIds[i] + " to collection " + collectionId);
			}
		}
        CitationService.save(permCollection);
  		setMode(state, Mode.LIST);
	}

	public void doDatabasePopulate( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// get category id
		String categoryId = params.get( "categoryId" );
		logger.debug( "doDatabasePopulate() categoryId from URL: " + categoryId );

		if( categoryId == null )
		{
			// should not be null
			setMode( state, Mode.ERROR );
			return;
		}
		else
		{
			/* TODO can probably do this in build-method (don't need category in state)*/
			// get selected category, put it in state
			SearchDatabaseHierarchy hierarchy = ( SearchDatabaseHierarchy )
			state.getAttribute( STATE_SEARCH_HIERARCHY );

			SearchCategory category = hierarchy.getCategory( categoryId );
			state.setAttribute( STATE_SELECTED_CATEGORY, category );

			setMode(state, Mode.DATABASE);
		}
	}

	/**
	*
	*/
	public void doImportPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		setMode(state, Mode.IMPORT_CITATIONS);

	}	// doImportPage
	
	
	public void doImport ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		ParameterParser params = data.getParameters();
		
		logger.debug( "doImport called.");

		Iterator iter = params.getNames();

		while (iter.hasNext())
		{
			logger.debug( "param = " + iter.next());			
		}
		
		String upload = params.get("risupload");
		logger.debug( "Upload String = " + upload);

		
		FileItem risImport = params.getFileItem("risupload");
		
		
		if (risImport == null)
			logger.debug( "risImport is null.");

	    logger.debug("Filename = " + risImport.getFileName());
	    

		InputStream risImportStream = risImport.getInputStream();
		
		InputStreamReader isr = new InputStreamReader(risImportStream);
		
		char[] chars = new char[500];
		int charRead = 0;
		
		try
		{
		  charRead = isr.read(chars);
		}
		catch(Exception e)
		{
			logger.debug("ISR error = " + e);
		}
		
		for (int i=0; i<charRead; i++)
		{
			logger.debug("char[" + i + "] = " + chars[i]);
		}
		
		String fileString = new String(chars);
		logger.debug("fileString = " + fileString);
		
/*		
		int charint = -1;
		
		try
		{
		  while ((charint = risImportStream.read()) != -1)
		  {
		    logger.debug( "Read in integer: " + charint);
			
		  }
		}
		catch(Exception e)
		{
			logger.debug( "Error in reading file: " + e);
		}

		byte[] bytes = risImport.get();
		
		logger.debug("Bytes length = " + bytes.length);
		
		for(int i=0; i<bytes.length; i++)
		{
			logger.debug("byte[" + i + "] = " + bytes[i]);
		}
*/
		
		setMode(state, Mode.IMPORT_CITATIONS);
	} // end doImport()
	
	/**
	*
	*/
	public void doCreate ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		setMode(state, Mode.CREATE);
		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);

	}	// doCreate

	/**
	 * Pushes a non-ignorable Mode onto the back-button stack within the given
	 * state object. Checks toPush against ignoreModes Set.
	 *
	 * @param state this session's state with non-null back-button stack
	 * @param toPush Mode to push on the stack
	 */
	protected void pushMode( SessionState state, Mode toPush )
	{
		// get stack from state
		Stack<Mode> stack = ( Stack<Mode> ) state.getAttribute(STATE_BACK_BUTTON_STACK);

		// (assuming stack is not null)
		// push only if toPush is not in ignoreModes and is not at the top of
		// the stack (a Mode should not be adjacent to itself)
		if( ignoreModes.contains( toPush ) )
		{
			return;
		}

		if( stack.size() > 0 && stack.peek().equals( toPush ) )
		{
			return;
		}

		stack.push( toPush );
		logger.debug( "pushMode pushed " + toPush );

		// put this stack back into state
		state.setAttribute( STATE_BACK_BUTTON_STACK, stack );
	}

	/**
     * @param state
     * @param new_mode
     */
    protected void setMode(SessionState state, Mode new_mode)
    {
	    Stack<Mode> stack = ( Stack<Mode> ) state.getAttribute(STATE_BACK_BUTTON_STACK);
	    if( stack == null )
	    {
	    	/* first time setMode has been called */

	    	// create a new Stack
	    	stack = new Stack<Mode>();

	    	// add to state
	    	state.setAttribute( STATE_BACK_BUTTON_STACK, stack );

	    	// add the previous mode (if any) to the stack
	    	Mode previous_mode = ( Mode ) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
	    	if( previous_mode != null )
	    	{
	    		pushMode( state, previous_mode );
	    	}
	    }

	    // push newly selected Mode on the stack
	    pushMode( state, new_mode );

	    // set state attributes
	    state.setAttribute( CitationHelper.STATE_HELPER_MODE, new_mode );
    	state.setAttribute( STATE_BACK_BUTTON_STACK, stack );
    }

	/**
	*
	*/
	public void doCreateCitation ( RunData data)
	{
		// get the state object and the parameter parser
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		Set validPropertyNames = CitationService.getValidPropertyNames();
		String mediatype = params.getString("type");

		CitationCollection collection = getCitationCollection(state, true);

		// create a citation
		Citation citation = CitationService.addCitation(mediatype);

		updateCitationFromParams(citation, params);

		// add citation to current collection
		collection.add(citation);
		CitationService.save(collection);

		// call buildListPanelContext to show updated list
		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);
		setMode(state, Mode.LIST);

	}	// doCreateCitation

	/**
	 * @param citation
	 * @param params
	 */
	protected void updateCitationFromParams(Citation citation, ParameterParser params)
	{
		Schema schema = citation.getSchema();


        List fields = schema.getFields();
        Iterator fieldIt = fields.iterator();
        while(fieldIt.hasNext())
        {
        	Field field = (Field) fieldIt.next();
        	String name = field.getIdentifier();
			if(field.isMultivalued())
			{
				List values = new Vector();

				String count = params.getString(name + "_count");
				int num = 10;
				try
				{
					num = Integer.parseInt(count);
					for(int i = 0; i < num; i++)
					{
						String value = params.getString(name + i);
						if(value != null && !values.contains(value))
						{
							values.add(value);
						}
					}
					citation.updateCitationProperty(name, values);
				}
				catch(NumberFormatException e)
				{

				}
			}
			else
			{
				String value = params.getString(name);
				citation.setCitationProperty(name, value);
				if(name.equals(Schema.TITLE))
				{
					citation.setDisplayName(value);
				}
			}
        }

        int urlCount = 0;
        try
        {
        	urlCount = params.getInt("url_count");
        }
        catch(Exception e)
        {
        	logger.debug("doCreateCitation: unable to parse int for urlCount");
        }

        for(int i = 0; i < urlCount; i++)
        {
        	String label = params.getString("label_" + i);

        	String url = params.getString("url_" + i);

        	String urlid = params.getString("urlid_" + i);

         	if(url == null)
        	{
        		logger.debug("doCreateCitation: url null? " + url);
        	}
        	else
        	{
	            try
	            {
	            	url = validateURL(url);
	            }
	            catch (MalformedURLException e)
	            {
		            logger.debug("doCreateCitation: unable to validate URL: " + url);
		            continue;
	            }
        	}

        	if(label == null || url == null)
        	{
        		logger.debug("doCreateCitation: label null? " + label + " url null? " + url);
        		continue;
        	}
        	else if(urlid == null || urlid.trim().equals(""))
        	{
        		citation.addCustomUrl(label, url);
        	}
        	else
        	{
        		citation.updateCustomUrl(urlid, label, url);
        	}
        }
	}

	/**
	*
	*/
	public void doEdit ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		String citationId = params.getString("citationId");
		String collectionId = params.getString("collectionId");


		CitationCollection collection = getCitationCollection(state, true);

		Citation citation = null;
		try
        {
	        citation = collection.getCitation(citationId);
        }
        catch (IdUnusedException e)
        {
	        // add an alert (below)
        }

        if(citation == null)
        {
        	addAlert(state, rb.getString("alert.access"));
        }
        else
        {
	        state.setAttribute(CitationHelper.CITATION_EDIT_ID, citationId);
	        state.setAttribute(CitationHelper.CITATION_EDIT_ITEM, citation);
	        setMode(state, Mode.EDIT);
        }

	}	// doEdit

	/**
	*
	*/
	public void doList ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		setMode(state, Mode.LIST);

	}	// doList

	public void doResults( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		setMode(state, Mode.RESULTS);
	}

	/**
	*
	*/
	public void doAddCitations ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		setMode(state, Mode.ADD_CITATIONS);

	}	// doAddCitations

	public void doMessageFrame(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// get params
		String citationId = params.getString("citationId");
		String collectionId = params.getString("collectionId");
		String operation = params.getString("operation");

		// check params
		if( operation == null )
		{
			logger.warn( "doMessageFrame() 'operation' null argument" );
			setMode(state, Mode.ERROR);
			return;
		}

		if( operation.trim().equalsIgnoreCase( "refreshCount" ) )
		{
			// do not need to do anything, let buildMessagePanelContext update
			// count for citations
			setMode( state, Mode.MESSAGE );
			return;
		}

		if( operation == null || citationId == null || collectionId == null )
		{
			logger.warn( "doMessageFrame() null argument - operation: " +
					operation + ", citationId: " + citationId + ", " +
							"collectionId: " + collectionId );
			setMode(state, Mode.ERROR);
			return;
		}

		// get Citation using citationId
		List<Citation> currentResults = (List<Citation>) state.getAttribute(STATE_CURRENT_RESULTS);
		Citation citation = null;
		for( Citation c : currentResults )
		{
			if( c.getId().equals( citationId ) )
			{
				citation = c;
				break;
			}
		}

		if( citation == null ) {
			logger.warn( "doMessageFrame() bad citationId: " + citationId );
			setMode(state, Mode.ERROR);
			return;
		}

		// get CitationCollection using collectionId
		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null)
		{
			logger.warn( "doMessageFrame() unable to access citationCollection " + collectionId );
		}

		// do operation
		if(operation.equalsIgnoreCase("add"))
		{
			logger.debug("adding citation " + citationId + " to " + collectionId);
			citation.setAdded( true );
			collection.add( citation );
			CitationService.save(collection);
		}
		else if(operation.equalsIgnoreCase("remove"))
		{
			logger.debug("removing citation " + citationId + " from " + collectionId);
			collection.remove( citation );
			citation.setAdded( false );
			CitationService.save(collection);
		}
		else
		{
			// do nothing
			logger.debug("null operation: " + operation);
		}

		// store the citation's new id to send back to UI
		state.setAttribute( STATE_CITATION_ID, citation.getId() );

		setMode(state, Mode.MESSAGE);
	}

	public void doRemoveAllCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		String collectionId = params.getString("collectionId");

		CitationCollection collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("CitationHelperAction.doRemoveCitation collection null: " + collectionId);
		}
		else
		{
			// remove all citations
			List<Citation> citations = collection.getCitations();

			if( citations != null && citations.size() > 0 )
			{
				for( Citation citation : citations )
				{
					collection.remove( citation );
				}
				CitationService.save(collection);
			}
		}

		setMode(state, Mode.LIST);

	}  // doRemoveAllCitations

	public void doRemoveSelectedCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		String collectionId = params.getString("collectionId");

		CitationCollection collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("doRemoveSelectedCitation() collection null: " + collectionId);
		}
		else
		{
			// remove selected citations
			String[] paramCitationIds = params.getStrings("citationId");
			if( paramCitationIds != null && paramCitationIds.length > 0 )
			{
				List<String> citationIds = new java.util.ArrayList<String>();
				citationIds.addAll(Arrays.asList(paramCitationIds));

				try
				{
					for( String citationId : citationIds )
					{
						Citation citation = collection.getCitation(citationId);
						collection.remove(citation);
					}
					CitationService.save(collection);
				}
				catch( IdUnusedException e )
				{
					logger.warn("doRemoveSelectedCitation() unable to get and remove citation", e );
				}
			}
		}

		state.setAttribute( STATE_LIST_NO_SCROLL, Boolean.TRUE );
		setMode(state, Mode.LIST);

	}  // doRemoveSelectedCitations

	/**
	*
	*/
	public void doReviseCitation (RunData data)
	{
		// get the state object and the parameter parser
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// Set validPropertyNames = CitationService.getValidPropertyNames();
		// String mediatype = params.getString("type");

		CitationCollection collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
		}
		else
		{
			String citationId = (String) state.getAttribute(CitationHelper.CITATION_EDIT_ID);
			if(citationId != null)
			{
				try
	            {
					Citation citation = collection.getCitation(citationId);
	
		            String schemaId = params.getString("type");
		            Schema schema = CitationService.getSchema(schemaId);
		            citation.setSchema(schema);
	
		    		updateCitationFromParams(citation, params);
	
		       		// add citation to current collection
		    		collection.saveCitation(citation);
		        }
	            catch (IdUnusedException e)
	            {
		            // TODO add alert and log error
	            }
	
	       		CitationService.save(collection);
			}
 		}

		setMode(state, Mode.LIST);

	}	// doReviseCitation

	/**
	 *
	 * @param data
	 */
	public void doCancelSearch( RunData data )
	{
		// get state and params
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel the running search
		ActiveSearch search = ( ActiveSearch )state.getAttribute( STATE_SEARCH_INFO );
		if( search != null )
		{
			Thread searchThread = search.getSearchThread();
			if( searchThread != null )
			{
				try
				{
					searchThread.interrupt();
				}
				catch( SecurityException se )
				{
					// not able to interrupt search
					logger.warn( "doSearch() [in ThreadGroup "
							+ Thread.currentThread().getThreadGroup().getName()
							+ "] unable to interrupt search Thread [name="
							+ searchThread.getName()
							+ ", id=" + searchThread.getId()
							+ ", group=" + searchThread.getThreadGroup().getName()
							+ "]");
				}
			}
		}

		// default return to search page
		setMode(state, Mode.SEARCH);

	}  // doCancelSearch

	/**
	*
	*/
	public void doSearch ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// remove attributes from an old search session, if any
		state.removeAttribute( STATE_SEARCH_RESULTS );
		state.removeAttribute( STATE_CURRENT_RESULTS );
		state.removeAttribute( STATE_KEYWORDS );

		// indicate a basic search
		state.setAttribute( STATE_BASIC_SEARCH, new Object() );

		try
	    {
	        SearchDatabaseHierarchy hierarchy = SearchManager.getSearchHierarchy();
	        if(hierarchy == null)
	        {
		        addAlert(state, rb.getString("search.problem"));
				setMode(state, Mode.ERROR);
				return;
	        }

	        state.setAttribute(STATE_SEARCH_HIERARCHY, hierarchy);

			setMode(state, Mode.SEARCH);
	    }
	    catch (SearchException e)
	    {
	        // addAlert(state, rb.getString("search.problem"));
	        addAlert(state, e.getMessage());
			setMode(state, Mode.ERROR);
	    }
	}	// doSearch

	/**
	 *
	 */
	public void doBeginSearch ( RunData data)
	{
		// get state and params
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// get search object from state
		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_INFO);
		if(search == null)
		{
			logger.debug( "doBeginSearch() got null ActiveSearch from state." );
			search = SearchManager.newSearch();
		}

		// get databases selected
		String[] databaseIds = params.getStrings( "databasesSelected" );
		logger.debug( "Databases selected:" );
		for( String databaseId : databaseIds )
		{
			logger.debug( "  " + databaseId );
		}

		// check the databases to make sure they are indeed searchable by this user
		if( databaseIds != null )
		{
			SearchDatabaseHierarchy hierarchy =
				(SearchDatabaseHierarchy)state.getAttribute(STATE_SEARCH_HIERARCHY);
			for( int i = 0; i < databaseIds.length; i++ )
			{
				if( !hierarchy.isSearchableDatabase( databaseIds[ i ] ) )
				{
					// TODO collect a list of the databases which are
					// not searchable and pass them to the UI

					// do not search if databases selected
					// are not searchable by this user
					state.setAttribute( STATE_UNAUTHORIZED_DB, Boolean.TRUE );
					logger.warn( "doBeginSearch() unauthorized database: " + databaseIds[i] );
					setMode(state, Mode.RESULTS);
					return;
				}
			}
      /*
       * Specify which databases should be searched
       */
			search.setDatabaseIds(databaseIds);
			state.setAttribute( STATE_CURRENT_DATABASES, databaseIds );
		}
		else
		{
			// no databases selected, cannot continue
			state.setAttribute( STATE_NO_DATABASES, Boolean.TRUE );
			setMode(state, Mode.RESULTS);
			return;
		}

		/*
		 *  do basic/advanced search-specific processing
		 */
		// determine which type of search has been issued
		String searchType = params.getString( "searchType" );
		if( searchType != null && searchType.equalsIgnoreCase( ActiveSearch.ADVANCED_SEARCH_TYPE ) )
		{
			doAdvancedSearch( params, state, search );
		}
		else
		{
			doBasicSearch( params, state, search );
		}

		/*
		 * BEGIN SEARCH
		 */
		try
		{
			// set search thread to the current thread
			search.setSearchThread( Thread.currentThread() );
			state.setAttribute( STATE_SEARCH_INFO, search );

			// initiate the search
	        List latestResults = search.viewPage();
	        String msg = search.getStatusMessage();
	        if(msg != null)
	        {
	        	addAlert(state, msg);
	        	search.setStatusMessage();
	        }

	        if( latestResults != null )
	        {
	        	state.setAttribute(STATE_SEARCH_RESULTS, search);
	        	state.setAttribute(STATE_CURRENT_RESULTS, latestResults);
		        setMode(state, Mode.RESULTS);
	        }
	        else
	        {
	        	// the search has been canceled - determine which page should
	        	// be reloaded
	    		String cancel = params.getString( "cancelOp" );
	        	if( cancel != null && !cancel.trim().equals("") )
	        	{
	        		if( cancel.equalsIgnoreCase( ELEMENT_ID_RESULTS_FORM ) )
	        		{
	        			setMode( state, Mode.RESULTS );
	        		}
	        		else
	        		{
	        			setMode( state, Mode.SEARCH );
	        		}
	        	}
	        }
	    }
	    catch(Exception e)
	    {
	    	logger.warn("doBeginSearch() caught Exception", e);
	    	state.setAttribute( STATE_NO_RESULTS, Boolean.TRUE );
			setMode(state, Mode.RESULTS);
			return;
	    }

	    ActiveSearch newSearch = SearchManager.newSearch();
		state.setAttribute( STATE_SEARCH_INFO, newSearch );

	}	// doBeginSearch

	/**
	 * Sets up a basic search.
	 *
	 * @param params request parameters from doBeginSearch
	 * @param state  session state
	 * @param search current search
	 */
	protected void doBasicSearch( ParameterParser params, SessionState state, ActiveSearch search )
	{
		// signal a basic search
		state.setAttribute( STATE_BASIC_SEARCH, new Object() );
		search.setSearchType( ActiveSearch.BASIC_SEARCH_TYPE );

		// get keywords
		String keywords = params.getString("keywords");
		if(keywords == null || keywords.trim().equals(""))
		{
			logger.warn( "doBasicSearch() getting null/empty keywords" );
		}

		// set up search query
		SearchQuery basicQuery = new org.sakaiproject.citation.util.impl.SearchQuery();
		basicQuery.addKeywords( keywords );

		// set query for this search
		search.setBasicQuery( basicQuery );

		// save state
		state.setAttribute( STATE_KEYWORDS, keywords );

	}  // doBasicSearch

	/**
	 * Sets up an advanced search.
	 *
	 * @param params request parameters from doBeginSearch
	 * @param state  session state
	 * @param search current search
	 */
	protected void doAdvancedSearch( ParameterParser params, SessionState state, ActiveSearch search )
	{
		// signal an advanced search
		state.removeAttribute( STATE_BASIC_SEARCH );
		search.setSearchType( ActiveSearch.ADVANCED_SEARCH_TYPE );

		// clear old state
		AdvancedSearchHelper.clearAdvancedFormState( state );

		// set selected fields
		AdvancedSearchHelper.setFieldSelections( params, state );

		// set entered criteria
		AdvancedSearchHelper.setFieldCriteria( params, state );

		// get a Map of advancedCritera for the search
		search.setAdvancedQuery( AdvancedSearchHelper.getAdvancedCriteria( state ) );
	}

	/**
	*
	*/
	public void doNextListPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);
			listIterator = collection.iterator();
			state.setAttribute(STATE_LIST_ITERATOR, listIterator);
		}
		if(listIterator.hasNextPage())
		{
			listIterator.nextPage();
		}

 	}	// doNextListPage

	/**
	*
	*/
	public void doPrevListPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);
			listIterator = collection.iterator();
			state.setAttribute(STATE_LIST_ITERATOR, listIterator);
		}
		if(listIterator.hasPreviousPage())
		{
			listIterator.previousPage();
		}

 	}	// doSearch

	/**
	*
	*/
	public void doLastListPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		CitationCollection collection = getCitationCollection(state, true);

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			listIterator = collection.iterator();
			state.setAttribute(STATE_LIST_ITERATOR, listIterator);
		}

		int pageSize = listIterator.getPageSize();
		int totalSize = collection.size();
		int lastPage = 0;
		if(totalSize > 0)
		{
			lastPage = (totalSize - 1) / pageSize;
		}
		listIterator.setPage(lastPage);

 	}	// doSearch

	/**
	*
	*/
	public void doFirstListPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);

			listIterator = collection.iterator();
			state.setAttribute(STATE_LIST_ITERATOR, listIterator);
		}

		listIterator.setPage(0);

 	}	// doSearch

	/**
	*
	*/
	public void doNextSearchPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = SearchManager.newSearch();
		}
		// search.prepareForNextPage();

		try
        {
	        List latestResults = search.viewPage(search.getViewPageNumber() + 1);
	        String msg = search.getStatusMessage();
	        if(msg != null)
	        {
	        	addAlert(state, msg);
	        	search.setStatusMessage();
	        }
	        state.setAttribute(STATE_CURRENT_RESULTS, latestResults);
			setMode(state, Mode.RESULTS);
        }
        catch (SearchException e)
        {
        	logger.warn("doNextSearchPage: " + e.getMessage());
        	addAlert(state, rb.getString( "error.search" ) );
			setMode(state, Mode.RESULTS);
        }
        catch(Exception e)
        {
        	logger.warn("doNextSearchPage: " + e.getMessage());
        }

 	}	// doSearch

	/**
	*
	*/
	public void doPrevSearchPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = SearchManager.newSearch();
		}
		// search.prepareForNextPage();

		try
        {
	        List latestResults = search.viewPage(search.getViewPageNumber() - 1);
	        String msg = search.getStatusMessage();
	        if(msg != null)
	        {
	        	addAlert(state, msg);
	        	search.setStatusMessage();
	        }
	        state.setAttribute(STATE_CURRENT_RESULTS, latestResults);
			setMode(state, Mode.RESULTS);
        }
        catch (SearchException e)
        {
        	logger.warn("doPrevSearchPage: " + e.getMessage());
        	addAlert(state, rb.getString( "error.search" ) );
			setMode(state, Mode.RESULTS);
        }
        catch(Exception e)
        {
        	logger.warn("doPrevSearchPage: " + e.getMessage());
        }

 	}	// doSearch

	/**
	*
	*/
	public void doFirstSearchPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// ParameterParser params = data.getParameters();

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = SearchManager.newSearch();
		}
		// search.prepareForNextPage();

		try
        {
	        List latestResults = search.viewPage(0);
	        String msg = search.getStatusMessage();
	        if(msg != null)
	        {
	        	addAlert(state, msg);
	        	search.setStatusMessage();
	        }
	        state.setAttribute(STATE_CURRENT_RESULTS, latestResults);
			setMode(state, Mode.RESULTS);
        }
        catch (SearchException e)
        {
        	logger.warn("doFirstSearchPage: " + e.getMessage());
        	addAlert(state, rb.getString( "error.search" ) );
			setMode(state, Mode.RESULTS);
        }
        catch(Exception e)
        {
        	logger.warn("doFirstSearchPage: " + e.getMessage());
        }

 	}	// doSearch

	/**
	*
	*/
	public void doChangeSearchPageSize ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = SearchManager.newSearch();
			state.setAttribute(STATE_SEARCH_RESULTS, search);
		}
		// search.prepareForNextPage();

		// check for top or bottom page selector
		String pageSelector = params.get( "pageSelector" );
		int pageSize;
		if( pageSelector.equals( "top" ) )
		{
			pageSize = params.getInt( "pageSizeTop" );
		}
		else
		{
			pageSize = params.getInt("pageSizeBottom");
		}

		if(pageSize > 0)
		{
			// use the new value
		}
		else
		{
			// use the old value
			pageSize = search.getViewPageSize();
		}

		state.setAttribute(STATE_RESULTS_PAGE_SIZE, new Integer(pageSize));

		try
        {
			int last = search.getLastRecordIndex();
			int page = (last - 1)/pageSize;

			search.setViewPageSize(pageSize);
	        List latestResults = search.viewPage(page);
	        String msg = search.getStatusMessage();
	        if(msg != null)
	        {
	        	addAlert(state, msg);
	        	search.setStatusMessage();
	        }
	        state.setAttribute(STATE_CURRENT_RESULTS, latestResults);
			setMode(state, Mode.RESULTS);
        }
        catch (SearchException e)
        {
        	logger.warn("doChangeSearchPageSize: " + e.getMessage());
        	addAlert(state, rb.getString( "error.search" ) );
			setMode(state, Mode.RESULTS);
        }
        catch(Exception e)
        {
        	logger.warn("doChangeSearchPageSize: " + e.getMessage());
        }

 	}	// doChangeSearchPageSize

	/**
	*
	*/
	public void doChangeListPageSize ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// check for top or bottom page selector
		String pageSelector = params.get( "pageSelector" );
		int pageSize;
		if( pageSelector.equals( "top" ) )
		{
			pageSize = params.getInt( "pageSizeTop" );
		}
		else
		{
			pageSize = params.getInt("pageSizeBottom");
		}

		if(pageSize > 0)
		{
			state.setAttribute(STATE_LIST_PAGE_SIZE, new Integer(pageSize));
		}

 	}	// doSearch

	/**
	*
	*/
	public void doView ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		String citationId = params.getString("citationId");
		String collectionId = params.getString("collectionId");

		CitationCollection collection = getCitationCollection(state, false);
		
		if(collection == null)
		{
			addAlert(state, rb.getString("alert.access"));
		}
		else
		{
			Citation citation = null;
			try
	        {
		        citation = collection.getCitation(citationId);
	        }
	        catch (IdUnusedException e)
	        {
		        // add an alert (below)
	        }

	        if(citation == null)
	        {
	        	addAlert(state, rb.getString("alert.access"));
	        }
	        else
	        {
		        state.setAttribute(CitationHelper.CITATION_VIEW_ID, citationId);
		        state.setAttribute(CitationHelper.CITATION_VIEW_ITEM, citation);
				setMode(state, Mode.VIEW);
	        }
		}

	}	// doView



	protected void initHelper(VelocityPortlet portlet, Context context,
	        RunData rundata, SessionState state)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		// TODO: if not enterring as a helper, we will need to create pipe???

		if(pipe.isActionCompleted())
		{
			return;
		}

		Mode mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null)
		{
			switch(pipe.getAction().getActionType())
			{
				case CREATE:
					ContentResource tempResource = createTemporaryResource(pipe);
					state.setAttribute(CitationHelper.RESOURCE_ID, tempResource.getId());

					String displayName = tempResource.getProperties().getProperty( org.sakaiproject.entity.api.ResourceProperties.PROP_DISPLAY_NAME );
					state.setAttribute( STATE_COLLECTION_TITLE , displayName );

					try
	                {
	                    state.setAttribute(STATE_COLLECTION_ID, new String(tempResource.getContent()));
	                }
	                catch (ServerOverloadException e)
	                {
	                    logger.warn("ServerOverloadException ", e);
	                }
	                state.setAttribute( STATE_RESOURCES_ADD, Boolean.TRUE );
					setMode(state, Mode.ADD_CITATIONS);
					break;
				case REVISE_CONTENT:
					state.setAttribute(CitationHelper.RESOURCE_ID, pipe.getContentEntity().getId());
					try
	                {
	                    state.setAttribute(STATE_COLLECTION_ID, new String(((ContentResource) pipe.getContentEntity()).getContent()));
	                }
	                catch (ServerOverloadException e)
	                {
	                    logger.warn("ServerOverloadException ", e);
	                }
	                state.removeAttribute( STATE_RESOURCES_ADD );
					setMode(state, Mode.LIST);
					break;
				default:
					break;
			}
		}

		if(state.getAttribute(STATE_RESULTS_PAGE_SIZE) == null)
		{
			state.setAttribute(STATE_RESULTS_PAGE_SIZE, DEFAULT_RESULTS_PAGE_SIZE);
		}

		if(state.getAttribute(STATE_LIST_PAGE_SIZE) == null)
		{
			state.setAttribute(STATE_LIST_PAGE_SIZE, DEFAULT_LIST_PAGE_SIZE);
		}

	}	// initHelper

	/**
     *
     * @param pipe
     * @return
     */
    protected ContentResource createTemporaryResource(ResourceToolActionPipe pipe)
    {
        try
        {
			ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
			ContentResourceEdit newItem = contentService.addResource(pipe.getContentEntity().getId(), "New Citation List", null, ContentHostingService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			newItem.setResourceType(CitationService.CITATION_LIST_ID);
			newItem.setContentType( ResourceType.MIME_TYPE_HTML );
			//newItem.setHidden();

			ResourcePropertiesEdit props = newItem.getPropertiesEdit();

			// set the alternative_reference to point to reference_root for CitationService
			props.addProperty(contentService.PROP_ALTERNATE_REFERENCE, org.sakaiproject.citation.api.CitationService.REFERENCE_ROOT);
			props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, ResourceType.MIME_TYPE_HTML);
			props.addProperty(CitationService.PROP_TEMPORARY_CITATION_LIST, Boolean.TRUE.toString());

			CitationCollection collection = CitationService.addCollection();
			newItem.setContent(collection.getId().getBytes());
			newItem.setContentType(ResourceType.MIME_TYPE_HTML);

			contentService.commitResource(newItem, NotificationService.NOTI_NONE);

			return newItem;
        }
        catch (PermissionException e)
        {
            // TODO Auto-generated catch block
            logger.warn("PermissionException ", e);
        }
        catch (IdUniquenessException e)
        {
            // TODO Auto-generated catch block
            logger.warn("IdUniquenessException ", e);
        }
        catch (IdLengthException e)
        {
            // TODO Auto-generated catch block
            logger.warn("IdLengthException ", e);
        }
        catch (IdInvalidException e)
        {
            // TODO Auto-generated catch block
            logger.warn("IdInvalidException ", e);
        }
        catch (IdUnusedException e)
        {
            // TODO Auto-generated catch block
            logger.warn("IdUnusedException ", e);
        }
        catch (OverQuotaException e)
        {
            // TODO Auto-generated catch block
            logger.warn("OverQuotaException ", e);
        }
        catch (ServerOverloadException e)
        {
            // TODO Auto-generated catch block
            logger.warn("ServerOverloadException ", e);
        }

 	    return null;
    }

	/**
	 * Remove the state variables used internally, on the way out.
	 */
	private void cleanupState(SessionState state)
	{
		state.removeAttribute(CitationHelper.STATE_HELPER_MODE);
		state.removeAttribute(STATE_BACK_BUTTON_STACK);
		// state.removeAttribute(VelocityPortletPaneledAction.STATE_HELPER);



		// re-enable observers
		VelocityPortletPaneledAction.enableObservers(state);

	}	// cleanupState

	protected String validateURL(String url) throws MalformedURLException
	{
		if (url == null || url.trim().equals (""))
		{
			throw new MalformedURLException();
		}

		url = url.trim();

		if (url.indexOf ("://") == -1)
		{
			// if it's missing the transport, add http://
			url = "http://" + url;
		}

		// valid protocol?
		try
		{
			// test to see if the input validates as a URL.
			// Checks string for format only.
			URL u = new URL(url);
		}
		catch (MalformedURLException e1)
		{
			try
			{
				Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
				Matcher matcher = pattern.matcher(url);
				if(matcher.matches())
				{
					// if URL has "unknown" protocol, check remaider with
					// "http" protocol and accept input it that validates.
					URL test = new URL("http://" + matcher.group(2));
				}
				else
				{
					throw e1;
				}
			}
			catch (MalformedURLException e2)
			{
				throw e1;
			}
		}
		return url;
	}

	public static class QuotedTextValidator
	{

		/**
		 * Return a string for insertion in a quote in an HTML tag (as the value of an element's attribute.
		 *
		 * @param string
		 *        The string to escape.
		 * @return the escaped string.
		 */
		public static String escapeQuotedString(String string)
		{
			if (string == null) return "";
			string = string.trim();
			try
			{
				// convert the string to bytes in UTF-8
				byte[] bytes = string.getBytes("UTF-8");

				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < bytes.length; i++)
				{
					byte b = bytes[i];
					if (b == '"')
					{
						buf.append("\\\"");
					}
					else if(b == '\\')
					{
						buf.append("\\\\");
					}
					else
					{
						buf.append((char) b);
					}
				}

				String rv = buf.toString();
				return rv;
			}
			catch (Exception e)
			{
				return string;
			}

		} // escapeQuotedString
	}

	/**
	 * Iterate over attributes in ToolSession and remove all attributes starting with a particular prefix.
	 * @param toolSession
	 * @param prefix
	 */
	protected void cleanup(ToolSession toolSession, String prefix)
	{
		Enumeration attributeNames = toolSession.getAttributeNames();
		while(attributeNames.hasMoreElements())
		{
			String aName = (String) attributeNames.nextElement();
			if(aName.startsWith(prefix))
			{
				toolSession.removeAttribute(aName);
			}
		}

	}
	
	public void doSortAllCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		// collecitonId holds the srting method (reused HTML hidden variable)
		String collectionId = params.getString("collectionId");

		CitationCollection collection = null;

		if(collectionId == null)
		{
			collectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
		}

        logger.debug("doSortCitations() sort type  = " + collectionId);

        collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("doSortCitations() collection null: " + collectionId);
		}
		else
		{
			// sort the citation list
			
	        logger.debug("doSortCitations() ready to sort");
	        
	        if (collectionId.equalsIgnoreCase(CitationCollection.SORT_BY_AUTHOR))
			  collection.setSort(CitationCollection.SORT_BY_AUTHOR, true);
	        else
		        if (collectionId.equalsIgnoreCase(CitationCollection.SORT_BY_DATE))
					  collection.setSort(CitationCollection.SORT_BY_DATE, true);
	        	
			
			Iterator iter = collection.iterator();
			
			while (iter.hasNext())
			{
				Citation tempCit = (Citation) iter.next();
				
				logger.debug("doSortCitaitons() tempcit 1 -------------");
				logger.debug("doSortCitaitons() tempcit 1 (author) = " + tempCit.getFirstAuthor());
				
		        logger.debug("doSortCitations() tempcit 1 = " + tempCit.getDisplayName());				
			} // end while
		} // end else
		setMode(state, Mode.LIST);

	}  // doSortCitations

	public class CitationListSecurityAdviser implements SecurityAdvisor
	{
		String userId;
		String function;
		String reference;

		public CitationListSecurityAdviser(String userId, String function, String reference)
        {
	        super();
	        this.userId = userId;
	        this.function = function;
	        this.reference = reference;
        }

		public SecurityAdvice isAllowed(String userId, String function, String reference)
        {
			SecurityAdvice advice = SecurityAdvice.PASS;
			if((this.userId == null || this.userId.equals(userId)) && (this.function == null || this.function.equals(function)) || (this.reference == null || this.reference.equals(reference)))
			{
				advice = SecurityAdvice.ALLOWED;
			}
			return advice;
        }

	}

}	// class CitationHelperAction
