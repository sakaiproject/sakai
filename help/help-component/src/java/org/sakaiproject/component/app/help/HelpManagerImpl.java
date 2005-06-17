/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component/src/java/org/sakaiproject/component/app/help/HelpManagerImpl.java,v 1.6 2005/06/11 17:16:54 jlannan.iupui.edu Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.help;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Context;
import org.sakaiproject.api.app.help.Glossary;
import org.sakaiproject.api.app.help.GlossaryEntry;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.api.app.help.RestConfiguration;
import org.sakaiproject.api.app.help.Source;
import org.sakaiproject.api.app.help.TableOfContents;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.api.kernel.tool.ToolManager;
import org.sakaiproject.component.app.help.model.CategoryBean;
import org.sakaiproject.component.app.help.model.ContextBean;
import org.sakaiproject.component.app.help.model.ResourceBean;
import org.sakaiproject.component.app.help.model.SourceBean;
import org.sakaiproject.component.app.help.model.TableOfContentsBean;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate.HibernateTransactionManager;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import sun.misc.BASE64Encoder;

/**
 * HelpManager provides database and search capabilitites for the Sakai help tool.
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 * 
 */
public class HelpManagerImpl extends HibernateDaoSupport implements HelpManager
{

  private static final String QUERY_GETRESOURCEBYDOCID = "query.getResourceByDocId";
  private static final String QUERY_GETCATEGORYBYNAME = "query.getCategoryByName";
  private static final String DOCID = "docId";
  private static final String NAME = "name";

  private static final String LUCENE_INDEX_PATH = System
      .getProperty("java.io.tmpdir")
      + File.separator + "sakai.help";

  private static final String TOC_API = "org.sakaiproject.api.app.help.TableOfContents";

  private static String REST_URL;
  private static String EXTERNAL_URL;

  private Map helpContextConfig = new HashMap();
  private int contextSize;

  private RestConfiguration restConfiguration;

  private TableOfContentsBean toc;
  private Boolean initialized = Boolean.FALSE;

  private Glossary glossary;
  private String supportEmailAddress;

  private ToolManager toolManager;
  private HibernateTransactionManager txManager;

  private static final Log LOG = LogFactory.getLog(HelpManagerImpl.class);

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
  public Set getResources(Long contextId)
  {
    ContextBean context = (ContextBean) getContext(contextId);
    return searchResources(new TermQuery(new Term("context", "\"" + contextId
        + "\"")));
  }

  /**
   * Store resource
   * @see org.sakaiproject.api.app.help.HelpManager#storeResource(org.sakaiproject.api.help.Resource)
   */
  public void storeResource(Resource resource)
  {
    if (getResourceByDocId(resource.getDocId()) == null)
    {
      getHibernateTemplate().saveOrUpdate(resource);
    }
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
    if (resource == null) return;
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
    if (source == null) return;
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
    if (context == null) return;
    getHibernateTemplate().delete(context);
  }

