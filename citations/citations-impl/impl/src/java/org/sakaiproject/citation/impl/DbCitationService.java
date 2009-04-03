/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;

/**
 *
 */
public class DbCitationService extends BaseCitationService
{
	/**
	 * 
	 */
	public class DbCitationStorage implements Storage
	{
		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#addCitation(java.lang.String)
		 */
		public Citation addCitation(String mediatype)
		{
			Citation citation = this.createCitation(mediatype);
			return citation;
		}

		public CitationCollection addCollection(Map attributes, List citations)
		{
			CitationCollection collection = this.createCollection(attributes, citations);
			return collection;
		}

		public Schema addSchema(Schema schema)
		{
			schema = this.createSchema(schema);
			return schema;
		}

		public boolean checkCitation(String citationId)
		{
			boolean check = this.validCitation(citationId);
			return check;
		}

		public boolean checkCollection(String collectionId)
		{
			boolean check = this.validCollection(collectionId);
			return check;
		}

		public boolean checkSchema(String schemaId)
		{
			boolean check = this.validSchema(schemaId);
			return check;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#close()
		 */
		public void close()
		{
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#copyAll(java.lang.String)
		 */
		public CitationCollection copyAll(String collectionId)
		{
			CitationCollection copy = this.duplicateAll(collectionId);
			return copy;
		}

		public Citation getCitation(String citationId)
		{
			Citation citation = this.retrieveCitation(citationId);
			return citation;
		}

		public CitationCollection getCollection(String collectionId)
		{
			CitationCollection collection = this.retrieveCollection(collectionId);
			return collection;
		}

		public Schema getSchema(String schemaId)
		{
			BasicSchema schema 	= (BasicSchema) ThreadLocalManager.get(schemaId);

			if(schema == null)
			{
				schema = (BasicSchema) this.retrieveSchema(schemaId);
			}
			else
			{
				schema = new BasicSchema(schema);
			}

			return schema;
		}

		public List getSchemas()
		{
			List schemas = (List) ThreadLocalManager.get("DbCitationStorage.getSchemas");

			if(schemas == null)
			{
				schemas = this.retrieveSchemas();
			}
			else
			{
				List rv = new Vector();
				Iterator it = schemas.iterator();
				while(it.hasNext())
				{
					Schema schema = (Schema) it.next();
					rv.add(new BasicSchema(schema));
				}
				schemas = rv;
			}
			return schemas;
		}

		public List listSchemas()
		{
			List schemaIds 	= (List) ThreadLocalManager.get("DbCitationStorage.listSchemas");

			if(schemaIds == null)
			{
				schemaIds = this.retrieveSchemaList();
			}
			else
			{
				schemaIds = new Vector(schemaIds);
			}

			return schemaIds;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#open()
		 */
		public void open()
		{
		}

		public void putSchemas(Collection schemas)
		{
			this.insertSchemas(schemas);
		}

		public void removeCitation(Citation edit)
		{
			this.deleteCitation(edit);
		}

		public void removeCollection(CitationCollection edit)
		{
			this.deleteCollection(edit);
		}

		public void removeSchema(Schema schema)
		{
			this.deleteSchema(schema);
		}

		public void saveCitation(Citation edit)
		{
			this.commitCitation(edit);
		}

		public void saveCollection(CitationCollection collection)
		{
			this.commitCollection(collection);
		}

		public void updateSchema(Schema schema)
		{
			this.reviseSchema(schema);
		}

		public void updateSchemas(Collection schemas)
		{
			this.reviseSchemas(schemas);
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#saveCitation(org.sakaiproject.citation.api.Citation)
         */
        protected void commitCitation(Citation citation)
        {
        	deleteCitation(citation);

			String statement = "insert into " + m_citationTableName + " (" + m_citationTableId + ", PROPERTY_NAME, PROPERTY_VALUE) values ( ?, ?, ? )";

			String citationId = citation.getId();

			boolean ok = true;
			Object[] fields = new Object[3];
			fields[0] = citationId;
			if(citation.getSchema() != null)
			{
				fields[1] = PROP_MEDIATYPE;
				fields[2] = citation.getSchema().getIdentifier();
				ok = m_sqlService.dbWrite(statement, fields);
			}

			String displayName = citation.getDisplayName();
			if(displayName != null)
			{
				fields[1] = PROP_DISPLAYNAME;
				fields[2] = displayName.trim();
				ok = m_sqlService.dbWrite(statement, fields);

			}

			if(citation.isAdded())
			{
				fields[1] = PROP_ADDED;
				fields[2] = Boolean.TRUE.toString();
				ok = m_sqlService.dbWrite(statement, fields);
			}

			List names = citation.listCitationProperties();
			Iterator nameIt = names.iterator();
			while(nameIt.hasNext())
			{
				String name = (String) nameIt.next();
				Object value = citation.getCitationProperty(name);
				if(value instanceof List)
				{
					List list = (List) value;
					for(int i = 0; i < list.size(); i++)
					{
						Object item = list.get(i);
						fields[1] = name + PROPERTY_NAME_DELIMITOR + i;
						fields[2] = item;

						ok = m_sqlService.dbWrite(statement, fields);

					}
				}
				else if(value instanceof String)
				{
					fields[1] = name;
					fields[2] = value;

					ok = m_sqlService.dbWrite(statement, fields);
				}
				else
				{
					M_log.info("DbCitationStorage.saveCitation value not List or String: " + value.getClass().getCanonicalName() + " " + value);
					fields[1] = name;
					fields[2] = value;

					ok = m_sqlService.dbWrite(statement, fields);
				}
			}

			int urlCount = 0;
			Map urls = ((BasicCitation) citation).m_urls;
			if(urls != null)
			{
				Iterator urlIt = urls.keySet().iterator();
				while(urlIt.hasNext())
				{
					String id = (String) urlIt.next();
					UrlWrapper wrapper = (UrlWrapper) urls.get(id);
					fields[1] = PROP_HAS_URL + PROPERTY_NAME_DELIMITOR + urlCount;
					fields[2] = id;
					ok = m_sqlService.dbWrite(statement, fields);
					commitUrl(id, wrapper.getLabel(), wrapper.getUrl(), wrapper.addPrefix());
					urlCount++;
				}
			}
			
			// preferred url
			if(citation.hasPreferredUrl())
			{
				fields[1] = PROP_HAS_PREFERRED_URL;
				fields[2] = citation.getPreferredUrlId();

				ok = m_sqlService.dbWrite(statement, fields);
			}
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#saveCollection(java.util.Collection)
         */
        protected void commitCollection(CitationCollection collection)
        {
        	deleteCollection(collection);

			String statement = "insert into " + m_collectionTableName + " (" + m_collectionTableId + ",PROPERTY_NAME,PROPERTY_VALUE) values ( ?, ?, ? )";

			boolean ok = true;

			String collectionId = collection.getId();
			Object[] fields = new Object[3];
			fields[0] = collectionId;

			List members = collection.getCitations();
			Iterator citationIt = members.iterator();
			while(citationIt.hasNext())
			{
				Citation citation = (Citation) citationIt.next();

				save(citation);

				// process the insert
				fields[1] = PROPERTY_HAS_CITATION;
				fields[2] = citation.getId();
				ok = m_sqlService.dbWrite(statement, fields);
			}
			
			long mostRecentUpdate = TimeService.newTime().getTime();			
			fields[1] = PROP_MOST_RECENT_UPDATE;
			fields[2] = Long.toString(mostRecentUpdate);
			ok = m_sqlService.dbWrite(statement, fields);
			
			fields[1] = PROP_SORT_ORDER;
			fields[2] = collection.getSort();
			ok = m_sqlService.dbWrite(statement, fields);

        }

 		/**
         * @param id
 		 * @param label
 		 * @param url
         */
        protected void commitUrl(String id, String label, String url, boolean addPrefix)
        {
	        deleteUrl(id);

			String statement = "insert into " + m_citationTableName + " (" + m_citationTableId + ", PROPERTY_NAME, PROPERTY_VALUE) values ( ?, ?, ? )";

			boolean ok = true;
			Object[] fields = new Object[3];
			fields[0] = id;
			fields[1] = PROP_URL_LABEL;
			fields[2] = label;
			ok = m_sqlService.dbWrite(statement, fields);

			fields[1] = PROP_URL_STRING;
			fields[2] = url;
			ok = m_sqlService.dbWrite(statement, fields);

			fields[1] = PROP_URL_ADD_PREFIX;
			fields[2] = addPrefix ? Citation.ADD_PREFIX_TEXT 
			                      : Citation.OMIT_PREFIX_TEXT;
			ok = m_sqlService.dbWrite(statement, fields);

		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#addCitation(java.lang.String)
         */
        protected Citation createCitation(String mediatype)
        {
           	// need to create a citation (referred to below as "edit")
        	BasicCitation edit = new BasicCitation(mediatype);

			String statement = "insert into " + m_citationTableName + " (" + m_citationTableId + ", PROPERTY_NAME, PROPERTY_VALUE) values ( ?, ?, ? )";

			String citationId = edit.getId();

			Object[] fields = new Object[3];
			fields[0] = citationId;

			fields[1] = PROP_MEDIATYPE;
			fields[2] = edit.getSchema().getIdentifier();
			boolean ok = m_sqlService.dbWrite(statement, fields);

			List names = edit.listCitationProperties();
			boolean first = true;
			Iterator nameIt = names.iterator();
			while(nameIt.hasNext())
			{
				String name = (String) nameIt.next();
				Object value = edit.getCitationProperty(name);

				fields[1] = name;
				fields[2] = value;

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}

			return edit;
       }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#putCollection(java.lang.String, java.util.Map, java.util.List)
         */
        protected CitationCollection createCollection(Map attributes, List citations)
        {
           	// need to create a collection (referred to below as "edit")
        	BasicCitationCollection edit = new BasicCitationCollection(attributes, citations);

			String statement = "insert into " + m_collectionTableName + " (" + m_collectionTableId + ",PROPERTY_NAME,PROPERTY_VALUE) values ( ?, ?, ? )";

			Object[] fields = new Object[3];
			fields[0] = edit.getId();
			fields[1] = PROPERTY_HAS_CITATION;
			boolean ok = true;

			List members = edit.getCitations();
			Iterator citationIt = members.iterator();
			while(citationIt.hasNext())
			{
				Citation citation = (Citation) citationIt.next();

		    	if(citation instanceof BasicCitation && ((BasicCitation) citation).isTemporary())
		    	{
		    		((BasicCitation) citation).m_id = IdManager.createUuid();
		    		((BasicCitation) citation).m_temporary = false;
		    		((BasicCitation) citation).m_serialNumber = null;
		    	}

				commitCitation(citation);

				// process the insert
				fields[2] = citation.getId();
				ok = m_sqlService.dbWrite(statement, fields);
			}

			edit.m_mostRecentUpdate = TimeService.newTime().getTime();			
			fields[1] = PROP_MOST_RECENT_UPDATE;
			fields[2] = Long.toString(edit.m_mostRecentUpdate);
			ok = m_sqlService.dbWrite(statement, fields);
			
			return edit;
       }

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#putSchema(org.sakaiproject.citation.api.Schema)
		 */
		protected Schema createSchema(Schema schema)
		{
			String statement = "insert into " + m_schemaTableName + " (" + m_schemaTableId + ",PROPERTY_NAME,PROPERTY_VALUE) values ( ?, ?, ? )";

			String schemaId = schema.getIdentifier();

			boolean ok = true;
			Object[] fields = new Object[3];
			fields[0] = schemaId;
			fields[1] = PROPERTY_HAS_FIELD;

			List schemaFields = schema.getFields();
			for(int i = 0; i < schemaFields.size(); i++)
			{
				Field field = (Field) schemaFields.get(i);
				if(field instanceof BasicField)
				{
					((BasicField) field).setOrder(i);
				}
				insertSchemaField(field, schemaId);
				fields[2] = field.getIdentifier();
				ok = m_sqlService.dbWrite(statement, fields);

			}


			Iterator namespaceIt = schema.getNamespaceAbbreviations().iterator();
			while(namespaceIt.hasNext())
			{
				String abbrev = (String) namespaceIt.next();
				String namespaceUri = schema.getNamespaceUri(abbrev);
				if(abbrev != null && namespaceUri != null)
				{
					fields[0] = schemaId;
					fields[1] = PROPERTY_HAS_NAMESPACE;
					fields[2] = namespaceUri;
					ok = m_sqlService.dbWrite(statement, fields);

					fields[0] = namespaceUri;
					fields[1] = PROPERTY_HAS_ABBREVIATION;
					fields[2] = abbrev;
					ok = m_sqlService.dbWrite(statement, fields);
				}
			}

			if(schema.getNamespaceAbbrev() != null && ! "".equals(schema.getNamespaceAbbrev().trim()))
			{
				fields[0] = schemaId;
				fields[1] = PROPERTY_NAMESPACE;
				fields[2] = schema.getNamespaceAbbrev();
				ok = m_sqlService.dbWrite(statement, fields);
			}

			return schema;
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#removeCitation(org.sakaiproject.citation.api.Citation)
         */
        protected void deleteCitation(Citation edit)
        {
          	String statement = "delete from " + m_citationTableName + " where (" + m_citationTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = edit.getId();

			boolean ok = m_sqlService.dbWrite(statement, fields);
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#removeCollection(org.sakaiproject.citation.api.CitationCollection)
         */
        protected void deleteCollection(CitationCollection edit)
        {
          	String statement = "delete from " + m_collectionTableName + " where (" + m_collectionTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = edit.getId();

			boolean ok = m_sqlService.dbWrite(statement, fields);
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#removeSchema(org.sakaiproject.citation.api.Schema)
         */
        protected void deleteSchema(Schema schema)
        {
         	String statement = "delete from " + m_schemaTableName + " where (" + m_schemaTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = schema.getIdentifier();

			boolean ok = m_sqlService.dbWrite(statement, fields);

         	// need to remove schema fields also
         	if(ok)
         	{
         		String statement2 = "delete from " + m_schemaFieldTableName + " where (" + m_schemaTableId + " = ?)";

         		ok = m_sqlService.dbWrite(statement2, fields);
         	}

        }

		/**
         * @param id
         */
        protected void deleteUrl(String id)
        {
          	String statement = "delete from " + m_citationTableName + " where (" + m_citationTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = id;

			boolean ok = m_sqlService.dbWrite(statement, fields);
        }

		protected CitationCollection duplicateAll(String collectionId)
		{
			CitationCollection original = this.retrieveCollection(collectionId);
			CitationCollection copy = null;

			if(original != null)
			{
				copy = new BasicCitationCollection();
				Iterator it = original.iterator();
				while(it.hasNext())
				{
					BasicCitation citation = (BasicCitation) it.next();
					BasicCitation newCite = new BasicCitation(citation.getSchema().getIdentifier());
					newCite.copy(citation);
					copy.add(newCite);
				}
				this.commitCollection(copy);
			}

			return copy;
		}

		/**
         * @param citationId
		 * @return
         */
        protected boolean getAdded(String citationId)
        {
			String statement = "select PROPERTY_VALUE from " + m_citationTableName + " where (CITATION_ID = ? and PROPERTY_NAME = ?)";

			Object fields[] = new Object[2];
			fields[0] = citationId;
			fields[1] = PROP_ADDED;

			List list = m_sqlService.dbRead(statement, fields, null);

			return ! list.isEmpty();
        }

		protected String getMediatype(String citationId)
		{
			String statement = "select PROPERTY_VALUE from " + m_citationTableName + " where (CITATION_ID = ? and PROPERTY_NAME = ?)";

			Object fields[] = new Object[2];
			fields[0] = citationId;
			fields[1] = PROP_MEDIATYPE;

			List list = m_sqlService.dbRead(statement, fields, null);

			String rv = UNKNOWN_TYPE;
			if(! list.isEmpty())
			{
				rv = (String) list.get(0);
			}

			return rv;
		}

		protected void insertSchemaField(Field field, String schemaId)
		{
			String statement = "insert into " + m_schemaFieldTableName + " (" + m_schemaTableId + "," + m_schemaFieldTableId + ",PROPERTY_NAME,PROPERTY_VALUE) values ( ?, ?, ?, ? )";

			boolean ok = true;

			Object[] fields = new Object[4];
			fields[0] = schemaId;
			fields[1] = field.getIdentifier();

			if(field instanceof BasicField)
			{
				int order = ((BasicField) field).getOrder();
				fields[2] = PROPERTY_HAS_ORDER;
				fields[3] = new Integer(order).toString();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}

			// PROPERTY_NAMESPACE namespace;
			if(field.getNamespaceAbbreviation() != null && ! "".equals(field.getNamespaceAbbreviation().trim()))
			{
				fields[2] = PROPERTY_NAMESPACE;
				fields[3] = field.getNamespaceAbbreviation();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
			// PROPERTY_LABEL label;
			if(field.getLabel() != null && ! "".equals(field.getLabel().trim()))
			{
				fields[2] = PROPERTY_LABEL;
				fields[3] = field.getLabel();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
			// PROPERTY_DESCRIPTION description;
			if(field.getDescription() != null && ! "".equals(field.getDescription().trim()))
			{
				fields[2] = PROPERTY_DESCRIPTION;
				fields[3] = field.getDescription();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
			// PROPERTY_REQUIRED required;
			fields[2] = PROPERTY_REQUIRED;
			fields[3] = new Boolean(field.isRequired()).toString();
			ok = m_sqlService.dbWrite(statement, fields);
			// PROPERTY_MINCARDINALITY minCardinality;
			fields[2] = PROPERTY_MINCARDINALITY;
			fields[3] = new Integer(field.getMinCardinality()).toString();
			ok = m_sqlService.dbWrite(statement, fields);
			// PROPERTY_MAXCARDINALITY maxCardinality;
			fields[2] = PROPERTY_MAXCARDINALITY;
			fields[3] = new Integer(field.getMaxCardinality()).toString();
			ok = m_sqlService.dbWrite(statement, fields);
			// PROPERTY_DEFAULTVALUE defaultValue;
			if(field.getDefaultValue() != null)
			{
				fields[2] = PROPERTY_DEFAULTVALUE;
				fields[3] = field.getDefaultValue().toString();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
			// PROPERTY_VALUETYPE valueType;
			if(field.getValueType() != null && ! "".equals(field.getValueType().trim()))
			{
				fields[2] = PROPERTY_VALUETYPE;
				fields[3] = field.getValueType();

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
			String risIdentifier = field.getIdentifier(RIS_FORMAT);
			if(risIdentifier != null && ! risIdentifier.trim().equals(""))
			{
				fields[2] = PROP_HAS_RIS_IDENTIFIER;
				fields[3] = risIdentifier;

				// process the insert
				ok = m_sqlService.dbWrite(statement, fields);
			}
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#putSchemas(java.util.Collection)
		 */
		protected void insertSchemas(Collection schemas)
		{
			Iterator it = schemas.iterator();
			while(it.hasNext())
			{
				Schema schema = (Schema) it.next();
				createSchema(schema);
				// ThreadLocalManager.set(schema.getIdentifier(), schema);
			}

			//ThreadLocalManager.set("DbCitationStorage.listSchemas", null);
			//ThreadLocalManager.set("DbCitationStorage.getSchemas", null);

		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#getCitation(java.lang.String)
		 */
		protected Citation retrieveCitation(String citationId)
		{
			String schemaId = getMediatype(citationId);
			boolean added = getAdded(citationId);

			Schema schema = retrieveSchema(schemaId);

			BasicCitation edit = new BasicCitation(citationId, schema);
			edit.setAdded(added);

			String statement = "select CITATION_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_citationTableName + " where (CITATION_ID = ?)  order by PROPERTY_NAME";
			String urlStatement = "select CITATION_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_citationTableName + " where (CITATION_ID = ?)";

			Object fields[] = new Object[1];
			fields[0] = citationId;

			List triples = m_sqlService.dbRead(statement, fields, new TripleReader());

			Iterator it = triples.iterator();
			while(it.hasNext())
			{
				Triple triple = (Triple) it.next();
				if(triple.isValid())
				{
					String name = triple.getName();
					String order = "0";
					Matcher matcher = MULTIVALUED_PATTERN.matcher(name);
					if(matcher.matches())
					{
						name = matcher.group(1);
						order = matcher.group(2);
					}
					if(PROP_HAS_URL.equals(name))
					{
						String id = (String) triple.getValue();
						fields[0] = id;
						List urlfields = m_sqlService.dbRead(statement, fields, new TripleReader());
						String label = null;
						String url = null;
						boolean addPrefix = false;
						Iterator urlFieldIt = urlfields.iterator();
						while(urlFieldIt.hasNext())
						{
							Triple urlField = (Triple) urlFieldIt.next();
							if(PROP_URL_LABEL.equals(urlField.getName()))
							{
								label = (String) urlField.getValue();
							}
							else if(PROP_URL_STRING.equals(urlField.getName()))
							{
								url = (String) urlField.getValue();
							}
							else if(PROP_URL_ADD_PREFIX.equals(urlField.getName()))
							{
							  String enabled = (String) urlField.getValue();
							  
								addPrefix = Citation.ADD_PREFIX_TEXT.equals(enabled);
							}
						}
						edit.m_urls.put(id, new UrlWrapper(label, url, addPrefix));
					}
					else if(PROP_HAS_PREFERRED_URL.equals(name))
					{
						edit.m_preferredUrl = (String) triple.getValue();
					}
					else if(isMultivalued(schemaId, name))
					{
						edit.addPropertyValue(name, triple.getValue());
					}
					else
					{
						edit.setCitationProperty(name, triple.getValue());
						if(PROP_DISPLAYNAME.equals(name.trim()))
						{
							edit.setDisplayName(triple.getValue().toString());
						}
					}
				}
			}

			return edit;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#getCollection(java.lang.String)
		 */
		protected CitationCollection retrieveCollection(String collectionId)
		{
			String statement = "select COLLECTION_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_collectionTableName + " where (COLLECTION_ID = ?)";

			BasicCitationCollection edit = new BasicCitationCollection(collectionId);

			Object fields[] = new Object[1];
			fields[0] = collectionId;

			List triples = m_sqlService.dbRead(statement, fields, new TripleReader());

			Iterator it = triples.iterator();
			while(it.hasNext())
			{
				Triple triple = (Triple) it.next();
				if(triple.isValid())
				{
					if(triple.getName().equals(PROPERTY_HAS_CITATION))
					{
						Citation citation = retrieveCitation((String) triple.getValue());
						edit.add(citation);
					}
					else if(triple.getName().equals(PROP_MOST_RECENT_UPDATE))
					{
						try
						{
							edit.m_mostRecentUpdate = Long.parseLong(triple.getValue().toString());
						}
						catch(Exception e)
						{
							// do nothing
						}
					}
					else if(triple.getName().equals(PROP_SORT_ORDER))
					{
						try
						{
							edit.setSort(triple.getValue().toString(), true);
						}
						catch(Exception e)
						{
							// do nothing
						}
					}
					/*
					 * TODO: else add property??
					 */
				}
			}

			return edit;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#getSchema(java.lang.String)
		 */
		protected Schema retrieveSchema(String schemaId)
		{
			BasicSchema schema = (BasicSchema) ThreadLocalManager.get(schemaId);
			if(schema == null)
			{
				String statement = "select SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_schemaTableName + " where (SCHEMA_ID = ?)";

				schema = new BasicSchema(schemaId);

				Object fields[] = new Object[1];
				fields[0] = schemaId;

				List triples = m_sqlService.dbRead(statement, fields, new TripleReader());

				Iterator it = triples.iterator();
				while(it.hasNext())
				{
					Triple triple = (Triple) it.next();
					if(triple.isValid())
					{
						if(triple.getName().equals(PROPERTY_HAS_FIELD))
						{
							String fieldId = (String) triple.getValue();
							BasicField field = retrieveSchemaField(schemaId, fieldId);
							schema.addField(field);
						}
						/*
						 * TODO: else add property??
						 */
					}
				}

				schema.sortFields();
				ThreadLocalManager.set(schemaId, new BasicSchema(schema));
			}
			else
			{
				schema = new BasicSchema(schema);
			}

			return schema;
		}

		/**
         * @param fieldId
		 * @return
         */
        protected BasicField retrieveSchemaField(String schemaId, String fieldId)
        {
			String statement = "select FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_schemaFieldTableName + " where (SCHEMA_ID = ? and FIELD_ID = ?)";

			Object[] fields = new Object[2];
			fields[0] = schemaId;
			fields[1] = fieldId;

			Map values = new Hashtable();

			List triples = m_sqlService.dbRead(statement, fields, new TripleReader());
			Iterator tripleIt = triples.iterator();
			while(tripleIt.hasNext())
			{
				Triple triple = (Triple) tripleIt.next();

				// PROPERTY_NAMESPACE namespace;
				if(triple.isValid())
				{
					values.put(triple.getName(), triple.getValue());
				}
			}
			String valueType = (String) values.remove(PROPERTY_VALUETYPE);
			String required = (String) values.remove(PROPERTY_REQUIRED);
			boolean isRequired = Boolean.TRUE.toString().equalsIgnoreCase(required);

			String maxCardinality = (String) values.remove(PROPERTY_MAXCARDINALITY);
			int max = 1;
			if(maxCardinality != null)
			{
				max = new Integer(maxCardinality).intValue();
			}
			String minCardinality = (String) values.remove(PROPERTY_MINCARDINALITY);
			int min = 0;
			if(minCardinality != null)
			{
				min = new Integer(minCardinality).intValue();
			}

			BasicField field = new BasicField(fieldId, valueType, true, isRequired, min, max);

			// PROPERTY_HAS_ORDER
			String order = (String) values.remove(PROPERTY_HAS_ORDER);
			if(order != null)
			{
				try
				{
					Integer o = new Integer(order);
					field.setOrder(o.intValue());
				}
				catch(Exception e)
				{
					// couldn't get integer out of the value
				}
			}

			// PROPERTY_NAMESPACE
			String namespace = (String) values.remove(PROPERTY_NAMESPACE);
			if(namespace != null)
			{
				field.setNamespaceAbbreviation(namespace);
			}
			// PROPERTY_LABEL label;
			String label = (String) values.remove(PROPERTY_LABEL);
			if(label != null)
			{
				field.setLabel(label);
			}
			// PROPERTY_DESCRIPTION description;
			String description = (String) values.remove(PROPERTY_DESCRIPTION);
			if(description != null)
			{
				field.setDescription(description);
			}
			// PROPERTY_DEFAULTVALUE defaultValue;
			String defaultValue = (String) values.remove(PROPERTY_DEFAULTVALUE);
			if(defaultValue == null)
			{
				// do nothing
			}
			else if(String.class.getName().equalsIgnoreCase(valueType))
			{
				field.setDefaultValue(defaultValue);
			}
			else if(Integer.class.getName().equalsIgnoreCase(valueType))
			{
				field.setDefaultValue(new Integer(defaultValue));
			}
			else if(Boolean.class.getName().equalsIgnoreCase(valueType))
			{
				field.setDefaultValue(new Boolean(defaultValue));
			}
			else if(Time.class.getName().equalsIgnoreCase(valueType))
			{
				field.setDefaultValue(TimeService.newTime(new Integer(defaultValue).longValue()));
			}
			// PROP_HAS_RIS_IDENTIFIER RIS identifier
			String risIdentifier = (String) values.remove(PROP_HAS_RIS_IDENTIFIER);
			if(risIdentifier != null)
			{
				field.setIdentifier(RIS_FORMAT, risIdentifier);
			}

	        return field;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#getSchemaNames()
		 */
		protected List retrieveSchemaList()
		{
			List schemaIds = (List) ThreadLocalManager.get("DbCitationStorage.listSchemas");
			if(schemaIds == null)
			{
				String statement = "select distinct SCHEMA_ID from " + m_schemaTableName + " order by SCHEMA_ID";

				schemaIds = m_sqlService.dbRead(statement, null, null);

				ThreadLocalManager.set("DbCitationStorage.listSchemas", schemaIds);
			}

			return new Vector(schemaIds);
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#getSchemas()
		 */
		protected List retrieveSchemas()
		{
			List schemas = (List) ThreadLocalManager.get("DbCitationStorage.getSchemas");
			if(schemas == null)
			{
				String statement = "select SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE from " + m_schemaTableName + " order by SCHEMA_ID";

				List triples = m_sqlService.dbRead(statement, null, new TripleReader());

				schemas = new Vector();

				BasicSchema schema = null;
				String schemaId = "";
				Iterator it = triples.iterator();
				while(it.hasNext())
				{
					Triple triple = (Triple) it.next();
					if(triple.isValid())
					{
						if(! schemaId.equals(triple.getId()))
						{
							schemaId = triple.getId();
							schema = new BasicSchema(schemaId);
							schemas.add(schema);
						}
						if(triple.getName().equals(PROPERTY_HAS_FIELD))
						{
							String fieldId = (String) triple.getValue();
							BasicField field = retrieveSchemaField(schemaId, fieldId);
							schema.addField(field);
						}
						/*
						 * TODO: else add property??
						 */
					}
				}
				Iterator schemaIt = schemas.iterator();
				while(schemaIt.hasNext())
				{
					BasicSchema sch = (BasicSchema) schemaIt.next();
					sch.sortFields();
				}
				ThreadLocalManager.set("DbCitationStorage.getSchemas", schemas);
			}

			List rv = new Vector();
			Iterator it = schemas.iterator();
			while(it.hasNext())
			{
				Schema schema = (Schema) it.next();
				rv.add(new BasicSchema(schema));
			}
			schemas = rv;

			return schemas;
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#updateSchema(org.sakaiproject.citation.api.Schema)
         */
        protected void reviseSchema(Schema schema)
        {
        	deleteSchema(schema);
        	createSchema(schema);
         }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#updateSchemas(java.util.Collection)
         */
        protected void reviseSchemas(Collection schemas)
        {
			Iterator it = schemas.iterator();
			while(it.hasNext())
			{
				Schema schema = (Schema) it.next();
	        	reviseSchema(schema);
			}
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#checkCitation(java.lang.String)
         */
        protected boolean validCitation(String citationId)
        {
           	String statement = "select " + m_citationTableId + " from " + m_citationTableName + " where ( " + m_citationTableId + " = ? )";

			Object fields[] = new Object[1];
			fields[0] = citationId;

			List rows = m_sqlService.dbRead(statement, fields, null);

			boolean found = ! rows.isEmpty();

        	return found;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#checkCollection(java.lang.String)
         */
        protected boolean validCollection(String collectionId)
        {
           	String statement = "select " + m_collectionTableId + " from " + m_collectionTableName + " where (" + m_collectionTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = collectionId;

			List rows = m_sqlService.dbRead(statement, fields, null);

			boolean found = ! rows.isEmpty();

        	return found;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.impl.BaseCitationService.Storage#checkSchema(java.lang.String)
         */
        protected boolean validSchema(String schemaId)
        {
           	String statement = "select " + m_schemaTableId + " from " + m_schemaTableName + " where (" + m_schemaTableId + " = ?)";

			Object fields[] = new Object[1];
			fields[0] = schemaId;

			List rows = m_sqlService.dbRead(statement, fields, null);

			boolean found = ! rows.isEmpty();

        	return found;
        }

		public long mostRecentUpdate(String collectionId) 
		{
           	String statement =  "select PROPERTY_VALUE from " + m_collectionTableName + " where (COLLECTION_ID = ? and PROPERTY_NAME = ?)" ;

			Object fields[] = new Object[2];
			fields[0] = collectionId;
			fields[1] = PROP_MOST_RECENT_UPDATE;

			List list = m_sqlService.dbRead(statement, fields, null);

			long time = 0L;
			if(list == null || list.isEmpty())
			{
				// do nothing
			}
			else
			{
				try
				{
					time = Long.parseLong(list.get(0).toString());
				}
				catch(Exception e)
				{
					// do nothing
				}
			}

			return time;
		}

	}

	public class Triple
	{
		protected String m_id;
		protected String m_name;
		protected Object m_value;
		/**
         * @param id
         * @param name
         * @param value
         */
        public Triple(String id, String name, String value)
        {
	        super();
	        m_id = id;
	        m_name = name;
	        m_value = value;
        }
		/**
         * @return the id
         */
        public String getId()
        {
        	return m_id;
        }
		/**
         * @return the name
         */
        public String getName()
        {
        	return m_name;
        }
		/**
         * @return the value
         */
        public Object getValue()
        {
        	return m_value;
        }
		/**
         * @return
         */
        public boolean isValid()
        {
	        return m_id != null && m_name != null && m_value != null;
        }
		/**
         * @param id the id to set
         */
        public void setId(String id)
        {
        	m_id = id;
        }
		/**
         * @param name the name to set
         */
        public void setName(String name)
        {
        	m_name = name;
        }
		/**
         * @param value the value to set
         */
        public void setValue(Object value)
        {
        	m_value = value;
        }

        public String toString()
        {
        	return "[triple id = " + ((m_id == null) ? "null" : m_id)
						+ " name = " + ((m_name == null) ? "null" : m_name)
						+ " value = " + ((m_value == null) ? "null" : m_value.toString() + "]");
        }

	}

	public class TripleReader implements SqlReader
	{

		/* (non-Javadoc)
         * @see org.sakaiproject.db.api.SqlReader#readSqlResultRecord(java.sql.ResultSet)
         */
        public Object readSqlResultRecord(ResultSet result)
        {
        	Triple triple = null;
        	String citationId = null;
        	String name = null;
        	String value = null;
	        try
            {
	        	citationId = result.getString(1);
	            name = result.getString(2);
	            value = result.getString(3);

	            triple = new Triple(citationId, name, value);
            }
            catch (SQLException e)
            {
	            M_log.debug("TripleReader: problem reading triple from result: citationId(" + citationId + ") name(" + name + ") value(" + value + ")");
	            return null;
            }
	        return triple;
        }

	}

	private static final int AUTO_FALSE			= 3;
	private static final int AUTO_TRUE			= 2;
	/* Connection management: Original auto-commit state (unknown, on, off) */
	private static final int AUTO_UNKNOWN		= 1;
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(DbCitationService.class);
	protected static final Pattern MULTIVALUED_PATTERN = Pattern.compile("^(.*)\\t(\\d+)$");
	
	protected static final String PROP_SORT_ORDER = "sakai:sort_order";

	protected static final String PROP_MOST_RECENT_UPDATE = "sakai:most_recent_update";
	
	protected static final String PROP_ADDED = "sakai:added";
	protected static final String PROP_DISPLAYNAME = "sakai:displayname";

	protected static final String PROP_HAS_RIS_IDENTIFIER = "sakai:ris_identifier";

	protected static final String PROP_HAS_URL = "sakai:has_url";
	
	protected static final String PROP_HAS_PREFERRED_URL = "sakai:has_preferred_url";

	protected static final String PROP_MEDIATYPE = "sakai:mediatype";

	protected static final String PROP_URL_LABEL = "sakai:url_label";

	protected static final String PROP_URL_STRING = "sakai:url_string";

	protected static final String PROP_URL_ADD_PREFIX = "sakai:url_add_prefix";

	protected static final String PROPERTY_NAME_DELIMITOR = "\t";

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	protected String m_citationTableId = "CITATION_ID";

	/** Table name for citation. */
	protected String m_citationTableName = "CITATION_CITATION";

	protected String m_collectionTableId = "COLLECTION_ID";

	/** Table name for collections. */
	protected String m_collectionTableName = "CITATION_COLLECTION";

	protected String m_schemaFieldTableId = "FIELD_ID";

	protected String m_schemaFieldTableName = "CITATION_SCHEMA_FIELD";

	protected String m_schemaTableId = "SCHEMA_ID";
	/** Table name for schemas. */
	protected String m_schemaTableName = "CITATION_SCHEMA";
	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_citation");
				M_log.info("init(): tables: " + m_collectionTableName + ", " + m_citationTableName + ", " + m_schemaTableName + ", " + m_schemaFieldTableName);
			}

			super.init();
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

	}	// init

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.impl.BaseCitationService#newStorage()
	 */
	public Storage newStorage()
	{
		return new DbCitationStorage();
	}

	/**
	 * Configuration: to run the ddl on init or not.
	 *
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
	}

	/**
	 * Dependency: SqlService.
	 *
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/**
	 * Error handling: Rollback the transaction, restore auto-commit, and
	 * return the borrowed connection
	 *
	 * @param conn Database Connection
	 * @param wasCommit Original connection auto-commit state
	 */
	private void errorCleanup(Connection conn, int wasCommit)
	{
		if (conn != null)
		{
			try { conn.rollback(); } catch (Throwable ignore) { }

			restoreAutoCommit(conn, wasCommit);
			m_sqlService.returnConnection(conn);
		}
	}
	
	/*
	 * Connection management helpers
	 */
	/**
	 * Determine auto-commit state
	 * @param conn Database Connection
	 * @return The original connection auto-commit state
	 */
	private int getAutoCommit(Connection conn)
	{
		try
		{
			boolean wasCommit = conn.getAutoCommit();

			return wasCommit ? AUTO_TRUE : AUTO_FALSE;
		}
		catch (SQLException exception)
		{
			M_log.warn("restoreAutoCommit: " + exception);
			return AUTO_UNKNOWN;
		}
	}

	/**
	 * Restore auto-commit state
	 *
	 * @param conn Database Connection
	 * @param wasCommit Original connection auto-commit state
	 */
	private void restoreAutoCommit(Connection conn, int wasCommit)
	{
		try
		{
			switch (wasCommit)
			{
				case AUTO_TRUE:
					conn.setAutoCommit(true);
					break;

				case AUTO_FALSE:
					conn.setAutoCommit(false);
					break;

				case AUTO_UNKNOWN:
					break;

				default:
					M_log.warn("restoreAutoCommit: unknown commit type: " + wasCommit);
					break;
			}
		}
		catch (Throwable throwable)
		{
			M_log.warn("restoreAutoCommit: " + throwable);
		}
	}

	/**
	 * Form a string of (field, field, field), for sql insert statements, one for each item in the fields array, plus one before, and one after.
	 *
	 * @param before
	 *        The first field name.
	 * @param values
	 *        The extra field names, in the middle.
	 * @param after
	 *        The last field name.
	 * @return A sql statement fragment for the insert fields.
	 */
	protected String insertFields(String before, String[] fields, String after)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(" (");

		buf.append(before);

		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				if(i == 0 && before == null)
				{
					buf.append(fields[i]);
				}
				else
				{
					buf.append("," + fields[i]);
				}
			}
		}

		if(after != null)
		{
			buf.append("," + after);
		}

		buf.append(")");

		return buf.toString();
	}
}
