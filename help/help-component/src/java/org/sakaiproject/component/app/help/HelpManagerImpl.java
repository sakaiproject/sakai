/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.component.app.help;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Context;
import org.sakaiproject.api.app.help.Glossary;
import org.sakaiproject.api.app.help.GlossaryEntry;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.api.app.help.RestConfiguration;
import org.sakaiproject.api.app.help.Source;
import org.sakaiproject.api.app.help.TableOfContents;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.help.model.CategoryBean;
import org.sakaiproject.component.app.help.model.ContextBean;
import org.sakaiproject.component.app.help.model.ResourceBean;
import org.sakaiproject.component.app.help.model.SourceBean;
import org.sakaiproject.component.app.help.model.TableOfContentsBean;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * HelpManager provides database and search capabilitites for the Sakai help tool.
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 *
 */
@Slf4j
public class HelpManagerImpl extends HibernateDaoSupport implements HelpManager
{

	private static final String QUERY_GETRESOURCEBYDOCID = "query.getResourceByDocId";
	private static final String QUERY_GETCATEGORYBYNAME = "query.getCategoryByName";
	private static final String QUERY_GET_WELCOME_PAGE = "query.getWelcomePage";
	private static final String DOCID = "docId";
	private static final String WELCOME_PAGE = "welcomePage";
	private static final String NAME = "name";

	private static final String DEFAULT_LUCENE_INDEX_PATH = System
	.getProperty("java.io.tmpdir")
	+ File.separator + "sakai.help";

	private static final String TOC_API = "org.sakaiproject.api.app.help.TableOfContents";


	private static String EXTERNAL_URL;
	private static String DEFAULT_HELP_FILE = "help.xml";
	private static String HELP_BASENAME = "help";
	private static String DEFAULT_LOCALE = "default";

	private Map<String, List> helpContextConfig = new HashMap<String, List>();
	private int contextSize;

	private RestConfiguration restConfiguration;
	private ServerConfigurationService serverConfigurationService;

	// Map which contains all localized help toc
	private Map<String, TableOfContentsBean> toc;

	// All supported locales
	private List<String> locales;

	private Boolean initialized = Boolean.FALSE;
	private Object initializedLock = new Object();

	private Glossary glossary;
	private String supportEmailAddress;

	private ToolManager toolManager;
	private HibernateTransactionManager txManager;

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getServerConfigurationService()
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#setServerConfigurationService(org.sakaiproject.service.framework.config.ServerConfigurationService)
	 */
	public void setServerConfigurationService(ServerConfigurationService s)
	{
		serverConfigurationService = s;
	}


	private PreferencesService  preferencesService;
	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public List getContexts(String mappedView)
	{
		return (List) helpContextConfig.get(mappedView);
	}

	public List getActiveContexts(Map session)
	{
		List contexts = (List) session.get("help_contexts");
		if (contexts == null)
		{
			contexts = new SizedList(getContextSize());
			session.put("help_contexts", contexts);
		}
		return contexts;
	}

	public void addContexts(Map session, String mappedView)
	{
		List newContexts = getContexts(mappedView);
		List contexts = getActiveContexts(session);
		if (newContexts != null)
		{
			contexts.addAll(newContexts);
		}
	}

	/**
	 * return list of resources matching context id
	 *
	 * @param contextId
	 * @return
	 */
	public Set<Resource> getResources(Long contextId)
	{
		return searchResources(new TermQuery(new Term("context", "\"" + contextId
				+ "\"")));
	}