  /**
   * @see org.sakaiproject.api.app.help.HelpManager#getResourcesForActiveContexts(java.util.Map)
   */
  public Map getResourcesForActiveContexts(Map session)
  {
    Map resourceMap = new HashMap();
    List activeContexts = getActiveContexts(session);
    for (Iterator i = activeContexts.iterator(); i.hasNext();)
    {
      String context = (String) i.next();
      try
      {
        Set resources = searchResources(new TermQuery(new Term("context", "\""
            + context + "\"")));
        if (resources != null && resources.size() > 0)
        {
          resourceMap.put(context, resources);
        }
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
    }
    return resourceMap;
  }

  /**
   * @see org.sakaiproject.api.app.help.HelpManager#searchResources(java.lang.String)
   */
  public Set searchResources(String queryStr)
  {
    initialize();

    try
    {
      return searchResources(queryStr, "content");
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
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

  /**
   * Search Resources
   * @param query
   * @return Set of matching results.
   */
  protected Set searchResources(Query query)
  {
    Set results = new HashSet();
    try
    {
      Searcher searcher = new IndexSearcher(LUCENE_INDEX_PATH);
      LOG.debug("Searching for: " + query.toString());

      Hits hits = searcher.search(query);
      LOG.debug(hits.length() + " total matching documents");

      for (int i = 0; i < hits.length(); i++)
      {
        ResourceBean resource = getResourceFromDocument(hits.doc(i));
        resource.setScore(hits.score(i) * 100);
        results.add(resource);
      }
      searcher.close();
    }
    catch (Exception e)
    {
      LOG.error(e);
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
  protected Set searchResources(String queryStr, String defaultField)
      throws ParseException
  {
    Analyzer analyzer = new StandardAnalyzer();
    Query query = QueryParser.parse(queryStr, defaultField, analyzer);
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
  protected Collection getResources()
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
  protected Document getDocument(ResourceBean resource) throws IOException,
      MalformedURLException
  {

    Document doc = new Document();

    if (resource.getContexts() != null)
    {
      for (Iterator i = resource.getContexts().iterator(); i.hasNext();)
      {
        doc.add(Field.Keyword("context", "\"" + ((String) i.next()) + "\""));
      }
    }

    doc.add(Field.Keyword("location", resource.getLocation()));
    doc.add(Field.Keyword("name", resource.getName()));
    doc.add(Field.Keyword("id", resource.getId().toString()));

    URL urlResource;
    URLConnection urlConnection = null;
    StringBuffer sBuffer = new StringBuffer();
    if (resource.getLocation().startsWith("/"))
    {
      // handle REST content
      if (!getRestConfiguration().getOrganization().equals("sakai"))
      {
        urlResource = new URL(getStaticRestUrl() + resource.getDocId()
            + "?domain=" + getRestConfiguration().getRestDomain());
        urlConnection = urlResource.openConnection();

        String basicAuthUserPass = getRestConfiguration().getRestCredentials();
        String encoding = new BASE64Encoder().encode(basicAuthUserPass
            .getBytes());

        urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

        sBuffer = new StringBuffer();

        BufferedReader br = new BufferedReader(new InputStreamReader(
            urlConnection.getInputStream()), 512);
        int readReturn = 0;
        char[] cbuf = new char[512];
        while ((readReturn = br.read(cbuf, 0, 512)) != -1)
        {
          sBuffer.append(cbuf, 0, readReturn);
        }

      }
      else
        if (!"".equals(EXTERNAL_URL))
        {
          // handle external help location
          urlResource = new URL(EXTERNAL_URL + resource.getLocation());
        }
        else
        {
          // handle classpath location
          urlResource = getClass().getResource(resource.getLocation());
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

    if (getRestConfiguration().getOrganization().equals("sakai"))
    {
      Reader reader = new BufferedReader(new InputStreamReader(urlResource
          .openStream()));

      int readReturn = 0;
      char[] cbuf = new char[512];
      while ((readReturn = reader.read(cbuf, 0, 512)) != -1)
      {
        sBuffer.append(cbuf, 0, readReturn);
      }

      doc.add(Field.Text("content", sBuffer.toString()));
    }
    else
    {
      doc.add(Field.Text("content", sBuffer.toString()));
    }

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
      toc = new TableOfContentsBean();
      Collection categories = getHibernateTemplate()
          .loadAll(CategoryBean.class);
      toc.setCategories(new TreeSet(categories));
    }
    return toc;
  }

  /**
   * Set Table Of Contents Bean.
   * @param toc
   */
  public void setToc(TableOfContentsBean toc)
  {
    this.toc = toc;
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
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        net.sf.hibernate.Query q = session
            .getNamedQuery(QUERY_GETRESOURCEBYDOCID);
        q.setString(DOCID, docId);
        return q.uniqueResult();
      }
    };
    Resource resource = (Resource) getHibernateTemplate().execute(hcb);
    return resource;
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
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        net.sf.hibernate.Query q = session
            .getNamedQuery(QUERY_GETCATEGORYBYNAME);
        q.setString(NAME, name);
        return q.uniqueResult();
      }
    };
    return (Category) getHibernateTemplate().execute(hcb);
  }

  /**
   * Store the mapping of Categories and Resources
   * @param categories
   */
  private void storeRecursive(Set categories)
  {
    Iterator i = categories.iterator();
    while (i.hasNext())
    {
      Category category = (Category) i.next();

      Set resourcesList = category.getResources();
      category.setResources(null);

      for (Iterator resourceIterator = resourcesList.iterator(); resourceIterator
          .hasNext();)
      {
        Resource resource = (Resource) resourceIterator.next();
        resource.setCategory(category);
      }

      category.setResources(resourcesList);
      this.storeCategory(category);

      Set subCategories = category.getCategories();
      storeRecursive(subCategories);
    }
  }

  /**
   * Get Support Email Address.
   * @see org.sakaiproject.api.app.help.HelpManager#getSupportEmailAddress()
   */
  public String getSupportEmailAddress()
  {
    if (supportEmailAddress == null)
    {
      //this.setSupportEmailAddres(serverConfigurationService
      //    .getString("support.email"));
    }
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
      synchronized (initialized)
      {
        if (!initialized.booleanValue())
        {
          dropExistingContent();

          if (!getRestConfiguration().getOrganization().equals("sakai"))
          {
            constructRestUrl();
          }

          // handle external help content
          EXTERNAL_URL = ServerConfigurationService.getString("help.location");
          if (!"".equals(EXTERNAL_URL))
          {
            if (EXTERNAL_URL.endsWith("/"))
            {
              // remove trailing forward slash
              EXTERNAL_URL = EXTERNAL_URL.substring(0,
                  EXTERNAL_URL.length() - 1);
            }
          }

          registerHelpContent();
          initialized = Boolean.TRUE;
        }
      }
    }
  }

  private void constructRestUrl()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("constructRestUrl()");
    }

    REST_URL = getRestConfiguration().getRestUrl() + "/"
        + getRestConfiguration().getRestDomain() + "/" + "document" + "/"
        + getRestConfiguration().getRestDomain() + "/";
  }

  
  /**
   * @see org.sakaiproject.api.app.help.HelpManager#getExternalLocation()
   */
  public String getExternalLocation()
  {
    return EXTERNAL_URL;    
  }
  
