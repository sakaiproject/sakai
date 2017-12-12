/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.sru.ss360search;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.indiana.lib.osid.base.repository.http.CreatorPartStructure;
import edu.indiana.lib.osid.base.repository.http.DatePartStructure;
import edu.indiana.lib.osid.base.repository.http.InLineCitationPartStructure;
import edu.indiana.lib.osid.base.repository.http.IsnIdentifierPartStructure;
import edu.indiana.lib.osid.base.repository.http.IssuePartStructure;
import edu.indiana.lib.osid.base.repository.http.OpenUrlPartStructure;
import edu.indiana.lib.osid.base.repository.http.PagesPartStructure;
import edu.indiana.lib.osid.base.repository.http.PreferredUrlPartStructure;
import edu.indiana.lib.osid.base.repository.http.SourceTitlePartStructure;
import edu.indiana.lib.osid.base.repository.http.StartPagePartStructure;
import edu.indiana.lib.osid.base.repository.http.TypePartStructure;
import edu.indiana.lib.osid.base.repository.http.URLPartStructure;
import edu.indiana.lib.osid.base.repository.http.VolumePartStructure;
import edu.indiana.lib.osid.base.repository.http.YearPartStructure;

import edu.indiana.lib.twinpeaks.search.MatchItem;
import edu.indiana.lib.twinpeaks.search.QueryBase;
import edu.indiana.lib.twinpeaks.search.SearchResultBase;
import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.SessionContext;
import edu.indiana.lib.twinpeaks.util.StatusUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Parse the 360 Search XML response
 */
@Slf4j
public class Response extends SearchResultBase implements Constants
{
  /**
   * Session context
   */
	private SessionContext sessionContext;

	/**
	 * Constructor
	 */
	public Response()
	{
		super();
	}

	/**
	 * Save various attributes of the general search request
	 *
	 * @param query The QueryBase extension that sent the search request
	 */
	public void initialize(QueryBase query)
	{
		super.initialize(query);

		sessionContext = SessionContext.getInstance(_sessionId);
	}

	/**
	 * Parse the search engine XML response - namespace aware
	 *
	 * Overrides <code>SearchResultBase#parseResponse()</code>
	 *
	 * @return Response as a DOM Document
	 */
	protected Document parseResponse() throws SearchException
	{
		try
		{
			return DomUtils.parseXmlBytesNS(_searchResponseBytes);
		}
		catch (Exception exception)
		{
			throw new SearchException(exception.toString());
		}
	}

