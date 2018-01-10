/*******************************************************************************
 * $URL$
 * $Id$
 * **********************************************************************************
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
 ******************************************************************************/

package org.sakaiproject.citation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.osid.repository.Asset;
import org.osid.repository.Part;
import org.osid.repository.PartIterator;
import org.osid.repository.Record;
import org.osid.repository.RecordIterator;
import org.osid.repository.RepositoryException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.citation.api.*;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.citation.impl.openurl.ContextObject;
import org.sakaiproject.citation.impl.openurl.OpenURLServiceImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceAction;
import org.sakaiproject.content.util.BasicSiteSelectableResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.util.BasicResourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 *
 *
 */
@Slf4j
public abstract class BaseCitationService implements CitationService
{
	protected boolean attemptToMatchSchema = false;

	protected static final List<String> AUTHOR_AS_KEY = new Vector<String>();
	static
	{
		AUTHOR_AS_KEY.add( CitationCollection.SORT_BY_AUTHOR );
		AUTHOR_AS_KEY.add( CitationCollection.SORT_BY_YEAR );
		AUTHOR_AS_KEY.add( CitationCollection.SORT_BY_TITLE );
		AUTHOR_AS_KEY.add( CitationCollection.SORT_BY_UUID );
	};

	protected static final List<String> YEAR_AS_KEY = new Vector<String>();

	static
	{
		YEAR_AS_KEY.add( CitationCollection.SORT_BY_YEAR );
		YEAR_AS_KEY.add( CitationCollection.SORT_BY_AUTHOR );
		YEAR_AS_KEY.add( CitationCollection.SORT_BY_TITLE );
		YEAR_AS_KEY.add( CitationCollection.SORT_BY_UUID );
	};

	protected static final List<String> TITLE_AS_KEY = new Vector<String>();

	static
	{
		TITLE_AS_KEY.add( CitationCollection.SORT_BY_TITLE );
		TITLE_AS_KEY.add( CitationCollection.SORT_BY_AUTHOR );
		TITLE_AS_KEY.add( CitationCollection.SORT_BY_YEAR );
		TITLE_AS_KEY.add( CitationCollection.SORT_BY_UUID );
	};

	public static final Map<String, String> GS_TAGS = new Hashtable<String, String>();
	static
	{
		//GS_TAGS.put("rft_val_fmt", "genre");
		GS_TAGS.put("rft.title", "title");
		GS_TAGS.put("rft.atitle", "title");
		GS_TAGS.put("rft.jtitle", "atitle");
		GS_TAGS.put("rft.btitle", "atitle");
		GS_TAGS.put("rft.aulast", "au");
		GS_TAGS.put("rft.aufirst", "au");
		GS_TAGS.put("rft.au", "au");
		GS_TAGS.put("rft.pub", "publisher");
		GS_TAGS.put("rft.volume", "volume");
		GS_TAGS.put("rft.issue", "issue");
		GS_TAGS.put("rft.pages", "pages");
		GS_TAGS.put("rft.date", "date");
		GS_TAGS.put("rft.issn", "id");
		GS_TAGS.put("rft.isbn", "id");
	}

	/**
	 *
	 */
	public class BasicCitation implements Citation
	{
		/* for OpenUrl creation */
		protected final static String OPENURL_VERSION = "Z39.88-2004";
		protected final static String OPENURL_CONTEXT_FORMAT = "info:ofi/fmt:kev:mtx:ctx";
		protected final static String OPENURL_JOURNAL_FORMAT = "info:ofi/fmt:kev:mtx:journal";
		protected final static String OPENURL_BOOK_FORMAT = "info:ofi/fmt:kev:mtx:book";

		protected Map m_citationProperties = null;
		protected Map m_urls;
		protected String m_citationUrl = null;
		protected String m_fullTextUrl = null;
		protected String m_id = null;
		protected String m_imageUrl = null;
		/* This only makes sense, and will only be set, in the context of a collection.*/
		protected int m_position;
		protected Schema m_schema;
		protected String m_searchSourceUrl = null;
		protected Integer m_serialNumber = null;
		protected boolean m_temporary = false;
		protected boolean m_isAdded = false;
		protected String m_preferredUrl;

		/**
		 * Constructs a temporary citation.
		 */
		protected BasicCitation()
		{
			m_serialNumber = nextSerialNumber();
			m_temporary = true;
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();
			setType(CitationService.UNKNOWN_TYPE);
		}

		/**
		 * Constructs a temporary citation based on an asset.
		 *
		 * @param asset
		 */
		protected BasicCitation(Asset asset)
		{
			m_serialNumber = nextSerialNumber();
			m_temporary = true;
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();

			boolean unknownSchema = true;
			String title = null;

			Set validProperties = getValidPropertyNames();
			Set multivalued = getMultivalued();

			String description;
      /*
       * How to use the preferred URL?  We can omit it, use it as the title
       * link, or supply it as the related link.
       *
			 * "preferred" (below) has one of three values: false, related-link,
			 *                                              or title-link
			 */
			String preferredUrl = null;
			String preferred = m_configService.getSiteConfigUsePreferredUrls();

			boolean usePreferredUrlAsTitle = preferred.equals("title-link");
			boolean usePreferredUrls = !preferred.equals("false");

			// assetId = asset.getId().getIdString();
			try
			{
				title = asset.getDisplayName();
				if (title != null)
				{
					m_citationProperties.put(Schema.TITLE, title);
				}

				description = asset.getDescription();
				if (description != null)
				{
					m_citationProperties.put("abstract", description);
				}

				RecordIterator rit = asset.getRecords();
				try
				{
					while (rit.hasNextRecord())
					{
						Record record;
						try
						{
							record = rit.nextRecord();
							preferredUrl = null;

							try
							{
								PartIterator pit = record.getParts();
								try
								{
									while (pit.hasNextPart())
									{
										try
										{
											Part part = pit.nextPart();
											String type = part.getPartStructure().getType()
											        .getKeyword();

											if (type == null)
											{
												// continue;
											}
											else if (validProperties.contains(type))
											{

												if (multivalued.contains(type))
												{
													List values = (List) m_citationProperties
													        .get(type);
													if (values == null)
													{
														values = new Vector();
														m_citationProperties.put(type, values);
													}
													values.add(part.getValue());
												}
												else
												{
												  m_citationProperties.put(type, part.getValue());
												}
											}
										  /*
										   * This type isn't described by the schema.  Is
										   * it a preferred (title link) URL?
										   */
											else if (type.equals("preferredUrl"))
										  {
											  preferredUrl = (String) part.getValue();
											}
											else if (type.equals("type"))
											{
												if (m_schema == null
												        || m_schema.getIdentifier().equals(
												                "unknown"))
												{
													if (getSynonyms("article").contains(
													        part.getValue().toString()
													                .toLowerCase()))
													{
														m_schema = BaseCitationService.this
														        .getSchema("article");
														unknownSchema = false;
													}
													else if (getSynonyms("book").contains(
													        part.getValue().toString()
													                .toLowerCase()))
													{
														m_schema = BaseCitationService.this
														        .getSchema("book");
														unknownSchema = false;
													}
													else if (getSynonyms("chapter").contains(
													        part.getValue().toString()
													                .toLowerCase()))
													{
														m_schema = BaseCitationService.this
														        .getSchema("chapter");
														unknownSchema = false;
													}
													else if (getSynonyms("report").contains(
													        part.getValue().toString()
													                .toLowerCase()))
													{
														m_schema = BaseCitationService.this
														        .getSchema("report");
														unknownSchema = false;
													}
													else
													{
														m_schema = BaseCitationService.this
														        .getSchema("unknown");
														unknownSchema = true;
													}
												}
												List values = (List) m_citationProperties.get(type);
												if (values == null)
												{
													values = new Vector();
													m_citationProperties.put(type, values);
												}
												values.add(part.getValue());
											}
											else
											{

											}

										}
										catch (RepositoryException e)
										{
											log.warn("BasicCitation(" + asset + ") ", e);
										}
									}
								}
								catch (RepositoryException e)
								{
									log.warn("BasicCitation(" + asset + ") ", e);
								}
							}
							catch (RepositoryException e1)
							{
								log.warn("BasicCitation(" + asset + ") ", e1);
							}
						}
						catch (RepositoryException e2)
						{
							log.warn("BasicCitation(" + asset + ") ", e2);
						}
					}
				}
				catch (RepositoryException e)
				{
					log.warn("BasicCitation(" + asset + ") ", e);
				}
			}
			catch (RepositoryException e)
			{
				log.warn("BasicCitation(" + asset + ") ", e);
			}

			if(unknownSchema && attemptToMatchSchema)
			{
				matchSchema();
			}

			setDefaults();
      /*
       * Did we find a preferred URL?  If so, should we use it?
       */
			if (usePreferredUrls && (preferredUrl != null))
			{
			  String id;
        /*
         * Save the URL without a label (it'll get the default label at
         * render-time).  This URL needs to have the prefix text added at
         * render time, and we'll [optionally] set it as the preferred
         * (or title) link.
         */
			  id = addCustomUrl("",  preferredUrl, Citation.ADD_PREFIX_TEXT);
			  if (usePreferredUrlAsTitle)
			  {
			    setPreferredUrl(id);
			  }
			}
		}

		/**
         *
         */
        protected void matchSchema()
        {
        	Map pros = new Hashtable();
        	Map cons = new Hashtable();
	        List schemas = getSchemas();
	        Set fieldNames = this.m_citationProperties.keySet();
	        Iterator schemaIt = schemas.iterator();
	        while(schemaIt.hasNext())
	        {
	        	Schema schema = (Schema) schemaIt.next();
	        	if(schema.getIdentifier().equals("unknown"))
	        	{
	        		continue;
	        	}

	        	pros.put(schema.getIdentifier(), new Counter());
	        	cons.put(schema.getIdentifier(), new Counter());

	        	Iterator fieldIt = fieldNames.iterator();
	        	while(fieldIt.hasNext())
	        	{
	        		String fieldName = (String) fieldIt.next();
	        		Field field = schema.getField(fieldName);
	        		if(field == null)
	        		{
	        			// this indicates that data would be lost.
	        			((Counter) cons.get(schema.getIdentifier())).increment();
	        		}
	        		else
	        		{
	        			// this is evidence that this schema might be best fit.
	        			((Counter) pros.get(schema.getIdentifier())).increment();
	        		}
	        	}
	        }

	        // elminate schema that lose data
	        for(Map.Entry<String, Counter> entry : ((Map<String, Counter>) cons).entrySet()) {
	        	if(entry.getValue().intValue() > 0) {
	        		pros.remove(entry.getKey());
	        	}
	        }
	        Iterator prosIt = pros.keySet().iterator();
	        int bestScore = 0;
	        String bestMatch = null;
	        // Nominate "article" as first candidate if it's not blocked
	        Object article = pros.get("article");
	        if(article != null)
	        {
	        	bestScore = ((Counter) article).intValue();
	        	bestMatch = "article";
	        }
	        while(prosIt.hasNext())
	        {
	        	String schemaId = (String) prosIt.next();
	        	int score = ((Counter) pros.get(schemaId)).intValue();
	        	if(score > bestScore)
	        	{
	        		bestScore = score;
	        		bestMatch = schemaId;
	        	}
	        }
	        if(bestMatch != null)
	        {
	        	m_schema = BaseCitationService.this.getSchema(bestMatch);
	        }
        }

		/**
		 * @param other
		 */
		public BasicCitation(BasicCitation other)
		{
			m_id = other.m_id;
			m_serialNumber = other.m_serialNumber;
			m_temporary = other.m_temporary;
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();
			setSchema(other.m_schema);

			copy(other);
		}

		/**
		 * Construct a citation not marked as temporary of a particular type.
		 *
		 * @param mediatype
		 */
		public BasicCitation(String mediatype)
		{
			m_id = m_idManager.createUuid();
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();
			setType(mediatype);
		}

		public BasicCitation(String citationId, Schema schema)
		{
			m_id = citationId;
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();
			setSchema(schema);
		}

		/**
		 * Construct a citation not marked as temporary of a particular type
		 * with a particular id.
		 *
		 * @param citationId
		 * @param mediatype
		 */
		public BasicCitation(String citationId, String mediatype)
		{
			m_id = citationId;
			m_citationProperties = new Hashtable();
			m_urls = new Hashtable();
			setType(mediatype);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#addCustomUrl(java.lang.String,
		 *      java.net.URL)
		 */
		public String addCustomUrl(String label, String url)
		{
			UrlWrapper wrapper = new UrlWrapper(label, url);
			String id = m_idManager.createUuid();
			m_urls.put(id, wrapper);
			return id;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#addCustomUrl(java.lang.String,
		 *                                                          java.net.URL,
		 *                                                          jave.lang.String)
		 */
		public String addCustomUrl(String label, String url, String prefixRequest)
		{
			UrlWrapper wrapper = new UrlWrapper(label, url,
			                                    getPrefixBoolean(prefixRequest));
			String id = m_idManager.createUuid();
			m_urls.put(id, wrapper);
			return id;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#updateCustomUrl(java.lang.String,
		 *                                                             java.lang.String,
		 *                                                             java.lang.String,
		 *                                                             java.lang.String)
		 */
		public void updateCustomUrl(String urlid, String label,
		                            String url, String prefixRequest)
		{
			UrlWrapper wrapper = new UrlWrapper(label, url,
			                                    getPrefixBoolean(prefixRequest));
			m_urls.put(urlid, wrapper);
		}

    /*
     * addCustomUrl()/updateCustomUrl() helper: Convert the "prefix request
     *                                          string" to a boolean value
     *
     * @param prefixRequest Prefix request text
     * @return true if the request is to add URL prefix
     */
    private boolean getPrefixBoolean(String prefixRequest)
    {
      if (!Citation.ADD_PREFIX_TEXT.equals(prefixRequest)
      &&  !Citation.OMIT_PREFIX_TEXT.equals(prefixRequest))
      {
        log.debug("Unexpected \"add prefix\" request: " + prefixRequest);
      }
      return Citation.ADD_PREFIX_TEXT.equals(prefixRequest);
    }

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#addPropertyValue(java.lang.String,
		 *      java.lang.Object)
		 */
		public void addPropertyValue(String name, Object value)
		{
			getCitationProperties();
			if (isMultivalued(name))
			{
				List list = (List) this.m_citationProperties.get(name);
				if (list == null)
				{
					list = new Vector();
					this.m_citationProperties.put(name, list);
				}
				list.add(value);
			}
			else
			{
				this.m_citationProperties.put(name, value);
			}
		}