  /**
   * @see org.sakaiproject.api.app.help.HelpManager#getStaticRestUrl()
   */
  public String getStaticRestUrl()
  {
    return REST_URL;
  }

  private void dropExistingContent()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("dropExistingContent()");
    }

    TransactionTemplate tt = new TransactionTemplate(txManager);
    tt.execute(new TransactionCallback()
    {
      public Object doInTransaction(TransactionStatus status)
      {
        getHibernateTemplate().delete("from CategoryBean");
        getHibernateTemplate().flush();
        return null;
      }
    });
  }

  /**
   * Register help content from classpath registration files. 
   * Index resources in Lucene.
   */
  private void registerHelpContent()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("registerHelpContent()");
    }

    Set toolSet = toolManager.findTools(null, null);
    List helpClasspathRegList = new ArrayList();

    for (Iterator i = toolSet.iterator(); i.hasNext();)
    {
      Tool tool = (Tool) i.next();
      if (tool != null && tool.getId() != null)
      {
        helpClasspathRegList.add("/"
            + tool.getId().toLowerCase().replaceAll("\\.", "_") + "/"
            + "help.xml");
      }
    }

    // add non-standard ids
    helpClasspathRegList.add("/sakai_workspace/help.xml");
    helpClasspathRegList.add("/sakai_web_content/help.xml");
    helpClasspathRegList.add("/sakai_general_info/help.xml");
    helpClasspathRegList.add("/sakai_grading/help.xml");
    helpClasspathRegList.add("/sakai_worksite/help.xml");

    Set allCategories = new TreeSet();

    for (Iterator i = helpClasspathRegList.iterator(); i.hasNext();)
    {
      String classpathUrl = (String) i.next();
      
      URL urlResource = null;
      
      if (!"".equals(EXTERNAL_URL))
      {
        // handle external help location
        try{
          urlResource = new URL(EXTERNAL_URL + classpathUrl);
        }
        catch (MalformedURLException e){
          LOG.debug("Unable to load external URL: " + classpathUrl);
          continue;
        }
      }
      else{        
        urlResource= getClass().getResource(classpathUrl);
        
        if (urlResource == null)
        {
          LOG.debug("Unable to load resource: " + classpathUrl);
          continue;
        }
      }                              

      if (urlResource == null)
      {
        LOG.debug("Unable to load classpath resource: " + classpathUrl);
        continue;
      }
      try
      {
        org.springframework.core.io.Resource resource = new InputStreamResource(
            urlResource.openStream(), classpathUrl);
        BeanFactory beanFactory = new XmlBeanFactory(resource);
        TableOfContents tocTemp = (TableOfContents) beanFactory
            .getBean(TOC_API);
        Set categories = tocTemp.getCategories();

        storeRecursive(categories);
        allCategories.addAll(categories);
      }
      catch (Exception e)
      {
        LOG.debug("Unable to load classpath resource: " + classpathUrl);
      }
    }

    toc = new TableOfContentsBean();
    toc.setCategories(allCategories);

    // create index in lucene
    IndexWriter writer = null;
    Date start = new Date();
    try
    {
      writer = new IndexWriter(LUCENE_INDEX_PATH, new StandardAnalyzer(), true);
    }
    catch (IOException e)
    {
      LOG.error("failed to create IndexWriter " + e.getMessage());
    }

    for (Iterator i = getResources().iterator(); i.hasNext();)
    {
      ResourceBean resource = (ResourceBean) i.next();

      try
      {
        Document doc = getDocument(resource);
        if (doc != null)
        {
          writer.addDocument(doc);
          LOG.info("added resource '" + resource.getName() + "', doc count="
              + writer.docCount());
        }
        else
        {
          LOG.debug("failed to add resource '" + "' (" + resource.getName());
        }
      }
      catch (IOException e)
      {
        LOG.error("I/O error while adding resource '" + "' ("
            + resource.getName() + "): " + e.getMessage());
      }
    }
    try
    {
      writer.optimize();
      writer.close();
    }
    catch (IOException e)
    {
      LOG.error("failed to close writer " + e.getMessage());
    }

    Date end = new Date();
    LOG.info("finished initializing lucene in "
        + (end.getTime() - start.getTime()) + " total milliseconds");
  }

}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component/src/java/org/sakaiproject/component/app/help/HelpManagerImpl.java,v 1.6 2005/06/11 17:16:54 jlannan.iupui.edu Exp $
 *
 **********************************************************************************/