	/**
	 * Parse the response
	 */
	public void doParse()
	{
		Document  responseDocument  = getSearchResponseDocument();
		Element   responseRoot      = responseDocument.getDocumentElement();
		Element   element;
		NodeList  recordList;

    /*
		 * Examine each result record
		 */
		element = DomUtils.getElementNS(NS_SRW, responseRoot, "records");
	  recordList = DomUtils.getElementListNS(NS_SRW, element, "record");

		for (int i = 0; i < recordList.getLength(); i++)
		{
			MatchItem item;
			Element   citationElement, recordElement;
			String    title, description, date;
			String    recordId, target;
			boolean   typeAdded;

      if (i == 0)
      { /*
         * The first record is the provider list (with status details)
         */
        continue;
      }
      /*
       * A data record
       *
       * Locate the citation root
			 */
			recordElement = (Element) recordList.item(i);
			citationElement = DomUtils.getElementNS(NS_CS, recordElement, "citation");

      if (citationElement == null)
      {
				log.error("No citation element in 360 Search response");
  			displayXml(recordElement);

				throw new SearchException("No citation element in 360 Search response");
      }
     /*
      * Find the target database, increment the hit count
      */
			target = DomUtils.getTextNS(NS_CS, citationElement, "databaseId");
			if (StringUtils.isNull(target))
			{
				log.warn("No database id in 360 Search response, ignoring");
  			displayXml(citationElement);

				continue;
			}
			StatusUtils.updateHits(sessionContext, target);
      /*
       * Find the unique record identifier
       */
			recordId = DomUtils.getTextNS(NS_DC, citationElement, "identifier");
			if (StringUtils.isNull(recordId))
			{
				recordId = "";
			}
      /*
       * Title, abstract
       */
			title = DomUtils.getTextNS(NS_DC, citationElement, "title");
			if (StringUtils.isNull(title))
			{
				title = "";
			}
      title = ResultUtils.normalize(title, "title", target);

			description = DomUtils.getTextNS(NS_DCTERMS, citationElement, "abstract");
			if (StringUtils.isNull(description))
			{
				description = "";
			}
      description = ResultUtils.normalize(description, "abstract", target);
			/*
			 * Save select search result data
			 */
		  item = new MatchItem();
			/*
			 * Title, abstract, database, record identifier
			 */
			item.setDisplayName(title);
			item.setDescription(description);
			item.setDatabase(target);
			item.setId(recordId);
			/*
			 * Publisher (not supported - is this right?)
			 *
       * addPartStructure(NS_DC, citationElement, "source", item,
       *                  PublisherPartStructure.getPartStructureId());
       */
			/*
			 * In-line Citation information (is this right?)
			 */
			addPartStructure(NS_DC, citationElement, "source", item,
            					InLineCitationPartStructure.getPartStructureId());
			/*
			 * Source title, volume, issue
			 */
			addPartStructure(NS_DC, citationElement, "source", item,
                       SourceTitlePartStructure.getPartStructureId());

			addPartStructure(NS_CS, citationElement, "volume", item,
					             VolumePartStructure.getPartStructureId());

			addPartStructure(NS_CS, citationElement, "issue", item,
					             IssuePartStructure.getPartStructureId());
			/*
			 * Pages
			 */
			addPartStructure(NS_CS, citationElement, "pages", item,
					             PagesPartStructure.getPartStructureId());

			addPartStructure(NS_CS, citationElement, "spage", item,
					             StartPagePartStructure.getPartStructureId());
			/*
			 * Date and Year
			 */
			date = null;
      /*
       * See if we have a normalized date (YYYY-MM-DD) (and/or the first author)
       */
			element = DomUtils.getElementNS(NS_CS, citationElement, "normalizedData");
			if (element != null)
			{
			  date = DomUtils.getTextNS(NS_DCTERMS, element, "issued");
      }
      /*
       * No normalized date ...
       */
      if (StringUtils.isNull(date))
      {
        addPartStructure(NS_CS, citationElement, "issued", item,
  				               YearPartStructure.getPartStructureId());
      }
      else
      { /*
         * Use the normalized date; set the year as well
         */
		    addPartStructure(item, DatePartStructure.getPartStructureId(), date);

        if (date.length() == 10)
        {
          String year = date.substring(0, 4);

          addPartStructure(item, YearPartStructure.getPartStructureId(), year);
        }
      }
			/*
			 * Type of publication
			 */
			typeAdded = addPartStructure(NS_DC, citationElement, "type", item,
			                             TypePartStructure.getPartStructureId());
			/*
			 * URLs
			 */
			addPartStructureList(NS_CS, citationElement, "url", item,
			                     URLPartStructure.getPartStructureId());
			/*
			 * Identifiers (ISSN, ISBN)
			 */
			if (addPartStructure(NS_CS, citationElement, "isbn", item,
					             IsnIdentifierPartStructure.getPartStructureId()))
		  {
		    /*
		     * Can we assume it's a book if we find an ISBN?
		     */
		    if (!typeAdded)
		    {
    			addPartStructure(item, TypePartStructure.getPartStructureId(), "book");
		    }
      }
			addPartStructure(NS_CS, citationElement, "issn", item,
					             IsnIdentifierPartStructure.getPartStructureId());
			/*
			 * Author (add each in turn)
			 */
			addAuthorList(citationElement, item);
			/*
			 * Find the preferred and Open URLs
			 */
			NodeList urlList = DomUtils.getElementListNS(NS_CS, citationElement, "url");

			if (urlList.getLength() == 0) log.warn("*** No URL element!");

			for (int urlIndex = 0; urlIndex < urlList.getLength(); urlIndex++)
			{
			  String type, url;
			  
  			element = (Element) urlList.item(urlIndex);
  			type    = element.getAttribute("type");
 			  url     = DomUtils.getText(element);
  			
  			log.debug("link resolver" + " VS " + type);
  			
  			if (!StringUtils.isNull(url))
  			{
    			if ("link resolver".equals(type))
    			{
    			  int index = url.indexOf("?");
    			  
    			  if (index == -1) index = 0;
    			  
      			addPartStructure(item,
              	             OpenUrlPartStructure.getPartStructureId(),
    	                       url.substring(index));
    			}
    			else
    			{
      			addPartStructure(item,
              	             PreferredUrlPartStructure.getPartStructureId(),
    	                       url);
      	  }
    	  }
      }			 
			/*
			 * Save the asset component we just created
			 */
			addItem(item);
		}
	}

	/*
	 * Helpers
	 */

  /**
   * Locate (and save) the authors (omit the normalized "first author")
   * @param citationElement The root element of this citation
   * @param item A MatchItem (eg Asset) object
   */
  protected void addAuthorList(Element citationElement, MatchItem item)
  {
    String    firstAuthor;
   	NodeList  authorList;
   	Element   element;

    /*
     * See if we have a normalized "first author"
     */
    firstAuthor = null;

		element = DomUtils.getElementNS(NS_CS, citationElement, "normalizedData");
		if (element != null)
		{
		  firstAuthor = DomUtils.getTextNS(NS_DC, element, "creator");
    }
   	/*
   	 * Find all of the creator elements (this will include the first author)
   	 */
   	authorList = DomUtils.getElementListNS(NS_DC, citationElement, "creator");
   	/*
   	 * An author list?  No? Look for a "first author" (shouldn't happen)
   	 */
   	if (authorList.getLength() == 0)
   	{
   	  if (firstAuthor != null)
   	  {
        addPartStructure(item,
                         CreatorPartStructure.getPartStructureId(),
					               firstAuthor);
   	    return;
   	  }
   	}
    /*
     * Add each author in turn (omitting the "first author")
     */
    for (int i = 0; i < authorList.getLength(); i++)
    {
      String author = DomUtils.getText(authorList.item(i));

      if (StringUtils.isNull(author))
      {
        continue;
      }

      if ((firstAuthor == null) || (!firstAuthor.equalsIgnoreCase(author)))
      {
        addPartStructure(item,
                         CreatorPartStructure.getPartStructureId(),
					               author);
      }
    }
  }