		/**
		 *
		 * @param citation
		 */
		public void copy(Citation citation)
		{
			BasicCitation other = (BasicCitation) citation;

			m_citationUrl = other.m_citationUrl;
			m_fullTextUrl = other.m_fullTextUrl;
			m_imageUrl = other.m_imageUrl;
			m_searchSourceUrl = other.m_searchSourceUrl;
			m_preferredUrl = other.m_preferredUrl;

			m_schema = other.m_schema;

			if (m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			m_citationProperties.clear();

			if (other.m_citationProperties != null)
			{
				Iterator propIt = other.m_citationProperties.keySet().iterator();
				while (propIt.hasNext())
				{
					String name = (String) propIt.next();
					Object obj = other.m_citationProperties.get(name);
					if (obj == null)
					{

					}
					else if (obj instanceof List)
					{
						List list = (List) obj;
						List copy = new Vector();
						Iterator valueIt = list.iterator();
						while (valueIt.hasNext())
						{
							Object val = valueIt.next();
							copy.add(val);
						}
						this.m_citationProperties.put(name, copy);
					}
					else if (obj instanceof String)
					{
						this.m_citationProperties.put(name, obj);
					}
					else
					{
						log.debug("BasicCitation copy constructor: property is not String or List: "
						                + name + " (" + obj.getClass().getName() + ") == " + obj);
						this.m_citationProperties.put(name, obj);
					}
				}
			}

			if (m_urls == null)
			{
				m_urls = new Hashtable();
			}
			m_urls.clear();

			if (other.m_urls != null)
			{
				Iterator urlIt = other.m_urls.keySet().iterator();
				while (urlIt.hasNext())
				{
					String id = (String) urlIt.next();
					UrlWrapper wrapper = (UrlWrapper) other.m_urls.get(id);

					// Do not want to addCustomUrl because that assigns a new, unique id to the customUrl.
					// This causes problems when we try to reference the preferredUrl by its id - it was
					// created and set in the 'other' citation
					//addCustomUrl(wrapper.getLabel(), wrapper.getUrl());

					// instead, we store the customUrl along with it's originial id -- since this citation
					// is a copy of 'other', there should be no harm in doing this
					m_urls.put(id, wrapper);
				}
			}
		}

		/*
		 * Simple helpers to export RIS items
		 * prefix will most often be empty, and is used to offer an "internal label"
		 * for stuff that gets shoved into the Notes (N1) field because there isn't
		 * a dedicated field (e.g., Rights)
		 *
		 * Outputs XX  - value
		 *   or
		 *         XX  - prefix: value
		 */

        public void exportRisField(String rislabel,  String value, StringBuilder buffer, String prefix)
		{
			// Get rid of the newlines and spaces
			value = value.replaceAll("\n", " ");
			rislabel = rislabel.trim();

			// Adjust the prefix to have a colon-space afterwards, if there *is* a prefix
      if (prefix == null)
      {
        prefix = "";
      }

      prefix = prefix.trim();

			if (!prefix.equals(""))
			{
				prefix = prefix + ": ";
			}

			// Export it only if there's a value, or if it's an ER tag (which is by design empty)
			if (value != null && !value.trim().equals("") || rislabel.equals("ER"))
			{
				buffer.append(rislabel + RIS_DELIM + prefix + value + "\n");
			}

		}

        /*
         * Again, without the prefix
         */

        public void exportRisField(String rislabel,  String value, StringBuilder buffer)
		{
        	exportRisField(rislabel, value, buffer, "");
        }

        /*
		 * If the value is a list, iterate over it and recursively call exportRISField
		 *
		 */

        public void exportRisField(String rislabel, List propvalues, StringBuilder buffer,  String prefix)
		{
			Iterator propvaliter = propvalues.iterator();
			while (propvaliter.hasNext())
			{
				exportRisField(rislabel, propvaliter.next(), buffer, prefix);
			}
		}

        /*
         * And again, to do the dispatch
         */

        public void exportRisField(String rislabel, Object val, StringBuilder buffer, String prefix)
		{
          if (val instanceof List)
          {
        	  exportRisField(rislabel, (List) val, buffer, prefix);
          } else
          {
        	  exportRisField(rislabel,  (String) val.toString(), buffer, prefix);
          }
        }

        /*
         * And, finally, a dispatcher to deal with items without a prefix
         */
        public void exportRisField(String rislabel, Object val, StringBuilder buffer)
		{
        	exportRisField(rislabel, val, buffer, "");
		}

		/*
		 *
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#exportToRis(java.io.OutputStream)
		 */
		public void exportRis(StringBuilder buffer) throws IOException
		{
			// Get the RISType and write a blank line and the TY tag
			String type = "article";
			if (m_schema != null)
			{
				type = m_schema.getIdentifier();
			}

			String ristype = (String) m_RISType.get(type);
			if (ristype == null)
			{
				ristype = (String) m_RISType.get("article");
			}
			exportRisField("TY", ristype, buffer);


			// Cycle through all the properties except for those that need
			// pre-processing (as listed in m_RISSpecialFields)

			// Deal with the "normal" fields

			List fields = m_schema.getFields();
			Iterator iter = fields.iterator();
			while (iter.hasNext())
			{
				Field field = (Field) iter.next();
				String fieldname = field.getIdentifier();
				if (m_RISSpecialFields.contains(fieldname))
				{
					continue; // Skip if this is a special field
				}
				String rislabel = field.getIdentifier(RIS_FORMAT);
				// SAK-16740 -- Need to skip fields if risLabel is "" (or null)
				if (rislabel != null && ! rislabel.trim().equals(""))
				{
					exportRisField(rislabel, getCitationProperty(fieldname, false), buffer);
				}
			}

			// Deal with the speical fields.

			/**
			 * Dates need to be of the formt YYYY/MM/DD/other, including the
			 * slashes even if the data is empty. Hence, we'll mostly be
			 * producing YYYY// for date formats
			 */

			// TODO: deal with real dates. Right now, just year

  		exportRisField("Y1", getCitationProperty(Schema.YEAR, false) + "//", buffer);

			// Other stuff goes into the note field -- including the note
			// itself of course.

			Iterator specIter = m_RISNoteFields.entrySet().iterator();
			while (specIter.hasNext())
			{
				Map.Entry entry = (Map.Entry) specIter.next();
				String fieldname = (String) entry.getKey();
				String prefix = (String) entry.getValue();
				exportRisField("N1", getCitationProperty(fieldname, false), buffer, prefix);
			}

			/**
			 * Deal with URLs.
			 */

			Iterator urlIDs = this.getCustomUrlIds().iterator();
			while (urlIDs.hasNext())
			{
				String id = urlIDs.next().toString();
				try
				{
					String url = this.getCustomUrl(id);
					String urlLabel = this.getCustomUrlLabel(id);

					exportRisField("UR", url, buffer); // URL
					exportRisField("NT",  url, buffer, urlLabel); // Note

				}
				catch (IdUnusedException e)
				{
					// do nothing
				}
			}

			// Write out the end-of-record identifier and an extra newline
			exportRisField("ER", "", buffer);
			buffer.append("\n");
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.Citation#getCitationProperties()
		 */
		public Map getCitationProperties()
		{
			if (m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}

			return m_citationProperties;

		}
		
		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.citation.api.Citation#getCitationProperty(java.lang.String)
		 */
		public Object getCitationProperty(String name) {
			return this.getCitationProperty(name, false);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.sakaiproject.citation.api.Citation#getCitationProperty(java.lang.String, boolean)
		 */
		public Object getCitationProperty(String name, boolean needSingleValue)
		{
			Object value = null;
			if(name == null) {
				value = "";
			} else {
				if (m_citationProperties == null)
				{
					m_citationProperties = new Hashtable();
				}
				value = m_citationProperties.get(name);
				if (value == null)
				{
					if (isMultivalued(name))
					{
						value = new Vector();
						((List) value).add("");
					}
					else
					{
						value = "";
					}
				} else if (List.class.isInstance(value)) {
					if(needSingleValue ) {
						value = ((List) value).get(0);
					}
				}
			}

			return value;

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getAuthor()
		 */
		public String getCreator()
		{
			List creatorList = null;

			Object creatorObj = m_citationProperties.get(Schema.CREATOR);

			if (creatorObj == null)
			{
				creatorList = new Vector();
				m_citationProperties.put(Schema.CREATOR, creatorList);
			}
			else if (creatorObj instanceof List)
			{
				creatorList = (List) creatorObj;
			}
			else if (creatorObj instanceof String)
			{
				creatorList = new Vector();
				creatorList.add(creatorObj);
				m_citationProperties.put(Schema.CREATOR, creatorList);
			}

			String creators = "";
			int count = 0;

			if (creatorList == null)
			{
				return creators;
			}

			StringBuilder buf = new StringBuilder();
			Iterator it = creatorList.iterator();
			while (it.hasNext())
			{
				String creator = (String) it.next();
				if (it.hasNext() && count > 0)
				{
					buf.append(";");
				}
				else if (it.hasNext())
				{
					// do nothing
				}
				else if (count > 1)
				{
					buf.append("; and ");
				}
				else if (count > 0)
				{
					buf.append(" and ");
				}
				buf.append(creator);
				count++;
			}
			creators = buf.toString();
			if (!creators.trim().equals("") && !creators.trim().endsWith("."))
			{
				creators = creators.trim() + ". ";
			}
			return creators;
		}

		/**
		 * Fetch a custom (direct) URL by ID.
		 *
		 * @see org.sakaiproject.citation.api.Citation#getCustomUrl(java.lang.String)
		 */
		public String getCustomUrl(String id) throws IdUnusedException
		{
			UrlWrapper wrapper;
	    StringBuilder urlBuffer;
	    String prefix;

	    if ((wrapper = (UrlWrapper) m_urls.get(id)) == null)
			{
				throw new IdUnusedException(id);
			}

  	  urlBuffer = new StringBuilder(wrapper.getUrl());

  	  if (wrapper.addPrefix())
  	  {
    	  if ((prefix = getUrlPrefix()) != null)
  			{
  			  urlBuffer.insert(0, prefix);
        }
      }
			return urlBuffer.toString();
		}

		/**
		 * Fetch a custom (direct) URL by ID.  The URL prefix (if applicable)
		 * is not added.
		 *
		 * @see org.sakaiproject.citation.api.Citation#getUnprefixedCustomUrl(java.lang.String)
		 */
		public String getUnprefixedCustomUrl(String id) throws IdUnusedException
		{
			UrlWrapper wrapper = (UrlWrapper) m_urls.get(id);

			if (wrapper == null)
			{
				throw new IdUnusedException(id);
			}
			return wrapper.getUrl();
		}

		/**
		 * Fetch the configured URL prefix string.
		 * @return The prefix (null if none)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getUnprefixedCustomUrl(java.lang.String)
		 */
		public String getUrlPrefix()
		{
      return m_configService.getSiteConfigPreferredUrlPrefix();
		}

		/**
		 * Add prefix text to this URL?
		 * @return true If the URL should get the prefix
		 */
		public boolean addPrefixToUrl(String id) throws IdUnusedException
		{
			UrlWrapper wrapper = (UrlWrapper) m_urls.get(id);

			if (wrapper == null)
			{
				throw new IdUnusedException(id);
			}
			return wrapper.addPrefix();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getUrlIds()
		 */
		public List getCustomUrlIds()
		{
			List rv = new Vector();
			if (!m_urls.isEmpty())
			{
				rv.addAll(m_urls.keySet());
			}
			return rv;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getUrlLabel(java.lang.String)
		 */
		public String getCustomUrlLabel(String id) throws IdUnusedException
		{
			UrlWrapper wrapper = (UrlWrapper) m_urls.get(id);
			if (wrapper == null)
			{
				throw new IdUnusedException(id);
			}

			return wrapper.getLabel();
		}

		public String getYear()
		{
			String yearDate = (String) getCitationProperty(Schema.YEAR, true);
			return yearDate;
		}

		public String getDisplayName()
		{
			String displayName = (String) getCitationProperty(Schema.TITLE, true);
			if (displayName == null || displayName.trim().equals(""))
			{
				displayName = "untitled";
				setCitationProperty(Schema.TITLE, "untitled");
			}
			displayName = displayName.trim();
			if (displayName.length() > 0 && !displayName.endsWith(".") && !displayName.endsWith("?") && !displayName.endsWith("!") && !displayName.endsWith(","))
			{
				displayName += ".";
			}
			return new String(displayName);

		}

		/**
		 * Get the primary URL for this resource
		 *
		 * Normally, this is an OpenURL created from citation properties, but if
		 * either the Repository OSID or the user has designated a preferred URL,
		 * we'll use it instead.
		 *
		 * @return The primary URL (null if none available)
		 */
    public String getPrimaryUrl()
    {
		  String url;
			/*
			 * Stop now if we haven't set up any Citation properties
			 */
			if (m_citationProperties == null)
			{
				return null;
			}
			/*
			 * Custom URL?
			 */
			if (hasPreferredUrl())
			{
			  String id = getPreferredUrlId();

  			try
	  		{
  			  return getCustomUrl(id);
  			}
        catch (IdUnusedException exception)
        {
          log.warn("No matching URL for ID: "
                  +  id
                  +  ", returning an OpenURL");
        }
      }
      /*
       * Use an OpenURL
       */
      return getOpenurl();
    }

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getFirstAuthor()
		 */
		public String getFirstAuthor()
		{
			String firstAuthor = null;
			List authors = (List) this.m_citationProperties.get(Schema.CREATOR);
			if (authors != null && !authors.isEmpty())
			{
				firstAuthor = (String) authors.get(0);
			}
			if (firstAuthor != null)
			{
				firstAuthor = firstAuthor.trim();
			}
			return firstAuthor;
		}

		public String getId()
		{
			if (isTemporary())
			{
				return m_serialNumber.toString();
			}
			return m_id;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getOpenurl()
		 */
		public String getOpenurl()
		{
			// check citationProperties
			if (m_citationProperties == null)
			{
				// citation properties do not exist as yet - no OpenUrl
				return null;
			}

      // SAK-16886 Honor parameters "hard coded" at the end of the resolver URL
			String resolverUrl    = m_configService.getSiteConfigOpenUrlResolverAddress();
			String firstDelimiter = (resolverUrl.indexOf("?") != -1) ? "&" : "?";
			String openUrlParams  = getOpenurlParameters();

			// return the URL-encoded string
			return resolverUrl + firstDelimiter + openUrlParams;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getOpenurlParameters()
		 */
		public String getOpenurlParameters()
		{
			// check citationProperties
			if (m_citationProperties == null)
			{
				// citation properties do not exist as yet - no OpenUrl
				return "";
			}

			ContextObject co = m_openURLService.convert(this);
			StringBuilder openUrl = new StringBuilder();
			//openUrl.append("?");
			if (co != null)
			{
				String openUrlParams = m_openURLService.toURL(co);
				openUrl.append(openUrlParams);
			}

			// genre needs some further work... TODO

			return openUrl.toString();
		}
		
		/**
		 * This only makes sense, and will only be set, in the context of a collection.
		 */
		public int getPosition()
		{
			return m_position;
		}

		public Schema getSchema()
		{
			return m_schema;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#getSource()
		 */
		public String getSource()
		{
			String place = (String) getCitationProperty("publicationLocation", true);
			String publisher = (String) getCitationProperty(Schema.PUBLISHER, true);
			String sourceTitle = (String) getCitationProperty(Schema.SOURCE_TITLE, true);
			String year = (String) getCitationProperty(Schema.YEAR, true);
			String volume = (String) getCitationProperty(Schema.VOLUME, true);
			String issue = (String) getCitationProperty(Schema.ISSUE, true);
			String pages = (String) getCitationProperty(Schema.PAGES, true);
			String startPage = (String) getCitationProperty("startPage", true);
			String endPage = (String) getCitationProperty("endPage", true);
			if (pages == null || pages.trim().equals(""))
			{
				pages = null;
				if (startPage != null && ! startPage.trim().equals(""))
				{
					pages = startPage.trim();
					if (endPage != null && ! endPage.trim().equals(""))
					{
						pages += "-" + endPage;
					}
				}
			}

			String source = "";
			String schemaId = "unknown";
			if (m_schema != null)
			{
				schemaId = m_schema.getIdentifier();
			}
			if ("book".equals(schemaId) || "report".equals(schemaId))
			{
				if (place != null && !place.trim().equals(""))
				{
					source += place;
				}
				if (publisher != null && !publisher.trim().equals(""))
				{
					if (source.length() > 0)
					{
						source = source.trim() + ": ";
					}
					source += publisher;
				}
				if (year != null && ! year.trim().equals(""))
				{
					if (source.length() > 0)
					{
						source = source.trim() + ", ";
					}
					source += year;
				}
			}
			else if ("article".equals(schemaId))
			{

				if (sourceTitle != null && !sourceTitle.trim().equals(""))
				{
					source += sourceTitle;
					if (volume != null && !volume.trim().equals(""))
					{
						source += ", " + volume;
						if (issue != null && !issue.trim().equals(""))
						{
							source += "(" + issue + ") ";
						}
					}
				}
				if(year != null && ! year.trim().equals(""))
				{
					source += " " + year;
				}
				if(source != null && source.length() > 1)
				{
					source = source.trim();
					if(!source.endsWith(".") && !source.endsWith("?") && !source.endsWith("!") && !source.endsWith(","))
					{
						source += ". ";
					}
				}
				if (pages != null && !pages.trim().equals(""))
				{
					source += pages;
				}
				if(source != null && source.length() > 1)
				{
					source = source.trim();
					if(!source.endsWith(".") && !source.endsWith("?") && !source.endsWith("!") && !source.endsWith(","))
					{
						source += ". ";
					}
				}
			}
			else if ("chapter".equals(schemaId))
			{
				if (sourceTitle != null && !sourceTitle.trim().equals(""))
				{
					source += "In " + sourceTitle;
					if (pages == null)
					{
						if (startPage != null && !startPage.trim().equals(""))
						{
							source = source.trim() + ", " + startPage;
							if (endPage != null && !endPage.trim().equals(""))
							{
								source = source.trim() + "-" + endPage;
							}
						}
					}
					else
					{
						source = source.trim() + ", " + pages;
					}
					if (publisher != null && !publisher.trim().equals(""))
					{
						if (place != null && !place.trim().equals(""))
						{
							source += place + ": ";
						}
						source += publisher;
						if (year != null && ! year.trim().equals(""))
						{
							source += ", " + year;
						}
					}
					else if (year != null && ! year.trim().equals(""))
					{
						source += " " + year;
					}
				}
			}
			else
			{
				if (sourceTitle != null && ! sourceTitle.trim().equals(""))
				{
					source += sourceTitle;
					if (volume != null && ! volume.trim().equals(""))
					{
						source += ", " + volume;
						if (issue != null && !issue.trim().equals(""))
						{
							source += "(" + issue + ") ";
						}
					}
					if (pages == null)
					{
						if (startPage != null && !startPage.trim().equals(""))
						{
							source += startPage;
							if (endPage != null && !endPage.trim().equals(""))
							{
								source += "-" + endPage;
							}
						}
					}
					else
					{
						if (source.length() > 0)
						{
							source += ". ";
						}
						source += pages + ". ";
					}
				}
				else if (publisher != null && ! publisher.trim().equals(""))
				{
					if (place != null && ! place.trim().equals(""))
					{
						source += place + ": ";
					}
					source += publisher;
					if (year != null && ! year.trim().equals(""))
					{
						source += ", " + year;
					}
				}
			}

			if (source.length() > 1 && !source.endsWith(".") && !source.endsWith("?") && !source.endsWith("!") && !source.endsWith(","))
			{
				source = source.trim() + ". ";
			}

			if( source.trim().endsWith( ".." ) )
			{
				source = source.substring( 0, source.length()-2 );
			}

			return source;
		}

		public String getAbstract()
		{
			if ((m_citationProperties != null) &&
			    (m_citationProperties.get( "abstract" ) != null))
			{
				String abstractText = m_citationProperties.get("abstract").toString().trim();

				if (abstractText.length() > 0)
				{
				  return abstractText;
				}
			}
			return null;
		}

		public String getSubjectString() {
			Object subjects = getCitationProperty( "subject", false );

			if ( subjects instanceof List )
			{
				List subjectList = ( List ) subjects;
				ListIterator subjectListIterator = subjectList.listIterator();

				StringBuilder subjectStringBuf = new StringBuilder();

				while ( subjectListIterator.hasNext() )
				{
					subjectStringBuf.append( ((String)subjectListIterator.next()).trim() + ", " );
				}

				String subjectString = subjectStringBuf.substring( 0, subjectStringBuf.length() - 2 );

				if ( subjectString.equals("") )
				{
					return null;
				}
				else
				{
					return subjectString;
				}
			}
			else
			{
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#hasUrls()
		 */
		public boolean hasCustomUrls()
		{
			return m_urls != null && !m_urls.isEmpty();
		}

		public boolean hasPropertyValue(String fieldId)
		{
			return hasCitationProperty(fieldId);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#importFromRis(java.io.InputStream)
		 */
	    public void importFromRis(InputStream ris) throws IOException
		{

			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#importFromRisList(java.util.List)
		 */
		public boolean importFromRisList(List risImportList)
		{
			String currentLine = null; // The active line being parsed from the list (e.g. "TY - BOOK")
			String RIScode = null; // The RIS Code (e.g. "TY" for "TY - BOOK")
			String RISvalue = null; // The RIS Value (e.g. "BOOK" for "TY - BOOK")
			Schema schema = null;  // The Citations Helper Schema to use based on the TY RIS field
			String schemaName = null; // The name/string of the schema used to lookup the schema
			List Fields = null; // This holds the mapped fields for a particular Schema
			Field tempField = null; // This holds the current field being evaluated while iterating through
			                        // Fields
			Iterator iter = null; // The iterator used to boogie through the Schema Fields
			boolean noFieldMapping = true; // Used to maintain/exit the tag lookup while loop
			String[] RIScodes = null; // This holds the RISCodes valid for a given schema
			String urlId = null; // Used to set the preferred URL

			String continueTag = null; // used to track EndNote continuation lines.
			int delimiterIndex  = 0; // used to find the index of the hyphen to separate RIScode from RISvalue

			log.debug("importFromRisList: In importFromRisList. List size is " + risImportList.size());

			// process loop that iterates list size many times
			for(int i=0; i< risImportList.size(); i++)
			{
				// get current RIS line
				String dirtyString = (String) risImportList.get(i);
				currentLine = dirtyString.replaceAll("[\uFEFF-\uFFFF]", "");
				currentLine = currentLine.trim();
				log.debug("importFromRisList: currentLine = " + currentLine);

				// If the RIS line is less than 4, it isn't really a valid line. Set some default values
				// that we know won't be processed for this line.
				if (currentLine.length() < 4)
				{
					RIScode = "CODENOTFOUND";
					RISvalue = "";
				}
				else
				{
					// get the RIS code

					// New parsing code 2008-09 based on first delimiter not index in String

					delimiterIndex  = currentLine.indexOf('-');

					// if we found a hyphen
					if (delimiterIndex != -1)
					{
						RIScode = currentLine.substring(0, delimiterIndex).trim();

						// get substring starting with hyphen. This guarantees that we at least have a
						// string of length 1 for processing

						RISvalue = currentLine.substring(delimiterIndex).trim();

						// if RISvalue's length is greater than 1 that means we have more than the hyphen for the
						// string.  We then discard the hyphen (1st character)

						if (RISvalue.length() > 1)
						{
							RISvalue = RISvalue.substring(1);
						}
						else
						{
							RISvalue = "";
						}

					}
					else
					{
						RIScode = "CODENOTFOUND";
						RISvalue = "";
					}

					log.debug("importFromRisList: substr value = " + RISvalue);
				}

				// Trim the value
				RISvalue = RISvalue.trim();

				// The RIS code TY must be the first entry is a RIS record. This sets the Schema type.
				if (i == 0)
				{
					if (! RIScode.equalsIgnoreCase("TY"))
					{
					   	log.debug("importFromRisList: FALSE - 1st entry in RIS must be TY. It isn't it is " + RISvalue);
					   	return false; // TY MUST be the first entry in a RIS citation
					}
					else // process the schema
					{
						// RIS resource type forced mappings

						if (RISvalue.equalsIgnoreCase("NEWS") || RISvalue.equalsIgnoreCase("MGZN"))
						{
						   	log.debug("importFromRisList: force mapping NEWS or MGZN resource type to JOUR");
							RISvalue = "JOUR";
						}


					   	log.debug("importFromRisList: size of m_RISTypeInverse = " + m_RISTypeInverse.size());
					 	log.debug("importFromRisList: RISvalue before schemaName = " + RISvalue);

					 	// get the Schema String name that we need to use for Schema look up from
					 	// the map m_RISTypeInverse using the RISvalue for RIScode "TY";
				    	schemaName = (String) m_RISTypeInverse.get(RISvalue);

				    	// If we couldn't find a valid schema name mapping, set the name to "unknown"
					    if (schemaName == null)
					    {
						   	log.debug("importFromRisList: Unknown Schema Name = " + RISvalue +
						    			     ". Setting schemeName to 'unknown'");
					    		schemaName = "unknown";
					    }
					    	log.debug("importFromRisList: Schema Name = " + schemaName);

					    	// Lookup the Schema based on the Schema string gotten from the reverse map
							schema = BaseCitationService.this.getSchema(schemaName);
					    	log.debug("importFromRisList: Retrieved Schema Name = " + schema.getIdentifier());
							setSchema(schema);
					} // end else (else processes RIScode == "TY")
				} // end if i == 0
				else // i > 0 so we are on a line other than the first line of the RIS record.  Let's process the RIS entries after the first mandatory TY/Schema code
				{
				   	if (RIScode.equalsIgnoreCase("ER")) // RIScode "ER" signifies the end of a citation record
					{
					   	log.debug("importFromRisList: Read an ER. End of citation.");

						return true; // ER signals end of citation
					} // end of citation

					// Get all the valid RIS fields for this particular schema type
					Fields = schema.getFields();
					iter = Fields.iterator();
					noFieldMapping = true;

					while (iter.hasNext() && noFieldMapping)
					{
						tempField = (Field) iter.next();

						// We found that this field is a valid field for this schema

						RIScodes = tempField.getIdentifierComplex(RIS_FORMAT);

						for(int j=0; j< RIScodes.length && noFieldMapping; j++)
						{
							// Need Trim in case RIS complex value has a space after the delimiter
							// (e.g. "BT, T1" vs "BT","T1")
							if (RIScode.equalsIgnoreCase(RIScodes[j]))
							{
								noFieldMapping = false;
								continueTag = null;
								log.debug("importFromRisList: Found field mapping");
							}
						} // end for j (loop through complex RIS codes)
					} // end while

					if (noFieldMapping) // couldn't find the field mapping
					{
						  log.debug("importFromRisList: Cannot find field mapping for RIScode " +
		                               RIScode + " for Schema = " + schema);

						  // recompute hyphen location for KWTag check. Computation earlier may have gotten mangled

						  delimiterIndex  = currentLine.indexOf('-');

						  if (delimiterIndex == -1)
						  {
							  RIScode = "CODENOTFOUND";
						  }
						  else
						  {
								RIScode = currentLine.substring(0, delimiterIndex).trim();
						  }

						  if (RIScode.equalsIgnoreCase("UR"))
						  {
							urlId = addCustomUrl("", RISvalue);
							setPreferredUrl(urlId);
							continueTag = RIScode.toUpperCase();
							log.debug("importFromRisList: set preferred url to " + urlId + " which is " + RISvalue);
						  }
						  else if (continueTag != null && (RIScode.length() != 2) ) // continuation and not a possible RIScode
						  {
							  log.debug("importFromRisList: continuation of tag found (EndNote oddity). Hacking tag and resending line through the import system");
							  risImportList.set(i, continueTag + " - " + currentLine);
							  i = i-1;
						  }

					} // end if noFieldMapping (field not found)
					else // ! noFieldMapping. We found a field in the Schema
					{
						log.debug("importFromRisList: Field mapping is " + tempField.getIdentifier() +
								     " => " + RISvalue);

						if (RIScode.equalsIgnoreCase("KW"))
							continueTag = RIScode.toUpperCase();

					  // We found a mapping.  Set the citation property.
					  setCitationProperty(tempField.getIdentifier(), RISvalue);

 					  // SAK-16949 -- If we have a date, we may need to extract and save
 					  //              the year as well.
 					  //
						if (RIScode.equalsIgnoreCase("Y1") || RIScode.equalsIgnoreCase("PY"))
						{
						  if (!schemaName.equalsIgnoreCase("electronic")
						  ||  !schemaName.equalsIgnoreCase("proceed")
						  ||  !schemaName.equalsIgnoreCase("thesis"))
						  {
						    setYearProperty(RISvalue);
						  }
						}

					} // end else which means we found the mapping

				} // end else of i == 0
			} // end for i

			// if we got here, the record wasn't properly formatted with an "ER" record (or other issues).
	    	log.debug("importFromRisList: FALSE - End of Input. Citation not added.");
			return false;

		}  // end ImportFromRisList

    /*
     * Set the year value.  This is based on a provided RIS date:
     *
     *    YYYY/MM/DD/Comment
     *
     * We reformat the date to reflect the ISO format:
     *
     *    YYYY-MM-DD
     */
    private void setDateProperty(String date)
    {
      StringBuilder normalized;
      String[]      components;
      /*
       * Save the date "as is".  We'll normalize and [possibly] save again.
       */
      setCitationProperty("date", date);

      if (date.length() < 4) return;

      normalized = new StringBuilder();
      components  = date.split("/");
      /*
       * Year
       */
      if (components.length == 0) return;

      for (int i = 0; i < 4; i++)
      {
        char c = components[0].charAt(i);

        if (!Character.isDigit(c)) return;

        normalized.append(c);
      }
      /*
       * Month
       */
      if (components.length == 1)
      {
        setCitationProperty("date", normalized.toString());
        return;
      }

      if (components[1].length() == 2)
      {
        normalized.append("-");

        for (int i = 0; i < 2; i++)
        {
          char c = components[1].charAt(i);

          if (Character.isDigit(c))
          {
            normalized.append(c);
          }
        }
      }
      /*
       * Day
       */
      if (components.length == 2)
      {
        if (normalized.length() < 7)
        {
          normalized.setLength(4);
        }
        setCitationProperty("date", normalized.toString());
        return;
      }

      if (components[2].length() == 2)
      {
        normalized.append("-");

        for (int i = 0; i < 2; i++)
        {
          char c = components[2].charAt(i);

          if (Character.isDigit(c))
          {
            normalized.append(c);
          }
        }

        if (normalized.length() < 10)
        {
          normalized.setLength(((normalized.length() < 7) ? 4 : 7));
        }
      }
      /*
       * Save the normalized date
       */
      setCitationProperty("date", normalized.toString());
      /*
       * Is there a note (and can we save it)?
       */
      if (components.length == 3) return;
      /*
       * Discard the comment field for now
       */
    }

    /*
     * Set the 4 digit year value.  This is based on a provided date.  We
     * assume RIS or ISO forrmatting, where the first four digits specify
     * a year.  Specifically:
     *
     *    YYYY/MM/DD/comment  (RIS)
     *    YYYY-MM-DD          (ISO)
     *    YYYY                (what we usually see in practice)
     */
    private void setYearProperty(String date)
    {
      StringBuilder year;

      if (date.length() < 4) return;

      year = new StringBuilder();

      for (int i = 0; i < 4; i++)
      {
        char c = date.charAt(i);

        if (!Character.isDigit(c)) return;

        year.append(c);
      }
      setCitationProperty(Schema.YEAR, year.toString());
    }

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#isAdded()
		 */
		public boolean isAdded()
		{
			return this.m_isAdded;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#isMultivalued(java.lang.String)
		 */
		public boolean isMultivalued(String fieldId)
		{
			boolean isMultivalued = false;
			if (isSchemaLimited(fieldId))
			{
				isMultivalued = isSchemaMultivalued(fieldId);
			}
			else
			{
				isMultivalued = isCurrentlyMultivalued(fieldId);
				// But if adding, then convert?
			}
			return isMultivalued;
		}

		/**
		 * Checks if the field is currently multivalued.
		 * This doesn't consult the schema, but looks what's actually stored.
		 * @param fieldId Field name.
		 * @return <code>true</code> if the field is multivalued.
		 */
		protected boolean isCurrentlyMultivalued(String fieldId)
		{
			// Don't use getCitationProperty as it ends up being recursive
			return (m_citationProperties.get(fieldId) instanceof List);
		}

		/**
		 * Should the field be limited to a single value.
		 * @param fieldId
		 * @return
		 */
		protected boolean isSchemaMultivalued(String fieldId)
		{
			// No check for m_schema being null as you should check isSchemaLimited first
			Field field = m_schema.getField(fieldId);
			return (field != null && field.isMultivalued());
		}

		/**
		 * @return
		 */
		public boolean isTemporary()
		{
			return m_temporary;
		}

		protected boolean isSchemaLimited(String fieldId)
		{
			return m_schema != null && m_schema.getField(fieldId) != null;
		}

		public List listCitationProperties()
		{
			if (m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}

			return new Vector(m_citationProperties.keySet());

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#setAdded()
		 */
		public void setAdded(boolean added)
		{
			this.m_isAdded = added;
		}

		public void setCitationProperty(String name, Object value)
		{
			if (m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			if (isMultivalued(name))
			{
				List list = (List) m_citationProperties.get(name);
				if (list == null)
				{
					list = new Vector();
					m_citationProperties.put(name, list);
				}
				if (value != null)
				{
					list.add(value);
				}
			}
			else
			{
				if (value == null)
				{
					m_citationProperties.remove(name);
				}
				else
				{
					Object newValue = value;
					// Make value multivalued if possible.
					// Only do this on setCitation.
					if (!isSchemaLimited(name)) 
					{
						if (hasCitationProperty(name))
						{
							Object existingValue =  m_citationProperties.get(name);
							List list = new Vector();
							list.add(existingValue);
							list.add(value);
							newValue = list;
						}
					}
					m_citationProperties.put(name, newValue);
				}
			}

		}

		public boolean hasCitationProperty(String fieldId)
		{
			Object val = m_citationProperties.get(fieldId);
			boolean hasValue = val != null;
			if (hasValue)
			{
				if (val instanceof List)
				{
					List list = (List) val;
					hasValue = !list.isEmpty();
				}
			}
   
			return hasValue;
		}


		protected void setDefaults()
		{
			if (m_schema != null)
			{
				List fields = m_schema.getFields();
				Iterator it = fields.iterator();
				while (it.hasNext())
				{
					Field field = (Field) it.next();
					if (field.isRequired())
					{
						Object value = field.getDefaultValue();
						if (value == null)
						{
							// do nothing -- there's no value to set
						}
						else if (field.isMultivalued())
						{
							List current_values = (List) this.getCitationProperty(field
							        .getIdentifier(), false);
							if (current_values.isEmpty())
							{
								this.setCitationProperty(field.getIdentifier(), value);
							}
						}
						else if (this.getCitationProperty(field.getIdentifier(), false) == null)
						{
							setCitationProperty(field.getIdentifier(), value);
						}
					}
				}
			}
		}

		public void setDisplayName(String name)
		{
			if (name == null || name.trim().equals(""))
			{
				addPropertyValue(Schema.TITLE, "untitled");
			}
			else
			{
				addPropertyValue(Schema.TITLE, name);
			}
		}
		
		/**
		 * This only makes sense, and will only be set, in the context of a collection.
		 */
		public void setPosition(int position)
		{
			this.m_position = position;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Citation#setSchema(org.sakaiproject.citation.api.Schema)
		 */
		public void setSchema(Schema schema)
		{
			this.m_schema = schema;
			setDefaults();

		}

		protected void setType(String mediatype)
		{
			Schema schema = m_storage.getSchema(mediatype);
			if (schema == null)
			{
				schema = m_storage.getSchema(CitationService.UNKNOWN_TYPE);
			}
			setSchema(schema);

		}

		public String toString()
		{
			return "BasicCitation: " + this.m_id;
		}

		public void updateCitationProperty(String name, List values)
		{
			// what if "name" is not a valid field in the schema??
			if (m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			if (isMultivalued(name))
			{
				List list = (List) m_citationProperties.get(name);
				if (list == null)
				{
					list = new Vector();
					m_citationProperties.put(name, list);
				}
				list.clear();
				if (values != null)
				{
					list.addAll(values);
				}
			}
			else
			{
				if (values == null || values.isEmpty())
				{
					m_citationProperties.remove(name);
				}
				else
				{
					m_citationProperties.put(name, values.get(0));
				}
			}

		}

		public String getPreferredUrlId()
		{
			return this.m_preferredUrl;
		}

		public boolean hasPreferredUrl()
		{
			return this.m_preferredUrl != null;
		}

		public void setPreferredUrl(String urlid)
		{
			if(urlid == null)
			{
				this.m_preferredUrl = null;
			}
			else if(this.m_urls.containsKey(urlid))
			{
				this.m_preferredUrl = urlid;
			}
		}

	} // BaseCitationService.BasicCitation

	/**
	 *
	 */
	public class BasicCitationCollection implements CitationCollection
	{
		protected final Comparator  DEFAULT_COMPARATOR= new BasicCitationCollection.PositionComparator();

		public class MultipleKeyComparator implements Comparator
		{
			protected List<String> m_keys = new Vector<String>();

			protected boolean m_ascending = true;

			public MultipleKeyComparator(List<String> keys, boolean ascending)
			{
				m_keys.addAll(keys);

			}

			public MultipleKeyComparator( MultipleKeyComparator mkc )
			{
				this.m_keys      = mkc.m_keys;
				this.m_ascending = mkc.m_ascending;
			}

			/* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare(Object arg0, Object arg1)
            {
	            int rv = 0;
				if (!(arg0 instanceof String) || !(arg1 instanceof String))
				{
					throw new ClassCastException();
				}

				Object obj0 = m_citations.get(arg0);
				Object obj1 = m_citations.get(arg1);


				if (!(obj0 instanceof Citation) || !(obj1 instanceof Citation))
				{
					throw new ClassCastException();
				}
				Citation cit0 = (Citation) obj0;
				Citation cit1 = (Citation) obj1;

	            Iterator keyIt = m_keys.iterator();
	            while(rv == 0 && keyIt.hasNext())
	            {
	            	String key = (String) keyIt.next();
	            	if(CitationCollection.SORT_BY_TITLE.equalsIgnoreCase(key))
	            	{
	            		/*
	            		String title0 = cit0.getDisplayName().toLowerCase();
	            		String title1 = cit1.getDisplayName().toLowerCase();
	            		*/
	            		String title0 = ((String)cit0.getCitationProperty( Schema.TITLE, true )).toLowerCase();
	            		String title1 = ((String)cit1.getCitationProperty( Schema.TITLE, true )).toLowerCase();

	            		if (title0 == null)
	            		{
	            			title0 = "";
	            		}

	            		if (title1 == null)
	            		{
	            			title1 = "";
	            		}

	            		rv = m_ascending ? title0.compareTo(title1) : title1.compareTo(title0);
	            	}
	            	else if(CitationCollection.SORT_BY_AUTHOR.equalsIgnoreCase(key))
	            	{
	            		String author0 = cit0.getCreator().toLowerCase();
	            		String author1 = cit1.getCreator().toLowerCase();

	            		if (author0 == null)
	            		{
	            			author0 = "";
	            		}

	            		if (author1 == null)
	            		{
	            			author1 = "";
	            		}
	            		rv = m_ascending ? author0.compareTo(author1) : author1.compareTo(author0);
	            	}
	            	else if(CitationCollection.SORT_BY_YEAR.equalsIgnoreCase(key))
	            	{
	            		String year0 = cit0.getYear();
	            		String year1 = cit1.getYear();

	            		if (year0 == null)
	            		{
	            			year0 = "";
	            		}

	            		if (year1 == null)
	            		{
	            			year1 = "";
	            		}
	            		rv = m_ascending ? year0.compareTo(year1) : year1.compareTo(year0);
	            	}
	            	else if( CitationCollection.SORT_BY_UUID.equalsIgnoreCase( key ) )
	            	{
	            		// not considering m_ascending for ids because they are random alpha-numeric strings
	            		rv = cit0.getId().compareTo(cit1.getId());
	            	}
	            }
	            return rv;
            }

            public void addKey(String key)
            {
            	m_keys.add(key);
            }
		}

		public class AuthorComparator extends MultipleKeyComparator
		{
			/**
			 * @param ascending
			 */
			public AuthorComparator(boolean ascending)
			{
				super(AUTHOR_AS_KEY, ascending);
			}

		}

		public class YearComparator extends MultipleKeyComparator
		{
			/**
			 * @param ascending
			 */
			public YearComparator(boolean ascending)
			{
				super(YEAR_AS_KEY, ascending);
			}

		} // end class DateComparator
		
		public class PositionComparator implements Comparator
		{
            public int compare(Object arg0, Object arg1)
            {
	            int rv = 0;
				if (!(arg0 instanceof String) || !(arg1 instanceof String))
				{
					throw new ClassCastException();
				}

				Object obj0 = m_citations.get(arg0);
				Object obj1 = m_citations.get(arg1);


				if (!(obj0 instanceof Citation) || !(obj1 instanceof Citation))
				{
					throw new ClassCastException();
				}
				Citation cit0 = (Citation) obj0;
				Citation cit1 = (Citation) obj1;
				
				if(cit0.getPosition() > cit1.getPosition()) return 1;
				else if(cit0.getPosition() == cit1.getPosition()) return 0;
				else return -1;
            }
		} // end class PositionComparator

		public class BasicIterator implements CitationIterator
		{
			protected List listOfKeys;

			// This is the firstItem on a given rendered page
			protected int firstItem;

			// This is the item where next() returns and increments.  At
			// the start of rendering a given page nextItem = firstItem and
			// increments until lastItem
			protected int nextItem;

			// This is the lastitem on a given rendered page
			protected int lastItem;

			public BasicIterator()
			{
				checkForUpdates();
				this.listOfKeys = new Vector(m_order);
				this.firstItem = 0;
				setIndexes();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#getPageSize()
			 */
			public int getPageSize()
			{
				// TODO Auto-generated method stub
				return m_pageSize;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#hasNext()
			 */
			public boolean hasNext()
			{
				boolean hasNext = false;
				if (m_ascending)
				{
					hasNext = this.nextItem < this.lastItem
					        && this.nextItem < this.listOfKeys.size();
				}
				else
				{
					hasNext = this.nextItem > this.lastItem && this.nextItem > 0;
				}
				return hasNext;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#hasNextPage()
			 */
			public boolean hasNextPage()
			{
				boolean hasNextPage = false;

				if (m_ascending)
					hasNextPage = this.firstItem + m_pageSize < this.listOfKeys.size();
				else
					hasNextPage = this.firstItem - m_pageSize >= 0;

				return hasNextPage;

//				return m_pageSize * (startPage + 1) < this.listOfKeys.size();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#hasPreviousPage()
			 */
			public boolean hasPreviousPage()
			{
				boolean hasPreviousPage = false;

				if (m_ascending)
					hasPreviousPage = this.firstItem > 0;
				else
					hasPreviousPage = this.firstItem + m_pageSize < this.listOfKeys.size();

  			return hasPreviousPage;

//				return this.startPage > 0;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#next()
			 */
			public Object next()
			{
				Object item = null;
				if (m_ascending)
				{
					if (this.nextItem >= this.lastItem || this.nextItem >= listOfKeys.size())
					{
						throw new NoSuchElementException();
					}
					item = m_citations.get(listOfKeys.get(this.nextItem++));
				}
				else
				{
					if (this.nextItem <= this.lastItem || this.nextItem <= 0)
					{
						throw new NoSuchElementException();
					}
					item = m_citations.get(listOfKeys.get(this.nextItem--));
				}
				return item;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#nextPage()
			 */
			public void nextPage()
			{
				if (m_ascending)
				{
					this.firstItem = this.firstItem + m_pageSize;
				}
				else
				{
					this.firstItem = this.firstItem - m_pageSize;
				}

				setIndexes();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#previousPage()
			 */
			public void previousPage()
			{
				if (m_ascending)
				{
					this.firstItem = this.firstItem - m_pageSize;
				}
				else
				{
					this.firstItem = this.firstItem + m_pageSize;
				}

				setIndexes();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see java.util.Iterator#remove()
			 */
			public void remove()
			{
				throw new UnsupportedOperationException();

			}

			protected void setIndexes()
			{
				this.nextItem = this.firstItem;

				if (m_ascending)
				{
					this.lastItem = Math.min(this.listOfKeys.size(), this.nextItem + m_pageSize);
				}
				else
				{
					this.lastItem = Math.max(0, this.nextItem - m_pageSize);
				}
			} // end setIndexes()

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#setSort(java.util.Comparator)
			 */
			public void setOrder(Comparator comparator)
			{
				m_comparator = comparator;
				if (comparator == null)
				{

				}
				else
				{
					Collections.sort(this.listOfKeys, m_comparator);
				}

				this.firstItem = 0;
				setIndexes();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.sakaiproject.citation.api.CitationIterator#setPageSize(int)
			 */
			public void setPageSize(int size)
			{
				if (size > 0)
				{
					// So that resizing the page at the start of a list (say 11-20)
					// with a new size of 20... gives us 1-20 not 11-30
					if (this.lastItem <= size)
					{
						this.firstItem = 0;
					}

					this.lastItem = this.firstItem + size;

					if (this.lastItem >= this.listOfKeys.size())
						this.lastItem = this.listOfKeys.size() - 1;

					m_pageSize = size;
					setIndexes();
				} // end if size > 0

			} // end setPageSize()

			public int getStart()
			{
				return this.firstItem;
			}

			public int getEnd()
			{
				return this.lastItem;
			}

			public void setStart(int i)
			{
				if (i >= 0 && i < this.listOfKeys.size())
				{
					this.firstItem = i;
					setIndexes();
				}
			} // end setStart()


		} // end class BasicIterator

		public class TitleComparator extends MultipleKeyComparator
		{
			/**
			 * @param ascending
			 */
			public TitleComparator(boolean ascending)
			{
				super(TITLE_AS_KEY, ascending);
			}

		}

		protected Map<String, Citation> m_citations = new LinkedHashMap<String, Citation>();

		protected List<CitationCollectionOrder> m_nestedCitationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		protected Comparator m_comparator = DEFAULT_COMPARATOR;

		protected String m_sortOrder;

		protected SortedSet<String> m_order;

		protected int m_pageSize = DEFAULT_PAGE_SIZE;

		protected String m_description;

		protected String m_id;

		protected String m_title;

		protected boolean m_temporary = false;

		protected Integer m_serialNumber;

		protected ActiveSearch m_mySearch;

		protected boolean m_ascending = true;

		protected long m_mostRecentUpdate = 0L;

		public BasicCitationCollection()
		{
			m_id = m_idManager.createUuid();
		}

		/**
		 * @param b
		 */
		public BasicCitationCollection(boolean temporary)
		{
			m_order = new TreeSet<String>(m_comparator);

			m_temporary = temporary;
			if (temporary)
			{
				m_serialNumber = nextSerialNumber();
			}
			else
			{
				m_id = m_idManager.createUuid();
			}
		}

		public BasicCitationCollection(Map attributes, List citations)
		{
			m_id = m_idManager.createUuid();

			m_order = new TreeSet<String>(m_comparator);

			if (citations != null)
			{
				Iterator citationIt = citations.iterator();
				while (citationIt.hasNext())
				{
					this.add((Citation) citationIt.next());
				}
			}
		}

		/**
		 * @param collectionId
		 */
		public BasicCitationCollection(String collectionId)
		{
			m_id = collectionId;

			m_order = new TreeSet(m_comparator);
		}

		public void add(Citation citation)
		{
			//checkForUpdates();
			if (!this.m_citations.keySet().contains(citation.getId()))
			{
				// Set this citation's position to the end. Used by the position
				// comparator and the reordering screen.
				citation.setPosition(m_citations.size() + 1);
				this.m_citations.put(citation.getId(), citation);
			}
			if(!this.m_order.contains(citation.getId()))
			{
				this.m_order.add(citation.getId());
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#addAll(org.sakaiproject.citation.api.CitationCollection)
		 */
		public void addAll(CitationCollection other)
		{
			checkForUpdates();
			if(this.m_order == null)
			{
				this.m_order = new TreeSet<String>();
			}
			for(String key : ((BasicCitationCollection) other).m_order )
			{
				try
				{
					Citation citation = other.getCitation(key);
					this.add(citation);
				}
				catch (IdUnusedException e)
				{
					log.debug("BasicCitationCollection.addAll citationId (" + key
					                + ") in m_order but not in m_citations; collectionId: "
					                + other.getId());
				}
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#clear()
		 */
		public void clear()
		{
			this.m_order.clear();
			this.m_citations.clear();
		}

		public boolean contains(Citation citation)
		{
			checkForUpdates();
			return this.m_citations.containsKey(citation.getId());
		}

		protected void copy(BasicCitationCollection other)
		{
			checkForUpdates();
			this.m_ascending = other.m_ascending;
			/*
			 * Get new instance of comparator
			 */
			if( other.m_comparator instanceof MultipleKeyComparator )
			{
				this.m_comparator = new MultipleKeyComparator( (MultipleKeyComparator)other.m_comparator );
			}
			else
			{
				// default to title, ascending
				this.m_comparator = new MultipleKeyComparator( TITLE_AS_KEY, true );
			}

			set(other, true);

		}

		public void exportRis(StringBuilder buffer, List<String>  citationIds) throws IOException
		{
			checkForUpdates();
			// output "header" info to buffer

			// Iterate over citations and output to ostream
			for( String citationId : citationIds )
			{
				Citation citation = (Citation) this.m_citations.get(citationId);
				if (citation != null)
				{
					citation.exportRis(buffer);
				}
			}
		}

		/**
		 * Compute an alternate root for a reference, based on the root
		 * property.
		 *
		 * @param rootProperty
		 *            The property name.
		 * @return The alternate root, or "" if there is none.
		 */
		protected String getAlternateReferenceRoot(String rootProperty)
		{
			// null means don't do this
			if (rootProperty == null || rootProperty.trim().equals(""))
			{
				return "";
			}

			// make sure it start with a separator and does not end with one
			if (!rootProperty.startsWith(Entity.SEPARATOR))
			{
				rootProperty = Entity.SEPARATOR + rootProperty;
			}

			if (rootProperty.endsWith(Entity.SEPARATOR))
			{
				rootProperty = rootProperty
				        .substring(0, rootProperty.length() - SEPARATOR.length());
			}

			return rootProperty;
		}

		public Citation getCitation(String citationId) throws IdUnusedException
		{
			checkForUpdates();
			Citation citation = (Citation) m_citations.get(citationId);
			if (citation == null)
			{
				throw new IdUnusedException(citationId);
			}
			return citation;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#getCitations()
		 */
		public List getCitations()
		{
			checkForUpdates();
			List citations = new Vector();
			if (m_citations == null)
			{
				m_citations = new Hashtable<String, Citation>();
			}
			if(m_order == null)
			{
				m_order = new TreeSet<String>();
			}

			Iterator keyIt = this.m_order.iterator();
			while (keyIt.hasNext())
			{
				String key = (String) keyIt.next();

				Object citation = this.m_citations.get(key);
				if (citation != null)
				{
					citations.add(citation);
				}
			}

			return citations;
		}

		public List<CitationCollectionOrder> getNestedCitationCollectionOrders() {
			return m_nestedCitationCollectionOrders;
		}

		public void setNestedCitationCollectionOrders(List<CitationCollectionOrder> m_nestedCitationCollectionOrders) {
			this.m_nestedCitationCollectionOrders = m_nestedCitationCollectionOrders;
		}

		public CitationCollection getCitations(Comparator c)
		{
			checkForUpdates();
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Comparator c, Filter f)
		{
			checkForUpdates();
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Filter f)
		{
			checkForUpdates();
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Map properties)
		{
			checkForUpdates();
			// TODO Auto-generated method stub
			return null;
		}

		// public Citation remove(int index)
		// {
		// // TODO
		// return null;
		// }
		//
		// public Citation remove(Map properties)
		// {
		// // TODO Auto-generated method stub
		// return null;
		// }

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#getDescription()
		 */
		public String getDescription()
		{
			checkForUpdates();
			return m_description;
		}

		public String getId()
		{
			return this.m_id;
		}
		
		@Override
		public Date getLastModifiedDate() {
			checkForUpdates();
			return new Date(this.m_mostRecentUpdate);
		}

		public ResourceProperties getProperties()
		{
			// TODO Auto-generated method stub
			return null;
		}

		// public void sort(Comparator c)
		// {
		// // TODO Auto-generated method stub
		//
		// }

		public String getReference()
		{

			return getReference(null);
		}

		public String getReference(String rootProperty)
		{

			return m_relativeAccessPoint + getAlternateReferenceRoot(rootProperty)
			        + Entity.SEPARATOR + getId();
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.CitationCollection#getSaveUrl()
         */
        public String getSort()
        {
        	if (m_sortOrder == null)
        		m_sortOrder = this.SORT_BY_DEFAULT_ORDER;

	        return m_sortOrder;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.CitationCollection#getSaveUrl()
         */
        public String getSaveUrl()
        {
	        String url = m_serverConfigurationService.getServerUrl() + "/savecite/" + this.getId() + "/";

	        return url;
        }

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#getTitle()
		 */
		public String getTitle()
		{
			checkForUpdates();
			return m_title;
		}

		public String getUrl()
		{
			return getUrl(null);
		}

		public String getUrl(String rootProperty)
		{
			return getAccessPoint(false) + getAlternateReferenceRoot(rootProperty)
			        + Entity.SEPARATOR + getId();
		}

		public boolean isEmpty()
		{
			checkForUpdates();
			return this.m_citations.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#iterator()
		 */
		public CitationIterator iterator()
		{
			checkForUpdates();
			return new BasicIterator();
		}

		// public Iterator iterator()
		// {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		//
		// public int lastIndexOf(Citation item)
		// {
		// // TODO Auto-generated method stub
		// return 0;
		// }
		//
		// public boolean move(int from, int to)
		// {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// public boolean moveToBack(int index)
		// {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// public boolean moveToFront(int index)
		// {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		public boolean remove(Citation item)
		{
			checkForUpdates();
			boolean success = true;
			this.m_order.remove(item.getId());
			Object obj = this.m_citations.remove(item.getId());
			if (obj == null)
			{
				success = false;
			}
			return success;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#saveCitation(org.sakaiproject.citation.api.Citation)
		 */
		public void saveCitation(Citation citation)
		{
			//checkForUpdates();
			// m_storage.saveCitation(citation);
			save(citation);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.CitationCollection#saveCitationCollectionOrder(org.sakaiproject.citation.api.CitationCollectionOrder)
		 */
		public void saveCitationCollectionOrder(CitationCollectionOrder citationCollectionOrder)
		{
			save(citationCollectionOrder);
		}
		/**
		 *
		 * @param comparator
		 */
		public void setSort(Comparator comparator)
		{
			checkForUpdates();
			this.m_comparator = comparator;
			SortedSet oldSet = this.m_order;
            this.m_order = new TreeSet<String>(this.m_comparator);
            this.m_order.addAll(oldSet);
		}

		/**
		 *
		 * @param sortBy
		 * @param ascending
		 */
		public void setSort(String sortBy, boolean ascending)
		{
			m_ascending = ascending;

			String status = "UNSET";

			if (sortBy == null || sortBy.equalsIgnoreCase(SORT_BY_DEFAULT_ORDER))
			{
				this.m_comparator = null;
			}
			else if (sortBy.equalsIgnoreCase(SORT_BY_AUTHOR))
			{
				 this.m_comparator = new AuthorComparator(ascending);
				 status = "AUTHOR SET";
			}
			else if (sortBy.equalsIgnoreCase(SORT_BY_TITLE))
			{
			      this.m_comparator = new TitleComparator(ascending);
				  status = "TITLE SET";
			}
			else if (sortBy.equalsIgnoreCase(SORT_BY_YEAR))
			{
					this.m_comparator = new YearComparator(ascending);
					status = "YEAR SET";
			}
			else if (sortBy.equalsIgnoreCase(SORT_BY_POSITION))
			{
					this.m_comparator = new PositionComparator();
					status = "POSITION SET";
			}

			if (this.m_comparator != null)
			{
				this.m_sortOrder = sortBy;
				SortedSet oldSet = this.m_order;
				this.m_order = new TreeSet<String>(this.m_comparator);
                this.m_order.addAll(oldSet);
			}

		} // end setSort(String, boolean)

		public int size()
		{
			checkForUpdates();
			return m_order.size();
		}

		public String toString()
		{
			return "BasicCitationCollection: " + this.m_id;
		}

		public Element toXml(Document doc, Stack stack)
		{
			// TODO Auto-generated method stub
			return null;
		}

		protected void checkForUpdates()
		{
			if(this.m_mostRecentUpdate < m_storage.mostRecentUpdate(this.m_id))
			{
				CitationCollection edit = m_storage.getCollection(this.m_id);
				if (edit == null)
				{

				}
				else
				{
					set((BasicCitationCollection) edit, false);
				}
			}

		}

		/**
		 * copy
		 * @param other
		 */
		protected void set(BasicCitationCollection other, boolean isTemporary)
		{
			this.m_description = other.m_description;
//			this.m_comparator = other.m_comparator;
			this.m_serialNumber = other.m_serialNumber;
			this.m_pageSize = other.m_pageSize;
			this.m_temporary = other.m_temporary;
			this.m_title = other.m_title;

			if(this.m_citations == null)
			{
				this.m_citations = new Hashtable<String, Citation>();
			}
			this.m_citations.clear();
			this.m_nestedCitationCollectionOrders.clear();
			if(this.m_order == null)
			{
				this.m_order = new TreeSet<String>(this.m_comparator);
			}
			this.m_order.clear();
			Iterator it = other.m_citations.keySet().iterator();
			while(it.hasNext())
			{
				String citationId = (String) it.next();
				BasicCitation oldCitation = (BasicCitation) other.m_citations.get(citationId);
				BasicCitation newCitation = new BasicCitation();
				try
				{
					newCitation.copy(oldCitation);
					newCitation.m_id = oldCitation.m_id;
					newCitation.m_temporary = isTemporary;
					this.saveCitation(newCitation);
					this.add(newCitation);
				}
				catch(Exception e)
				{
					log.warn("copy(" + oldCitation.getId() + ") ==> " + newCitation.getId(), e);
				}
			}

			Iterator iterator = other.getNestedCitationCollectionOrders().iterator();
			while(iterator.hasNext()) {
				CitationCollectionOrder citationCollectionOrder = (CitationCollectionOrder) iterator.next();
				if (citationCollectionOrder.isCitation()){
					try {
						// copy the citation
						CitationCollection collection = getCollection(citationCollectionOrder.getCollectionId());
						BasicCitation oldCitation = (BasicCitation) collection.getCitation(citationCollectionOrder.getCitationid());
						BasicCitation newCitation = new BasicCitation();
						newCitation.copy(oldCitation);
						newCitation.m_temporary = isTemporary;
						if (isTemporary) { // save the citation if it's not saved yet
							this.saveCitation(newCitation);
						}

						// copy the citation's citationCollectionOrder
						CitationCollectionOrder newCitationCollectionOrder = citationCollectionOrder.copy(this.getId(), newCitation.getId());
						if (isTemporary){ // save the citationCollectionOrder if it's not saved yet
							this.saveCitationCollectionOrder(newCitationCollectionOrder);
						}

					} catch (IdUnusedException e) {
						log.warn("copying citationcollectionorder(" + citationCollectionOrder.getCitationid() + ") ==> " + citationCollectionOrder.getValue(), e);
					}
				}
				else {
					// copy the citationCollectionOrder
					CitationCollectionOrder newCitationCollectionOrder = citationCollectionOrder.copy(this.getId());
					if (isTemporary){ // save the citationCollectionOrder if it's not saved yet
						this.saveCitationCollectionOrder(newCitationCollectionOrder);
					}
				}
			}

			this.m_mostRecentUpdate = TimeService.newTime().getTime();

		}

	} // BaseCitationService.BasicCitationCollection

	/**
	 *
	 */
	public class BasicField implements Field
	{
		protected Object defaultValue;

		protected String description;

		protected String identifier;

		protected String label;

		protected int maxCardinality;

		protected int minCardinality;

		protected String namespace;

		protected int order;

		protected boolean required;

		protected String valueType;

		protected Map identifiers;

		protected boolean isEditable;

		// delimiter used to separate Field identifiers
		public final static String DELIMITER = ",";

		/**
		 * @param field
		 */
		public BasicField(Field other)
		{
			this.identifier = other.getIdentifier();
			this.valueType = other.getValueType();
			this.required = other.isRequired();
			this.minCardinality = other.getMinCardinality();
			this.maxCardinality = other.getMaxCardinality();
			this.namespace = other.getNamespaceAbbreviation();
			this.description = other.getDescription();
			this.identifiers = new Hashtable();
			this.isEditable = other.isEditable();

			if (other instanceof BasicField)
			{
				this.order = ((BasicField) other).getOrder();
				Iterator it = ((BasicField) other).identifiers.keySet().iterator();
				while (it.hasNext())
				{
					String format = (String) it.next();
					this.identifiers.put(format, ((BasicField) other).identifiers.get(format));
				}
			}
		}

		public BasicField(String identifier, String valueType, boolean isEditable,
		        boolean required, int minCardinality, int maxCardinality)
		{
			this.identifier = identifier;
			this.valueType = valueType;
			this.required = required;
			this.minCardinality = minCardinality;
			this.maxCardinality = maxCardinality;
			this.namespace = "";
			this.label = "";
			this.description = "";
			this.order = 0;
			this.identifiers = new Hashtable();
			this.isEditable = true;
		}

		public Object getDefaultValue()
		{
			return defaultValue;
		}

		public String getDescription()
		{
			return this.description;
		}

		public String getIdentifier()
		{
			return identifier;
		}

		public String getIdentifier(String format)
		{
			String tempString = null;
			String[] tokens = null;

			tempString = (String) this.identifiers.get(format);

			if (tempString == null)
				tempString = "";

			tempString = tempString.trim();

			// Is this a compound/delimited value?
			if (tempString.indexOf(DELIMITER) != -1)
			{
				// split the string based on the delimiter
				tokens = tempString.split(DELIMITER);
				// use the first delimiter as the identifier
				tempString = tokens[0];
			} // end getIdentifier

			return tempString;
		}

		public String[] getIdentifierComplex(String format)
		{
			String tempString = null;
			String[] tokens = null;

			tempString = (String) this.identifiers.get(format);

			if (tempString == null)
				tempString = "";

			tempString = tempString.trim();

			// Is this a compound/delimited value?
			if (tempString.indexOf(DELIMITER) != -1)
			{
				// split the string based on the delimiter
				tokens = tempString.split(DELIMITER);
			}
			else // it's a simple value
			{
				tokens = new String[1];
				tokens[0] = tempString;
			}

			return tokens;
		} // end getComplexIdentifers

		public String getLabel()
		{
			return this.label;
		}

		public int getMaxCardinality()
		{
			return maxCardinality;
		}

		public int getMinCardinality()
		{
			return minCardinality;
		}

		public String getNamespaceAbbreviation()
		{
			return this.namespace;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Schema.Field#getOrder()
		 */
		public int getOrder()
		{
			return order;
		}

		public String getValueType()
		{
			return valueType;
		}

		public boolean isEditable()
		{
			return isEditable;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Schema.Field#isMultivalued()
		 */
		public boolean isMultivalued()
		{
			return this.maxCardinality > 1;
		}

		public boolean isRequired()
		{
			return required;
		}

		public void setDefaultValue(Object value)
		{
			this.defaultValue = value;
		}

		/**
		 * @param label
		 */
		public void setDescription(String description)
		{
			this.description = description;
		}

		public void setEditable(boolean isEditable)
		{
			this.isEditable = isEditable;
		}

		public void setIdentifier(String format, String identifier)
		{
			this.identifiers.put(format, identifier);

		}

		/**
		 * @param label
		 */
		public void setLabel(String label)
		{
			this.label = label;
		}

		/**
		 * @param maxCardinality
		 *            The maxCardinality to set.
		 */
		public void setMaxCardinality(int maxCardinality)
		{
			this.maxCardinality = maxCardinality;
		}

		/**
		 * @param minCardinality
		 *            The minCardinality to set.
		 */
		public void setMinCardinality(int minCardinality)
		{
			this.minCardinality = minCardinality;
		}

		public void setNamespaceAbbreviation(String namespace)
		{
			this.namespace = namespace;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Schema.Field#setOrder(int)
		 */
		public void setOrder(int order)
		{
			this.order = order;

		}

		/**
		 * @param required
		 *            The required to set.
		 */
		public void setRequired(boolean required)
		{
			this.required = required;
		}

		/**
		 * @param valueType
		 *            The valueType to set.
		 */
		public void setValueType(String valueType)
		{
			this.valueType = valueType;
		}

		public String toString()
		{
			return "BasicField: " + this.identifier;
		}

	}

	/**
	 *
	 */
	protected class BasicSchema implements Schema
	{
		protected String defaultNamespace;

		protected List fields;

		protected String identifier;

		protected Map index;

		protected Map namespaces;

		protected Map identifiers;

		/**
		 *
		 */
		public BasicSchema()
		{
			this.fields = new Vector();
			this.index = new Hashtable();
			this.identifiers = new Hashtable();
		}

		/**
		 * @param schema
		 */
		public BasicSchema(Schema other)
		{
			this.identifier = other.getIdentifier();
			this.defaultNamespace = other.getNamespaceAbbrev();
			namespaces = new Hashtable();
			List nsAbbrevs = other.getNamespaceAbbreviations();
			if (nsAbbrevs != null)
			{
				Iterator nsIt = nsAbbrevs.iterator();
				while (nsIt.hasNext())
				{
					String nsAbbrev = (String) nsIt.next();
					String ns = other.getNamespaceUri(nsAbbrev);
					namespaces.put(nsAbbrev, ns);
				}
			}
			this.identifiers = new Hashtable();
			if (other instanceof BasicSchema)
			{
				Iterator it = ((BasicSchema) other).identifiers.keySet().iterator();
				while (it.hasNext())
				{
					String format = (String) it.next();
					this.identifiers.put(format, ((BasicSchema) other).identifiers.get(format));
				}
			}

			this.fields = new Vector();
			this.index = new Hashtable();
			List fields = other.getFields();
			Iterator fieldIt = fields.iterator();
			while (fieldIt.hasNext())
			{
				Field field = (Field) fieldIt.next();
				this.fields.add(new BasicField(field));
				index.put(field.getIdentifier(), field);
			}
		}

		/**
		 * @param schemaId
		 */
		public BasicSchema(String schemaId)
		{
			this.identifier = schemaId;
			this.fields = new Vector();
			this.index = new Hashtable();
			this.identifiers = new Hashtable();
		}

		public void addAlternativeIdentifier(String fieldId, String altFormat, String altIdentifier)
		{
			BasicField field = (BasicField) this.index.get(fieldId);
			if (field != null)
			{
				field.setIdentifier(altFormat, altIdentifier);
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Schema#addField(org.sakaiproject.citation.api.Schema.Field)
		 */
		public void addField(Field field)
		{
			this.index.put(field.getIdentifier(), field);
			this.fields.add(field);

		}

		/**
		 * @param order
		 * @param field
		 */
		public void addField(int order, Field field)
		{
			fields.add(order, field);
			index.put(identifier, field);
		}

		public BasicField addField(String identifier, String valueType, boolean isEditable,
		        boolean required, int minCardinality, int maxCardinality)
		{
			if (fields == null)
			{
				fields = new Vector();
			}
			if (index == null)
			{
				index = new Hashtable();
			}
			BasicField field = new BasicField(identifier, valueType, isEditable, required,
			        minCardinality, maxCardinality);
			fields.add(field);
			index.put(identifier, field);
			return field;
		}

		public BasicField addOptionalField(String identifier, String valueType, int minCardinality,
		        int maxCardinality)
		{
			return addField(identifier, valueType, true, false, minCardinality, maxCardinality);
		}

		public BasicField addRequiredField(String identifier, String valueType, int minCardinality,
		        int maxCardinality)
		{
			return addField(identifier, valueType, true, true, minCardinality, maxCardinality);
		}

		public Field getField(int index)
		{
			if (fields == null)
			{
				fields = new Vector();
			}
			return (Field) fields.get(index);
		}

		public Field getField(String name)
		{
			if (index == null)
			{
				index = new Hashtable();
			}
			return (Field) index.get(name);
		}

		public List getFields()
		{
			if (fields == null)
			{
				fields = new Vector();
			}
			return fields;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.citation.api.Schema#getIdentifier()
		 */
		public String getIdentifier()
		{
			return this.identifier;
		}

		public String getIdentifier(String format)
		{
			return (String) this.identifiers.get(format);
		}

		public String getNamespaceAbbrev()
		{
			return defaultNamespace;
		}

		public List getNamespaceAbbreviations()
		{
			if (namespaces == null)
			{
				namespaces = new Hashtable();
			}
			Collection keys = namespaces.keySet();
			List rv = new Vector();
			if (keys != null)
			{
				rv.addAll(keys);
			}
			return rv;
		}

		public String getNamespaceUri(String abbrev)
		{
			if (namespaces == null)
			{
				namespaces = new Hashtable();
			}
			return (String) namespaces.get(abbrev);
		}

		public List getRequiredFields()
		{
			if (fields == null)
			{
				fields = new Vector();
			}
			List required = new Vector();
			Iterator it = fields.iterator();
			while (it.hasNext())
			{
				Field field = (Field) it.next();
				if (field.isRequired())
				{
					required.add(field);
				}
			}

			return required;
		}

		/**
		 * @param identifier
		 */
		public void setIdentifier(String identifier)
		{
			this.identifier = identifier;
		}

		public void setIdentifier(String format, String identifier)
		{
			this.identifiers.put(format, identifier);

		}

		/**
		 *
		 */
		public void sortFields()
		{
			Collections.sort(fields, new Comparator()
			{

				public int compare(Object arg0, Object arg1)
				{
					if (arg0 instanceof BasicField && arg1 instanceof BasicField)
					{
						Integer int0 = new Integer(((BasicField) arg0).getOrder());
						Integer int1 = new Integer(((BasicField) arg1).getOrder());
						return int0.compareTo(int1);
					}
					else if (arg0 instanceof Field && arg1 instanceof Field)
					{
						String lbl0 = ((Field) arg0).getLabel();
						String lbl1 = ((Field) arg1).getLabel();
						return lbl0.compareTo(lbl1);
					}
					else
					{
						throw new ClassCastException(arg0.toString() + " " + arg1.toString());
					}
				}

			});

		}

		public String toString()
		{
			return "BasicSchema: " + this.identifier;
		}

	}

	/**
	 *
	 */
	protected interface Storage
	{
		/**
		 * @param mediatype
		 * @return
		 */
		public Citation addCitation(String mediatype);

		public CitationCollection addCollection(Map attributes, List citations);

		public Schema addSchema(Schema schema);

		public boolean checkCitation(String citationId);

		public boolean checkCollection(String collectionId);

		public boolean checkSchema(String schemaId);

		public long mostRecentUpdate(String collectionId);

		/**
		 * Close.
		 */
		public void close();

		public CitationCollection copyAll(String collectionId);

		public Citation getCitation(String citationId);

		public CitationCollection getCollection(String collectionId);

		public Schema getSchema(String schemaId);

		public List getSchemas();

		/**
		 * @return
		 */
		public List listSchemas();

		/**
		 * Open and be ready to read / write.
		 */
		public void open();

		public void putSchemas(Collection schemas);

		public void removeCitation(Citation edit);

		public void removeCollection(CitationCollection edit);

		public void removeSchema(Schema schema);

		public void saveCitation(Citation edit);

		public void saveCitationCollectionOrder(CitationCollectionOrder citationCollectionOrder);

		public void saveCollection(CitationCollection collection);

		public void saveSection(CitationCollectionOrder citationCollectionOrder);

		public void saveSubsection(CitationCollectionOrder citationCollectionOrder);

		public void saveCitationCollectionOrders(List<CitationCollectionOrder> citationCollectionOrders, String citationCollectionId);

		public void updateCitationCollectionOrder(CitationCollectionOrder citationCollectionOrder);

		public CitationCollectionOrder getNestedSections(String citationCollectionId);

		public CitationCollection getUnnestedCitationCollection(String citationCollectionId);

		public List<CitationCollectionOrder> getNestedCollectionAsList(String citationCollectionId);

		public String getNextCitationCollectionOrderId(String collectionId);

		public CitationCollectionOrder getCitationCollectionOrder(String collectionId, int locationId);

		public void removeLocation(String collectionId, int locationId);

		public void updateSchema(Schema schema);

		public void updateSchemas(Collection schemas);

	} // interface Storage

	/**
	 *
	 */
	public class UrlWrapper
	{
		protected String  m_label;
		protected String  m_url;
    protected boolean m_addPrefix;
		/**
		 * @param label Link label
		 * @param url URL
		 * @param addPrefix Add the configured prefix text?
		 */
		public UrlWrapper(String label, String url, boolean addPrefix)
		{
			m_label = label;
			m_url = url;
			m_addPrefix = addPrefix;
		}

		/**
		 * @param label Link label
		 * @param url URL
		 */
		public UrlWrapper(String label, String url)
		{
			m_label = label;
			m_url = url;
			m_addPrefix = false;
		}

		/**
		 * @return the label
		 */
		public String getLabel()
		{
			return m_label;
		}

		/**
		 * @return the url
		 */
		public String getUrl()
		{
			return m_url;
		}
		/**
		 * @return the "add prefix" setting
		 */
		public boolean addPrefix()
		{
			return m_addPrefix;
		}

		/**
		 * @param label
		 *            the label to set
		 */
		public void setLabel(String label)
		{
			m_label = label;
		}

		/**
		 * @param url
		 *            the url to set
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

		/**
		 * @param addPrefix
		 *            the "add prefix" setting
		 */
		public void setAddPrefix(boolean addPrefix)
		{
			m_addPrefix = addPrefix;
		}
	}

	public static ResourceLoader rb;

	protected static final String PROPERTY_DEFAULTVALUE = "sakai:defaultValue";

	protected static final String PROPERTY_DESCRIPTION = "sakai:description";

	protected static final String PROPERTY_HAS_ABBREVIATION = "sakai:hasAbbreviation";

	protected static final String PROPERTY_HAS_CITATION = "sakai:hasCitation";

	protected static final String PROPERTY_HAS_FIELD = "sakai:hasField";

	protected static final String PROPERTY_HAS_NAMESPACE = "sakai:hasNamespace";

	protected static final String PROPERTY_HAS_ORDER = "sakai:hasOrder";

	protected static final String PROPERTY_HAS_SCHEMA = "sakai:hasSchema";

	protected static final String PROPERTY_LABEL = "sakai:label";

	protected static final String PROPERTY_MAXCARDINALITY = "sakai:maxCardinality";

	protected static final String PROPERTY_MINCARDINALITY = "sakai:minCardinality";

	protected static final String PROPERTY_NAMESPACE = "sakai:namespace";

	protected static final String PROPERTY_REQUIRED = "sakai:required";

	protected static final String PROPERTY_VALUETYPE = "sakai:valueType";

	public static final String SCHEMA_PREFIX = "schema.";

	protected static AtomicInteger m_nextSerialNumber;
	/*
	 * RIS MAPPINGS below
	 */

	protected static final String RIS_DELIM = "  - ";

	/**
	 * Set up a mapping of our type to RIS 'TY - ' values
	 */
	protected static final Map m_RISType = new Hashtable();

	protected static final Map m_RISTypeInverse = new Hashtable();

	/**
	 * Which fields map onto the RIS Notes field? Include a prefix for the data,
	 * if necessary.
	 */
	protected static final Map m_RISNoteFields = new Hashtable();

	/**
	 * Which fields need special processing for RIS export?
	 */
	protected static final Set m_RISSpecialFields = new java.util.HashSet();

	static
	{
		m_RISType.put("unknown", "JOUR"); // Default to journal article
		m_RISType.put("article", "JOUR");
		m_RISType.put("book", "BOOK");
		m_RISType.put("chapter", "CHAP");
		m_RISType.put("report", "RPRT");
		m_RISType.put("proceed", "CONF");
		m_RISType.put("electronic", "ELEC");
		m_RISType.put("thesis", "THES");


		m_RISTypeInverse.put("BOOK", "book");
		m_RISTypeInverse.put("CHAP", "chapter");
		m_RISTypeInverse.put("JOUR", "article");
		m_RISTypeInverse.put("RPRT", "report");
		m_RISTypeInverse.put("CONF", "proceed");
		m_RISTypeInverse.put("ELEC", "electronic");
		m_RISTypeInverse.put("THES", "thesis");
	}

	static
	{
		m_RISNoteFields.put("language", "Language: ");
		m_RISNoteFields.put("doi", "DOI: ");
		m_RISNoteFields.put("rights", "Rights: ");
	}

	static
	{
		m_RISSpecialFields.add("date");
		m_RISSpecialFields.add("doi");
	}

	public static String escapeFieldName(String original)
	{
		if (original == null)
        {
	        return "";
        }
		original = original.trim();
		try
		{
			// convert the string to bytes in UTF-8
			byte[] bytes = original.getBytes("UTF-8");

			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < bytes.length; i++)
			{
				byte b = bytes[i];
				// escape ascii control characters, ascii high bits, specials
				if (Schema.ESCAPE_FIELD_NAME.indexOf((char) b) != -1)
				{
					buf.append(Schema.ESCAPE_CHAR); // special funky way to
					// encode bad URL characters
					// - ParameterParser will
					// decode it
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
			log.warn("BaseCitationService.escapeFieldName: ", e);
			return original;
		}

	}

	/** Dependency: CitationsConfigurationService. */
	protected ConfigurationService m_configService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ContentHostingService.
	 * This is used for permission checking and the entity methods.
	 */

	protected ContentHostingService m_contentHostingService = null;

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/** Depenedency: IdManager */
	protected IdManager m_idManager = null;

	/** Dependency: OpenURLServiceImpl */
	protected OpenURLServiceImpl m_openURLService;

	protected String m_defaultSchema;

	/** A Storage object for persistent storage. */
	protected Storage m_storage = null;

	protected String m_relativeAccessPoint;


	/**
	 * Dependency: the ResourceTypeRegistry
	 */
	protected ResourceTypeRegistry m_resourceTypeRegistry;

	/**
	 * Dependency: inject the ResourceTypeRegistry
	 * @param registry
	 */
	public void setResourceTypeRegistry(ResourceTypeRegistry registry)
	{
		m_resourceTypeRegistry = registry;
	}

	public void setOpenURLService(OpenURLServiceImpl openURLServiceImpl)
	{
		m_openURLService = openURLServiceImpl;
	}

	/**
	 * @return the ResourceTypeRegistry
	 */
	public ResourceTypeRegistry getResourceTypeRegistry()
	{
		return m_resourceTypeRegistry;
	}

	public static final String PROP_TEMPORARY_CITATION_LIST = "citations.temporary_citation_list";

	/**
	 * Checks permissions to add a CitationList.  Returns true if the user
	 * has permission to add a resource in the collection identified by the
	 * parameter.
	 * @param contentCollectionId
	 * @return
	 */
	public boolean allowAddCitationList(String contentCollectionId)
	{
		return m_contentHostingService.allowAddResource(contentCollectionId + "testing");
	}

	/**
	 * Checks permission to revise a CitationList, including permissions
	 * to add, remove or revise citations within the CitationList. Returns
	 * true if the user has permission to revise the resource identified by
	 * the parameter.  Also returns true if all of these conditions are met:
	 * (1) the user is the creator of the specified resource, (2) the specified
	 * resource is a temporary CitationList (as identified by the value of
	 * the PROP_TEMPORARY_CITATION_LIST property), and (3) the user has
	 * permission to add resources in the collection containing the
	 * resource.
	 * @param contentResourceId
	 * @return
	 */
	public boolean allowReviseCitationList(String contentResourceId)
	{
		boolean allowed = m_contentHostingService.allowUpdateResource(contentResourceId);
		if(!allowed)
		{
			try
			{
				ResourceProperties props = m_contentHostingService.getProperties(contentResourceId);
				String temp_res = props.getProperty(CitationService.PROP_TEMPORARY_CITATION_LIST);
				String creator = props.getProperty(ResourceProperties.PROP_CREATOR);
				String contentCollectionId = m_contentHostingService.getContainingCollectionId(contentResourceId);
	     		SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
				String currentUser = sessionManager.getCurrentSessionUserId();

				allowed = this.allowAddCitationList(contentCollectionId) && (temp_res != null) && currentUser.equals(creator);
			}
			catch(PermissionException e)
			{
				// do nothing: return false
			}
            catch (IdUnusedException e)
            {
				// do nothing: return false
            }
		}
		return allowed;
	}

	/**
	 *
	 * @return
	 */
	public boolean allowRemoveCitationList(String contentResourceId)
	{
		boolean allowed = m_contentHostingService.allowUpdateResource(contentResourceId);
		if(!allowed)
		{
			try
			{
				ResourceProperties props = m_contentHostingService.getProperties(contentResourceId);
				String temp_res = props.getProperty(CitationService.PROP_TEMPORARY_CITATION_LIST);
				String creator = props.getProperty(ResourceProperties.PROP_CREATOR);
				String contentCollectionId = m_contentHostingService.getContainingCollectionId(contentResourceId);
	     		SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
				String currentUser = sessionManager.getCurrentSessionUserId();

				allowed = this.allowAddCitationList(contentCollectionId) && (temp_res != null) && currentUser.equals(creator);
			}
			catch(PermissionException e)
			{
				// do nothing: return false
			}
            catch (IdUnusedException e)
            {
				// do nothing: return false
            }
		}
		return allowed;
	}



	public Citation addCitation(String mediatype)
	{
		// work around to map google scholar's schemas with our's
		if (mediatype != null && mediatype.equalsIgnoreCase("proceeding"))
		{
			mediatype = "proceed";
		}

		Citation edit = m_storage.addCitation(mediatype);

		return edit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#addCollection()
	 */
	public CitationCollection addCollection()
	{
		CitationCollection edit = m_storage.addCollection(null, null);
		return edit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String,
	 *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
	 *      java.util.List)
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath,
	        List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#copyAll(java.lang.String)
	 */
	public CitationCollection copyAll(String collectionId)
	{
		return m_storage.copyAll(collectionId);
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		if(m_storage != null)
		{
			m_storage.close();
			m_storage = null;
		}
	}

	/**
	 * Access the partial URL that forms the root of calendar URLs.
	 *
	 * @param relative
	 *            if true, form within the access path only (i.e. starting with
	 *            /content)
	 * @return the partial URL that forms the root of calendar URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : m_serverConfigurationService.getAccessUrl())
		        + m_relativeAccessPoint;

	} // getAccessPoint

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getCollection(java.lang.String)
	 */
	public CitationCollection getCollection(String collectionId) throws IdUnusedException
	{
		CitationCollection edit = m_storage.getCollection(collectionId);
		if (edit == null)
		{
			throw new IdUnusedException(collectionId);
		}
		return edit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getDefaultSchema()
	 */
	public Schema getDefaultSchema()
	{
		Schema rv = null;
		if (m_defaultSchema != null)
		{
			rv = m_storage.getSchema(m_defaultSchema);
		}
		return rv;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
	 */
	public Entity getEntity(Reference ref)
	{
		Entity entity = null;
		if (APPLICATION_ID.equals(ref.getType()))
		{
			if ( REF_TYPE_EXPORT_RIS_SEL.equals(ref.getSubType()) ||
					REF_TYPE_EXPORT_RIS_ALL.equals(ref.getSubType()) )
			{
				// these entities are citation collections
				String id = ref.getId();
				if (id == null || id.trim().equals(""))
				{
					String reference = ref.getReference();
					if (reference != null && reference.startsWith(REFERENCE_ROOT))
					{
						id = reference.substring(REFERENCE_ROOT.length(), reference.length());
					}
				}

				if (id != null && !id.trim().equals(""))
				{
					entity = m_storage.getCollection(id);
				}
			}
			else if (REF_TYPE_VIEW_LIST.equals(ref.getSubType()))
			{
				// these entities are actually in /content
				String id = ref.getId();
				if (id == null || id.trim().equals(""))
				{
					String reference = ref.getReference();
					if (reference.startsWith(REFERENCE_ROOT))
					{
						reference = reference
						        .substring(REFERENCE_ROOT.length(), reference.length());
					}
					if (reference.startsWith(m_contentHostingService.REFERENCE_ROOT))
					{
						id = reference.substring(m_contentHostingService.REFERENCE_ROOT.length(),
						        reference.length());
					}
				}

				if (id != null && !id.trim().equals(""))
				{
					try
					{
						entity = m_contentHostingService.getResource(id);
					}
					catch (PermissionException e)
					{
						log.warn("getEntity(" + id + ") ", e);
					}
					catch (IdUnusedException e)
					{
						log.warn("getEntity(" + id + ") ", e);
					}
					catch (TypeException e)
					{
						log.warn("getEntity(" + id + ") ", e);
					}
				}
			}
		}

		// and maybe others are in /citation

		return entity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference,
	 *      java.lang.String)
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		Collection azGroups = null;
		
		// entities that are actually in /content use the /content authz groups 
		if(ref != null && ref.getReference() != null && ref.getReference().startsWith("/citation/content/")) {
			String altRef = ref.getReference().substring("/citation".length());
			azGroups = m_contentHostingService.getEntityAuthzGroups(m_entityManager.newReference(altRef), userId);
		}

		return azGroups;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// if it's a /content item, return its props

		// otherwise return null

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityUrl(Reference ref)
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
	 */
	public HttpAccess getHttpAccess()
	{
		// if it's a /content item, the access is via CitationListAccessServlet
		return new CitationListAccessServlet();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
	 */
	public String getLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public Set getMultivalued()
	{
		Set multivalued = new TreeSet();
		Iterator schemaIt = m_storage.getSchemas().iterator();
		while (schemaIt.hasNext())
		{
			Schema schema = (Schema) schemaIt.next();
			{
				Iterator fieldIt = schema.getFields().iterator();
				while (fieldIt.hasNext())
				{
					Field field = (Field) fieldIt.next();
					if (field.getMaxCardinality() > 1)
					{
						multivalued.add(field.getIdentifier());
					}
				}
			}
		}

		return multivalued;
	}

	/**
	 * @return
	 */
	public Set getMultivalued(String type)
	{
		Set multivalued = new TreeSet();
		Schema schema = m_storage.getSchema(type);
		{
			Iterator fieldIt = schema.getFields().iterator();
			while (fieldIt.hasNext())
			{
				Field field = (Field) fieldIt.next();
				if (field.getMaxCardinality() > 1)
				{
					multivalued.add(field.getIdentifier());
				}
			}
		}

		return multivalued;
	}

	public Schema getSchema(String name)
	{
		Schema schema = m_storage.getSchema(name);
		return schema;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getSchemas()
	 */
	public List getSchemas()
	{
		List schemas = new Vector(m_storage.getSchemas());
		return schemas;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.Schema#getSynonyms(java.lang.String)
	 */
	protected Set getSynonyms(String mediatype)
	{
		Set synonyms = new TreeSet();
		if (mediatype.equalsIgnoreCase("article"))
		{
			synonyms.add("article");
			synonyms.add("journal article");
			synonyms.add("journal");
			synonyms.add("periodical");
			synonyms.add("newspaper article");
			synonyms.add("magazine article");
			synonyms.add("editorial");
			synonyms.add("peer reviewed article");
			synonyms.add("peer reviewed journal article");
			synonyms.add("book review");
			synonyms.add("review");
			synonyms.add("meeting");
			synonyms.add("wire feed");
			synonyms.add("wire story");
			synonyms.add("news");
			synonyms.add("journal article (cije)");
		}
		else if (mediatype.equalsIgnoreCase("book"))
		{
			synonyms.add("book");
			synonyms.add("bk");
		}
		else if (mediatype.equalsIgnoreCase("chapter"))
		{
			synonyms.add("chapter");
			synonyms.add("book chapter");
			synonyms.add("book section");
		}
		else if (mediatype.equalsIgnoreCase("report"))
		{
			synonyms.add("report");
			synonyms.add("editorial material");
			synonyms.add("technical report");
			synonyms.add("se");
			synonyms.add("document (rie)");
		}

		return synonyms;
	}

	public Citation getTemporaryCitation()
	{
		return new BasicCitation();
	}

	public Citation getTemporaryCitation(Asset asset)
	{
		return new BasicCitation(asset);
	}

	public CitationCollection getTemporaryCollection()
	{
		return new BasicCitationCollection(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getValidPropertyNames()
	 */
	public Set getValidPropertyNames()
	{
		Set names = new TreeSet();
		Iterator schemaIt = m_storage.getSchemas().iterator();
		while (schemaIt.hasNext())
		{
			Schema schema = (Schema) schemaIt.next();
			{
				Iterator fieldIt = schema.getFields().iterator();
				while (fieldIt.hasNext())
				{
					Field field = (Field) fieldIt.next();
					names.add(field.getIdentifier());
				}
			}
		}
		return names;

	} // getValidPropertyNames

	public class CitationListCreateAction extends BaseInteractionAction
	{

		/**
         * @param id
         * @param actionType
         * @param typeId
         * @param helperId
         * @param requiredPropertyKeys
         */
        public CitationListCreateAction(String id, ActionType actionType, String typeId, String helperId, List requiredPropertyKeys)
        {
	        super(id, actionType, typeId, helperId, requiredPropertyKeys);
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.content.util.BaseResourceAction#available(org.sakaiproject.content.api.ContentEntity)
         */
        @Override
        public boolean available(ContentEntity entity)
        {
	        return super.available(entity);
        }


	}

	/**
	 *
	 *
	 */
	public void init()
	{
		m_storage = newStorage();
		m_nextSerialNumber = new AtomicInteger(0);

		m_relativeAccessPoint = CitationService.REFERENCE_ROOT;

		rb = new ResourceLoader("citations");

		//initializeSchemas();
	    m_defaultSchema = "article";

		// register as an entity producer
		m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);

		if(m_configService.isCitationsEnabledByDefault() ||
				m_configService.isAllowSiteBySiteOverride() )
		{
			registerResourceType();
		}

	}

	/**
     *
     */
    protected void registerResourceType()
    {
	    ResourceTypeRegistry registry = getResourceTypeRegistry();

	    List requiredPropertyKeys = new Vector();
	    requiredPropertyKeys.add(ContentHostingService.PROP_ALTERNATE_REFERENCE);
	    requiredPropertyKeys.add(ResourceProperties.PROP_CONTENT_TYPE);

	    BaseInteractionAction createAction = new CitationListCreateAction(ResourceToolAction.CREATE,
	    		ResourceToolAction.ActionType.CREATE_BY_HELPER,
	    		CitationService.CITATION_LIST_ID,
	    		CitationService.HELPER_ID,
	    		new Vector());

	    createAction.setLocalizer(
	    		new BaseResourceAction.Localizer()
	    		{
	    			public String getLabel()
	    			{
	    				return rb.getString("action.create");
	    			}
	    		});

	    BaseInteractionAction reviseAction = new BaseInteractionAction(ResourceToolAction.REVISE_CONTENT,
	    		ResourceToolAction.ActionType.REVISE_CONTENT,
	    		CitationService.CITATION_LIST_ID,
	    		CitationService.HELPER_ID,
	    		new Vector());

	    reviseAction.setLocalizer(
	    		new BaseResourceAction.Localizer()
	    		{
	    			public String getLabel()
	    			{
	    				return rb.getString("action.revise");
	    			}
	    		});

	    BaseServiceLevelAction moveAction = new BaseServiceLevelAction(ResourceToolAction.MOVE,
	    		ResourceToolAction.ActionType.MOVE,
	    		CitationService.CITATION_LIST_ID,
	    		true );

	    BaseServiceLevelAction revisePropsAction = new BaseServiceLevelAction(ResourceToolAction.REVISE_METADATA,
	    		ResourceToolAction.ActionType.REVISE_METADATA,
	    		CitationService.CITATION_LIST_ID,
	    		false );

	    BaseServiceLevelAction makeSitePageAction = new BaseServiceLevelAction(ResourceToolAction.MAKE_SITE_PAGE,
	    		ResourceToolAction.ActionType.MAKE_SITE_PAGE,
	    		CitationService.CITATION_LIST_ID,
	    		true );

	    BasicSiteSelectableResourceType typedef = new BasicSiteSelectableResourceType(CitationService.CITATION_LIST_ID);
	    typedef.setSizeLabeler(new CitationSizeLabeler());
	    typedef.setLocalizer(new CitationLocalizer());
	    typedef.addAction(createAction);
	    typedef.addAction(reviseAction);
	    typedef.addAction(new CitationListDeleteAction());
	    typedef.addAction(new CitationListCopyAction());
	    typedef.addAction(new CitationListDuplicateAction());
	    typedef.addAction(revisePropsAction);
	    typedef.addAction(moveAction);
	    typedef.setEnabledByDefault(m_configService.isCitationsEnabledByDefault());
	    typedef.setIconLocation("sakai/citationlist.gif");
	    typedef.setHasRightsDialog(false);

	    registry.register(typedef, new CitationContentChangeHandler());
    }

	/**
     *
     */
    protected void initializeSchemas()
    {
	    BasicSchema unknown = new BasicSchema();
	    unknown.setIdentifier(CitationService.UNKNOWN_TYPE);

	    BasicSchema article = new BasicSchema();
	    article.setIdentifier("article");

	    BasicSchema book = new BasicSchema();
	    book.setIdentifier("book");

	    BasicSchema chapter = new BasicSchema();
	    chapter.setIdentifier("chapter");

	    BasicSchema report = new BasicSchema();
	    report.setIdentifier("report");

	    /* schema ordering is different for different types */

	    /*
	     * UNKNOWN (GENERIC)
	     */
	    unknown.addField(Schema.CREATOR, Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    unknown.addAlternativeIdentifier(Schema.CREATOR, RIS_FORMAT, "A1");

	    unknown.addField(Schema.TITLE, Schema.SHORTTEXT, true, true, 1, 1);
	    unknown.addAlternativeIdentifier(Schema.TITLE, RIS_FORMAT, "T1");

	    unknown.addField(Schema.YEAR, Schema.NUMBER, true, false, 0, 1);
	    // SAK-16740 -- rislabel for "year" is "Y1" (or "PY")
	    unknown.addAlternativeIdentifier(Schema.YEAR, RIS_FORMAT, "Y1");

	    unknown.addField("date", Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("date", RIS_FORMAT, "Y1");

	    unknown.addField(Schema.PUBLISHER, Schema.SHORTTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.PUBLISHER, RIS_FORMAT, "PB");

	    unknown.addField("publicationLocation", Schema.SHORTTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("publicationLocation", RIS_FORMAT, "CY");

	    unknown.addField(Schema.VOLUME, Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.VOLUME, RIS_FORMAT, "VL");

	    unknown.addField(Schema.ISSUE, Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.ISSUE, RIS_FORMAT, "IS");

	    unknown.addField(Schema.PAGES, Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.PAGES, RIS_FORMAT, "SP");

	    unknown.addField("startPage", Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("startPage", RIS_FORMAT, "SP");

	    unknown.addField("endPage", Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("endPage", RIS_FORMAT, "EP");

	    unknown.addField("edition", Schema.NUMBER, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("edition", RIS_FORMAT, "VL");

	    unknown.addField("editor", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    unknown.addAlternativeIdentifier("editor", RIS_FORMAT, "A3");

	    unknown.addField(Schema.SOURCE_TITLE, Schema.SHORTTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.SOURCE_TITLE, RIS_FORMAT, "T3");

	    unknown.addField("Language", Schema.NUMBER, true, false, 0, 1);

	    unknown.addField("abstract", Schema.LONGTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("abstract", RIS_FORMAT, "N2");

	    unknown.addField("note", Schema.LONGTEXT, true, false, 0, Schema.UNLIMITED);
	    unknown.addAlternativeIdentifier("note", RIS_FORMAT, "N1");

	    unknown.addField(Schema.ISN, Schema.SHORTTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier(Schema.ISN, RIS_FORMAT, "SN");

	    unknown.addField("subject", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    unknown.addAlternativeIdentifier("subject", RIS_FORMAT, "KW");

	    unknown.addField("locIdentifier", Schema.SHORTTEXT, true, false, 0, 1);
	    unknown.addAlternativeIdentifier("locIdentifier", RIS_FORMAT, "M1");

	    unknown.addField("dateRetrieved", Schema.DATE, false, false, 0, 1);

	    unknown.addField("openURL", Schema.SHORTTEXT, false, false, 0, 1);

	    unknown.addField("doi", Schema.NUMBER, true, false, 0, 1);

	    unknown.addField("rights", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);

	    /*
	     * ARTICLE
	     */
	    article.addField(Schema.CREATOR, Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    article.addAlternativeIdentifier(Schema.CREATOR, RIS_FORMAT, "A1");

	    article.addField(Schema.TITLE, Schema.SHORTTEXT, true, true, 1, 1);
	    article.addAlternativeIdentifier(Schema.TITLE, RIS_FORMAT, "T1");

	    article.addField(Schema.SOURCE_TITLE, Schema.SHORTTEXT, true, false, 0, 1);
	    article.addAlternativeIdentifier(Schema.SOURCE_TITLE, RIS_FORMAT, "JF");

	    article.addField(Schema.YEAR, Schema.NUMBER, true, false, 0, 1);
	    // SAK-16740 -- rislabel for "year" is "Y1" (or "PY")
	    article.addAlternativeIdentifier(Schema.YEAR, RIS_FORMAT, "Y1");

	    article.addField("date", Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier("date", RIS_FORMAT, "Y1");

	    article.addField(Schema.VOLUME, Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier(Schema.VOLUME, RIS_FORMAT, "VL");

	    article.addField(Schema.ISSUE, Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier(Schema.ISSUE, RIS_FORMAT, "IS");

	    article.addField(Schema.PAGES, Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier(Schema.PAGES, RIS_FORMAT, "SP");

	    article.addField("startPage", Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier("startPage", RIS_FORMAT, "SP");

	    article.addField("endPage", Schema.NUMBER, true, false, 0, 1);
	    article.addAlternativeIdentifier("endPage", RIS_FORMAT, "EP");

	    article.addField("abstract", Schema.LONGTEXT, true, false, 0, 1);
	    article.addAlternativeIdentifier("abstract", RIS_FORMAT, "N2");

	    article.addField("note", Schema.LONGTEXT, true, false, 0, Schema.UNLIMITED);
	    article.addAlternativeIdentifier("note", RIS_FORMAT, "N1");

	    article.addField(Schema.ISN, Schema.SHORTTEXT, true, false, 0, 1);
	    article.addAlternativeIdentifier(Schema.ISN, RIS_FORMAT, "SN");

	    article.addField("subject", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    article.addAlternativeIdentifier("subject", RIS_FORMAT, "KW");

	    article.addField("Language", Schema.NUMBER, true, false, 0, 1);

	    article.addField("locIdentifier", Schema.SHORTTEXT, true, false, 0, 1);
	    article.addAlternativeIdentifier("locIdentifier", RIS_FORMAT, "M1");

	    article.addField("dateRetrieved", Schema.DATE, false, false, 0, 1);

	    article.addField("openURL", Schema.SHORTTEXT, false, false, 0, 1);

	    article.addField("doi", Schema.NUMBER, true, false, 0, 1);

	    article.addField("rights", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);


	    /*
	     * BOOK
	     */
	    book.addField(Schema.CREATOR, Schema.SHORTTEXT, true, true, 1, Schema.UNLIMITED);
	    book.addAlternativeIdentifier(Schema.CREATOR, RIS_FORMAT, "A1");

	    book.addField(Schema.TITLE, Schema.SHORTTEXT, true, true, 1, 1);
	    book.addAlternativeIdentifier(Schema.TITLE, RIS_FORMAT, "BT");

	    book.addField(Schema.YEAR, Schema.NUMBER, true, false, 0, 1);
	    // SAK-16740 -- rislabel for "year" is "Y1" (or "PY")
	    book.addAlternativeIdentifier(Schema.YEAR, RIS_FORMAT, "Y1");

	    book.addField("date", Schema.NUMBER, true, false, 0, 1);
	    book.addAlternativeIdentifier("date", RIS_FORMAT, "Y1");

	    book.addField(Schema.PUBLISHER, Schema.SHORTTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier(Schema.PUBLISHER, RIS_FORMAT, "PB");

	    book.addField("publicationLocation", Schema.SHORTTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier("publicationLocation", RIS_FORMAT, "CY");

	    book.addField("edition", Schema.NUMBER, true, false, 0, 1);
	    book.addAlternativeIdentifier("edition", RIS_FORMAT, "VL");

	    book.addField("editor", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    book.addAlternativeIdentifier("editor", RIS_FORMAT, "A3");

	    book.addField(Schema.SOURCE_TITLE, Schema.SHORTTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier(Schema.SOURCE_TITLE, RIS_FORMAT, "T3");

	    book.addField("abstract", Schema.LONGTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier("abstract", RIS_FORMAT, "N2");

	    book.addField("note", Schema.LONGTEXT, true, false, 0, Schema.UNLIMITED);
	    book.addAlternativeIdentifier("note", RIS_FORMAT, "N1");

	    book.addField(Schema.ISN, Schema.SHORTTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier(Schema.ISN, RIS_FORMAT, "SN");

	    book.addField("subject", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    book.addAlternativeIdentifier("subject", RIS_FORMAT, "KW");

	    book.addField("Language", Schema.NUMBER, true, false, 0, 1);

	    book.addField("locIdentifier", Schema.SHORTTEXT, true, false, 0, 1);
	    book.addAlternativeIdentifier("locIdentifier", RIS_FORMAT, "M1");

	    book.addField("dateRetrieved", Schema.DATE, false, false, 0, 1);

	    book.addField("openURL", Schema.SHORTTEXT, false, false, 0, 1);

	    book.addField("doi", Schema.NUMBER, true, false, 0, 1);

	    book.addField("rights", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);

	    book.addAlternativeIdentifier(Schema.PAGES, RIS_FORMAT, "SP");

	    /*
	     * CHAPTER
	     */
	    chapter.addField(Schema.CREATOR, Schema.SHORTTEXT, true, true, 1, Schema.UNLIMITED);
	    chapter.addAlternativeIdentifier(Schema.CREATOR, RIS_FORMAT, "A1");

	    chapter.addField(Schema.TITLE, Schema.SHORTTEXT, true, true, 1, 1);
	    chapter.addAlternativeIdentifier(Schema.TITLE, RIS_FORMAT, "CT");

	    chapter.addField(Schema.YEAR, Schema.NUMBER, true, false, 0, 1);
	    // SAK-16740 -- rislabel for "year" is "Y1" (or "PY")
	    chapter.addAlternativeIdentifier(Schema.YEAR, RIS_FORMAT, "Y1");

	    chapter.addField("date", Schema.NUMBER, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("date", RIS_FORMAT, "Y1");

	    chapter.addField(Schema.PUBLISHER, Schema.SHORTTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier(Schema.PUBLISHER, RIS_FORMAT, "PB");

	    chapter.addField("publicationLocation", Schema.SHORTTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("publicationLocation", RIS_FORMAT, "CY");

	    chapter.addField("edition", Schema.NUMBER, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("edition", RIS_FORMAT, "VL");

	    chapter.addField("editor", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    chapter.addAlternativeIdentifier("editor", RIS_FORMAT, "ED");

	    chapter.addField(Schema.SOURCE_TITLE, Schema.SHORTTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier(Schema.SOURCE_TITLE, RIS_FORMAT, "BT");

	    chapter.addField(Schema.PAGES, Schema.NUMBER, true, false, 0, 1);
	    chapter.addAlternativeIdentifier(Schema.PAGES, RIS_FORMAT, "SP");

	    chapter.addField("startPage", Schema.NUMBER, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("startPage", RIS_FORMAT, "SP");

	    chapter.addField("endPage", Schema.NUMBER, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("endPage", RIS_FORMAT, "EP");

	    chapter.addField("abstract", Schema.LONGTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("abstract", RIS_FORMAT, "N2");

	    chapter.addField("note", Schema.LONGTEXT, true, false, 0, Schema.UNLIMITED);
	    chapter.addAlternativeIdentifier("note", RIS_FORMAT, "N1");

	    chapter.addField(Schema.ISN, Schema.SHORTTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier(Schema.ISN, RIS_FORMAT, "SN");

	    chapter.addField("subject", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    chapter.addAlternativeIdentifier("subject", RIS_FORMAT, "KW");

	    chapter.addField("Language", Schema.NUMBER, true, false, 0, 1);

	    chapter.addField("locIdentifier", Schema.SHORTTEXT, true, false, 0, 1);
	    chapter.addAlternativeIdentifier("locIdentifier", RIS_FORMAT, "M1");

	    chapter.addField("dateRetrieved", Schema.DATE, false, false, 0, 1);

	    chapter.addField("openURL", Schema.SHORTTEXT, false, false, 0, 1);

	    chapter.addField("doi", Schema.NUMBER, true, false, 0, 1);

	    chapter.addField("rights", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);


	    /*
	     * REPORT
	     */
	    report.addField(Schema.CREATOR, Schema.SHORTTEXT, true, true, 1, Schema.UNLIMITED);
	    report.addAlternativeIdentifier(Schema.CREATOR, RIS_FORMAT, "A1");

	    report.addField(Schema.TITLE, Schema.SHORTTEXT, true, true, 1, 1);
	    report.addAlternativeIdentifier(Schema.TITLE, RIS_FORMAT, "T1");

	    report.addField(Schema.YEAR, Schema.NUMBER, true, false, 0, 1);
	    // SAK-16740 -- rislabel for "year" is "Y1" (or "PY")
	    report.addAlternativeIdentifier(Schema.YEAR, RIS_FORMAT, "Y1");

	    report.addField("date", Schema.NUMBER, true, false, 0, 1);
	    report.addAlternativeIdentifier("date", RIS_FORMAT, "Y1");

	    report.addField(Schema.PUBLISHER, Schema.SHORTTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier(Schema.PUBLISHER, RIS_FORMAT, "PB");

	    report.addField("publicationLocation", Schema.SHORTTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier("publicationLocation", RIS_FORMAT, "CY");

	    report.addField("editor", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    report.addAlternativeIdentifier("editor", RIS_FORMAT, "A3");

	    report.addField("edition", Schema.NUMBER, true, false, 0, 1);
	    report.addAlternativeIdentifier("edition", RIS_FORMAT, "VL");

	    report.addField(Schema.SOURCE_TITLE, Schema.SHORTTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier(Schema.SOURCE_TITLE, RIS_FORMAT, "T3");

	    report.addField(Schema.PAGES, Schema.NUMBER, true, false, 0, 1);
	    report.addAlternativeIdentifier(Schema.PAGES, RIS_FORMAT, "SP");

	    report.addField("abstract", Schema.LONGTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier("abstract", RIS_FORMAT, "N2");

	    report.addField(Schema.ISN, Schema.SHORTTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier(Schema.ISN, RIS_FORMAT, "SN");

	    report.addField("note", Schema.LONGTEXT, true, false, 0, Schema.UNLIMITED);
	    report.addAlternativeIdentifier("note", RIS_FORMAT, "N1");

	    report.addField("subject", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);
	    report.addAlternativeIdentifier("subject", RIS_FORMAT, "KW");

	    report.addField("Language", Schema.NUMBER, true, false, 0, 1);

	    report.addField("locIdentifier", Schema.SHORTTEXT, true, false, 0, 1);
	    report.addAlternativeIdentifier("locIdentifier", RIS_FORMAT, "M1");

	    report.addField("dateRetrieved", Schema.DATE, false, false, 0, 1);

	    report.addField("openURL", Schema.SHORTTEXT, false, false, 0, 1);

	    report.addField("doi", Schema.NUMBER, true, false, 0, 1);

	    report.addField("rights", Schema.SHORTTEXT, true, false, 0, Schema.UNLIMITED);


	    /* IGNORING 'Citation' field for now...
	    unknown.addField("inlineCitation", Schema.SHORTTEXT, false, false, 0, Schema.UNLIMITED);
	    article.addField("inlineCitation", Schema.SHORTTEXT, false, false, 0, Schema.UNLIMITED);
	    book.addField("inlineCitation", Schema.SHORTTEXT, false, false, 0, Schema.UNLIMITED);
	    chapter.addField("inlineCitation", Schema.SHORTTEXT, false, false, 0, Schema.UNLIMITED);
	    report.addField("inlineCitation", Schema.SHORTTEXT, false, false, 0, Schema.UNLIMITED);
	    */

	    if (m_storage.checkSchema(unknown.getIdentifier()))
	    {
	    	m_storage.updateSchema(unknown);
	    }
	    else
	    {
	    	m_storage.addSchema(unknown);
	    }

	    if (m_storage.checkSchema(article.getIdentifier()))
	    {
	    	m_storage.updateSchema(article);
	    }
	    else
	    {
	    	m_storage.addSchema(article);
	    }

	    if (m_storage.checkSchema(book.getIdentifier()))
	    {
	    	m_storage.updateSchema(book);
	    }
	    else
	    {
	    	m_storage.addSchema(book);
	    }

	    if (m_storage.checkSchema(chapter.getIdentifier()))
	    {
	    	m_storage.updateSchema(chapter);
	    }
	    else
	    {
	    	m_storage.addSchema(chapter);
	    }

	    if (m_storage.checkSchema(report.getIdentifier()))
	    {
	    	m_storage.updateSchema(report);
	    }
	    else
	    {
	    	m_storage.addSchema(report);
	    }

    }

	public class CitationListDeleteAction extends BaseServiceLevelAction
	{
		public CitationListDeleteAction()
		{
			super(ResourceToolAction.DELETE, ResourceToolAction.ActionType.DELETE, CitationService.CITATION_LIST_ID, true);
		}

		public void finalizeAction(Reference reference)
		{
			try
			{
				ContentResource resource = (ContentResource) reference.getEntity();
				String collectionId = new String(resource.getContent());
				CitationCollection collection = getCollection(collectionId);
				removeCollection(collection);
			}
			catch(IdUnusedException e)
			{
				log.warn("IdUnusedException ", e);
			}
			catch(ServerOverloadException e)
			{
				log.warn("ServerOverloadException ", e);
			}
		}
	}

	public class CitationLocalizer implements BasicResourceType.Localizer
	{
		/**
		 *
		 * @return
		 */
		public String getLabel()
		{
			return rb.getString("list.title");
		}

		/**
		 *
		 * @param member
		 * @return
		 */
		public String getLocalizedHoverText(ContentEntity member)
		{

			return rb.getString("list.title");
		}

	}

	public class CitationSizeLabeler implements BasicResourceType.SizeLabeler
	{

		public String getLongSizeLabel(ContentEntity entity)
		{
			return getSizeLabel(entity);
		}

		public String getSizeLabel(ContentEntity entity)
		{
			String label = null;
			if(entity instanceof ContentResource)
			{
				byte[] collectionId = null;
				ContentResource resource = (ContentResource) entity;
				try
				{
					collectionId = resource.getContent();
					if(collectionId != null)
					{
						CitationCollection collection = getCollection(new String(collectionId));
						String[] args = new String[]{ Integer.toString(collection.size())};
						label = rb.getFormattedMessage("citation.count", args);
					}
				}
				catch(Exception e)
				{
					String citationCollectionId = "null";
					if(collectionId != null)
					{
						citationCollectionId = collectionId.toString();
					}
					log.warn("Unable to determine size of CitationCollection for entity: entityId == " + entity.getId() + " citationCollectionId == " + collectionId + " exception == " + e.toString());
				}
			}
			return label;
		}

	}

	public class CitationListCopyAction extends BaseServiceLevelAction
	{

		public CitationListCopyAction()
		{
			super(ResourceToolAction.COPY, ResourceToolAction.ActionType.COPY, CitationService.CITATION_LIST_ID, true);
		}

		@Override
		public void finalizeAction(Reference reference)
		{
			copyCitationCollection(reference);
		}

	}

	public class CitationListDuplicateAction extends BaseServiceLevelAction
	{

		public CitationListDuplicateAction()
		{
			super(ResourceToolAction.DUPLICATE, ResourceToolAction.ActionType.DUPLICATE, CitationService.CITATION_LIST_ID, false);
		}

		@Override
		public void finalizeAction(Reference reference)
		{
			copyCitationCollection(reference);
		}

	}

	/**
	 * @param schemaId
	 * @param fieldId
	 * @return
	 */
	public boolean isMultivalued(String schemaId, String fieldId)
	{
		Schema schema = getSchema(schemaId.toLowerCase());
		if (schema == null)
		{
			if (getSynonyms("article").contains(schemaId.toLowerCase()))
			{
				schema = getSchema("article");
			}
			else if (getSynonyms("book").contains(schemaId.toLowerCase()))
			{
				schema = getSchema("book");
			}
			else if (getSynonyms("chapter").contains(schemaId.toLowerCase()))
			{
				schema = getSchema("chapter");
			}
			else if (getSynonyms("report").contains(schemaId.toLowerCase()))
			{
				schema = getSchema("report");
			}
			else
			{
				schema = this.getSchema("unknown");
			}
		}
		Field field = schema.getField(fieldId);
		if (field == null)
		{
			return false;
		}
		return (field.isMultivalued());
	}

	/**
	 * Access a list of all schemas that have been defined (other than the
	 * "unknown" type).
	 *
	 * @return A list of Strings representing the identifiers for known schemas.
	 */
	public List listSchemas()
	{
		Set names = (Set) m_storage.listSchemas();
		return new Vector(names);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String,
	 *      org.w3c.dom.Element, java.lang.String, java.lang.String,
	 *      java.util.Map, java.util.Map, java.util.Set)
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId,
	        Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Construct a Storage object.
	 *
	 * @return The new storage object.
	 */
	public abstract Storage newStorage();

	/**
	 * @return
	 */
	protected Integer nextSerialNumber()
	{
		return m_nextSerialNumber.getAndIncrement();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String,
	 *      org.sakaiproject.entity.api.Reference)
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		boolean citationEntity = false;
		if (reference.startsWith(CitationService.REFERENCE_ROOT))
		{
			citationEntity = true;
			String[] parts = StringUtils.split(reference, Entity.SEPARATOR);

			String subType = null;
			String context = null;
			String id = null;
			String container = null;

			// the first part will be citation, then next the service; two examples of arrays:
			// [citation, content, group, dbde854b-80f3-460f-b89e-340879538239, test123]
			// [citation, export_ris_all, b60b889d-79fe-454b-8283-e41a135ad62a]
			if (parts.length > 2)
			{
				subType = parts[1];
				if (CitationService.REF_TYPE_EXPORT_RIS_ALL.equals(subType) ||
						CitationService.REF_TYPE_EXPORT_RIS_SEL.equals(subType))
				{
					context = parts[2];
					id = parts[2];
					ref.set(APPLICATION_ID, subType, id, container, context);
				}
				else if ("content".equals(subType))
				{
					String wrappedRef = reference.substring(REFERENCE_ROOT.length(), reference
					        .length());
					Reference wrapped = m_entityManager.newReference(wrappedRef);
					if(ref == null) {
						log.warn("CitationService.parseEntityReference called with null Reference object", new Throwable());
					} else {
						ref.set(APPLICATION_ID, REF_TYPE_VIEW_LIST, wrapped.getId(), wrapped
						        .getContainer(), wrapped.getContext());
					}
				}
				else
				{
					log.warn(".parseEntityReference(): unknown citation subtype: " + subType
					        + " in ref: " + reference);
					citationEntity = false;
				}
			}
			else
			{
				citationEntity = false;
			}
		}

		return citationEntity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#removeCollection(org.sakaiproject.citation.api.CitationCollection)
	 */
	public void removeCollection(CitationCollection edit)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#save(org.sakaiproject.citation.api.Citation)
	 */
	public void save(Citation citation)
	{
		if (citation instanceof BasicCitation && ((BasicCitation) citation).isTemporary())
		{
			((BasicCitation) citation).m_id = m_idManager.createUuid();
			((BasicCitation) citation).m_temporary = false;
			((BasicCitation) citation).m_serialNumber = null;
		}
		this.m_storage.saveCitation(citation);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#save(org.sakaiproject.citation.api.CitationCollectionOrder)
	 */
	public void save(CitationCollectionOrder citationCollectionOrder)
	{
		this.m_storage.saveCitationCollectionOrder(citationCollectionOrder);
	}

	public void setIdManager(IdManager idManager)
	{
		m_idManager = idManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#save(org.sakaiproject.citation.api.CitationCollection)
	 */
	public void save(CitationCollection collection)
	{
		this.m_storage.saveCollection(collection);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#saveSection(org.sakaiproject.citation.api.CitationCollectionOrder)
	 */
	public void saveSection(CitationCollectionOrder citationCollectionOrder)
	{
		this.m_storage.saveSection(citationCollectionOrder);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#saveSubsection(org.sakaiproject.citation.api.CitationCollectionOrder)
	 */
	public void saveSubsection(CitationCollectionOrder citationCollectionOrder)
	{
		this.m_storage.saveSubsection(citationCollectionOrder);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#save(java.util.ArrayList, java.lang.String)
	 */
	public void save(List<CitationCollectionOrder> citationCollectionOrders, String citationCollectionId ) {
		this.m_storage.saveCitationCollectionOrders(citationCollectionOrders, citationCollectionId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#updateSection(org.sakaiproject.citation.api.CitationCollectionOrder)
	 */
	public void updateSection(CitationCollectionOrder citationCollectionOrder)
	{
		this.m_storage.updateCitationCollectionOrder(citationCollectionOrder);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getNestedCollection(java.lang.String)
	 */
	public CitationCollectionOrder getNestedCollection(String citationCollectionId)
	{
		return this.m_storage.getNestedSections(citationCollectionId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getUnnestedCitationCollection(java.lang.String)
	 */
	public CitationCollection getUnnestedCitationCollection(String citationCollectionId)
	{
		return this.m_storage.getUnnestedCitationCollection(citationCollectionId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getNestedCollectionAsList(java.lang.String)
	 */
	public List<CitationCollectionOrder> getNestedCollectionAsList(String citationCollectionId) {
		return this.m_storage.getNestedCollectionAsList(citationCollectionId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getNextCitationCollectionOrderId(java.lang.String)
	 */
	public String getNextCitationCollectionOrderId(String collectionId) {
		return this.m_storage.getNextCitationCollectionOrderId(collectionId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.citation.api.CitationService#getCitationCollectionOrder(java.lang.String, java.lang.String)
	 */
	public CitationCollectionOrder getCitationCollectionOrder(String collectionId, int locationId) {
		return this.m_storage.getCitationCollectionOrder(collectionId, locationId);
	}

	/*
	* (non-Javadoc)

	* @see org.sakaiproject.citation.api.CitationService#removeLocation(java.lang.String, int)
	*/
	public void removeLocation(String collectionId, int locationId)
	{
		this.m_storage.removeLocation(collectionId, locationId);
	}
	/**
	 * Dependency: ConfigurationService.
	 *
	 * @param service
	 *            The ConfigurationService.
	 */
	public void setConfigurationService(ConfigurationService service)
	{
		m_configService = service;
	}

	/**
	 * Dependency: ContentHostingService.
	 *
	 * @param service
	 *            The ContentHostingService.
	 */
	public void setContentHostingService(ContentHostingService service)
	{
		m_contentHostingService = service;
	}

	/**
	 * Dependency: EntityManager.
	 *
	 * @param service
	 *            The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}

	/**
	 * Dependency: ServerConfigurationService.
	 *
	 * @param service
	 *            The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		m_serverConfigurationService = service;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
	 */
	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public final class Counter
	{
		private int value;
		private Integer lock = new Integer(0);

		public Counter()
		{
			value = 0;
		}

		public Counter(int val)
		{
			value = val;
		}


		public void increment()
		{
			synchronized(lock)
			{
				value++;
			}
		}

		public void decrement()
		{
			synchronized(lock)
			{
				value--;
			}
		}

		public int intValue()
		{
			return value;
		}
	}

	/**
     * @return the attemptToMatchSchema
     */
    public boolean isAttemptToMatchSchema()
    {
    	return attemptToMatchSchema;
    }

	/**
     * @param attemptToMatchSchema the attemptToMatchSchema to set
     */
    public void setAttemptToMatchSchema(boolean attemptToMatchSchema)
    {
    	this.attemptToMatchSchema = attemptToMatchSchema;
    }

	/**
     * @param reference
     */
    public void copyCitationCollection(Reference reference)
    {
        ContentHostingService contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);
		try
		{
			ContentResourceEdit edit = contentService.editResource(reference.getId());
			String collectionId = new String(edit.getContent());
			CitationCollection oldCollection = getUnnestedCitationCollection(collectionId);
			BasicCitationCollection newCollection = new BasicCitationCollection();
			newCollection.copy((BasicCitationCollection) oldCollection);
			save(newCollection);
			edit.setContent(newCollection.getId().getBytes());
			// When duplicating/copying a citations list notifications shouldn't be sent so that 
			// this follow the behaviour of the standard resource types in Sakai.
			contentService.commitResource(edit, NotificationService.NOTI_NONE);
		}
		catch(IdUnusedException e)
		{
			log.warn("IdUnusedException ", e);
		}
		catch(ServerOverloadException e)
		{
			log.warn("ServerOverloadException ", e);
		}
		catch (PermissionException e)
		{
			log.warn("PermissionException ", e);
		}
		catch (TypeException e)
		{
			log.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			log.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			log.warn("OverQuotaException ", e);
		}
    }
    public Citation addCitation(HttpServletRequest request) {
        ContextObject co = m_openURLService.parse(request);
        Citation citation = null;
        if (co != null) {
            citation = m_openURLService.convert(co);
        }
        return citation;
    }
    
    @Override    
    public Citation copyCitation(Citation citation) {
    	BasicCitation c = new BasicCitation();
    	c.copy(citation);
    	return c;
    }

} // BaseCitationService

