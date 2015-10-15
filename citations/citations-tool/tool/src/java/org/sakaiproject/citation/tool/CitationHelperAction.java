/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
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
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.citation.api.SearchCategory;
import org.sakaiproject.citation.api.SearchDatabaseHierarchy;
import org.sakaiproject.citation.api.SearchManager;
import org.sakaiproject.citation.util.api.SearchCancelException;
import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.citation.util.api.SearchQuery;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

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
	
	/** Shared messages */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.sharedI18n.SharedProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.sharedI18n.bundle.shared";
	private static final String RESOURCECLASS = "resource.class.shared";
	private static final String RESOURCEBUNDLE = "resource.bundle.shared";
	
	private ResourceLoader srb;
	
	protected CitationService citationService;
	protected ConfigurationService configurationService;
	protected ServerConfigurationService scs;
	protected SearchManager searchManager;

	protected ContentHostingService contentService;
	protected EntityManager entityManager;
	protected SessionManager sessionManager;

	protected static FormattedText formattedText;
	protected static ToolManager toolManager;

	public static final Integer DEFAULT_RESULTS_PAGE_SIZE = new Integer(10);
	public static final Integer DEFAULT_LIST_PAGE_SIZE = new Integer(50);
	
	public static Integer defaultListPageSize = DEFAULT_LIST_PAGE_SIZE;

	protected static final String ELEMENT_ID_CREATE_FORM = "createForm";
	protected static final String ELEMENT_ID_EDIT_FORM = "editForm";
	protected static final String ELEMENT_ID_LIST_FORM = "listForm";
	protected static final String ELEMENT_ID_SEARCH_FORM = "searchForm";
	protected static final String ELEMENT_ID_RESULTS_FORM = "resultsForm";
	protected static final String ELEMENT_ID_VIEW_FORM = "viewForm";

	/**
	 * The calling application reflects the nature of our caller
	 */
	public final static String CITATIONS_HELPER_CALLER = "citations_helper_caller";

	public enum Caller
	{
		RESOURCE_TOOL,
		EDITOR_INTEGRATION;
	}

	/**
	 * Mode defines a complete set of values describing the user's navigation intentions
	 */
	public enum Mode
	{
		NEW_RESOURCE,
		DATABASE,
		CREATE,
		EDIT,
		ERROR,
		ERROR_FATAL,
		LIST,
		REORDER,
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
	protected static final String STATE_CANCEL_PAGE = CitationHelper.CITATION_PREFIX + "cancel_page";
	protected static final String STATE_CITATION_COLLECTION_ID = CitationHelper.CITATION_PREFIX + "citation_collection_id";
	protected static final String STATE_CITATION_COLLECTION = CitationHelper.CITATION_PREFIX + "citation_collection";
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
	protected static final String STATE_RESOURCE_ENTITY_PROPERTIES = CitationHelper.CITATION_PREFIX + "citationList_properties";
	protected static final String STATE_SORT = CitationHelper.CITATION_PREFIX + "sort";

	protected static final String TEMPLATE_NEW_RESOURCE = "citation/new_resource";
	protected static final String TEMPLATE_CREATE = "citation/create";
	protected static final String TEMPLATE_EDIT = "citation/edit";
	protected static final String TEMPLATE_ERROR = "citation/error";
	protected static final String TEMPLATE_ERROR_FATAL = "citation/error_fatal";
	protected static final String TEMPLATE_LIST = "citation/list";
	protected static final String TEMPLATE_REORDER = "citation/reorder";
	protected static final String TEMPLATE_ADD_CITATIONS = "citation/add_citations";
	protected static final String TEMPLATE_IMPORT_CITATIONS = "citation/import_citations";
	protected static final String TEMPLATE_MESSAGE = "citation/_message";
	protected static final String TEMPLATE_SEARCH = "citation/search";
	protected static final String TEMPLATE_RESULTS = "citation/results";
	protected static final String TEMPLATE_VIEW = "citation/view";
	protected static final String TEMPLATE_DATABASE = "citation/_databases";
	
	protected static final String PROP_ACCESS_MODE = "accessMode";
	protected static final String PROP_IS_COLLECTION = "isCollection";
	protected static final String PROP_IS_DROPBOX = "isDropbox";
	protected static final String PROP_IS_GROUP_INHERITED = "isGroupInherited";
	protected static final String PROP_IS_GROUP_POSSIBLE = "isGroupPossible";
	protected static final String PROP_IS_HIDDEN = "isHidden";
	protected static final String PROP_IS_PUBVIEW = "isPubview";
	protected static final String PROP_IS_PUBVIEW_INHERITED = "isPubviewInherited";
	protected static final String PROP_IS_PUBVIEW_POSSIBLE = "isPubviewPossible";
	protected static final String PROP_IS_SINGLE_GROUP_INHERITED = "isSingleGroupInherited";
	protected static final String PROP_IS_SITE_COLLECTION = "isSiteCollection";
	protected static final String PROP_IS_SITE_ONLY = "isSiteOnly";
	protected static final String PROP_IS_USER_SITE = "isUserSite";
	protected static final String PROP_POSSIBLE_GROUPS = "possibleGroups";
	protected static final String PROP_RELEASE_DATE = "releaseDate";
	protected static final String PROP_RELEASE_DATE_STR = "releaseDateStr";
	protected static final String PROP_RETRACT_DATE = "retractDate";
	protected static final String PROP_RETRACT_DATE_STR = "retractDateStr";
	protected static final String PROP_USE_RELEASE_DATE = "useReleaseDate";
	protected static final String PROP_USE_RETRACT_DATE = "useRetractDate";

	public static final String CITATION_ACTION = "citation_action";
	public static final String UPDATE_RESOURCE = "update_resource";
	public static final String CREATE_RESOURCE = "create_resource";
	public static final String IMPORT_CITATIONS = "import_citations";
	public static final String UPDATE_SAVED_SORT = "update_saved_sort";
	public static final String CHECK_FOR_UPDATES = "check_for_updates";

	public static final String MIMETYPE_JSON = "application/json";
	public static final String MIMETYPE_HTML = "text/html";
	public static final String REQUESTED_MIMETYPE = "requested_mimetype";

	public static final String CHARSET_UTF8 = "UTF-8";

	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	public static final long ONE_DAY = 24L * 60L * 60L * 1000L;
	
	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	public static final long ONE_WEEK = 7L * ONE_DAY;


	public void init() throws ServletException {
		scs
			= (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		
		String resourceClass = scs.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		String resourceBundle = scs.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
		srb = new Resource().getLoader(resourceClass, resourceBundle);		
		
		if(scs != null) {
			defaultListPageSize = scs.getInt("citations.default.list.page.size", DEFAULT_LIST_PAGE_SIZE);
		} else {
			logger.warn("Failed to get default list page size as ServerConfigurationService is null. Defaulting to " + DEFAULT_LIST_PAGE_SIZE);
			defaultListPageSize = DEFAULT_LIST_PAGE_SIZE;
		}
	}
	
	/**
	 * Check for the helper-done case locally and handle it before letting the VPPA.toolModeDispatch() handle the actual dispatch.
	 * @see org.sakaiproject.cheftool.VelocityPortletPaneledAction#toolModeDispatch(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		logger.debug("toolModeDispatch()");
		//SessionState sstate = getState(req);
		ToolSession toolSession = getSessionManager().getCurrentToolSession();

		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);

		// if we're done or not properly initialized, redirect to Resources
		if ( done != null || !initHelper( getState(req) ) )
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = getToolManager().getCurrentTool();

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
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		logger.debug("doGet()");
		String isAjaxRequest = req.getParameter("ajaxRequest"); 
		if(isAjaxRequest != null && isAjaxRequest.trim().equalsIgnoreCase(Boolean.toString(true))) {
			ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);
			if(params == null) {
				params = new ParameterParser(req);
			}
			
			SessionState state = getState(req);
			
			// Check whether this is an AJAX request expecting a JSON response and if it is
			// dispatch the request to the buildJsonResponse() method, avoiding VPPA's 
			// html rendering. Other options might be HTML-fragment, XML, etc.
			//String requestedMimetype = (String) toolSession.getAttribute(REQUESTED_MIMETYPE);
			String requestedMimetype = params.getString(REQUESTED_MIMETYPE);
			if(logger.isDebugEnabled()) {
				logger.debug("doGet() requestedMimetype == " + requestedMimetype);
			}
			if(requestedMimetype != null && requestedMimetype.equals(MIMETYPE_JSON)) {
				doGetJsonResponse(params, state, req, res);
			} else if(requestedMimetype != null && requestedMimetype.equals(MIMETYPE_HTML)) {
				doGetHtmlFragmentResponse(params, state, req, res);
			} else {
				// throw something
			}
	
			return;
		}
		super.doGet(req, res);
			
	}

	protected void doGetHtmlFragmentResponse(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		
	}

	protected void doGetJsonResponse(ParameterParser params, SessionState state,
			HttpServletRequest req, HttpServletResponse res) {
		res.setCharacterEncoding(CHARSET_UTF8);
		res.setContentType(MIMETYPE_JSON);
		
		Map<String,Object> jsonMap = new HashMap<String,Object>();
		String sakai_csrf_token = params.getString("sakai_csrf_token");
		if(sakai_csrf_token != null && ! sakai_csrf_token.trim().equals("")) {
			jsonMap.put("sakai_csrf_token", sakai_csrf_token);
		}
		jsonMap.put("timestamp", Long.toString(System.currentTimeMillis()));
		
		String citation_action = params.getString("citation_action");
		if(citation_action != null && citation_action.trim().equals(CHECK_FOR_UPDATES)) {
			Map<String,Object> result = this.checkForUpdates(params, state, req, res);
			jsonMap.putAll(result);
		} 
		
		jsonMap.put("secondsBetweenSaveciteRefreshes", new Integer(this.configurationService.getSecondsBetweenSaveciteRefreshes()));
		
		// convert to json string
		String jsonString = JSONObject.fromObject(jsonMap).toString();
		try {
			PrintWriter writer = res.getWriter();
			writer.print(jsonString);
			writer.flush();
		} catch (IOException e) {
			logger.warn("IOException in doGetJsonResponse() " + e);
		}
		
	}

	protected Map<String, Object> checkForUpdates(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {

		Map<String, Object> result = new HashMap<String, Object>();
		boolean changed = false;
		long lastcheckLong = 0L;
		String lastcheck = params.getString("lastcheck");
		if(lastcheck == null || lastcheck.trim().equals("")) {
			// do nothing
		} else {
			try {
				lastcheckLong = Long.parseLong(lastcheck);
			} catch(Exception e) {
				logger.warn("Error parsing long from string: " + lastcheck, e);
			}
		}
		if(lastcheckLong > 0L) {
			String citationCollectionId = params.getString("citationCollectionId");
			if(citationCollectionId != null && !citationCollectionId.trim().equals("")) {
				try {
					CitationCollection citationCollection = this.citationService.getCollection(citationCollectionId);
					if(citationCollection.getLastModifiedDate().getTime() > lastcheckLong) {
						changed = true;
						result.put("html", "<div>something goes here</div>");
					}
					
				} catch (IdUnusedException e) {
					logger.warn("IdUnusedException in checkForUpdates() " + e);
				}
				
			}
		}

		result.put("changed", Boolean.toString(changed));
		
		return result;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		logger.debug("doPost()");
		
		// handle AJAX requests here and send other requests on to the VPPA dispatcher
		String isAjaxRequest = req.getParameter("ajaxRequest"); 
		if(isAjaxRequest != null && isAjaxRequest.trim().equalsIgnoreCase(Boolean.toString(true))) {
			ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);
			if(params == null) {
				params = new ParameterParser(req);
			}
			
			SessionState state = getState(req);

			// Check whether this is an AJAX request expecting a JSON response and if it is
			// dispatch the request to the buildJsonResponse() method, avoiding VPPA's 
			// html rendering. Other options might be HTML-fragment, XML, etc.
			//String requestedMimetype = (String) toolSession.getAttribute(REQUESTED_MIMETYPE);
			String requestedMimetype = params.getString(REQUESTED_MIMETYPE);
			if(logger.isDebugEnabled()) {
				logger.debug("doPost() requestedMimetype == " + requestedMimetype);
			}
			if(requestedMimetype != null && requestedMimetype.equals(MIMETYPE_JSON)) {
				doPostJsonResponse(params, state, req, res);
			} else if(requestedMimetype != null && requestedMimetype.equals(MIMETYPE_HTML)) {
				doPostHtmlFragmentResponse(params, state, req, res);
			}

			return;
		}
		super.doPost(req, res);
	}

	protected void doPostHtmlFragmentResponse(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> result = this.ensureCitationListExists(params, state, req, res);
		
		res.setCharacterEncoding(CHARSET_UTF8);
		res.setContentType(MIMETYPE_HTML);

		String sakai_csrf_token = params.getString("sakai_csrf_token");
		if(sakai_csrf_token != null && ! sakai_csrf_token.trim().equals("")) {
			setVmReference("sakai_csrf_token", sakai_csrf_token, req);
		}
		
		for(Map.Entry<String,Object> entry : result.entrySet()) {
			setVmReference(entry.getKey(), entry.getValue(), req);
		}
	}

	protected void doPostJsonResponse(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		
		res.setCharacterEncoding(CHARSET_UTF8);
		res.setContentType(MIMETYPE_JSON);
		
		Map<String,Object> jsonMap = new HashMap<String,Object>();
		String sakai_csrf_token = params.getString("sakai_csrf_token");
		if(sakai_csrf_token != null && ! sakai_csrf_token.trim().equals("")) {
			jsonMap.put("sakai_csrf_token", sakai_csrf_token);
		}
		jsonMap.put("timestamp", Long.toString(System.currentTimeMillis()));
		
		String citation_action = params.getString("citation_action");
		if(citation_action != null && citation_action.trim().equals(UPDATE_RESOURCE)) {
			Map<String,Object> result = this.updateCitationList(params, state, req, res);
			jsonMap.putAll(result);
		} else if(citation_action != null && citation_action.trim().equals(UPDATE_SAVED_SORT)) {
			Map<String,Object> result = this.updateSavedSort(params, state, req, res);
			jsonMap.putAll(result);
		} else {
			Map<String,Object> result = this.createCitationList(params, state, req, res);
			jsonMap.putAll(result);			
		}
		
		jsonMap.put("secondsBetweenSaveciteRefreshes", this.configurationService.getSecondsBetweenSaveciteRefreshes());
		
		// convert to json string
		String jsonString = JSONObject.fromObject(jsonMap).toString();
		try {
			PrintWriter writer = res.getWriter();
			writer.print(jsonString);
			writer.flush();
		} catch (IOException e) {
			logger.warn("IOException in doPostJsonResponse() ", e);
			// what goes back?
		}

	}

	protected Map<String, Object> updateSavedSort(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> results = new HashMap<String, Object>();
		String message = null;
		String citationCollectionId = params.getString("citationCollectionId");
		String new_sort = params.getString("new_sort");
		if(citationCollectionId == null || citationCollectionId.trim().equals("")) {
			// need to report error
			results.put("message", rb.getString("sort.save.error"));
		} else {
			if(new_sort == null || new_sort.trim().equals("")) {
				new_sort = "default";
			} 
			try {
				CitationCollection citationCollection = this.citationService.getCollection(citationCollectionId);
				citationCollection.setSort(new_sort, true);
				this.citationService.save(citationCollection);
				results.put("message", rb.getString("sort.save.success"));
			} catch (IdUnusedException e) {
				// need to report error
				results.put("message", rb.getString("sort.save.error"));
			}
			
		}
		return results;
	}

	protected Map<String, Object> createCitationList(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.putAll(this.ensureCitationListExists(params, state, req, res));
		return results;
	}

	protected Map<String, Object> updateCitationList(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> results = new HashMap<String, Object>();
		
		String resourceUuid = params.getString("resourceUuid");
		String message = null;
		if(resourceUuid == null) {
			results.putAll(this.ensureCitationListExists(params, state, req, res));
		} else {
			try {
				int priority = this.capturePriority(params);
				String resourceId = this.getContentService().resolveUuid(resourceUuid);
				ContentResourceEdit edit = getContentService().editResource(resourceId);
				this.captureDisplayName(params, state, edit, results);
				this.captureDescription(params, state, edit, results);
				this.captureAccess(params, state, edit, results);
				this.captureAvailability(params, edit, results);
				getContentService().commitResource(edit, priority);
				message = "Resource updated";
			} catch (IdUnusedException e) {
				message = e.getMessage();
				logger.warn("IdUnusedException in updateCitationList() " + e);
			} catch (TypeException e) {
				message = e.getMessage();
				logger.warn("TypeException in updateCitationList() " + e);
			} catch (InUseException e) {
				message = e.getMessage();
				logger.warn("InUseException in updateCitationList() " + e);
			} catch (PermissionException e) {
				message = e.getMessage();
				logger.warn("PermissionException in updateCitationList() " + e);
			} catch (OverQuotaException e) {
				message = e.getMessage();
				logger.warn("OverQuotaException in updateCitationList() " + e);
			} catch (ServerOverloadException e) {
				message = e.getMessage();
				logger.warn("ServerOverloadException in updateCitationList() " + e);
			} catch (VirusFoundException e) {
				message = e.getMessage();
				logger.warn("VirusFoundException in updateCitationList() " + e);
			}
			if(message != null && ! message.trim().equals("")) {
				results.put("message", message);
			}
		}
		return results;
	}

	private int capturePriority(ParameterParser params) {
		int priority = NotificationService.NOTI_NONE;
		if(params != null) {
			String notify = params.getString("notify");
			if("r".equals(notify)) {
				priority = NotificationService.NOTI_REQUIRED;
			} else if("o".equals(notify)) {
				priority = NotificationService.NOTI_OPTIONAL;
			}
		}
		return priority;
	}

	/**
	 * Check whether we are editing an existing resource or working on a new citation list. 
	 * If it exists, we'll update any attributes that have changed.  If it's new, we will 
	 * create it and return the resourceUuid, along with other attributes, in a map.
	 * @param params
	 * @param state
	 * @param req
	 * @param res
	 * @return
	 */
	protected Map<String,Object> ensureCitationListExists(ParameterParser params,
			SessionState state, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> results = new HashMap<String,Object>();
		String message = null;

		String displayName = params.getString("displayName");
		if(displayName == null) {
			// error ??
		}
				
		CitationCollection cCollection = this.getCitationCollection(state, true);
		
		if(cCollection == null) {
			// error
		} else {
			String citationCollectionId = cCollection.getId();
			
			String contentCollectionId = params.getString("contentCollectionId");
			if(contentCollectionId == null || contentCollectionId.trim().equals("")) {
				ToolSession toolSession = getSessionManager().getCurrentToolSession();
				ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
				contentCollectionId = pipe.getContentEntity().getId();
			}
			
			ContentResource resource = null;
			String resourceUuid = params.getString("resourceUuid");
			String resourceId = null;
			if(resourceUuid == null || resourceUuid.trim().equals("")) {
				// create resource
				if(contentCollectionId == null) {
					// error?
					message = rb.getString("resource.null_collectionId.error");
				} else {
					int priority = this.capturePriority(params);
					
					// create resource
					try {
						ContentResourceEdit edit = getContentService().addResource(contentCollectionId, displayName, null, ContentHostingService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
						
						edit.setResourceType(CitationService.CITATION_LIST_ID);
						byte[] bytes = citationCollectionId.getBytes();
						edit.setContent(bytes );
						captureDescription(params, state, edit, results);
						captureAccess(params, state, edit, results);
						captureAvailability(params, edit, results);
						ResourceProperties properties = edit.getPropertiesEdit();
						properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
						properties.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, CitationService.REFERENCE_ROOT);
						properties.addProperty(ResourceProperties.PROP_CONTENT_TYPE, ResourceType.MIME_TYPE_HTML);

						getContentService().commitResource(edit, priority);
						resourceId = edit.getId();
						message =  rb.getFormattedMessage("resource.new.success", new String[]{ displayName });
					} catch (IdUniquenessException e) {
						message = e.getMessage();
						logger.warn("IdUniquenessException in ensureCitationListExists() " + e);
					} catch (IdLengthException e) {
						message = e.getMessage();
						logger.warn("IdLengthException in ensureCitationListExists() " + e);
					} catch (IdInvalidException e) {
						message = e.getMessage();
						logger.warn("IdInvalidException in ensureCitationListExists() " + e);
					} catch (OverQuotaException e) {
						message = e.getMessage();
						logger.warn("OverQuotaException in ensureCitationListExists() " + e);
					} catch (ServerOverloadException e) {
						message = e.getMessage();
						logger.warn("ServerOverloadException in ensureCitationListExists() " + e);
					} catch (PermissionException e) {
						message = e.getMessage();
						logger.warn("PermissionException in ensureCitationListExists() " + e);
					} catch (IdUnusedException e) {
						message = e.getMessage();
						logger.warn("IdUnusedException in ensureCitationListExists() " + e);
					}
				}
				
				
			} else {
				// get resource
				resourceId = this.getContentService().resolveUuid(resourceUuid);
				if(citationCollectionId == null) {
					try {
						resource = this.contentService.getResource(resourceId);
						citationCollectionId = new String(resource.getContent());
					} catch (IdUnusedException e) {
						message = e.getMessage();
						logger.warn("IdUnusedException in getting resource in ensureCitationListExists() " + e);
					} catch (TypeException e) {
						message = e.getMessage();
						logger.warn("TypeException in getting resource in ensureCitationListExists() " + e);
					} catch (PermissionException e) {
						message = e.getMessage();
						logger.warn("PermissionException in getting resource in ensureCitationListExists() " + e);
					} catch (ServerOverloadException e) {
						message = e.getMessage();
						logger.warn("ServerOverloadException in getting citationCollectionId in ensureCitationListExists() " + e);
					}
				}
				// possibly revise displayName, other properties 
				// commit changes
				// report success/failure
			}
			results.put("citationCollectionId", citationCollectionId);
			//results.put("resourceId", resourceId);
			resourceUuid = this.getContentService().getUuid(resourceId);
			
			if(logger.isDebugEnabled()) {
				logger.debug("ensureCitationListExists() created new resource with resourceUuid == " + resourceUuid + " and resourceId == " + resourceId);
			}
			results.put("resourceUuid", resourceUuid );
			String clientId = params.getString("saveciteClientId");
			
			if(clientId != null && ! clientId.trim().equals("")) {
				Locale locale = rb.getLocale();
				List<Map<String,String>> saveciteClients = getConfigurationService().getSaveciteClientsForLocale(locale);
				if(saveciteClients != null) {
					for(Map<String,String> client : saveciteClients) {
						if(client != null && client.get("id") != null && client.get("id").equalsIgnoreCase(clientId)) {
							String saveciteUrl = getSearchManager().getSaveciteUrl(resourceUuid,clientId);
							try {
								saveciteUrl = java.net.URLEncoder.encode(saveciteUrl,"UTF-8");
							} catch (UnsupportedEncodingException e) {
								logger.warn("Error encoding savecite URL",e);
							}
							// ${client.searchurl_base}?linkurl_base=${client.saveciteUrl}#if(${client.linkurl_id})&linkurl_id=${client.linkurl_id}
							StringBuilder buf = new StringBuilder();
							buf.append(client.get("searchurl_base"));
							buf.append("?linkurl_base=");
							buf.append(saveciteUrl);
							if(client.get("linkurl_id") != null && ! client.get("linkurl_id").trim().equals("")) {
								buf.append("&linkurl_id=");
								buf.append(client.get("linkurl_id"));
							}
							buf.append('&');
							
							results.put("saveciteUrl", buf.toString());
							break;
						}
					}
				}
			}
			results.put("contentCollectionId", contentCollectionId);
		}
		results.put("message", message);
		
		return results;
	}

	private void captureDescription(ParameterParser params, SessionState state,
			ContentResourceEdit edit, Map<String, Object> results) {
		String description = params.get("description");
		String oldDescription = edit.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
		if(description == null || description.trim().equals("")) {
			if(oldDescription != null) {
				edit.getPropertiesEdit().removeProperty(ResourceProperties.PROP_DESCRIPTION);
				results.put("description", "");
			}
		} else {
			if(oldDescription == null || ! oldDescription.equals(description)) {
				edit.getPropertiesEdit().removeProperty(ResourceProperties.PROP_DESCRIPTION);
				edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DESCRIPTION, description);
				results.put("description", description);
			}
		}
		
	}

	protected void captureDisplayName(ParameterParser params, SessionState state, 
			ContentResourceEdit edit, Map<String, Object> results) {
		String displayName = params.getString("displayName");
		if(displayName == null || displayName.trim().equals("")) {
			throw new RuntimeException("invalid name for resource: " + displayName);
		}
		String oldDisplayName = edit.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if(oldDisplayName == null || ! oldDisplayName.equals(displayName)) {
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.removeProperty(ResourceProperties.PROP_DISPLAY_NAME);
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
			results.put("displayName", displayName);
		}
		
	}

	/**
	 * @param params
	 * @param edit
	 * @param results TODO
	 */
	protected void captureAvailability(ParameterParser params,
			ContentResourceEdit edit, Map<String, Object> results) {
		boolean hidden = params.getBoolean("hidden");
		boolean useReleaseDate = params.getBoolean("use_start_date");
		DateFormat df = DateFormat.getDateTimeInstance();
		Time releaseDate = null;
		if(useReleaseDate) {
			String releaseDateStr = params.getString(PROP_RELEASE_DATE);
			if(releaseDateStr != null) {
				try {
					releaseDate = TimeService.newTime(df.parse(releaseDateStr).getTime());
				} catch (ParseException e) {
					logger.warn("ParseException in captureAvailability() " + e);
				}
			}
		}
		Time retractDate = null;
		boolean useRetractDate = params.getBoolean("use_end_date");
		if(useRetractDate) {
			String retractDateStr = params.getString(PROP_RETRACT_DATE);
			if(retractDateStr != null) {
				try {
					retractDate = TimeService.newTime(df.parse(retractDateStr).getTime());
				} catch (ParseException e) {
					logger.warn("ParseException in captureAvailability() " + e);
				}
			}
		}
		boolean oldHidden = edit.isHidden();
		Time oldReleaseDate = edit.getReleaseDate();
		Time oldRetractDate = edit.getRetractDate();
		boolean changesFound = false;
		if(oldHidden != hidden) {
			results.put(PROP_IS_HIDDEN, Boolean.toString(hidden));
			changesFound = true;
		}
		if(oldReleaseDate == null && releaseDate == null) {
			// no change here
		} else if((oldReleaseDate == null) || ! oldReleaseDate.equals(releaseDate)) {
			if(releaseDate == null) {
				results.put(PROP_RELEASE_DATE_STR, df.format(new Date()));
			} else {
				results.put(PROP_RELEASE_DATE_STR, df.format(new Date(releaseDate.getTime())));
			}
			results.put(PROP_RELEASE_DATE, releaseDate);
			results.put(PROP_USE_RELEASE_DATE, useReleaseDate);
			changesFound = true;
		}
		if(oldRetractDate == null && retractDate == null) {
			// no change here
		} else if (oldRetractDate == null  || ! oldRetractDate.equals(retractDate)) {
			if(retractDate == null) {
				results.put(PROP_RETRACT_DATE_STR, df.format(new Date(System.currentTimeMillis() + ONE_WEEK)));
			} else {
				results.put(PROP_RETRACT_DATE_STR, df.format(new Date(retractDate.getTime() )));
			}
			results.put(PROP_RETRACT_DATE, retractDate);
			changesFound = true;
		}
		if(changesFound) {
			edit.setAvailability(hidden, releaseDate, retractDate);
		}
	}
	
	protected void captureAccess(ParameterParser params, SessionState state,
			ContentResourceEdit edit, Map<String, Object> results) {
		
		Map<String,Object> entityProperties = (Map<String, Object>) state.getAttribute(STATE_RESOURCE_ENTITY_PROPERTIES);
		boolean changesFound = false;
		String access_mode = params.getString("access_mode");
		if(access_mode == null) {
			access_mode = AccessMode.INHERITED.toString();
		}
		String oldAccessMode = entityProperties.get(PROP_ACCESS_MODE).toString();
		if(oldAccessMode == null) {
			oldAccessMode = AccessMode.INHERITED.toString();
		}
		if(! access_mode.equals(oldAccessMode)) {
			results.put(PROP_ACCESS_MODE, AccessMode.fromString(access_mode));
			changesFound = true;
		}
		if(AccessMode.GROUPED.toString().equals(access_mode)) {
			// we inherit more than one group and must check whether group access changes at this item
			String[] access_groups = params.getStrings("access_groups");
			
			SortedSet<String> new_groups = new TreeSet<String>();
			if(access_groups != null) {
				new_groups.addAll(Arrays.asList(access_groups));
			}
			
			List<Map<String,String>> possibleGroups = (List<Map<String, String>>) entityProperties.get(PROP_POSSIBLE_GROUPS);
			if(possibleGroups == null) {
				possibleGroups = new ArrayList<Map<String,String>>();
			}
			Map<String, String> possibleGroupMap = mapGroupRefs(possibleGroups);
			SortedSet<String> new_group_refs = convertToRefs(new_groups, possibleGroupMap );
			
			boolean groups_are_inherited = (new_groups.size() == possibleGroupMap.size()) && possibleGroupMap.keySet().containsAll(new_groups);
			
			try {
				if(groups_are_inherited) {
					edit.clearGroupAccess();
					edit.setGroupAccess(new_group_refs);
				} else {
					edit.setGroupAccess(new_group_refs);
				}
				edit.clearPublicAccess();
			} catch (InconsistentException e) {
				logger.warn("InconsistentException in captureAccess() " + e);
			} catch (PermissionException e) {
				logger.warn("PermissionException in captureAccess() " + e);
			}
		} else if("public".equals(access_mode)) {
			Boolean isPubviewInherited = (Boolean) entityProperties.get(PROP_IS_PUBVIEW_INHERITED);
			if(isPubviewInherited == null || ! isPubviewInherited) {
				try {
					edit.setPublicAccess();
				} catch (InconsistentException e) {
					logger.warn("InconsistentException in captureAccess() " + e);
				} catch (PermissionException e) {
					logger.warn("PermissionException in captureAccess() " + e);
				}
			}
		} else if(AccessMode.INHERITED.toString().equals(access_mode)) {
			try {
				if(edit.getAccess() == AccessMode.GROUPED) {
					edit.clearGroupAccess();
				}
				edit.clearPublicAccess();
			} catch (InconsistentException e) {
				logger.warn("InconsistentException in captureAccess() " + e);
			} catch (PermissionException e) {
				logger.warn("PermissionException in captureAccess() " + e);
			} 
		}
		
		// isPubview
		results.put(PROP_IS_PUBVIEW, getContentService().isPubView(edit.getId()));
		// isPubviewInherited
		results.put(PROP_IS_PUBVIEW_INHERITED, new Boolean(getContentService().isInheritingPubView(edit.getId())));
		// isPubviewPossible
		Boolean preventPublicDisplay = (Boolean) state.getAttribute("resources.request.prevent_public_display");
		if(preventPublicDisplay == null) {
			preventPublicDisplay = Boolean.FALSE;
		}
		results.put(PROP_IS_PUBVIEW_POSSIBLE, new Boolean(! preventPublicDisplay.booleanValue()));
		
		// accessMode
		results.put(PROP_ACCESS_MODE, edit.getAccess());
		// isGroupInherited
		results.put(PROP_IS_GROUP_INHERITED, AccessMode.GROUPED == edit.getInheritedAccess());
		// possibleGroups
		Collection<Group> inheritedGroupObjs = edit.getInheritedGroupObjects();
		Map<String,Map<String,String>> groups = new HashMap<String,Map<String,String>>();
		if(inheritedGroupObjs != null) {
			for(Group group : inheritedGroupObjs) {
				Map<String, String> grp = new HashMap<String, String>();
				grp.put("groupId", group.getId());
				grp.put("title", group.getTitle());
				grp.put("description", group.getDescription());
				grp.put("entityRef", group.getReference());
				groups.put(grp.get("groupId"), grp);
			}
		}
		results.put(PROP_POSSIBLE_GROUPS, groups);
		// isGroupPossible
		results.put(PROP_IS_GROUP_POSSIBLE, new Boolean(groups != null && groups.size() > 0));
		// isSingleGroupInherited
		results.put(PROP_IS_SINGLE_GROUP_INHERITED, new Boolean(groups != null && groups.size() == 1));
		// isSiteOnly = ! isPubviewPossible && ! isGroupPossible
		results.put(PROP_IS_SITE_ONLY, new Boolean(preventPublicDisplay.booleanValue() && (groups == null || groups.size() < 1)));
		// isUserSite
		SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
		Reference ref = getEntityManager().newReference(edit.getReference());
		results.put(PROP_IS_USER_SITE, siteService.isUserSite(ref.getContext()));
	}

	private Map<String, String> mapGroupRefs(
			List<Map<String, String>> possibleGroups) {
		
		Map<String, String> groupRefMap = new HashMap<String, String>();
		for(Map<String, String> groupInfo : possibleGroups) {
			if(groupInfo.get("groupId") != null && groupInfo.get("entityRef") != null) {
				groupRefMap.put(groupInfo.get("groupId"), groupInfo.get("entityRef"));
			}
		}
		return groupRefMap ;
	}

	public SortedSet<String> convertToRefs(Collection<String> groupIds, Map<String, String> possibleGroupMap) 
	{
		SortedSet<String> groupRefs = new TreeSet<String>();
		for(String groupId : groupIds)
		{
			String groupRef = possibleGroupMap.get(groupId);
			if(groupRef != null)
			{
				groupRefs.add(groupRef);
			}
		}
		return groupRefs;

	}

	protected void preserveEntityIds(ParameterParser params, SessionState state) {
		String resourceId = params.getString("resourceId");
		String resourceUuid = params.getString("resourceUuid");
		String citationCollectionId = params.getString("citationCollectionId");
		
		if(resourceId == null || resourceId.trim().equals("")) {
			// do nothing
		} else {
			state.setAttribute(CitationHelper.RESOURCE_ID, resourceId);
		}
		
		if(resourceUuid == null || resourceUuid.trim().equals("")) {
			// do nothing
		} else {
			state.setAttribute(CitationHelper.RESOURCE_UUID, resourceUuid);
		}
		
		if(citationCollectionId == null || citationCollectionId.trim().equals("")) {
			// do nothing
		} else {
			state.setAttribute(CitationHelper.CITATION_COLLECTION_ID, citationCollectionId);
		}
		
	}

	protected void putCitationCollectionDetails( Context context, SessionState state )
    {
		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		String refStr = getContentService().getReference(resourceId);
		Reference ref = getEntityManager().newReference(refStr);
		String collectionTitle = null;
		if( ref != null )
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if(collectionTitle == null) {
			collectionTitle = (String)state.getAttribute( STATE_COLLECTION_TITLE );
		}
		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", getFormattedText().escapeHtml(collectionTitle));
		}

		// get the collection we're now working on
		String citationCollectionId = (String)state.getAttribute(STATE_CITATION_COLLECTION_ID);
		context.put( "citationCollectionId", citationCollectionId );

		CitationCollection collection = getCitationCollection(state, false);
		int collectionSize = 0;

		if (collection == null)
		{
			logger.warn( "buildAddCitationsPanelContext unable to access citationCollection " + citationCollectionId );
			return;
		}
		else
		{
			// get the size of the list
			collectionSize = collection.size();
		}
		context.put( "collectionSize", new Integer( collectionSize ) );
    }

	public String buildImportCitationsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("state", state);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		// validator
		context.put("xilator", new Validator());

		context.put("FORM_NAME", "importForm");
		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
    	context.put("state", state);

    	 // always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		// body onload handler
		context.put("sakai_onload", "setMainFrameHeight( window.name )");

		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		String refStr = getContentService().getReference(resourceId);
		Reference ref = getEntityManager().newReference(refStr);
		String collectionTitle = null;
		if( ref != null )
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if(collectionTitle == null) {
			collectionTitle = (String)state.getAttribute( STATE_COLLECTION_TITLE );
		}
		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", getFormattedText().escapeHtml(collectionTitle));
		}

		// get the collection we're now working on
		String citationCollectionId = (String)state.getAttribute(STATE_CITATION_COLLECTION_ID);
		context.put( "citationCollectionId", citationCollectionId );

		CitationCollection citationCollection = getCitationCollection(state, false);
		int collectionSize = 0;
		if(citationCollection == null)
		{
			logger.warn( "buildAddCitationsPanelContext unable to access citationCollection " + citationCollectionId );

			int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
			context.put("requestStateId", requestStateId);

			return TEMPLATE_ERROR;
		}
		else
		{
			// get the size of the list
			collectionSize = citationCollection.size();
		}

		context.put( "collectionSize", new Integer( collectionSize ) );
		
		Locale locale = rb.getLocale();
		List<Map<String,String>> saveciteClients = getConfigurationService().getSaveciteClientsForLocale(locale);
		
		if(saveciteClients != null) {
			for(Map<String,String> client : saveciteClients) {
				String saveciteUrl = getSearchManager().getSaveciteUrl(getContentService().getUuid(resourceId),client.get("id"));
				try {
					client.put("saveciteUrl", java.net.URLEncoder.encode(saveciteUrl,"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					logger.warn("Error encoding savecite URL",e);
				}

			}
			
			context.put("saveciteClients",saveciteClients); 
		}
		

		// determine which features to display
		if( getConfigurationService().isGoogleScholarEnabled() )
		{
			String googleUrl = getSearchManager().getGoogleScholarUrl(getContentService().getUuid(resourceId));
			String sakaiInstance = scs.getString("ui.service", "Sakai");
			context.put( "googleUrl", googleUrl );

			// object array for formatted messages
			Object[] googleArgs = { rb.getFormattedMessage( "linkLabel.google.sakai", sakaiInstance ) };
			context.put( "googleArgs", googleArgs );
		}

		if( getConfigurationService().librarySearchEnabled() )
		{
			context.put( "searchLibrary", Boolean.TRUE );
		}

		// form name
		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

		return TEMPLATE_ADD_CITATIONS;

    } // buildAddCitationsPanelContext

	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildCreatePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
    	context.put("state", state);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		// validator
		context.put("xilator", new Validator());

		//context.put("sakai_onload", "setPopupHeight('create');checkinWithOpener('create');");
		//context.put("sakai_onunload", "window.opener.parent.popups['create']=null;");

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);

		List schemas = getCitationService().getSchemas();
		context.put("TEMPLATES", schemas);

		Schema defaultSchema = getCitationService().getDefaultSchema();
		context.put("DEFAULT_TEMPLATE", defaultSchema);

		// Object array for instruction message
		Object[] instrArgs = { rb.getString( "submit.create" ) };
		context.put( "instrArgs", instrArgs );

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
		context.put("stlang", srb);

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

		// change mode back to SEARCH (DATABASE not needed anymore)
		setMode( state, Mode.SEARCH );

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
		context.put("stlang", srb);

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
		String citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
		context.put("citationId", citationId);
		context.put("citationCollectionId", citationCollectionId);

		List schemas = getCitationService().getSchemas();
		context.put("TEMPLATES", schemas);

		context.put("DEFAULT_TEMPLATE", citation.getSchema());

		// Object array for formatted instruction
		Object[] instrArgs = { rb.getString( "submit.edit" ) };
		context.put( "instrArgs", instrArgs );

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

	    return TEMPLATE_ERROR;
    }

    private String buildErrorFatalPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		//context.put("sakai_onload", "setPopupHeight('error');");
		//context.put("sakai_onunload", "window.opener.parent.popups['error']=null;");

	    return TEMPLATE_ERROR_FATAL;
    }
	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