	/**
	 * Store resource
	 * @see org.sakaiproject.api.app.help.HelpManager#storeResource(org.sakaiproject.api.help.Entity)
	 */
	public void storeResource(Resource resource)
	{
		getHibernateTemplate().saveOrUpdate(resource);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getResource(java.lang.Long)
	 */
	public Resource getResource(Long id)
	{
		return (ResourceBean) getHibernateTemplate().get(ResourceBean.class, id);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#deleteResource(java.lang.Long)
	 */
	public void deleteResource(Long resourceId)
	{
		Resource resource = getResource(resourceId);
		if (resource == null) {
			return;
		}
		getHibernateTemplate().delete(resource);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getSource(java.lang.Long)
	 */
	public Source getSource(Long id)
	{
		try
		{
			return (SourceBean) getHibernateTemplate().load(SourceBean.class, id);
		}
		catch (HibernateObjectRetrievalFailureException e)
		{
			return null;
		}
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#storeSource(org.sakaiproject.api.help.Source)
	 */
	public void storeSource(Source source)
	{
		getHibernateTemplate().saveOrUpdate(source);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#deleteSource(java.lang.Long)
	 */
	public void deleteSource(Long sourceId)
	{
		Source source = getSource(sourceId);
		if (source == null) {
			return;
		}
		getHibernateTemplate().delete(source);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getContext(java.lang.Long)
	 */
	public Context getContext(Long id)
	{
		try
		{
			return (ContextBean) getHibernateTemplate().load(ContextBean.class, id);
		}
		catch (HibernateObjectRetrievalFailureException e)
		{
			return null;
		}
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#storeContext(org.sakaiproject.api.help.Context)
	 */
	public void storeContext(Context context)
	{
		getHibernateTemplate().saveOrUpdate(context);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#deleteContext(java.lang.Long)
	 */
	public void deleteContext(Long contextId)
	{
		Context context = getContext(contextId);
		if (context == null) {
			return;
		}
		getHibernateTemplate().delete(context);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getResourcesForActiveContexts(java.util.Map)
	 */
	public Map getResourcesForActiveContexts(Map session)
	{
		Map<String, Set<Resource>> resourceMap = new HashMap<String, Set<Resource>>();
		List<String> activeContexts = getActiveContexts(session);
		for(String context : activeContexts)
		{
			try
			{
				Set<Resource> resources = searchResources(new TermQuery(new Term("context", "\""
						+ context + "\"")));
				if (resources != null && resources.size() > 0)
				{
					resourceMap.put(context, resources);
				}
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
			}
		}
		return resourceMap;
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#searchResources(java.lang.String)
	 */
	public Set<Resource> searchResources(String queryStr)
	{
		initialize();

		try
		{
			return searchResources(queryStr, "content");
		}
		catch (ParseException e)
		{
			log.debug("ParseException parsing Help search query  " + queryStr, e);
			return null;
		}
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getTableOfContents()
	 */
	public TableOfContents getTableOfContents()
	{
		initialize();
		return getToc();
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#setTableOfContents(org.sakaiproject.api.help.TableOfContents)
	 */
	public void setTableOfContents(TableOfContents toc)
	{
		setToc((TableOfContentsBean) toc);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#searchGlossary(java.lang.String)
	 */
	public GlossaryEntry searchGlossary(String keyword)
	{
		return getGlossary().find(keyword);
	}

	private String getHelpIndexPath() {
  		return serverConfigurationService.getString("help.indexpath", DEFAULT_LUCENE_INDEX_PATH);
	}
	/**
	 * Search Resources
	 * @param query
	 * @return Set of matching results.
	 */
	protected Set<Resource> searchResources(Query query)
	{
		Set<Resource> results = new HashSet<Resource>();

		String locale = getSelectedLocale().toString();
		if (!toc.containsKey(locale)) {
			locale = DEFAULT_LOCALE;
		}

		String luceneFolder = getHelpIndexPath() + File.separator + locale;

        IndexReader reader = null;
		FSDirectory dir = null;
		try
		{
			dir = FSDirectory.open(new File(luceneFolder));
			reader = DirectoryReader.open(dir);
			IndexSearcher searcher = new IndexSearcher(reader);

			log.debug("Searching for: " + query.toString());

			//Hits hits = searcher.search(query);
			TopDocs topDocs = searcher.search(query, 1000);
			ScoreDoc[] hits = topDocs.scoreDocs;
			log.debug(hits.length + " total matching documents");

			for (ScoreDoc scoreDoc : hits) {
				Document doc = searcher.doc(scoreDoc.doc);
				ResourceBean resource = getResourceFromDocument(doc);
				resource.setScore(scoreDoc.score * 100);
				results.add(resource);
			}

		}
		catch (Exception e)
		{
			log.error(e.getMessage());
		}
		finally 
		{
            //http://mail-archives.apache.org/mod_mbox/lucene-java-user/201304.mbox/%3CCAGaRif0agg+XCXbccdxUmB5h9v5dHqjEvwi5X_vmU3sMM20QZg@mail.gmail.com%3E
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//nothing to do
				}
			}
			if (dir != null) {
				dir.close();
			}
		}
		return results;
	}

	/**
	 * Search Lucene
	 *
	 * @param queryStr
	 * @param defaultField
	 * @return
	 * @throws ParseException
	 */
	protected Set<Resource> searchResources(String queryStr, String defaultField)
	throws ParseException
	{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		QueryParser parser = new QueryParser(Version.LUCENE_40, defaultField, analyzer);
		Query query = parser.parse(queryStr);
		return searchResources(query);
	}

	/**
	 * Get Resource From Document.
	 * @param document
	 * @return resource bean
	 */
	protected ResourceBean getResourceFromDocument(Document document)
	{
		Long id = new Long(document.getField("id").stringValue());
		return (ResourceBean) getResource(id);
	}

	/**
	 * Get entire Collection of Resources.
	 * @return collection of resources
	 */
	protected Collection<? extends Resource> getResources()
	{
		return getHibernateTemplate().loadAll(ResourceBean.class);
	}

	/**
	 * Get ContextSize.
	 * @return size of Context.
	 */
	public int getContextSize()
	{
		return contextSize;
	}

	/**
	 * Set ContextSize
	 * @param contextSize
	 */
	public void setContextSize(int contextSize)
	{
		this.contextSize = contextSize;
	}

	/**
	 * Get Document.
	 * @param resource
	 * @return document
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	protected Document getDocument(ResourceBean resource) throws IOException, MalformedURLException {

		Document doc = new Document();
		if (resource.getContexts() != null)
		{
			for (String context : resource.getContexts())
			{
				doc.add(new Field("context", "\"" + context + "\"", Field.Store.YES, Field.Index.NOT_ANALYZED));
			}
		}

		URL urlResource;
		URLConnection urlConnection = null;
		//For local file override
		
		String sakaiHomePath = serverConfigurationService.getSakaiHomePath();
  		String localHelpPath = sakaiHomePath+serverConfigurationService.getString("help.localpath","/help/");
		File localFile = new File(localHelpPath+resource.getLocation());
		boolean localFileIsFile = false;
		if(localFile.isFile()) { 
			log.debug("Local help file overrides: "+resource.getLocation());
			localFileIsFile = true;
		}
		StringBuilder sb = new StringBuilder();
		if (resource.getLocation() == null || resource.getLocation().startsWith("/"))
		{
			// handle REST content
			if (!getRestConfiguration().getOrganization().equals("sakai"))
			{
				urlResource = new URL(getRestConfiguration().getRestUrlInDomain() + resource.getDocId()
						+ "?domain=" + getRestConfiguration().getRestDomain());
				urlConnection = urlResource.openConnection();

				String basicAuthUserPass = getRestConfiguration().getRestCredentials();
				String encoding = Base64.encodeBase64(basicAuthUserPass.getBytes("utf-8")).toString();       
				urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream()), 512);
				try {
					int readReturn = 0;
					char[] cbuf = new char[512];
					while ((readReturn = br.read(cbuf, 0, 512)) != -1)
					{
						sb.append(cbuf, 0, readReturn);
					}
				} finally {
					br.close();
				}

				// if document is coming from corpus then get document name from xml and assign to resource
				String resourceName = getRestConfiguration().getResourceNameFromCorpusDoc(sb.toString());
				resource.setName(resourceName);
				storeResource(resource);
			}
			else if (!"".equals(EXTERNAL_URL))
			{
				// handle external help location
				urlResource = new URL(EXTERNAL_URL + resource.getLocation());
			}
			else
			{
				// Add the home folder file reading here
				if(localFileIsFile) { 
					urlResource = localFile.toURI().toURL();
				}
				else {
				// handle classpath location
					urlResource = getClass().getResource(resource.getLocation());
				}
			}
		}
		else
		{
			// handle external location specified in reg file
			urlResource = new URL(resource.getLocation());
		}

		if (urlResource == null)
		{
			return null;
		}

		if (resource.getLocation() != null){
			String resLocation = resource.getLocation();
			if(localFileIsFile) { 
				resLocation = localFile.getPath();
			}
			doc.add(new Field("location", resLocation, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}


		//doc.add(Field.Keyword("id", resource.getId().toString()));
		doc.add(new Field("id", resource.getId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		if (getRestConfiguration().getOrganization().equals("sakai"))
		{
			Reader reader = new BufferedReader(new InputStreamReader(urlResource.openStream()));
			try {
				int readReturn = 0;
				char[] cbuf = new char[512];
				while ((readReturn = reader.read(cbuf, 0, 512)) != -1)
				{
					sb.append(cbuf, 0, readReturn);
				}
			} finally {
				reader.close();
			}
		}
		//doc.add(Field.Text("content", sb.toString()));
		doc.add(new Field("content", sb.toString(), Field.Store.YES, Field.Index.ANALYZED));

		return doc;
	}

	/**
	 * Get Table Of Contents Bean.
	 * @return table of contents bean
	 */
	public TableOfContentsBean getToc()
	{
		if (toc == null)
		{
			return null;
		}
		String locale = getSelectedLocale().toString();
		if (toc.containsKey(locale)) {
			return toc.get(locale);
		}
		else {
			return toc.get(DEFAULT_LOCALE);
		}
	}

	/**
	 * Set Table Of Contents Bean.
	 * @param toc
	 */
	public void setToc(TableOfContentsBean toc)
	{
		this.toc.put(DEFAULT_LOCALE, toc);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getGlossary()
	 */
	public Glossary getGlossary()
	{
		return glossary;
	}

	/**
	 * Set Glossary.
	 * @param glossary
	 */
	public void setGlossary(Glossary glossary)
	{
		this.glossary = glossary;
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#storeCategory(org.sakaiproject.api.help.Category)
	 */
	public void storeCategory(Category category)
	{
		getHibernateTemplate().saveOrUpdate(category);
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#createCategory()
	 */
	public Category createCategory()
	{
		return new CategoryBean();
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#createResource()
	 */
	public Resource createResource()
	{
		return new ResourceBean();
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getResourceByDocId(java.lang.String)
	 */
	public Resource getResourceByDocId(final String docId)
	{
		HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				org.hibernate.Query q = session
				.getNamedQuery(QUERY_GETRESOURCEBYDOCID);

				q.setString(DOCID, (docId == null) ? null : docId.toLowerCase());
				if (q.list().size() == 0){
					return null;
				}
				else{
					return (Resource) q.list().get(0);
				}
			}
		};
		Resource resource = (Resource) getHibernateTemplate().execute(hcb);
		return resource;
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getWelcomePage()
	 */
	public String getWelcomePage()
	{
		initialize();
		HibernateCallback<List<ResourceBean>> hcb = session -> session
            .getNamedQuery(QUERY_GET_WELCOME_PAGE)
				.setString(WELCOME_PAGE, "true")
				.list();

		List<ResourceBean> list = getHibernateTemplate().execute(hcb);
        if (list.isEmpty()) {
            return null;
		}
        return list.get(0).getDocId();
	}

	/**
	 * Find a Category by name
	 * @param name
	 * @return Category
	 */
	public Category getCategoryByName(final String name)
	{
		HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				org.hibernate.Query q = session
				.getNamedQuery(QUERY_GETCATEGORYBYNAME);
				q.setString(NAME, (name == null) ? name : name.toLowerCase());
				return q.uniqueResult();
			}
		};
		return (Category) getHibernateTemplate().execute(hcb);
	}

	/**
	 * Index Categories and Resources
	 * @param categories
	 */
	private void indexRecursive(IndexWriter indexWriter, Set<Category> categories)
	{
		for (Category category: categories)
		{
			Set<Resource> resourcesList = category.getResources();

			for (Resource resource : resourcesList) {
				try
				{
					Document doc = getDocument((ResourceBean)resource);
					if (doc != null)
					{
						indexWriter.addDocument(doc);
						log.debug("added resource '" + resource.getName() + "', doc count="
								+ indexWriter.maxDoc());
					}
					else
					{
						log.debug("failed to add resource '" + "' (" + resource.getName());
					}
				}
				catch (IOException e)
				{
					log.error("I/O error while adding resource '" + "' ("
							+ resource.getName() + "): " + e.getMessage(), e);
				}
			}

			Set<Category> subCategories = category.getCategories();
			indexRecursive(indexWriter, subCategories);
		}
	}

	/**
	 * Store the mapping of Categories and Resources
	 * @param categories
	 */
	private void storeRecursive(Set<Category> categories)
	{
		for(Category category: categories)
		{
			Set<Resource> resourcesList = category.getResources();
			category.setResources(null);

			for (Resource resource: resourcesList)
			{
				resource.setDocId(resource.getDocId().toLowerCase());
				resource.setCategory(category);
			}

			category.setResources(resourcesList);
			this.storeCategory(category);

			Set<Category> subCategories = category.getCategories();
			storeRecursive(subCategories);
		}
	}

	/**
	 * Get Support Email Address.
	 * @see org.sakaiproject.api.app.help.HelpManager#getSupportEmailAddress()
	 */
	public String getSupportEmailAddress()
	{
		return supportEmailAddress;
	}

	/**
	 * set Support Email Address.
	 * @param email
	 */
	public void setSupportEmailAddress(String email)
	{
		this.supportEmailAddress = email;
	}

	/**
	 * get tool manager
	 * @return Returns the toolManager.
	 */
	public ToolManager getToolManager()
	{
		return toolManager;
	}

	/**
	 * set tool manager
	 * @param toolManager The toolManager to set.
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * @param txManager The txManager to set.
	 */
	public void setTxManager(HibernateTransactionManager txManager)
	{
		this.txManager = txManager;
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getRestConfiguration()
	 */
	public RestConfiguration getRestConfiguration()
	{
		return restConfiguration;
	}

	/**
	 * set REST configuration
	 * @param restConfiguration
	 */
	public void setRestConfiguration(RestConfiguration restConfiguration)
	{
		this.restConfiguration = restConfiguration;
	}


	/**
	 * Reinitialize help content from UI
	 */
	public void reInitialize(){
		synchronized (initializedLock)
		{   
			initialized = Boolean.FALSE;
		}
		initialize();
	}

	/**
	 * Synchronize first access to tool.
	 * @see org.sakaiproject.api.app.help.HelpManager#initialize()
	 */
	public void initialize()
	{
		if (initialized.booleanValue())
		{
			return;
		}
		else
		{
			synchronized (initializedLock)
			{
				if (!initialized.booleanValue())
				{
					dropExistingContent();

					// handle external help content
					EXTERNAL_URL = getServerConfigurationService().getString(
					"help.location");
					if (!"".equals(EXTERNAL_URL))
					{
						if (EXTERNAL_URL.endsWith("/"))
						{
							// remove trailing forward slash
							EXTERNAL_URL = EXTERNAL_URL.substring(0,
									EXTERNAL_URL.length() - 1);
						}
					}

					// Get all supported locales
					locales = new ArrayList<String>();
					Locale[] sl = serverConfigurationService.getSakaiLocales();
					for (Locale element : sl) {
					    locales.add(element.toString()); // Locale toString should generate en_GB type identifiers
					}

					// Add default locale
					locales.add(DEFAULT_LOCALE);

					toc = new HashMap<String, TableOfContentsBean>();
					registerHelpContent();
					initialized = Boolean.TRUE;
				}
			}
		}
	}

	/**
	 * @see org.sakaiproject.api.app.help.HelpManager#getExternalLocation()
	 */
	public String getExternalLocation()
	{
		return EXTERNAL_URL;
	}

	private void dropExistingContent()
	{
		if (log.isDebugEnabled())
		{
			log.debug("dropExistingContent()");
		}

		TransactionTemplate tt = new TransactionTemplate(txManager);
		tt.execute(new TransactionCallback()
		{
			public Object doInTransaction(TransactionStatus status)
			{
				getHibernateTemplate().bulkUpdate("delete CategoryBean");
				getHibernateTemplate().flush();
				return null;
			}
		});
	}

	/**
	 * Returns the user locale
	 * @param prefLocales
	 *            The prefLocales to set.
	 */
	private Locale getSelectedLocale() {

		Locale loc = preferencesService.getLocale(userDirectoryService.getCurrentUser().getId());
		if (loc != null)
		{
			return loc;
		} else {
			return Locale.getDefault();
		}
	}

	/**
	 * Register help content either locally or externally
	 * Index resources in Lucene
	 */
	private void registerHelpContent()
	{
		if (log.isDebugEnabled())
		{
			log.debug("registerHelpContent()");
		}

		// register external help docs
		if (!"".equals(EXTERNAL_URL))
		{
			registerExternalHelpContent(EXTERNAL_URL + "/" + DEFAULT_HELP_FILE);
		}
		else
		{
			registerStaticContent();
		}

		// Create lucene indexes for each toc (which key is either a locale or 'default')
		for (String key : toc.keySet())
		{
			String luceneIndexPath = getHelpIndexPath() + File.separator + key;
			TableOfContentsBean currentToc = toc.get(key);

			// create index in lucene
			IndexWriter writer = null;
			Date start = new Date();
			try
			{
				//writer = new IndexWriter(luceneIndexPath, new StandardAnalyzer(Version.LUCENE_40), true);
				FSDirectory directory = FSDirectory.open(new File(luceneIndexPath));
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40));

				writer = new IndexWriter(directory, config);
			}
			catch (IOException e)
			{
				log.error("failed to create IndexWriter " + e.getMessage(), e);
				return;
			}

			// Index categories and resources
			indexRecursive(writer, currentToc.getCategories());

			try
			{
				writer.commit();
				writer.close();
			}
			catch (IOException e)
			{
				log.error("failed to close writer " + e.getMessage(), e);
			}

			Date end = new Date();
			log.info("finished initializing lucene for '" + key + "' in "
					+ (end.getTime() - start.getTime()) + " total milliseconds");
		}
	}

	/**
	 * register external help content
	 * build document from external reg file
	 * @param externalHelpReg
	 */
	public void registerExternalHelpContent(String helpFile)
	{
		Set<Category> categories = new TreeSet<Category>();
		URL urlResource = null;
		InputStream ism = null;
		BufferedInputStream bis = null;

		try
		{
			try {
				urlResource = new URL(EXTERNAL_URL + "/" + helpFile);
				ism = urlResource.openStream();
			} catch (IOException e) {
				// Try default help file
				helpFile = DEFAULT_HELP_FILE;
				urlResource = new URL(EXTERNAL_URL + "/" + helpFile);
				ism = urlResource.openStream();
			}

			bis = new BufferedInputStream(ism);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder builder = dbf.newDocumentBuilder();

			InputSource is = new org.xml.sax.InputSource(bis);
			org.w3c.dom.Document xmlDocument = builder.parse(is);

			Node helpRegNode = (Node) xmlDocument.getDocumentElement();
			recursiveExternalReg(helpRegNode, null, categories);

			// handle corpus docs
			if (!getRestConfiguration().getOrganization().equals("sakai")){

				// get corpus document
				String corpusXml = getRestConfiguration().getCorpusDocument();
				DocumentBuilderFactory dbfCorpus = DocumentBuilderFactory.newInstance();
				dbfCorpus.setNamespaceAware(true);
				DocumentBuilder builderCorpus = dbfCorpus.newDocumentBuilder();
				StringReader sReader = new StringReader(corpusXml);
				InputSource isCorpus = new org.xml.sax.InputSource(sReader);
				org.w3c.dom.Document xmlDocumentCorpus = builderCorpus.parse(isCorpus);

				registerCorpusDocs(xmlDocumentCorpus);
				sReader.close();
			}

		}
		catch (MalformedURLException e)
		{
			log.warn("Unable to load external URL: " + EXTERNAL_URL + "/" + helpFile, e);
		}
		catch (IOException e)
		{
			log.warn("I/O error opening external URL: " + EXTERNAL_URL + "/" + helpFile, e);
		}
		catch (ParserConfigurationException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (SAXException e)
		{
			log.error(e.getMessage(), e);
		}
		finally
		{
			try{
				if (bis != null){
					bis.close();
				}
			}
			catch (IOException e){
				log.error("error closing stream", e);
			}
		}

		// Add to toc map
		TableOfContentsBean externalToc = new TableOfContentsBean();
		externalToc.setCategories(categories);
		setTableOfContents(externalToc);
	}

	/**
	 ** @return Locale based on its string representation (language_region)
	 **/
	private Locale getLocaleFromString(String localeString) {
		return serverConfigurationService.getLocaleFromString(localeString);
	}

	/**
	 * Adds help for a specific locale
	 * @param path
	 * @param locale
	 */
	private void addToolHelp(String path, String locale)
	{
		URL urlResource = null;
		String classpathUrl = null;

	    String sakaiHomePath = serverConfigurationService.getSakaiHomePath();
	    String localHelpPath = sakaiHomePath+serverConfigurationService.getString("help.localpath","/help/");
	    
	    File localFile = null;
	    
		// find default help file
		if ( locale.equals(DEFAULT_LOCALE) ) {
			classpathUrl = path + "/" + HELP_BASENAME + ".xml";
			
			localFile = new File(localHelpPath+classpathUrl);
			if(localFile.isFile()) {
				try {
					urlResource = localFile.toURI().toURL();
				} catch (MalformedURLException e) {
					urlResource = getClass().getResource(classpathUrl);
				}
			} else {
				urlResource = getClass().getResource(classpathUrl);
			}
		}

		// find localized help file
		else {
			classpathUrl = path + "/" + HELP_BASENAME + "_" + locale + ".xml";
			localFile = new File(localHelpPath+classpathUrl);
			if(localFile.isFile()) {
				try {
					urlResource = localFile.toURI().toURL();
				} catch (MalformedURLException e) {
					urlResource = getClass().getResource(classpathUrl);
				}
			} else {
				urlResource = getClass().getResource(classpathUrl);
			}

			// If language/region help file not found, look for language-only help file
			if ( urlResource == null ) {
				Locale nextLocale = getLocaleFromString(locale);
				classpathUrl = path + "/" + HELP_BASENAME + "_" + nextLocale.getLanguage() + ".xml";
				localFile = new File(localHelpPath+classpathUrl);
				if(localFile.isFile()) {
					try {
						urlResource = localFile.toURI().toURL();
					} catch (MalformedURLException e) {
						urlResource = getClass().getResource(classpathUrl);
					}
				} else {
					urlResource = getClass().getResource(classpathUrl);
				}
			}

			// If language-only help file not found, look for default help file
			if ( urlResource == null ) {
				classpathUrl = path + "/" + HELP_BASENAME + ".xml";
				localFile = new File(localHelpPath+classpathUrl);
				if(localFile.isFile()) {
					try {
						urlResource = localFile.toURI().toURL();
					} catch (MalformedURLException e) {
						urlResource = getClass().getResource(classpathUrl);
					}
				} else {
					urlResource = getClass().getResource(classpathUrl);
				}
			}
		}

		// Url exists?
		if (urlResource != null)
		{
			TableOfContentsBean localizedToc;

			// Add this tool categories to this tool toc
			try
			{
				org.springframework.core.io.Resource resource =
					new UrlResource(urlResource);  
				BeanFactory beanFactory = new XmlBeanFactory(resource);
				TableOfContents tocTemp = (TableOfContents) beanFactory.getBean(TOC_API);
				Set<Category> categories = tocTemp.getCategories();
				storeRecursive(categories);

				// Get localized toc
				if (toc.containsKey(locale)) {
					localizedToc = toc.get(locale);
				}
				else { // Create and add localized toc
					localizedToc = new TableOfContentsBean();
					toc.put(locale, localizedToc);
				}

				// Update localized toc categories
				localizedToc.getCategories().addAll(categories);
			}
			catch (Exception e)
			{
				log.warn("Unable to load help index from " + classpathUrl + " : " + e.getMessage());
			}
		}
	}


	/**
	 * register local content
	 */
	public void registerStaticContent()
	{
		//  register static content
		Set<Tool> toolSet = toolManager.findTools(null, null);

		// find out what we want to ignore
		List<String> hideHelp = Arrays.asList(StringUtils.split(serverConfigurationService.getString("help.hide"), ","));
		if (hideHelp == null) {
		    hideHelp = new ArrayList<String> ();
		}

		for (Tool tool : toolSet) {
			if (tool != null && tool.getId() != null && !hideHelp.contains(tool.getId()))
			{
				String[] extraCollections = {};
				String toolHelpCollections = tool.getRegisteredConfig().getProperty(TOOLCONFIG_HELP_COLLECTIONS);

				if (toolHelpCollections != null) {
					extraCollections = StringUtils.split(toolHelpCollections, ",");
				}

				// Loop throughout the locales list
				for (String locale : locales)
				{
					// Add localized tool helps
					addToolHelp("/" + tool.getId().toLowerCase().replaceAll("\\.", "_"), locale);

					// Add any other optional collections
					for (String extraCollection : extraCollections) {
						addToolHelp("/" + extraCollection, locale);
					}	           
				}
			}
		}

		// Sort the help topics for each locale
		for (String locale : locales) {
			TableOfContentsBean localizedToc = toc.get(locale);

			// Sort this localized toc categories with a TreeSet
			if (localizedToc != null) {
				Set<Category> sortedCategories = new TreeSet<Category>();		    
				Set<Category> categories = localizedToc.getCategories();
				sortedCategories.addAll(categories);
				
				for (Category cat : categories) {
					if (hideHelp.contains(cat.getName())) {
						sortedCategories.remove(cat);
					}
				}
				localizedToc.setCategories(sortedCategories);
			}
		}

	}

	private static int cnt = 0;

	/**
	 * Parse external help reg doc recursively
	 * @param n
	 * @param category
	 */
	public void recursiveExternalReg(Node n, Category category, Set<Category> categories)
	{

		if (n == null)
		{
			return;
		}

		NodeList nodeList = n.getChildNodes();
		int nodeListLength = nodeList.getLength();

		for (int i = 0; i < nodeListLength; i++)
		{
			if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			Node currentNode = nodeList.item(i);

			if ("category".equals(currentNode.getNodeName()))
			{
				Category childCategory = new CategoryBean();
				childCategory.setName(currentNode.getAttributes().getNamedItem("name")
						.getNodeValue());

				if (category != null)
				{
					childCategory.setParent(category);
					category.getCategories().add(childCategory);
				}

				storeCategory(childCategory);
				categories.add(childCategory);

				log.info("adding help category: " + childCategory.getName());

				recursiveExternalReg(currentNode, childCategory, categories);
			}
			else
				if ("resource".equals(currentNode.getNodeName()))
				{
					Resource resource = new ResourceBean();
					NamedNodeMap nnm = currentNode.getAttributes();

					if (nnm != null)
					{
						// name required
						resource.setName(nnm.getNamedItem("name").getNodeValue());

						if (nnm.getNamedItem("location") != null)
						{
							resource.setLocation(nnm.getNamedItem("location").getNodeValue());
						}

						if (nnm.getNamedItem("docId") != null)
						{
							resource.setDocId(nnm.getNamedItem("docId").getNodeValue());
						}
						else
						{
							resource.setDocId(Integer.valueOf(cnt).toString());
							cnt++;
						}

						//defaultForTool is an optional attribute
						if (nnm.getNamedItem("defaultForTool") != null)
						{
							resource.setDefaultForTool(nnm.getNamedItem("defaultForTool")
									.getNodeValue());
						}

						// welcomePage is an optional attribute
						if (nnm.getNamedItem("welcomePage") != null)
						{
							resource.setWelcomePage(nnm.getNamedItem("welcomePage")
									.getNodeValue().toLowerCase());
						}
					}

					resource.setCategory(category);
					category.getResources().add(resource);
					storeResource(resource);

					log.info("adding help resource: " + resource + " to category: "
							+ category.getName());
					recursiveExternalReg(currentNode, category, categories);
				}
		}

	}

	/**
	 * Parse corpus document
	 * @param doc document
	 */
	public void registerCorpusDocs(org.w3c.dom.Document doc)
	{
		if (doc == null) {
			return;
		}

		List<String> arrayCorpus = new ArrayList<String>();

		NodeList nodeList = doc.getElementsByTagName("id");
		int nodeListLength = nodeList.getLength();

		for (int i = 0; i < nodeListLength; i++)
		{
			Node currentNode = nodeList.item(i);
			NodeList nlChildren = currentNode.getChildNodes();

			for (int j = 0; j < nlChildren.getLength(); j++){
				if (nlChildren.item(j).getNodeType() == Node.TEXT_NODE){
					arrayCorpus.add(nlChildren.item(j).getNodeValue());
				}
			}
		}

		// iterate through corpus docs and add to home category if not already
		// added by help.xml external registration

		// if Home category does not exist, then create it
		if (getCategoryByName("Home") == null){
			Category cat = new CategoryBean();
			cat.setName("Home");
			storeCategory(cat);
		}

		for (int i = 0; i < arrayCorpus.size(); i++){
			String currentDocId = (String) arrayCorpus.get(i);

			// if the corpus doc does not already exist from help.xml, then add it to the Home category
			if (this.getResourceByDocId(currentDocId) == null){
				Resource resource = new ResourceBean();
				resource.setDocId(currentDocId);
				resource.setName(currentDocId);
				Category homeCategory = getCategoryByName("Home");
				resource.setCategory(homeCategory);
				homeCategory.getResources().add(resource);
				storeResource(resource);
			}
		}
	}

}

