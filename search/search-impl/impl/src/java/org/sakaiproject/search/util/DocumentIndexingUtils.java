/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/search/trunk/search-impl/impl/src/java/org/sakaiproject/search/component/dao/impl/SearchIndexBuilderWorkerDaoJdbcImpl.java $
 * $Id: SearchIndexBuilderWorkerDaoJdbcImpl.java 103115 2012-01-13 14:05:23Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 **********************************************************************************/package org.sakaiproject.search.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.StoredDigestContentProducer;

/**
 * Utilities for indexing documents
 * @author dhorwitz
 * @since 1.5.0
 *
 */
public class DocumentIndexingUtils {
	
	private static final Log log = LogFactory.getLog(DocumentIndexingUtils.class);
	
	
	/**
	 * Index a Sakai entity to a document
	 * @param ref the reference of the entity 
	 * @param digestStorageUtil
	 * @param sep the {@link EntityContentProducer} to index the document
	 * @param the URL of the server
	 * @return The Lucene document suitable for adding to the Index
	 */
	public static Document createIndexDocument(String ref, DigestStorageUtil digestStorageUtil,
			 EntityContentProducer sep, String serverURL) {
		Reader contentReader = null;
		Document doc = new Document();
		
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

									
		// add last part of the index as this is the filename
		String idIndex = sep.getId(ref);
		if (idIndex != null && idIndex.indexOf("/") > 0) {
			idIndex = idIndex.substring(idIndex.lastIndexOf("/"));
		}
		idIndex = filterPunctuation(idIndex);
		
		doc.add(new Field(SearchService.FIELD_CONTENTS,
				idIndex, Field.Store.NO,
				Field.Index.ANALYZED, Field.TermVector.YES));

		// add the title 
		String title = filterPunctuation(sep.getTitle(ref));
		doc.add(new Field(SearchService.FIELD_CONTENTS,
				title, Field.Store.NO,
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
			//its possible that there is no content to index
			if (content != null && content.trim().length() > 0) {
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
		}

		doc.add(new Field(SearchService.FIELD_TITLE,
				filterNull(sep.getTitle(ref)),
				Field.Store.COMPRESS, Field.Index.ANALYZED,
				Field.TermVector.YES));
		doc.add(new Field(SearchService.FIELD_TOOL,
				filterNull(sep.getTool()), Field.Store.COMPRESS,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field(SearchService.FIELD_URL,
				filterUrl(filterNull(sep.getUrl(ref)), serverURL),
				Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));
		doc.add(new Field(SearchService.FIELD_SITEID,
				filterNull(sep.getSiteId(ref)),
				Field.Store.COMPRESS, Field.Index.NOT_ANALYZED));

		// add the custom properties

		Map<String, ?> m = sep.getCustomProperties(ref);
		if (m != null)
		{
			Set<?> entries = m.entrySet();
			for (Iterator<?> cprops = entries.iterator(); cprops
					.hasNext();)
			{
				Entry<String, ?> entry = (Entry<String, ?>) cprops.next();
				String key = entry.getKey();
				Object value = entry.getValue();
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
		log.debug("Indexing Document " + doc); //$NON-NLS-1$
		return doc;
	}

	/**
	 * @param title
	 * @return
	 */
	private static String filterNull(String s)
	{
		if (s == null)
		{
			return "";
		}
		return s;
	}
	
	
	private static String filterPunctuation(String term) {
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
	 * @param string
	 * @return
	 */
	private static String filterUrl(String url, String serverURL)
	{
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

}