	/**
	 * Locate (and save as PartStructure id/value pairs) all matching items
	 *
	 * @param namespace Root element namespace URI
	 * @param rootElement Start looking here
	 * @param partDataName Name of the XML element we're looking for
	 * @param item Current MatchItem (eg Asset)
	 * @param id Part ID
	 * @return true If PartStructure data was added, false if none found
	 */
	private boolean addPartStructureList(String namespace,
	                                     Element parentElement,
			                                 String partDataName,
			                                 MatchItem item,
			                                 org.osid.shared.Id id)
  {
    return addPartStructureList(namespace, parentElement, partDataName,
                                null, null, item, id);
  }

	/**
	 * Locate (and save as PartStructure id/value pairs) all matching items
	 *
	 * @param namespace Root element namespace URI
	 * @param rootElement Start looking here
	 * @param partDataName Name of the XML element we're looking for
	 * @param partAttributeName Name of the XML attribute we're looking for
	 *                (use null to skip the attribute check)
	 * @param partAttributeValue Attribute value we're looking for
	 *                (can be null if partAttributeName is null)
	 * @param item Current MatchItem (eg Asset)
	 * @param id Part ID
	 * @return true If PartStructure data was added, false if none found
	 */
	private boolean addPartStructureList(String namespace,
	                                     Element parentElement,
			                                 String partDataName,
			                                 String partAttributeName,
			                                 String partAttributeValue,
			                                 MatchItem item,
			                                 org.osid.shared.Id id)
  {
		NodeList nodeList = DomUtils.getElementListNS(namespace,
		                                              parentElement,
		                                              partDataName);
		boolean partsAdded = false;

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Element element = (Element) nodeList.item(i);

			if ((partAttributeName == null) ||
			    (element.getAttribute(partAttributeName).equals(partAttributeValue)))
			{
  			String text = DomUtils.getText(element);

  			if (!StringUtils.isNull(text))
  			{
  				addPartStructure(partDataName, item, id, text);
  				partsAdded = true;
  			}
  		}
    }
		return partsAdded;
	}

	/**
	 * Locate (in response XML) and save PartStructure data
	 *
	 * @param namespace Namespace specification
	 * @param parentElement Parent Element - the search starts here
	 * @param partDataName The name of the child element where Part data is found
	 * @param item Current MatchItem (eg Asset)
	 * @param id Part ID
	 * @return true If Part data was added, false if no data was found
	 */
	private boolean addPartStructure(String namespace,
	                                 Element parentElement,
			                             String partDataName,
			                             MatchItem item,
			                             org.osid.shared.Id id)
  {
		String value = DomUtils.getTextNS(namespace, parentElement, partDataName);

    if (StringUtils.isNull(value))
    {
      return false;
    }
		return addPartStructure(partDataName, item, id, value);
	}

	/**
	 * Save (add new) PartStructure data
	 *
	 * @param item Current MatchItem (eg Asset)
	 * @param id Part ID
	 * @param value Part value
	 * @return true If Part data was added, false if no data was found
	 */
	private boolean addPartStructure(MatchItem item,
	                                 org.osid.shared.Id id,
			                             String value)
	{
    return addPartStructure(null, item, id, value);
  }

	/**
	 * Add new PartStructure data - the data itself is normalized as required
	 *
   * @param partDataName The XML element name of this part
   * @param item Current MatchItem (eg Asset)
	 * @param id Part ID
	 * @param value Part value
	 * @return true If Part data was added, false if no data was found
	 */
	private boolean addPartStructure(String partDataName,
	                                 MatchItem item,
	                                 org.osid.shared.Id id,
			                             String value)
	{
		boolean partAdded   = false;
		String  text        = value;

		if (text != null)
		{
			text = ResultUtils.normalize(text, partDataName, item.getDatabase());
		}

		if (!StringUtils.isNull(text))
		{
			item.addPartStructure(id, text);
			partAdded = true;
		}

		return partAdded;
	}

  /*
   * XML helpers
   */

	/**
	 * Display XML (with optional warning header)
	 *
	 * @param errorText Error message (null for none)
	 * @param recordElement The XML object to display (Document or Element)
	 */
	private static void displayXml(String errorText, Object xmlObject) {

		try
		{
			log.debug("{} {}", errorText, xmlObject);
		} catch (Exception ignore) { }
	}

	/**
	 * Display XML information
	 *
	 * @param xmlObject XML to display (Document or Element)
	 */
	private void displayXml(Object xmlObject) {

		try
		{
			log.debug("{}", xmlObject);
		} catch (Exception ignore) { }
	}
}