//		state.setAttribute("fromListPage", true);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

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
		try {
			ContentResource resource = this.getContentService().getResource(resourceId);
			String description = resource.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
			context.put("description", description);
		} catch (Exception e) {
			// TODO: Fix this. What exception is this dealing with?
			logger.warn(e.getMessage(), e);
		}
		String refStr = getContentService().getReference(resourceId);
		Reference ref = getEntityManager().newReference(refStr);
		String collectionTitle = null;
		if( ref != null && ref.getProperties() != null)
		{
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if(collectionTitle == null) {
			collectionTitle = (String)state.getAttribute( STATE_COLLECTION_TITLE );
		}
		if( collectionTitle != null && !collectionTitle.trim().equals("") )
		{
			context.put( "collectionTitle", getFormattedText().escapeHtml(collectionTitle));
		}

		context.put("openUrlLabel", getConfigurationService().getSiteConfigOpenUrlLabel());

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
			listPageSize = defaultListPageSize;
			state.setAttribute(STATE_LIST_PAGE_SIZE, listPageSize);
		}
		context.put("listPageSize", listPageSize);

		CitationIterator newIterator = collection.iterator();
		CitationIterator oldIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(oldIterator != null)
		{
			newIterator.setPageSize(listPageSize.intValue());
			newIterator.setStart(oldIterator.getStart());
//			newIterator.setPage(oldIterator.getPage());
		}
		context.put("citations", newIterator);
		context.put("citationCollectionId", collection.getId());
		context.put("resourceId", resourceId);
		
		if(! collection.isEmpty())
		{
			context.put("show_citations", Boolean.TRUE);

//			int page = newIterator.getPage();
//			int pageSize = newIterator.getPageSize();
			int totalSize = collection.size();

			int start = newIterator.getStart();
			int end = newIterator.getEnd();
//			int start = page * pageSize + 1;
//			int end = Math.min((page + 1) * pageSize, totalSize);

			Integer[] position = { new Integer(start+1) , new Integer(end), new Integer(totalSize)};
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
		Object[] instrMainArgs = { getConfigurationService().getSiteConfigOpenUrlLabel() };
		context.put( "instrMainArgs", instrMainArgs );

		Object[] instrSubArgs = { rb.getString( "label.finish" ) };
		context.put( "instrSubArgs", instrSubArgs );

		Object[] emptyListArgs = { rb.getString( "label.menu" ) };
		context.put( "emptyListArgs", emptyListArgs );

		String sort = (String) state.getAttribute(STATE_SORT);

		if (sort == null  || sort.trim().length() == 0)
			sort = collection.getSort();

		context.put("sort", sort);

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

		return TEMPLATE_LIST;

	}	// buildListPanelContext
	
	public String buildReorderPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state) {
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		// validator
		context.put("xilator", new Validator());

		if( state.removeAttribute( STATE_LIST_NO_SCROLL ) == null ) {
			context.put("sakai_onload", "setMainFrameHeight( window.name )");
		}
		else {
			context.put("sakai_onload", "resizeFrame()");
		}

		// get the citation list title
		String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
		String refStr = getContentService().getReference(resourceId);
		Reference ref = getEntityManager().newReference(refStr);
		String collectionTitle = null;
		if( ref != null ) {
			collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		if(collectionTitle == null) {
			collectionTitle = (String)state.getAttribute( STATE_COLLECTION_TITLE );
		}
		else if( !collectionTitle.trim().equals("") ) {
			context.put( "collectionTitle", Validator.escapeHtml(collectionTitle));
		}

		CitationCollection collection = getCitationCollection(state, true);

		collection.setSort(CitationCollection.SORT_BY_POSITION,true);

		CitationIterator newIterator = collection.iterator();
		newIterator.setPageSize(collection.size());
		context.put("citations", newIterator);
		context.put("citationCollectionId", collection.getId());
		state.setAttribute(STATE_LIST_ITERATOR, newIterator);

		return TEMPLATE_REORDER;
	}


	/**
	 * This method retrieves the CitationCollection for the current session.
	 * If the CitationCollection is already in session-state and has not been
	 * updated in the persistent storage since it was last accessed, the copy
	 * in session-state will be returned.  If it has been updated in storage,
	 * the copy in session-state will be updated and returned. If the
	 * CitationCollection has not yet been created in storage and the second
	 * parameter is true, this method will create the collection and return it.
	 * In that case, values will be added to session-state for attributes named
	 * STATE_CITATION_COLLECTION and STATE_CITATION_COLLECTION_ID. If the CitationCollection has
	 * not yet been created in storage and the second parameter is false, the
	 * method will return null.
	 * @param state The SessionState object for the current session.
	 * @param create A flag indicating whether the collection should be created
	 * 	if it does not already exist.
	 * @return The CitationCollection for the current session, or null.
	 */
	protected CitationCollection getCitationCollection(SessionState state, boolean create)
	{
		CitationCollection citationCollection = (CitationCollection) state.getAttribute(STATE_CITATION_COLLECTION);
		if(citationCollection == null)
		{
			String citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
			if(citationCollectionId == null && create)
			{
				citationCollection = getCitationService().addCollection();
				getCitationService().save(citationCollection);
			}
			else
			{
				try
	            {
		            citationCollection = getCitationService().getCollection(citationCollectionId);
	            }
	            catch (IdUnusedException e)
	            {
		            logger.warn("IdUnusedException: CitationHelperAction.getCitationCollection() unable to access citationCollection " + citationCollectionId);
	            }
				if(citationCollection == null && create)
				{
					citationCollection = getCitationService().addCollection();
					getCitationService().save(citationCollection);
				}
			}
			if(citationCollection != null) {
				state.setAttribute(STATE_CITATION_COLLECTION, citationCollection);
				state.setAttribute(STATE_CITATION_COLLECTION_ID, citationCollection.getId());
			}
		}
		return citationCollection;
	}

	/**
	 * build the context.
	 *
	 * @return The name of the template to use.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		logger.debug("buildMainPanelContext()");
		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		//context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		//context.put("citationToolId", CitationHelper.CITATION_ID);
		//context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		// always put whether this is a Resources Add or Revise operation
		if( state.getAttribute( STATE_RESOURCES_ADD ) != null )
		{
			context.put( "resourcesAddAction", Boolean.TRUE );
		}

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);

		String template = "";
		Mode mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null)
		{
			// mode really shouldn't be null here
			logger.warn( "buildMainPanelContext() getting null Mode from state" );
			mode = Mode.NEW_RESOURCE;
			//mode = Mode.ADD_CITATIONS;
			setMode(state, mode);
		}

		// add mode to the template
		context.put( "citationsHelperMode", mode );

		switch(mode)
		{
			case NEW_RESOURCE:
				template = buildNewResourcePanelContext(portlet, context, rundata, state);
				break;
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
			case ERROR_FATAL:
				template = buildErrorFatalPanelContext(portlet, context, rundata, state);
				break;
			case LIST:
				template = buildListPanelContext(portlet, context, rundata, state);
				break;
			case REORDER:
				template = buildReorderPanelContext(portlet, context, rundata, state);
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

	public String buildNewResourcePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state) {

		logger.debug("buildNewResourcePanelContext()");

		context.put("MIMETYPE_JSON", MIMETYPE_JSON);
		context.put("REQUESTED_MIMETYPE", REQUESTED_MIMETYPE);
		
		context.put("xilator", new Validator());
		
		context.put("availability_is_enabled", Boolean.TRUE);
		context.put("GROUP_ACCESS", AccessMode.GROUPED);
		context.put("INHERITED_ACCESS", AccessMode.INHERITED);
		
		Boolean resourceAdd = (Boolean) state.getAttribute(STATE_RESOURCES_ADD);
		if(resourceAdd != null && resourceAdd.equals(true)) {
			context.put("resourceAdd", Boolean.TRUE);
			context.put(CITATION_ACTION, CREATE_RESOURCE);
		} else {
			context.put(CITATION_ACTION, UPDATE_RESOURCE);
		}
		
    	// resource-related
    	String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
    	String resourceUuid = (String) state.getAttribute(CitationHelper.RESOURCE_UUID);
    	
    	if(resourceId == null || resourceId.trim().equals("")) {
	    	if(resourceUuid == null || resourceUuid.trim().equals("")) {
	    		// Will be dealt with later by creating new resource when needed
	    	} else if(resourceUuid.startsWith("/")) {
	    		// UUID and ID may be switched
	    		resourceId = resourceUuid;
	    		resourceUuid = this.getContentService().getUuid(resourceId);
	    		if(resourceUuid != null) {
	    			state.setAttribute(CitationHelper.RESOURCE_ID, resourceId);
	    			state.setAttribute(CitationHelper.RESOURCE_UUID, resourceUuid);
	    		}
	    	} else {
	    		// see if we can get the resourceId from the UUID
	    		resourceId = this.getContentService().resolveUuid(resourceUuid);
	    		if(resourceId != null) {
	    			state.setAttribute(CitationHelper.RESOURCE_ID, resourceId);
	    		}
	    	}
    	} else if(resourceUuid == null || resourceUuid.trim().equals("")) {
    		resourceUuid = this.getContentService().getUuid(resourceId);
    		if(resourceUuid != null) {
    			state.setAttribute(CitationHelper.RESOURCE_UUID, resourceUuid);
    		}
    	}
 
    	if(logger.isDebugEnabled()) {
			logger.debug("buildNewResourcePanelContext()  resourceUuid == " + resourceUuid + "  resourceId == " + resourceId);
		}
		
    	String citationCollectionId = null;
    	ContentResource resource = null;
    	Map<String,Object> contentProperties = null;
    	if(resourceId == null) {
    		
    	} else {
	    	try {
				resource = getContentService().getResource(resourceId);
			} catch (IdUnusedException e) {
				logger.warn("IdUnusedException geting resource in buildNewResourcePanelContext() " + e);
			} catch (TypeException e) {
				logger.warn("TypeException geting resource in buildNewResourcePanelContext() " + e);
			} catch (PermissionException e) {
				logger.warn("PermissionException geting resource in buildNewResourcePanelContext() " + e);
			}
	    	
//	    	String guid = getContentService().getUuid(resourceId);
//	    	context.put("RESOURCE_ID", guid);
    	}

		if(resource == null) {
			context.put(CITATION_ACTION, CREATE_RESOURCE);
			
			ToolSession toolSession = getSessionManager().getCurrentToolSession();
			ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
			String contentCollectionId = pipe.getContentEntity().getId();
			context.put("contentCollectionId", contentCollectionId);
			ContentCollection collection;
			try {
				collection = getContentService().getCollection(contentCollectionId);
				contentProperties = this.getProperties(collection, state);
			} catch (IdUnusedException e) {
				logger.warn("IdUnusedException geting collection in buildNewResourcePanelContext() " + e);
			} catch (TypeException e) {
				logger.warn("TypeException geting collection in buildNewResourcePanelContext() " + e);
			} catch (PermissionException e) {
				logger.warn("PermissionException geting collection in buildNewResourcePanelContext() " + e);
			}
		} else {
			ResourceProperties props = resource.getProperties();
			contentProperties = this.getProperties(resource, state);
			context.put("resourceTitle", props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			context.put("resourceDescription", props.getProperty(ResourceProperties.PROP_DESCRIPTION));
			//resourceUuid = this.getContentService().getUuid(resourceId);
			context.put("resourceUuid", resourceUuid );
			context.put("contentCollectionId", resource.getContainingCollection().getId());
			try {
				citationCollectionId = new String(resource.getContent());
				if(citationCollectionId != null) {
					state.setAttribute(STATE_CITATION_COLLECTION_ID, citationCollectionId);
				}
			} catch (ServerOverloadException e) {
				logger.warn("ServerOverloadException geting props in buildNewResourcePanelContext() " + e);
			}
			
			context.put(CITATION_ACTION, UPDATE_RESOURCE);
		}
		if(contentProperties == null) {
			contentProperties = new HashMap<String,Object>();
		}
		context.put("contentProperties", contentProperties);
		int collectionSize = 0;
		CitationCollection citationCollection = getCitationCollection(state, true);
		if(citationCollection == null) {
			logger.warn( "buildAddCitationsPanelContext unable to access citationCollection " + citationCollectionId );

			int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
			context.put("requestStateId", requestStateId);

			return TEMPLATE_ERROR;
		} else {
			// get the size of the list
			collectionSize = citationCollection.size();
			citationCollectionId = citationCollection.getId();
		}
    	context.put("citationCollectionId", citationCollectionId);
		context.put( "collectionSize", new Integer( collectionSize ) );
    	

		Locale locale = rb.getLocale();
		List<Map<String,String>> saveciteClients = getConfigurationService().getSaveciteClientsForLocale(locale);
		
		if(saveciteClients != null) {
			if(resource != null && resourceId != null) {
				for(Map<String,String> client : saveciteClients) {
					String saveciteUrl = getSearchManager().getSaveciteUrl(resourceUuid,client.get("id"));
					try {
						client.put("saveciteUrl", java.net.URLEncoder.encode(saveciteUrl,"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.warn("Error encoding savecite URL",e);
					}
	
				}
			}
			
			context.put("saveciteClients",saveciteClients); 
		}

		// determine which features to display
		if( getConfigurationService().isGoogleScholarEnabled() ) {
			String googleUrl = getSearchManager().getGoogleScholarUrl(getContentService().getUuid(resourceId));
			String sakaiInstance = scs.getString("ui.service", "Sakai");
			context.put( "googleUrl", googleUrl );

			// object array for formatted messages
			Object[] googleArgs = { rb.getFormattedMessage( "linkLabel.google.sakai", sakaiInstance ) };
			context.put( "googleArgs", googleArgs );
		}

		if( getConfigurationService().librarySearchEnabled() ) {
			context.put( "searchLibrary", Boolean.TRUE );
		}
		
		if(citationCollection == null || citationCollection.size() <= 0) {
			
		} else {
			context.put("openUrlLabel", getConfigurationService().getSiteConfigOpenUrlLabel());
			
			String currentSort = (String) state.getAttribute(STATE_SORT);

			if (currentSort == null  || currentSort.trim().length() == 0)
				currentSort = citationCollection.getSort();

			if(currentSort == null || currentSort.trim().length() == 0) {
				currentSort = CitationCollection.SORT_BY_TITLE;
			}
			
			context.put("currentSort", currentSort);
			
			String savedSort = citationCollection.getSort();
			if(savedSort == null || savedSort.trim().equals("")) {
				savedSort = CitationCollection.SORT_BY_TITLE;
			}
			
			if(savedSort != currentSort) {
				
				citationCollection.setSort(currentSort, true);
			}
			
			//context.put(PARAM_FORM_NAME, ELEMENT_ID_LIST_FORM);

			// collection size
			context.put( "collectionSize", new Integer( citationCollection.size() ) );

			// export URLs
			String exportUrlSel = citationCollection.getUrl(CitationService.REF_TYPE_EXPORT_RIS_SEL);
			String exportUrlAll = citationCollection.getUrl(CitationService.REF_TYPE_EXPORT_RIS_ALL);
			context.put("exportUrlSel", exportUrlSel);
			context.put("exportUrlAll", exportUrlAll);

			Integer listPageSize = (Integer) state.getAttribute(STATE_LIST_PAGE_SIZE);
			if(listPageSize == null)
			{
				listPageSize = defaultListPageSize;
				state.setAttribute(STATE_LIST_PAGE_SIZE, listPageSize);
			}
			context.put("listPageSize", listPageSize);

			CitationIterator newIterator = citationCollection.iterator();
			CitationIterator oldIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
			if(oldIterator == null) {
				newIterator.setPageSize(listPageSize.intValue());
				newIterator.setStart(0);
			} else {
				newIterator.setPageSize(listPageSize.intValue());
				newIterator.setStart(oldIterator.getStart());
//				newIterator.setPage(oldIterator.getPage());
			}
			context.put("citations", newIterator);
			context.put("citationCollectionId", citationCollection.getId());
			if(! citationCollection.isEmpty())
			{
				context.put("show_citations", Boolean.TRUE);

//				int page = newIterator.getPage();
//				int pageSize = newIterator.getPageSize();
				int totalSize = citationCollection.size();

				int start = newIterator.getStart();
				int end = newIterator.getEnd();
//				int start = page * pageSize + 1;
//				int end = Math.min((page + 1) * pageSize, totalSize);

				Integer[] position = { new Integer(start+1) , new Integer(end), new Integer(totalSize)};
				String showing = (String) rb.getFormattedMessage("showing.results", position);
				context.put("showing", showing);
			}
			state.setAttribute(STATE_LIST_ITERATOR, newIterator);
			
			// constant schema identifier
			context.put( "titleProperty", Schema.TITLE );

			int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
			context.put("requestStateId", requestStateId);

			
		}
		
		return TEMPLATE_NEW_RESOURCE;
	}

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
		String citationCollectionId = (String)state.getAttribute(STATE_CITATION_COLLECTION_ID);
		context.put( "citationCollectionId", citationCollectionId );

		int size = 0;
		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null)
		{
			logger.warn( "buildMessagePanelContext unable to access citationCollection " + citationCollectionId );
		}
		else
		{
			size = collection.size();
		}

		// get the size of the list
		context.put( "citationCount", new Integer( size ) );

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
    Caller caller = getCaller(state);

		// always put appropriate bundle in velocity context
		context.put("tlang", rb);
		context.put("stlang", srb);

		// validators
		context.put("TextValidator", new QuotedTextValidator());
		context.put("xilator", new Validator());

		// Set:
		//  * the javascript to run on page load
		//  * the page execution context (resources tool or editor integration)
    switch (caller)
    {
      case EDITOR_INTEGRATION:
    		context.put("sakai_onload", "SRC_initializePageInfo('"
    		        +   ELEMENT_ID_RESULTS_FORM
    		        +   "','"
    		        +   rb.getString("add.results")
    		        +   "'); SRC_verifyWindowOpener();");

   			context.put("editorIntegration", Boolean.TRUE);
   			context.put("resourcesTool", Boolean.FALSE);
        break;

      case RESOURCE_TOOL:
      default:
    		context.put("sakai_onload", "setMainFrameHeight( window.name ); highlightButtonSelections( '" + rb.getString("remove.results") + "' )");

   			context.put("editorIntegration", Boolean.FALSE);
   			context.put("resourcesTool", Boolean.TRUE);

    		// put the citation list title and size
		    putCitationCollectionDetails(context, state);
        break;
    }

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
		context.put( "openUrlLabel", getConfigurationService().getSiteConfigOpenUrlLabel() );

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

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
    	Caller caller = getCaller(state);

    	// always put appropriate bundle in velocity context
    	context.put("tlang", rb);
    	context.put("stlang", srb);

    	// validators
    	context.put("TextValidator", new QuotedTextValidator());
    	context.put("xilator", new Validator());

    	// Set:
    	//  * the javascript to run on page load
    	//  * the page execution context (resources tool or editor integration)
    	switch (caller)
    	{
    	case EDITOR_INTEGRATION:
    		context.put("sakai_onload", "SRC_verifyWindowOpener(); showTopCategory();");

    		context.put("editorIntegration", Boolean.TRUE);
    		context.put("resourcesTool", Boolean.FALSE);
    		break;

    	case RESOURCE_TOOL:
    	default:
    		context.put("sakai_onload", "setMainFrameHeight( window.name ); showTopCategory();");

    	context.put("editorIntegration", Boolean.FALSE);
    	context.put("resourcesTool", Boolean.TRUE);

    	// put citation list title/size
    	putCitationCollectionDetails(context, state);
    	break;
    	}

    	// resource-related
    	String resourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
    	String guid = getContentService().getUuid(resourceId);
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
    	context.put( "openUrlLabel", getConfigurationService().getSiteConfigOpenUrlLabel() );

    	// object arrays for formatted messages
    	Object[] instrArgs = { rb.getString( "submit.search" ) };
    	context.put( "instrArgs", instrArgs );

    	String searchType = null;

    	if (searchInfo != null)
    	{
    		searchType = searchInfo.getSearchType();
    	}

    	if (searchType == null)
    		searchType = ActiveSearch.BASIC_SEARCH_TYPE;

    	context.put("searchType", searchType);


		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
		context.put("stlang", srb);

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
		String citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
		context.put("citationId", citationId);
		context.put("citationCollectionId", citationCollectionId);

		List schemas = getCitationService().getSchemas();
		context.put("TEMPLATES", schemas);

		context.put("DEFAULT_TEMPLATE", citation.getSchema());

		int requestStateId = preserveRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX});
		context.put("requestStateId", requestStateId);

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
    	SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = getSessionManager().getCurrentToolSession();
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if (pipe == null)
		{
			logger.warn( "doFinish() pipe = null");

			setMode(state, Mode.ERROR_FATAL);

			return;
		}

		int citationCount = 0;

//		if(pipe.getAction().getActionType() == ResourceToolAction.ActionType.CREATE_BY_HELPER)
//		{
//			/* PIPE remove */
////			SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
//
//			SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);
//	    	// delete the temporary resource
//			String temporaryResourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
//			ContentResource tempResource = null;
//			try
//            {
//				// get the temp resource
//	            tempResource = getContentService().getResource(temporaryResourceId);
//
//	            // use the temp resource to 'create' the real resource
//	            pipe.setRevisedContent(tempResource.getContent());
//
//	            // remove the temp resource
//	            if( getCitationService().allowRemoveCitationList( temporaryResourceId ) )
//	            {
//	            	// setup a SecurityAdvisor
//	            	CitationListSecurityAdviser advisor = new CitationListSecurityAdviser(
//		            		getSessionManager().getCurrentSessionUserId(),
//		            		ContentHostingService.AUTH_RESOURCE_REMOVE_ANY,
//		            		tempResource.getReference() );
//
//	            	try {
//	            		securityService.pushAdvisor(advisor);
//	            		
//			            // remove temp resource
//			            getContentService().removeResource(temporaryResourceId);
//	            	} catch(Exception e) {
//	            		logger.warn("Exception removing temporary resource for a citation list: " + temporaryResourceId + " --> " + e);
//	            	} finally {
//			            // pop advisor
//			            securityService.popAdvisor(advisor);
//	            	}
//	            	
//		            tempResource = null;
//	            }
//            }
//            catch (PermissionException e)
//            {
//
//	            logger.warn("PermissionException ", e);
//            }
//            catch (IdUnusedException e)
//            {
//
//	            logger.warn("IdUnusedException ", e);
//            }
//            catch (TypeException e)
//            {
//
//	            logger.warn("TypeException ", e);
//            }
////          catch (InUseException e)
////          {
////
////	            logger.warn("InUseException ", e);
////          }
//            catch (ServerOverloadException e)
//            {
//
//	            logger.warn("ServerOverloadException ", e);
//            }
//	        catch (Exception e)
//	        {
//
//		        logger.warn("Exception ", e);
//	        }
//		}

//		// set content (mime) type
//		pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
//        pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_TYPE, ResourceType.MIME_TYPE_HTML);
//
//		// set the alternative_reference to point to reference_root for CitationService
//		pipe.setRevisedResourceProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, CitationService.REFERENCE_ROOT);

		/* PIPE remove */
//		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		// get the collection we're now working on
		CitationCollection collection = getCitationCollection(state, true);
		if(collection == null) {
			// error
		} else {
		
			String citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
	
			String[] args = new String[]{ Integer.toString(collection.size()) };
			String size_str = rb.getFormattedMessage("citation.count",  args);
	    	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_LENGTH, size_str);
	
	    	// leave helper mode
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
	
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
			toolSession.removeAttribute(CitationHelper.CITATION_HELPER_INITIALIZED);
	
			cleanup(toolSession, CitationHelper.CITATION_PREFIX, state);
	
			// Remove session sort
			state.removeAttribute(STATE_SORT);
	
			// Remove session collection
			state.removeAttribute(STATE_CITATION_COLLECTION_ID);
			state.removeAttribute(STATE_CITATION_COLLECTION);
	
			state.removeAttribute("fromListPage");
		}

	}	// doFinish

    /**
     * Cancel the action for which the helper was launched.
     */
    public void doCancel(RunData data)
    {
    	SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = getSessionManager().getCurrentToolSession();
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if (pipe == null)
		{
			logger.warn( "doCancel() pipe = null");

			setMode(state, Mode.ERROR_FATAL);

			return;
		}

		if(pipe.getAction().getActionType() == ResourceToolAction.ActionType.CREATE_BY_HELPER)
		{
			// TODO: delete the citation collection and all citations

	    	// TODO: delete the temporary resource
//			String temporaryResourceId = (String) state.getAttribute(CitationHelper.RESOURCE_ID);
//			ContentResourceEdit edit = null;
//			try
//            {
//	            edit = getContentService().editResource(temporaryResourceId);
//	            getContentService().removeResource(edit);
//	            edit = null;
//            }
//            catch (PermissionException e)
//            {
//
//	            logger.warn("PermissionException ", e);
//            }
//            catch (IdUnusedException e)
//            {
//
//	            logger.warn("IdUnusedException ", e);
//            }
//            catch (TypeException e)
//            {
//
//	            logger.warn("TypeException ", e);
//            }
//            catch (InUseException e)
//            {
//
//	            logger.warn("InUseException ", e);
//            }
//
//            if(edit != null)
//            {
//            	getContentService().cancelResource(edit);
//            }
		}

    	// leave helper mode
		pipe.setActionCanceled(false);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);
		pipe.setRevisedMimeType(pipe.getMimeType());

		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		toolSession.removeAttribute(CitationHelper.CITATION_HELPER_INITIALIZED);

		state.removeAttribute("fromListPage");

		cleanup(toolSession, CitationHelper.CITATION_PREFIX, state);

    }

	/**
	* Adds a citation to the current citation collection.  Called from the search-results popup.
	*/
	public void doAddCitation ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// get the citation from search results, add it to the citation collection, and rebuild the context
		String[] citationIds = params.getStrings("citationId");
		String citationCollectionId = params.getString("citationCollectionId");

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
		if(permCollection == null) {
			// error
		} else {
			
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
			        logger.warn("doAdd: unable to add citation " + citationIds[i] + " to collection " + citationCollectionId);
				}
			}
	        getCitationService().save(permCollection);
		}
        // setMode(state, Mode.LIST);
 	}

	/**
	* Removes a citation from the current citation collection.  Called from the search-results popup.
	*/
	public void doRemove ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// get the citation number from search results, remove it from the citation collection, and rebuild the context
		// get the citation from search results, add it to the citation collection, and rebuild the context
		String[] citationIds = params.getStrings("citationId");
		String citationCollectionId = params.getString("citationCollectionId");

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		CitationCollection tempCollection = search.getSearchResults();
		Map index = search.getIndex();
		if(index == null)
		{
			index = new Hashtable();
			search.setIndex(index);
		}

		CitationCollection permCollection = getCitationCollection(state, true);
		if(permCollection == null) {
			// error
		} else {
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
			        logger.warn("doAdd: unable to add citation " + citationIds[i] + " to collection " + citationCollectionId);
				}
			}
	        getCitationService().save(permCollection);
		}
  		//setMode(state, Mode.LIST);
  		setMode(state, Mode.NEW_RESOURCE);
	}

	public void doDatabasePopulate( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// get category id
		String categoryId = params.get( "categoryId" );
		if(logger.isDebugEnabled()) {
			logger.debug( "doDatabasePopulate() categoryId from URL: " + categoryId );
		}
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
	 * @param data
	 */
	public void doImportPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);
		
		this.preserveEntityIds(params, state);

		setMode(state, Mode.IMPORT_CITATIONS);

	}	// doImportPage

	/**
	 *
	 * @param data
	 */
	public void doImport ( RunData data)
	{
		logger.debug( "doImport called.");

		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		Iterator iter = params.getNames();

		String param = null;

		while (iter.hasNext())
		{
			param = (String) iter.next();
			if(logger.isDebugEnabled()) {
				logger.debug( "param = " + param);
				logger.debug( param + " value = " + params.get(param));
			}
		}

		String citationCollectionId = params.getString("citationCollectionId");

		if(citationCollectionId == null)
		{
			citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
		}

		CitationCollection collection = null;
        collection = getCitationCollection(state, false);

		String ristext = params.get("ristext");

		// We're going to read the RIS file from the submitted form's textarea first. If that's
		// empty we'll read it from the uploaded file.  We'll crate a BufferedReader in either
		// circumstance so that the parsing code need not know where the ris text came from.
		java.io.BufferedReader bread = null;

		if (ristext.trim().length() > 0) // form has text in the risimport textarea
		{
			java.io.StringReader risStringReader = new java.io.StringReader(ristext);
			bread = new java.io.BufferedReader(risStringReader);
			logger.debug( "String buffered reader ready");

		} // end RIS text is in the textarea
		else // textarea empty, set the read of the import from the file
		{
		  String upload = params.get("risupload");
		  if(logger.isDebugEnabled()) {
			  logger.debug( "Upload String = " + upload);
		  }

		  FileItem risImport = params.getFileItem("risupload");

		  if (risImport == null)
		  {
			logger.debug( "risImport is null.");
			return;
		  }

		  if(logger.isDebugEnabled()) {
			  logger.debug("Filename = " + risImport.getFileName());
		  }


	      InputStream risImportStream = risImport.getInputStream();

			// Attempt to detect the encoding of the file.
			BOMInputStream irs = new BOMInputStream(risImportStream);
		
			// below is needed if UTF-8 above is commented out
			Reader isr = null;
			String bomCharsetName = null;
			try
			{
				 bomCharsetName = irs.getBOMCharsetName();
				if (bomCharsetName != null)
				{
					isr = new InputStreamReader(risImportStream, bomCharsetName);
				}
			} catch (UnsupportedEncodingException uee)
			{
				// Something strange as the JRE should support all the formats.
				if(logger.isInfoEnabled()) {
					logger.info("Problem using character set when importing RIS: "+ bomCharsetName);
				}
			}
			catch (IOException ioe)
			{
				// Probably won't get any further, but may as well try.
				if(logger.isDebugEnabled()) {
					logger.debug("Problem reading the character set from RIS import: "+ ioe.getMessage());
				}
			}
			// Fallback to platform default
			if (isr == null) {
				isr = new InputStreamReader(irs);
			}


			bread = new java.io.BufferedReader(isr);
		} // end set the read of the import from the uploaded file.

		// The below code is a major work in progress.
		// This code is for demonstration purposes only. No gambling or production use!

		StringBuilder fileString = new StringBuilder();
		String importLine = null;
		java.util.List importList = new java.util.ArrayList();

		// Read the BufferedReader and populate the importList. Each entry in the list
		// is a line in the RIS import "file".
		try
		{
			while ((importLine = bread.readLine()) != null)
			{
				importLine = importLine.trim();

				if (importLine != null && importLine.length() > 2)
				{
				  importList.add(importLine);
				  if(logger.isDebugEnabled()) {
					  fileString.append("\n");
					  fileString.append(importLine);
				  }
				}

			} // end while
		} // end try
		catch(Exception e)
		{
			logger.debug("ISR error = " + e);
		} // end catch
		finally {
		    if (bread != null) {
		        try {
                    bread.close();
                } catch (IOException e) {
                    // tried
                }
		    }
		}

		if(logger.isDebugEnabled()) {
			logger.debug("fileString = \n" + fileString.toString());
		}


		// tempList holds the entries read in to make a citation up to and
		// including the ER entry from importList
		List tempList = new java.util.ArrayList();

		Citation importCitation = getCitationService().getTemporaryCitation();
		CitationCollection importCollection = getCitationService().getTemporaryCollection();

		int sucessfullyReadCitations = 0;
		int totalNumberCitations = 0;

		// Read each entry in the RIS List and build a citation
		for(int i=0; i< importList.size(); i++)
		{
			String importEntryString = (String) importList.get(i);
//			logger.debug("Import line (#1) = " + importEntryString);
//			logger.debug("Substring is = " + importEntryString.substring(0, 2));
			tempList.add(importEntryString);

			// make sure importEntryString can be tested for "ER" existence. It could
			// be a dinky invalid line less than 2 characters.
			if (importEntryString != null && importEntryString.length() > 1 &&
				importEntryString.substring(0, 2).equalsIgnoreCase("ER"))
			{
				// end of citation (signaled by ER).

				totalNumberCitations++;
				if(logger.isDebugEnabled()) {
					logger.debug("------> Trying to add citation " + totalNumberCitations);
				}
				if (importCitation.importFromRisList(tempList)) // import went well
				{
					importCollection.add(importCitation);
					sucessfullyReadCitations++;
				}
				tempList.clear();
				importCitation = getCitationService().getTemporaryCitation();
			}
		} // end for

		if(logger.isDebugEnabled()) {
			logger.debug("Done reading in " + sucessfullyReadCitations + " / " + totalNumberCitations + " citations.");
		}

		collection.addAll(importCollection);
        getCitationService().save(collection);

        // remove collection from state
        state.removeAttribute(STATE_CITATION_COLLECTION);


		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);
	} // end doImport()
	
	public void doCreateResource(RunData data) 
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		setMode(state, Mode.NEW_RESOURCE);
		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);

	}

	/**
	*
	*/
	public void doCreate ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		setMode(state, Mode.CREATE);
		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);

	}	// doCreate

	/**
	 * Fetch the calling application
	 * @param state The session state
	 * @return The calling application (default to Resources if nothing is set)
	 */
	protected Caller getCaller(SessionState state)
	{
		Caller caller = (Caller) state.getAttribute(CITATIONS_HELPER_CALLER);

		return (caller == null) ? Caller.RESOURCE_TOOL : Caller.EDITOR_INTEGRATION;
	}

	/**
	 * Set the calling applcation
	 * @param state The session state
	 * @param caller The calling application
	 */
	protected void setCaller(SessionState state, Caller caller)
	{
		state.setAttribute(CITATIONS_HELPER_CALLER, caller);
	}

	/**
	 * @param state
	 * @param new_mode
	 */
	protected void setMode(SessionState state, Mode new_mode)
	{
		// set state attributes
		state.setAttribute( CitationHelper.STATE_HELPER_MODE, new_mode );
	}

	/**
	 *
	 */
	public void doCreateCitation ( RunData data)
	{
		// get the state object and the parameter parser
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		Set validPropertyNames = getCitationService().getValidPropertyNames();
		String mediatype = params.getString("type");

		CitationCollection collection = getCitationCollection(state, true);
		if(collection == null) {
			// error
		} else {
			// create a citation
			Citation citation = getCitationService().addCitation(mediatype);
	
			updateCitationFromParams(citation, params);
	
			// add citation to current collection
			collection.add(citation);
			getCitationService().save(collection);
		}
		// call buildListPanelContext to show updated list
		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);
		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

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

        // clear preferredUrl - if there is one, we will find it after looping
        // through all customUrls below
        citation.setPreferredUrl( null );
        String id = null;

        for(int i = 0; i < urlCount; i++)
        {
        	String label = params.getString("label_" + i);

        	String url = params.getString("url_" + i);

        	String urlid = params.getString("urlid_" + i);

        	String preferred = params.getString( "pref_" + i );

        	String addPrefix = params.getString( "addprefix_" + i );

         	if(url == null)
        	{
         		if(logger.isDebugEnabled()) {
         			logger.debug("doCreateCitation: url null? " + url);
         		}
        	}
        	else
        	{
	            try
	            {
	            	url = validateURL(url);
	            }
	            catch (MalformedURLException e)
	            {
	            	if(logger.isDebugEnabled()) {
	            		logger.debug("doCreateCitation: unable to validate URL: " + url);
	            	}
		            continue;
	            }
        	}

        	if(label == null || url == null)
        	{
        		if(logger.isDebugEnabled()) {
        			logger.debug("doCreateCitation: label null? " + label + " url null? " + url);
        		}
        		continue;
        	}
        	else if(urlid == null || urlid.trim().equals(""))
        	{
        		id = citation.addCustomUrl(label, url, addPrefix);

        		if( preferred != null && !preferred.trim().equals( "" ) )
        		{
        			// this customUrl is the new preferredUrl
        			citation.setPreferredUrl( id );
        		}
        	}
        	else
        	{
        		// update an existing customUrl
        		citation.updateCustomUrl(urlid, label, url, addPrefix);

            	if( preferred != null && !preferred.trim().equals( "" ) )
        		{
            		// this customUrl is the new preferredUrl
        			citation.setPreferredUrl( urlid );
        		}
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

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationId = params.getString("citationId");
		String citationCollectionId = params.getString("citationCollectionId");


		CitationCollection collection = getCitationCollection(state, true);
		if(collection == null) {
			// error
		} else {
		
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
		}

	}	// doEdit

	/**
	*
	*/
	public void doList ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

	}	// doList

	public void doResults( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		setMode(state, Mode.RESULTS);
	}

	/**
	*
	*/
	public void doAddCitations ( RunData data)
	{
		logger.debug("doAddCitations()");
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);
		preserveEntityIds(params, state);
		
		//setMode(state, Mode.ADD_CITATIONS);
		setMode(state, Mode.NEW_RESOURCE);
		
		logger.debug("doAddCitations()");

	}	// doAddCitations

	public void doMessageFrame(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// get params
		String citationId = params.getString("citationId");
		String citationCollectionId = params.getString("citationCollectionId");
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

		if( operation == null || citationId == null || citationCollectionId == null )
		{
			logger.warn( "doMessageFrame() null argument - operation: " +
					operation + ", citationId: " + citationId + ", " +
							"citationCollectionId: " + citationCollectionId );
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

		// get CitationCollection using citationCollectionId
		CitationCollection collection = getCitationCollection(state, false);
		if(collection == null) {
			logger.warn( "doMessageFrame() unable to access citationCollection " + citationCollectionId );
		} else {

			// do operation
			if(operation.equalsIgnoreCase("add"))
			{
				if(logger.isDebugEnabled()) {
					logger.debug("adding citation " + citationId + " to " + citationCollectionId);
				}
				citation.setAdded( true );
				collection.add( citation );
				getCitationService().save(collection);
			}
			else if(operation.equalsIgnoreCase("remove"))
			{
				if(logger.isDebugEnabled()) {
					logger.debug("removing citation " + citationId + " from " + citationCollectionId);
				}
				collection.remove( citation );
				citation.setAdded( false );
				getCitationService().save(collection);
			}
			else
			{
				// do nothing
				if(logger.isDebugEnabled()) {
					logger.debug("null operation: " + operation);
				}
			}
	
			// store the citation's new id to send back to UI
			state.setAttribute( STATE_CITATION_ID, citation.getId() );
		}
		setMode(state, Mode.MESSAGE);
	}

	public void doRemoveAllCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationCollectionId = params.getString("citationCollectionId");

		CitationCollection collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("CitationHelperAction.doRemoveCitation collection null: " + citationCollectionId);
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
				getCitationService().save(collection);
			}
		}

		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

	}  // doRemoveAllCitations
	
	public void doImportCitationFromResourceUrl( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
	    ParameterParser params = data.getParameters();
	    String resourceUrl = params.getString("resourceUrl");
	
	    CitationCollection collection = getCitationCollection(state, false);
	        
	    if(resourceUrl != null)
	    {
	        if(logger.isDebugEnabled()) logger.debug("RESOURCE URL: " + resourceUrl);
	        
	        String resourceId = resourceUrl.substring(resourceUrl.indexOf("/group"));
	        
	        if(logger.isDebugEnabled()) logger.debug("RESOURCE ID: " + resourceId);
	        
	        ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
	        try
	        {
	            ContentResource resource = contentService.getResource(resourceId);
	            ResourceProperties props = resource.getProperties();
	            String displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
	            Citation citation = citationService.addCitation("unknown");
	            citation.setDisplayName(displayName);
	            citation.setCitationProperty("resourceId", resourceId);
	            //User user = UserDirectoryService.getUser(props.getProperty(ResourceProperties.PROP_CREATOR));
	            //citation.setCitationProperty(Schema.CREATOR,user.getLastName() + ", " + user.getFirstName());
	            String urlId = citation.addCustomUrl(resourceUrl, resourceUrl);
	            citation.setPreferredUrl(urlId);
	            collection.add(citation);
	            citationService.save(collection);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	          
	    state.setAttribute("sort", CitationCollection.SORT_BY_TITLE);
	           
	    setMode(state, Mode.LIST);

	} // doImportCitationsFromResourceUrl
	
	public void doShowReorderCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		setMode(state, Mode.REORDER);

	}  // doShowReorderCitations
	
	public void doReorderCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();
		String orderedCitationIds = params.getString("orderedCitationIds");
		
		CitationCollection collection = getCitationCollection(state, false);
		
		String[] splitIds = orderedCitationIds.split(",");
		
		try
		{
			for(int i = 1;i <= splitIds.length;i++)
			{
				collection.getCitation(splitIds[i - 1]).setPosition(i);
			}
			getCitationService().save(collection);
		}
		catch(IdUnusedException iue)
		{
			logger.error("One of the supplied citation ids was invalid. The new order was not saved.");
		}
		
		// Had to do this to force a reload from storage in buildListPanelContext
		state.removeAttribute(STATE_CITATION_COLLECTION);
		
	    state.setAttribute(STATE_SORT, CitationCollection.SORT_BY_POSITION);
		
		setMode(state, Mode.NEW_RESOURCE);

	}  // doReorderCitations

	public void doRemoveSelectedCitations( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationCollectionId = params.getString("citationCollectionId");

		CitationCollection collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("doRemoveSelectedCitation() collection null: " + citationCollectionId);
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
					getCitationService().save(collection);
				}
				catch( IdUnusedException e )
				{
					logger.warn("doRemoveSelectedCitation() unable to get and remove citation", e );
				}
			}
		}

		state.setAttribute( STATE_LIST_NO_SCROLL, Boolean.TRUE );
		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

	}  // doRemoveSelectedCitations

	/**
	*
	*/
	public void doReviseCitation (RunData data)
	{
		// get the state object and the parameter parser
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// Set validPropertyNames = getCitationService().getValidPropertyNames();
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
		            Schema schema = getCitationService().getSchema(schemaId);
		            citation.setSchema(schema);

		    		updateCitationFromParams(citation, params);

		       		// add citation to current collection
		    		collection.saveCitation(citation);
		        }
	            catch (IdUnusedException e)
	            {
		            // TODO add alert and log error
	            }

	       		getCitationService().save(collection);
			}
 		}

		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

	}	// doReviseCitation

	/**
	 *
	 * @param data
	 */
	public void doCancelSearch( RunData data )
	{
		// get state and params
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

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

	}  // doCancelSearch

	/**
	 * Resources Tool/Citation Helper search
	 * @param data Runtime data
	 */
	public void doSearch ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		logger.debug("doSearch()");

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		//doSearchCommon(state, Mode.ADD_CITATIONS);
		doSearchCommon(state, Mode.NEW_RESOURCE);
	}

	/**
	 * Common "doSearch()" support
	 * @param state Session state
	 * @param errorMode Next mode to set if we have database hierarchy problems
	 */
	protected void doSearchCommon(SessionState state, Mode errorMode)
	{
		// remove attributes from an old search session, if any
		state.removeAttribute( STATE_SEARCH_RESULTS );
		state.removeAttribute( STATE_CURRENT_RESULTS );
		state.removeAttribute( STATE_KEYWORDS );

		// indicate a basic search
		state.setAttribute( STATE_BASIC_SEARCH, new Object() );

		try
		{
			SearchDatabaseHierarchy hierarchy = getSearchManager().getSearchHierarchy();
			if (hierarchy == null)
			{
				addAlert(state, rb.getString("search.problem"));
				setMode(state, errorMode);
				return;
			}

			state.setAttribute(STATE_SEARCH_HIERARCHY, hierarchy);
			setMode(state, Mode.SEARCH);
		}
		catch (SearchException exception)
		{
			String error = exception.getMessage();

			if ((error == null) || (error.length() == 0))
			{
				error = rb.getString("search.problem");
			}
			addAlert(state, error);
			setMode(state, Mode.ERROR);
		}
	}

	/**
	 *
	 */
	public void doBeginSearch ( RunData data)
	{
		// get state and params
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		// get search object from state
		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_INFO);
		if(search == null)
		{
			logger.debug( "doBeginSearch() got null ActiveSearch from state." );
			search = getSearchManager().newSearch();
		}

		// get databases selected
		String[] databaseIds = params.getStrings( "databasesSelected" );

		// check the databases to make sure they are indeed searchable by this user
		if( databaseIds != null )
		{
			if(logger.isDebugEnabled())
			{
				logger.debug( "Databases selected:" );
				for( String databaseId : databaseIds )
				{
					logger.debug( "  " + databaseId );
				}
			}

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

		// check for a cancel
    	String cancel = params.getString( "cancelOp" );
    	if( cancel != null && !cancel.trim().equals("") )
    	{
    		if( cancel.equalsIgnoreCase( ELEMENT_ID_RESULTS_FORM ) )
    		{
    			state.setAttribute( STATE_CANCEL_PAGE, Mode.RESULTS );
    		}
    		else
    		{
    			state.setAttribute( STATE_CANCEL_PAGE, Mode.SEARCH );
    		}
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
	    }
	    catch(SearchException se)
	    {
	    	// either page indices are off or there has been a metasearch error

	    	// do some logging & find the proper alert message
	    	StringBuilder alertMsg = new StringBuilder( se.getMessage() );
	    	logger.warn("doBeginSearch() SearchException: " + alertMsg );

	    	if( search.getStatusMessage() != null && !search.getStatusMessage().trim().equals("") )
	    	{
	    		logger.warn( " |-- nested metasearch error: " + search.getStatusMessage() );
	    		alertMsg.append( " (" + search.getStatusMessage() + ")" );
	    	}

	    	// add an alert and set the next mode
	    	addAlert( state, alertMsg.toString() );
	    	state.setAttribute( STATE_NO_RESULTS, Boolean.TRUE );
			setMode(state, Mode.RESULTS);
			return;
	    }
	    catch( SearchCancelException sce )
	    {
	    	logger.debug( "doBeginSearch() SearchCancelException: user cancelled search" );
	    	setMode( state, (Mode)state.getAttribute(STATE_CANCEL_PAGE) );
	    }

	    ActiveSearch newSearch = getSearchManager().newSearch();
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
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);
			if(collection == null) {
				// error
			} else {
				listIterator = collection.iterator();
				state.setAttribute(STATE_LIST_ITERATOR, listIterator);
			}
		}
		if(listIterator != null && listIterator.hasNextPage())
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
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);
			if(collection == null) {
				// error
			} else {
				listIterator = collection.iterator();
				state.setAttribute(STATE_LIST_ITERATOR, listIterator);
			}
		}
		if(listIterator != null && listIterator.hasPreviousPage())
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
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		CitationCollection collection = getCitationCollection(state, true);
		if(collection == null) {
			// error
		} else {
			CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
			if(listIterator == null)
			{
				listIterator = collection.iterator();
				state.setAttribute(STATE_LIST_ITERATOR, listIterator);
			} else {
	
				int pageSize = listIterator.getPageSize();
				int totalSize = collection.size();
				int lastPage = 0;
		
				listIterator.setStart(totalSize - pageSize);
			}
		}

 	}	// doSearch

	/**
	*
	*/
	public void doFirstListPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();
		
		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		if(state.getAttribute(CitationHelper.RESOURCE_ID) == null) {
			String resourceId = params.get("resourceId");
			if(resourceId == null || resourceId.trim().equals("")) {
				String resourceUuid = (String) state.getAttribute(CitationHelper.RESOURCE_UUID);
				if(resourceUuid == null || resourceUuid.trim().equals("")) {
					resourceUuid = params.get("resourceUuid");
				}
				if(resourceUuid == null || resourceUuid.trim().equals("")) {
					// Error? We can't identify resource
				} else {
					resourceId = this.getContentService().resolveUuid(resourceUuid);
					state.setAttribute(CitationHelper.RESOURCE_ID, resourceId);
					state.setAttribute(CitationHelper.RESOURCE_UUID, resourceUuid);
				}
			} else {
				state.setAttribute(CitationHelper.RESOURCE_ID, resourceId);
			}
		}

		CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
		if(listIterator == null)
		{
			CitationCollection collection = getCitationCollection(state, true);
			if(collection == null) {
				// error
			} else {
				listIterator = collection.iterator();
				state.setAttribute(STATE_LIST_ITERATOR, listIterator);
			}
		}
		if(listIterator != null) {
			listIterator.setStart(0);
		}

 	}	// doSearch

	/**
	*
	*/
	public void doNextSearchPage ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = getSearchManager().newSearch();
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
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = getSearchManager().newSearch();
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
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = getSearchManager().newSearch();
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

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		ActiveSearch search = (ActiveSearch) state.getAttribute(STATE_SEARCH_RESULTS);
		if(search == null)
		{
			search = getSearchManager().newSearch();
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

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		int pageSize = params.getInt( "newPageSize" );
		if(pageSize < 1) {
			// check for top or bottom page selector
			String pageSelector = params.get( "pageSelector" );
			if( pageSelector.equals( "top" ) )
			{
				pageSize = params.getInt( "pageSizeTop" );
			}
			else
			{
				pageSize = params.getInt("pageSizeBottom");
			}
		}

		if(pageSize > 0)
		{
			state.setAttribute(STATE_LIST_PAGE_SIZE, new Integer(pageSize));
			CitationIterator tempIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
			tempIterator.setPageSize(pageSize);
			state.removeAttribute(STATE_LIST_ITERATOR);
			state.setAttribute(STATE_LIST_ITERATOR, tempIterator);
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

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationId = params.getString("citationId");
		String citationCollectionId = params.getString("citationCollectionId");

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

	/**
	 * This method is used to ensure the Citations Helper is not invoked by
	 * the Resources tool in a state other than ADD_CITATIONS or LIST.  It uses
	 * a simple state machine to accomplish this
	 *
	 * @return the Mode that the Citations Helper should be in
	 */
	protected Mode validateState()
	{

		return null;
	}


	/**
	 * This method is called upon each Citations Helper request to properly
	 * initialize the Citations Helper in case of a null Mode.  Returns true if
	 * succeeded, false otherwise
	 *
	 * @param state
	 */
	protected boolean initHelper(SessionState state)
	{
		logger.debug("initHelper()");
		Mode mode;

		/*
		 * Editor Integration support
		 */
		if (getCaller(state) == Caller.EDITOR_INTEGRATION)
		{
			mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);

			if (mode == null)
			{
				if(logger.isDebugEnabled()) {
					logger.debug("initHelper(): mode is undefined, using " + Mode.NEW_RESOURCE);
				}
				setMode(state, Mode.NEW_RESOURCE);
			}

			if (state.getAttribute(STATE_RESULTS_PAGE_SIZE) == null)
			{
				if(logger.isDebugEnabled()) {
					logger.debug("initHelper(): result page size is undefined, using " 
							+    DEFAULT_RESULTS_PAGE_SIZE);
				}
						
				state.setAttribute(STATE_RESULTS_PAGE_SIZE, DEFAULT_RESULTS_PAGE_SIZE);
			}

			return true;
		}

		/*
		 * Resources Tool support
		 */
		ToolSession toolSession = getSessionManager().getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		// TODO: if not entering as a helper, will we need to create pipe???

		if (pipe == null)
		{
			logger.warn( "initHelper() pipe = null");

			setMode(state, Mode.ERROR_FATAL);

			return true;

		}

		if(pipe.isActionCompleted())
		{
			return true;
		}

		/*
		 * Resources Tool/Citation Helper support
		 */

		if( toolSession.getAttribute(CitationHelper.CITATION_HELPER_INITIALIZED) == null )
		{
			// we're starting afresh: an action has been clicked in Resources

			// set the Mode according to our action
			switch(pipe.getAction().getActionType())
			{
			//case CREATE:
			case CREATE_BY_HELPER:
//				ContentResource tempResource = createTemporaryResource(pipe);
//
//				// tempResource could be null if exception encountered
//				if( tempResource == null )
//				{
//					// leave helper
//					pipe.setActionCompleted( true );
//					toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
//					toolSession.removeAttribute(CitationHelper.CITATION_HELPER_INITIALIZED);
//					cleanup( toolSession, CitationHelper.CITATION_PREFIX, state);
//
//					return false;
//				}

//				state.setAttribute(CitationHelper.RESOURCE_ID, tempResource.getId());
//
//				String displayName = tempResource.getProperties().getProperty( org.sakaiproject.entity.api.ResourceProperties.PROP_DISPLAY_NAME );
//				state.setAttribute( STATE_COLLECTION_TITLE , displayName );
//
//				try
//				{
//					state.setAttribute(STATE_COLLECTION_ID, new String(tempResource.getContent()));
//				}
//				catch (ServerOverloadException e)
//				{
//					logger.warn("ServerOverloadException ", e);
//				}
				state.setAttribute( STATE_RESOURCES_ADD, Boolean.TRUE );
				//setMode(state, Mode.ADD_CITATIONS);
				setMode(state, Mode.NEW_RESOURCE);
				break;
			case REVISE_CONTENT:
				state.setAttribute(CitationHelper.RESOURCE_ID, pipe.getContentEntity().getId());
				try
				{
					state.setAttribute(STATE_CITATION_COLLECTION_ID, new String(((ContentResource) pipe.getContentEntity()).getContent()));
				}
				catch (ServerOverloadException e)
				{
					logger.warn("ServerOverloadException ", e);
				}
				state.removeAttribute( STATE_RESOURCES_ADD );
				setMode(state, Mode.NEW_RESOURCE);
				break;
			default:
				break;
			}

			// set Citations Helper to "initialized"
			//pipe.setInitializationId( "initialized" );
			toolSession.setAttribute(CitationHelper.CITATION_HELPER_INITIALIZED, Boolean.toString(true));
		}

		else
		{
			// we're in the middle of a Citations Helper workflow:
			// Citations Helper has been "initialized"
			// (pipe.initializationId != null)

			// make sure we have a Mode to display
			mode = (Mode) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
			if( mode == null )
			{
				// default to ADD_CITATIONS
				//setMode( state, Mode.ADD_CITATIONS );
				setMode( state, Mode.NEW_RESOURCE );
			}
		}

		if(state.getAttribute(STATE_RESULTS_PAGE_SIZE) == null)
		{
			state.setAttribute(STATE_RESULTS_PAGE_SIZE, DEFAULT_RESULTS_PAGE_SIZE);
		}

		if(state.getAttribute(STATE_LIST_PAGE_SIZE) == null)
		{
			state.setAttribute(STATE_LIST_PAGE_SIZE, defaultListPageSize);
		}

		return true;

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
			ContentResourceEdit newItem = getContentService().addResource(pipe.getContentEntity().getId(), rb.getString("new.citations.list"), null, ContentHostingService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			newItem.setResourceType(getCitationService().CITATION_LIST_ID);
			newItem.setContentType( ResourceType.MIME_TYPE_HTML );
			//newItem.setHidden();

			ResourcePropertiesEdit props = newItem.getPropertiesEdit();

			// set the alternative_reference to point to reference_root for CitationService
			props.addProperty(getContentService().PROP_ALTERNATE_REFERENCE, CitationService.REFERENCE_ROOT);
			props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, ResourceType.MIME_TYPE_HTML);
			props.addProperty(getCitationService().PROP_TEMPORARY_CITATION_LIST, Boolean.TRUE.toString());

			CitationCollection collection = getCitationService().addCollection();
			newItem.setContent(collection.getId().getBytes());
			newItem.setContentType(ResourceType.MIME_TYPE_HTML);

			getContentService().commitResource(newItem, NotificationService.NOTI_NONE);

			return newItem;
        }
        catch (PermissionException e)
        {
            logger.warn("PermissionException ", e);
        }
        catch (IdUniquenessException e)
        {
            logger.warn("IdUniquenessException ", e);
        }
        catch (IdLengthException e)
        {
            logger.warn("IdLengthException ", e);
        }
        catch (IdInvalidException e)
        {
            logger.warn("IdInvalidException ", e);
        }
        catch (IdUnusedException e)
        {
            logger.warn("IdUnusedException ", e);
        }
        catch (OverQuotaException e)
        {
            logger.warn( e.getMessage() );

            // send an error back to Resources
            pipe.setErrorEncountered( true );
            pipe.setErrorMessage( rb.getString( "action.create.quota" ) );
        }
        catch (ServerOverloadException e)
        {
            logger.warn("ServerOverloadException ", e);
        }

 	    return null;
    }

	protected String validateURL(String url) throws MalformedURLException
	{
		if (url == null || url.trim().equals (""))
		{
			throw new MalformedURLException();
		}

		url = url.trim();

		// does this URL start with a transport?
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

				StringBuilder buf = new StringBuilder();
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

  	/**
	   * Return a string that is safe to place into a JavaScript value wrapped
	   * in single quotes.  In addition, all double quotes (") are replaced by
	   * the entity <i>&quot</i>.
	   *
		 * @param string The original string
		 * @return The [possibly] escaped string
		 */
		public static String escapeHtmlAndJsQuoted(String string)
		{
		  String escapedText = getFormattedText().escapeJsQuoted(string);

		  return escapedText.replaceAll("\"", "&quot;");
		}
	}

	/**
	 * Cleans up tool state used internally. Useful before leaving helper mode.
	 *
	 * @param toolSession
	 * @param prefix
	 */
	protected void cleanup(ToolSession toolSession, String prefix,
			SessionState sessionState )
	{
		// cleanup everything dealing with citations
		Enumeration attributeNames = toolSession.getAttributeNames();
		while(attributeNames.hasMoreElements())
		{
			String aName = (String) attributeNames.nextElement();
			if(aName.startsWith(prefix))
			{
				toolSession.removeAttribute(aName);
			}
		}

		// re-enable observers
		VelocityPortletPaneledAction.enableObservers(sessionState);
	}

	public void doSortCollection( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationCollectionId = params.getString("citationCollectionId");

		String sort = params.getString("currentSort");
        if(sort == null || sort.trim().equals("")) {
        	sort = CitationCollection.SORT_BY_TITLE;
        }

		CitationCollection collection = null;

		if(citationCollectionId == null)
		{
			citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
		}

		if(logger.isDebugEnabled()) {
			logger.debug("doSortCollection sort type  = " + sort);
		}

        collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("doSortCollection() collection null: " + citationCollectionId);
		}
		else
		{
			// sort the citation list

	        logger.debug("doSortCollection() ready to sort");

	        if (sort.equalsIgnoreCase(CitationCollection.SORT_BY_TITLE))
				  collection.setSort(CitationCollection.SORT_BY_TITLE, true);
	        else if (sort.equalsIgnoreCase(CitationCollection.SORT_BY_AUTHOR))
			       collection.setSort(CitationCollection.SORT_BY_AUTHOR, true);
	        else if (sort.equalsIgnoreCase(CitationCollection.SORT_BY_YEAR))
				   collection.setSort(CitationCollection.SORT_BY_YEAR , true);
	        else if (sort.equalsIgnoreCase(CitationCollection.SORT_BY_POSITION))
				   collection.setSort(CitationCollection.SORT_BY_POSITION , true);

	        state.setAttribute(STATE_SORT, sort);

			Iterator iter = collection.iterator();

			while (iter.hasNext())
			{
				Citation tempCit = (Citation) iter.next();

				if(logger.isDebugEnabled()) {
					logger.debug("doSortCollection() tempcit 1 -------------");
					logger.debug("doSortCollection() tempcit 1 (author) = " + tempCit.getFirstAuthor());
					logger.debug("doSortCollection() tempcit 1 (year)   = " + tempCit.getYear());
					logger.debug("doSortCollection() tempcit 1 = " + tempCit.getDisplayName());
				}
			} // end while

			// set the list iterator to the start of the list after a change in sort
			CitationIterator listIterator = (CitationIterator) state.getAttribute(STATE_LIST_ITERATOR);
			if(listIterator != null)
			{
				listIterator.setStart(0);
			}

		} // end else

		//setMode(state, Mode.LIST);
		setMode(state, Mode.NEW_RESOURCE);

	}  // doSortCollection

	public void doSaveCollection(RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters();

		int requestStateId = params.getInt("requestStateId", 0);
		restoreRequestState(state, new String[]{CitationHelper.RESOURCES_REQUEST_PREFIX, CitationHelper.CITATION_PREFIX}, requestStateId);

		String citationCollectionId = params.getString("citationCollectionId");

		CitationCollection collection = null;

		if(citationCollectionId == null)
		{
			citationCollectionId = (String) state.getAttribute(STATE_CITATION_COLLECTION_ID);
		}

        collection = getCitationCollection(state, false);

		if(collection == null)
		{
			// TODO add alert and log error
	        logger.warn("doSaveCollection() collection null: " + citationCollectionId);
	        return;
		}
		else
		{
			// save the collection (this will persist the sort order to the db)
	        getCitationService().save(collection);

	        String sort = collection.getSort();

	        if (sort != null)
	          state.setAttribute(STATE_SORT, sort);

			//setMode(state, Mode.LIST);
			setMode(state, Mode.NEW_RESOURCE);

		}
	} // end doSaveCollection

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

	// temporary -- replace with a method in content-util
	public static int preserveRequestState(SessionState state, String[] prefixes)
	{
		Map requestState = new HashMap();

		int requestStateId = 0;
		while(requestStateId == 0)
		{
			requestStateId = (int) (Math.random() * Integer.MAX_VALUE);
		}

		List<String> attrNames = state.getAttributeNames();
		for(String attrName : attrNames)
		{
			for(String prefix : prefixes)
			{
				if(attrName.startsWith(prefix))
				{
					requestState.put(attrName,state.getAttribute(attrName));
					break;
				}
			}
		}

		Object pipe = state.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			requestState.put(ResourceToolAction.ACTION_PIPE, pipe);
		}

		Tool tool = getToolManager().getCurrentTool();
		Object url = state.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		if( url != null)
		{
			requestState.put(tool.getId() + Tool.HELPER_DONE_URL, url);
		}

		state.setAttribute(CitationHelper.RESOURCES_SYS_PREFIX + requestStateId, requestState);
		logger.debug("preserveRequestState() requestStateId == " + requestStateId + "\n" + requestState);
		return requestStateId;
	}

	// temporary -- replace with a value in content-util or content-api
	public static void restoreRequestState(SessionState state, String[] prefixes, int requestStateId)
	{
		Map<String, String> requestState = (Map<String, String>) state.removeAttribute(CitationHelper.RESOURCES_SYS_PREFIX + requestStateId);
		logger.debug("restoreRequestState() requestStateId == " + requestStateId + "\n" + requestState);
		if(requestState != null)
		{
			List<String> attrNames = state.getAttributeNames();
			for(String attrName : attrNames)
			{
				for(String prefix : prefixes)
				{
					if(attrName.startsWith(prefix))
					{
						state.removeAttribute(attrName);
						break;
					}
				}
			}

			for(Map.Entry<String, String> entry : requestState.entrySet())
			{
				state.setAttribute(entry.getKey(), entry.getValue());
			}
		}

	}
	
	protected Map<String,Object> getProperties(ContentEntity entity, SessionState state) {
		Map<String,Object> props = new HashMap<String,Object>();
		
		ResourceProperties properties = entity.getProperties();
		Reference ref = getEntityManager().newReference(entity.getReference());
		DateFormat df = DateFormat.getDateTimeInstance();
		
		// isHidden
		props.put(PROP_IS_HIDDEN, new Boolean(entity.isHidden()));
		// releaseDate, useReleaseDate
		Date releaseDate = null;
		if(entity.getReleaseDate() == null) {
			releaseDate = new Date(System.currentTimeMillis());
			props.put(PROP_USE_RELEASE_DATE, Boolean.FALSE);
		} else {
			releaseDate = new Date(entity.getReleaseDate().getTime());
			props.put(PROP_USE_RELEASE_DATE, Boolean.TRUE);
		}
		props.put(PROP_RELEASE_DATE_STR, df.format(releaseDate));
		props.put(PROP_RELEASE_DATE, releaseDate);
		// retractDate, useRetractDate
		Date retractDate = null;
		if(entity.getRetractDate() == null) {
			retractDate = new Date(System.currentTimeMillis() + ONE_WEEK);
			props.put(PROP_USE_RETRACT_DATE, Boolean.FALSE);
		} else {
			retractDate = new Date(entity.getRetractDate().getTime());
			props.put(PROP_USE_RETRACT_DATE, Boolean.TRUE);
		}
		props.put(PROP_RETRACT_DATE_STR, df.format(retractDate));
		props.put(PROP_RETRACT_DATE, retractDate);
		
		// isCollection
		props.put(PROP_IS_COLLECTION, entity.isCollection());
		// isDropbox
		props.put(PROP_IS_DROPBOX, new Boolean(getContentService().isInDropbox(entity.getId())));
		// isSiteCollection
		props.put(PROP_IS_SITE_COLLECTION, new Boolean(ref.getContext() != null && ref.getContext().equals(entity.getId())));
		// isPubview
		props.put(PROP_IS_PUBVIEW, getContentService().isPubView(entity.getId()));
		// isPubviewInherited
		props.put(PROP_IS_PUBVIEW_INHERITED, new Boolean(getContentService().isInheritingPubView(entity.getId())));
		// isPubviewPossible
		Boolean preventPublicDisplay = (Boolean) state.getAttribute("resources.request.prevent_public_display");
		if(preventPublicDisplay == null) {
			preventPublicDisplay = Boolean.FALSE;
		}
		props.put(PROP_IS_PUBVIEW_POSSIBLE, new Boolean(! preventPublicDisplay.booleanValue()));
		
		// accessMode
		AccessMode accessMode = entity.getAccess();
		props.put(PROP_ACCESS_MODE, accessMode);
		// isGroupInherited
		props.put(PROP_IS_GROUP_INHERITED, AccessMode.GROUPED == entity.getInheritedAccess());
				
		SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
		
		Set<String> currentGroups = new TreeSet<String>();
		if(AccessMode.GROUPED == accessMode) {
			for(Group gr : (Collection<Group>) entity.getGroupObjects()) {
				currentGroups.add(gr.getId());
			}
		} 
		
		// possibleGroups
		Collection<Group> inheritedGroupObjs = null;
		if(entity.getInheritedAccess() == AccessMode.GROUPED) {
			inheritedGroupObjs = entity.getInheritedGroupObjects();
		} else {
			try {
				Site site = siteService.getSite(ref.getContext());
				inheritedGroupObjs = site.getGroups();
			} catch (IdUnusedException e) {
				logger.warn("IdUnusedException in getProperties() " + e);
			}
		}
		List<Map<String,String>> groups = new ArrayList<Map<String,String>>();
		if(inheritedGroupObjs != null) {
			Collection<Group> groupsWithRemovePermission = null;
			if(AccessMode.GROUPED == accessMode)
			{
				groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(entity.getId());
				String container = ref.getContainer();
				if(container != null)
				{
					Collection<Group> more = contentService.getGroupsWithRemovePermission(container);
					if(more != null && ! more.isEmpty())
					{
						groupsWithRemovePermission.addAll(more);
					}
				}
			} else if(AccessMode.GROUPED == entity.getInheritedAccess()) {
				groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(ref.getContainer());
			}
			else if(ref.getContext() != null && contentService.getSiteCollection(ref.getContext()) != null)
			{
				groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(contentService.getSiteCollection(ref.getContext()));
			}
			
			Set<String> idsOfGroupsWithRemovePermission = new TreeSet<String>();
			if(groupsWithRemovePermission != null) {
				for(Group gr : groupsWithRemovePermission) {
					idsOfGroupsWithRemovePermission.add(gr.getId());
				}
 			}
			
			for(Group group : inheritedGroupObjs) {
				Map<String, String> grp = new HashMap<String, String>();
				grp.put("groupId", group.getId());
				grp.put("title", group.getTitle());
				grp.put("description", group.getDescription());
				grp.put("entityRef", group.getReference());
				if(currentGroups.contains(group.getId())) {
					grp.put("isLocal", Boolean.toString(true));
				}
				if(idsOfGroupsWithRemovePermission.contains(group.getId())) {
					grp.put("allowedRemove", Boolean.toString(true));
				}
				groups.add(grp);
			}
		}
		props.put(PROP_POSSIBLE_GROUPS, groups);
		// isGroupPossible
		props.put(PROP_IS_GROUP_POSSIBLE, new Boolean(groups != null && groups.size() > 0));
		// isSingleGroupInherited
		props.put(PROP_IS_SINGLE_GROUP_INHERITED, new Boolean(groups != null && groups.size() == 1));
		// isSiteOnly = ! isPubviewPossible && ! isGroupPossible
		props.put(PROP_IS_SITE_ONLY, new Boolean(preventPublicDisplay.booleanValue() && (groups == null || groups.size() < 1)));
		// isUserSite
		props.put(PROP_IS_USER_SITE, siteService.isUserSite(ref.getContext()));

		// getSelectedConditionKey
		// getSubmittedResourceFilter
		// isUseConditionalRelease

		state.setAttribute(STATE_RESOURCE_ENTITY_PROPERTIES, props);

		return props;
	}

	protected CitationService getCitationService() {
		if(this.citationService == null) {
			this.citationService = (CitationService) ComponentManager.get(CitationService.class);
		}
		return this.citationService;
	}
	
	protected ConfigurationService getConfigurationService() {
		if(this.configurationService == null) {
			this.configurationService = (ConfigurationService) ComponentManager.get(ConfigurationService.class);
		}
		return this.configurationService;
	}
	
	protected SearchManager getSearchManager() {
		if(this.searchManager == null) {
			this.searchManager = (SearchManager) ComponentManager.get(SearchManager.class);
		}
		return this.searchManager;
	}

	protected ContentHostingService getContentService() {
		if(this.contentService == null) {
			this.contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		}
		return this.contentService;
	}
	
	protected EntityManager getEntityManager() {
		if(this.entityManager == null) {
			this.entityManager = (EntityManager) ComponentManager.get(EntityManager.class);
		}
		return this.entityManager;
	}
	
	protected SessionManager getSessionManager() {
		if(this.sessionManager == null) {
			this.sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
		}
		return this.sessionManager;
	}
	
	protected static ToolManager getToolManager() {
		if(toolManager == null) {
			toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
		}
		return toolManager;
	}
	
	protected static FormattedText getFormattedText() {
		if(formattedText == null) {
			formattedText = (FormattedText) ComponentManager.get(FormattedText.class);
		}
		return formattedText;
	}

}	// class CitationHelperAction
