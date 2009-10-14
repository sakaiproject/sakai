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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.indexer.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.StoredDigestContentProducer;
import org.sakaiproject.search.api.rdf.RDFIndexException;
import org.sakaiproject.search.api.rdf.RDFSearchService;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.IndexWorker;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.api.IndexWorkerListener;
import org.sakaiproject.search.indexer.api.NoItemsToIndexException;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionIndexManager;
import org.sakaiproject.search.util.DigestStorageUtil;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class TransactionalIndexWorker implements IndexWorker
{

	private static final Log log = LogFactory.getLog(TransactionalIndexWorker.class);

	/**
	 * dependency
	 */
	private SearchIndexBuilder searchIndexBuilder;

	/**
	 * dependency
	 */
	private TransactionIndexManager transactionIndexManager;

	/**
	 * dependency
	 */
	private ServerConfigurationService serverConfigurationService;

	/**
	 * dependency
	 */
	private RDFSearchService rdfSearchService;

	
	private SearchService searchService;
	
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * dependency
	 */
	private List<IndexWorkerListener> indexWorkerListeners = new ArrayList<IndexWorkerListener>();

	/**
	 * dependency
	 */
	private List<IndexWorkerDocumentListener> indexWorkerDocumentListeners = new ArrayList<IndexWorkerDocumentListener>();

	private ThreadLocalManager threadLocalManager;

	public void init()
	{

	}

	public void destroy()
	{

	}

	public int process(int batchSize)
	{
		
		// get a list to perform this transaction
		IndexTransaction t = null;
		try
		{
			long start = System.currentTimeMillis();
			Map<String, Object> m = new HashMap<String, Object>();
			m.put(SearchBuilderQueueManager.BATCH_SIZE, batchSize);
			
			t = transactionIndexManager.openTransaction(m);
			int n = processTransaction(t);
			t.prepare();
			long transactionID = t.getTransactionId();
			t.commit();
			
			long end = System.currentTimeMillis();
			long time = end-start;
			if ( time == 0 ) {
				log.info("Indexed "+n+" documents in "+time+" ms into save point "+transactionID);				
			} else {
				double dps = n*1000;
				dps = dps /(1.0*time);
				log.info("Indexed "+n+" documents in "+time+" ms "+dps+" documents/second into save point "+transactionID);
			}
			return n;
		}
		catch (NoItemsToIndexException nodx)
		{
			log.info("No Items To Index ");
			if (t != null)
			{
				try
				{
					t.rollback();
				}
				catch (Exception ex)
				{
					log.warn("Transaction Rollback Failed ", ex);
				}
			}
			return 0;
		}
		catch (IndexTransactionException iex)
		{
			if (t == null)
			{
				log.warn("Transaction Failed to open ", iex);
			}
			else
			{
				log.warn("Transaction Failed ", iex);
				try
				{
					t.rollback();
				}
				catch (Exception ex)
				{
					log.warn("Transaction Rollback Failed ", ex);
				}
			}
			return -1;
		}
		finally
		{
			if (t != null)
			{
				try
				{
					t.close();
				}
				catch (Exception ex)
				{
					log.warn("Transaction Close Failed ", ex);
				}
			}
		}
	}

	private int processTransaction(IndexTransaction transaction)
			throws IndexTransactionException
	{
		IndexWriter indexWrite = null;
		IndexReader indexReader = null;
		int nprocessed = 0;
		DigestStorageUtil digestStorageUtil = new DigestStorageUtil(searchService);
		try
		{
			fireIndexStart();

			long last = System.currentTimeMillis();
			Map<String, SearchBuilderItem> finalState = new HashMap<String, SearchBuilderItem>();
			for (Iterator<SearchBuilderItem> tditer = ((IndexUpdateTransaction) transaction)
					.lockedItemIterator(); tditer.hasNext();)
			{
				SearchBuilderItem sbi = tditer.next();
				finalState.put(sbi.getId(),sbi);
			}
			for (SearchBuilderItem sbi : finalState.values())
			{
				if (log.isDebugEnabled())
				{
					log.debug("Item [" + sbi.getName() + "] state ["
							+ (sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate()])
							+ " action ["
							+ SearchBuilderItem.actions[sbi.getSearchaction()] + "]");
				}
				if (SearchBuilderItem.ACTION_ADD.equals(sbi.getSearchaction()) )
				{
					indexReader = ((IndexUpdateTransaction) transaction).getIndexReader();
					int ndel = indexReader.deleteDocuments(new Term(
							SearchService.FIELD_REFERENCE, sbi.getName()));
				}
				else if (SearchBuilderItem.ACTION_DELETE
						.equals(sbi.getSearchaction()))
				{
					if (log.isDebugEnabled())
					{
						log.debug("-------------------Delete "+sbi.getId());
					}
					indexReader = ((IndexUpdateTransaction) transaction)
							.getIndexReader();
					int ndel = indexReader.deleteDocuments(new Term(
							SearchService.FIELD_REFERENCE, sbi.getName()));
					digestStorageUtil.deleteAllDigests(sbi.getName());
					
					
					nprocessed++;
				}


			}
			for (SearchBuilderItem sbi : finalState.values())
			{

				
				Reader contentReader = null;
				String ref = null;
				try
				{
					if (log.isDebugEnabled())
					{
						log.debug("Item [" + sbi.getName() + "] state ["
								+ (sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate()])
								+ " action ["
								+ SearchBuilderItem.actions[sbi.getSearchaction()] + "]");
					}
					if (SearchBuilderItem.ACTION_ADD.equals(sbi.getSearchaction()))
					{
						ref = sbi.getName();
						fireStartDocument(ref);

						try
						{
							// Entity entity = ref.getEntity();
							EntityContentProducer sep = searchIndexBuilder
									.newEntityContentProducer(ref);
							boolean indexDoc = true;
							if (searchIndexBuilder.isOnlyIndexSearchToolSites())
							{
								try
								{
									String siteId = sep.getSiteId(sbi.getName());
									Site s = SiteService.getSite(siteId);
									ToolConfiguration t = s
											.getToolForCommonId("sakai.search"); //$NON-NLS-1$
									if (t == null)
									{
										indexDoc = false;
										log.debug("Not indexing " //$NON-NLS-1$
												+ sbi.getName()
												+ " as it has no search tool"); //$NON-NLS-1$
									}
								}
								catch (Exception ex)
								{
									indexDoc = false;
									log.debug("Not indexing  " + sbi.getName() //$NON-NLS-1$
											+ " as it has no site", ex); //$NON-NLS-1$

								}
							}
							if (indexDoc && sep != null && sep.isForIndex(ref)
									&& sep.getSiteId(ref) != null)
							{

								Document doc = new Document();
								Reference r;
								String container = sep.getContainer(ref);
								if (container == null) container = ""; //$NON-NLS-1$
								doc.add(new Field(SearchService.DATE_STAMP, String
										.valueOf(System.currentTimeMillis()),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_CONTAINER,
										filterNull(container), Field.Store.COMPRESS,
										Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_ID, filterNull(sep
										.getId(ref)), Field.Store.COMPRESS,
										Field.Index.NO));
								doc.add(new Field(SearchService.FIELD_TYPE,
										filterNull(sep.getType(ref)),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_SUBTYPE,
										filterNull(sep.getSubType(ref)),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_REFERENCE,
										filterNull(ref), Field.Store.COMPRESS,
										Field.Index.NOT_ANALYZED));

								doc.add(new Field(SearchService.FIELD_CONTEXT,
										filterNull(sep.getSiteId(ref)),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
								
								// add last part of the index as this is the filename
								String idIndex = sep.getId(ref);
								if (idIndex != null && idIndex.indexOf("/") > 0) {
									idIndex = idIndex.substring(idIndex.lastIndexOf("/"));
								}
								idIndex = filterPunctuation(idIndex);
								
								doc.add(new Field(SearchService.FIELD_CONTENTS,
										idIndex, Field.Store.COMPRESS,
										Field.Index.ANALYZED, Field.TermVector.YES));

								// add the title 
								String title = filterPunctuation(sep.getTitle(ref));
								doc.add(new Field(SearchService.FIELD_CONTENTS,
										title, Field.Store.COMPRESS,
										Field.Index.ANALYZED, Field.TermVector.YES));

								if (sep.isContentFromReader(ref))
								{
									contentReader = sep.getContentReader(ref);
									if (log.isDebugEnabled())
									{
										log.debug("Adding Content for " + ref + " using "
												+ contentReader);
									}
									doc.add(new Field(SearchService.FIELD_CONTENTS,
											contentReader, Field.TermVector.YES));
								}
								else
								{
									String content = sep.getContent(ref);
									if (log.isDebugEnabled())
									{
										log.debug("Adding Content for " + ref + " as ["
												+ content + "]");
									}
									int docCount = digestStorageUtil.getDocCount(ref) + 1;
									doc.add(new Field(SearchService.FIELD_CONTENTS,
											filterNull(content), Field.Store.NO,
											Field.Index.ANALYZED, Field.TermVector.YES));
							if (sep instanceof StoredDigestContentProducer) {
										doc.add(new Field(SearchService.FIELD_DIGEST_COUNT,
												Integer.valueOf(docCount).toString(), Field.Store.COMPRESS, Field.Index.NO, Field.TermVector.NO));
										digestStorageUtil.saveContentToStore(ref, content, docCount);
										if (docCount > 2) {
											digestStorageUtil.cleanOldDigests(ref);
										}
									}

								}

								doc.add(new Field(SearchService.FIELD_TITLE,
										filterNull(sep.getTitle(ref)),
										Field.Store.COMPRESS, Field.Index.ANALYZED,
										Field.TermVector.YES));
								doc.add(new Field(SearchService.FIELD_TOOL,
										filterNull(sep.getTool()), Field.Store.COMPRESS,
										Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_URL,
										filterUrl(filterNull(sep.getUrl(ref))),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
								doc.add(new Field(SearchService.FIELD_SITEID,
										filterNull(sep.getSiteId(ref)),
										Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));

								// add the custom properties

								Map m = sep.getCustomProperties(ref);
								if (m != null)
								{
									for (Iterator cprops = m.keySet().iterator(); cprops
											.hasNext();)
									{
										String key = (String) cprops.next();
										Object value = m.get(key);
										String[] values = null;
										if (value instanceof String)
										{
											values = new String[1];
											values[0] = (String) value;
										}
										if (value instanceof String[])
										{
											values = (String[]) value;
										}
										if (values == null)
										{
											log
													.info("Null Custom Properties value has been suppled by " //$NON-NLS-1$
															+ sep + " in index " //$NON-NLS-1$
															+ key);
										}
										else
										{
											for (int i = 0; i < values.length; i++)
											{
												if (key.startsWith("T"))
												{
													key = key.substring(1);
													doc.add(new Field(key,
															filterNull(values[i]),
															Field.Store.COMPRESS,
															Field.Index.ANALYZED,
															Field.TermVector.YES));
												}
												else
												{
													doc.add(new Field(key,
															filterNull(values[i]),
															Field.Store.COMPRESS,
															Field.Index.NOT_ANALYZED));
												}
											}
										}
									}
								}

								log.debug("Indexing Document " + doc); //$NON-NLS-1$

								indexWrite = ((IndexUpdateTransaction) transaction)
										.getIndexWriter();
								indexWrite.addDocument(doc);

								log.debug("Done Indexing Document " + doc); //$NON-NLS-1$

								processRDF(ref, sep);

								sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
								nprocessed++;

							}
							else
							{
								if (log.isDebugEnabled())
								{
									if (!indexDoc)
									{
										log
												.debug("Ignored Document: Filtered out by site " + ref); //$NON-NLS-1$
									}
									else if (sep == null)
									{
										log
												.debug("Ignored Document: No EntityContentProducer " + ref); //$NON-NLS-1$

									}
									else if (!sep.isForIndex(ref))
									{
										log
												.debug("Ignored Document: Marked as Ignore " + ref); //$NON-NLS-1$

									}
									else if (sep.getSiteId(ref) == null)
									{
										log.debug("Ignored Document: No Site ID " + ref); //$NON-NLS-1$

									}
									else
									{
										log
												.debug("Ignored Document: Reason Unknown " + ref); //$NON-NLS-1$

									}
								}
								sbi.setSearchstate(SearchBuilderItem.STATE_FAILED);
							}
						}
						catch (Exception e1)
						{
							log.warn(" Failed to index document for " + ref + " cause: " //$NON-NLS-1$
									+ e1.getMessage(), e1);
							sbi.setSearchstate(SearchBuilderItem.STATE_FAILED);
						}
					}
				}
				finally
				{
					if (contentReader != null)
					{
						try
						{
							contentReader.close();
						}
						catch (IOException ioex)
						{
							log.warn("Error closing contentReader", ioex);
						}
					}
					fireEndDocument(ref);
				}

			}
		}
		catch (Exception ex)
		{
			log.error("Failed to Add Documents ", ex);
			throw new IndexTransactionException(ex);
		}
		finally
		{
			fireIndexEnd();
		}
		return nprocessed;

	}








	private String filterPunctuation(String term) {
		if ( term == null ) {
			return "";
		}
		char[] endTerm = term.toCharArray();
		for ( int i = 0; i < endTerm.length; i++ ) {
			if ( !Character.isLetterOrDigit(endTerm[i]) ) {
				endTerm[i] = ' ';
			}
		}
		return new String(endTerm);
	}

	/**
	 * 
	 */
	private void fireIndexStart()
	{
		for (Iterator<IndexWorkerListener> itl = indexWorkerListeners.iterator(); itl
				.hasNext();)
		{
			IndexWorkerListener iwl = itl.next();
			iwl.indexWorkerStart(this);
		}
	}

	/**
	 * @param ref
	 */
	private void fireStartDocument(String ref)
	{
		for (Iterator<IndexWorkerDocumentListener> itl = indexWorkerDocumentListeners
				.iterator(); itl.hasNext();)
		{
			IndexWorkerDocumentListener iwl = itl.next();
			iwl.indexDocumentStart(this, ref);
		}
	}

	/**
	 * 
	 */
	private void fireEndDocument(String ref)
	{
		for (Iterator<IndexWorkerDocumentListener> itl = indexWorkerDocumentListeners
				.iterator(); itl.hasNext();)
		{
			IndexWorkerDocumentListener iwl = itl.next();
			iwl.indexDocumentEnd(this, ref);
		}
	}

	/**
	 * 
	 */
	private void fireIndexEnd()
	{
		for (Iterator<IndexWorkerListener> itl = indexWorkerListeners.iterator(); itl
				.hasNext();)
		{
			IndexWorkerListener iwl = itl.next();
			iwl.indexWorkerEnd(this);
		}
	}

	private void processRDF(String ref, EntityContentProducer sep)
			throws RDFIndexException
	{
		if (rdfSearchService != null)
		{
			String s = sep.getCustomRDF(ref);
			if (s != null)
			{
				rdfSearchService.addData(s);
			}
		}
	}

	public void addIndexWorkerListener(IndexWorkerListener indexWorkerListener)
	{
		List<IndexWorkerListener> tl = new ArrayList<IndexWorkerListener>();
		tl.addAll(indexWorkerListeners);
		tl.add(indexWorkerListener);
		indexWorkerListeners = tl;
	}

	public void removeIndexWorkerListener(IndexWorkerListener indexWorkerListener)
	{
		List<IndexWorkerListener> tl = new ArrayList<IndexWorkerListener>();
		tl.addAll(indexWorkerListeners);
		tl.remove(indexWorkerListener);
		indexWorkerListeners = tl;
	}

	public void addIndexWorkerDocumentListener(
			IndexWorkerDocumentListener indexWorkerDocumentListener)
	{
		List<IndexWorkerDocumentListener> tl = new ArrayList<IndexWorkerDocumentListener>();
		tl.addAll(indexWorkerDocumentListeners);
		tl.add(indexWorkerDocumentListener);
		indexWorkerDocumentListeners = tl;
	}

	public void removeIndexWorkerDocumentListener(
			IndexWorkerDocumentListener indexWorkerDocumentListener)
	{
		List<IndexWorkerDocumentListener> tl = new ArrayList<IndexWorkerDocumentListener>();
		tl.addAll(indexWorkerDocumentListeners);
		tl.remove(indexWorkerDocumentListener);
		indexWorkerDocumentListeners = tl;
	}

	/**
	 * @return the indexWorkerDocumentListeners
	 */
	public List<IndexWorkerDocumentListener> getIndexWorkerDocumentListeners()
	{
		return indexWorkerDocumentListeners;
	}

	/**
	 * @param indexWorkerDocumentListeners
	 *        the indexWorkerDocumentListeners to set
	 */
	public void setIndexWorkerDocumentListeners(
			List<IndexWorkerDocumentListener> indexWorkerDocumentListeners)
	{
		this.indexWorkerDocumentListeners = indexWorkerDocumentListeners;
	}

	/**
	 * @return the indexWorkerListeners
	 */
	public List<IndexWorkerListener> getIndexWorkerListeners()
	{
		return indexWorkerListeners;
	}

	/**
	 * @param indexWorkerListeners
	 *        the indexWorkerListeners to set
	 */
	public void setIndexWorkerListeners(List<IndexWorkerListener> indexWorkerListeners)
	{
		this.indexWorkerListeners = indexWorkerListeners;
	}

	/**
	 * @param title
	 * @return
	 */
	private String filterNull(String s)
	{
		if (s == null)
		{
			return "";
		}
		return s;
	}

	/**
	 * @param string
	 * @return
	 */
	private String filterUrl(String url)
	{
		String serverURL = serverConfigurationService.getServerUrl();
		if (url != null && url.startsWith(serverURL))
		{
			String absUrl = url.substring(serverURL.length());
			if (!absUrl.startsWith("/"))
			{
				absUrl = "/" + absUrl;
			}
			return absUrl;
		}
		return url;
	}

	/**
	 * @return the rdfSearchService
	 */
	public RDFSearchService getRdfSearchService()
	{
		return rdfSearchService;
	}

	/**
	 * @param rdfSearchService
	 *        the rdfSearchService to set
	 */
	public void setRdfSearchService(RDFSearchService rdfSearchService)
	{
		this.rdfSearchService = rdfSearchService;
	}

	/**
	 * @return the searchIndexBuilder
	 */
	public SearchIndexBuilder getSearchIndexBuilder()
	{
		return searchIndexBuilder;
	}

	/**
	 * @param searchIndexBuilder
	 *        the searchIndexBuilder to set
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService
	 *        the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the transactionIndexManager
	 */
	public TransactionIndexManager getTransactionIndexManager()
	{
		return transactionIndexManager;
	}

	/**
	 * @param transactionIndexManager
	 *        the transactionIndexManager to set
	 */
	public void setTransactionIndexManager(TransactionIndexManager transactionIndexManager)
	{
		this.transactionIndexManager = transactionIndexManager;
	}

	/**
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

}
