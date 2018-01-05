/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.singlesearch.web2;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.indiana.lib.osid.base.repository.http.CreatorPartStructure;
import edu.indiana.lib.osid.base.repository.http.DOIPartStructure;
import edu.indiana.lib.osid.base.repository.http.DataSource;
import edu.indiana.lib.osid.base.repository.http.DatePartStructure;
import edu.indiana.lib.osid.base.repository.http.EditionPartStructure;
import edu.indiana.lib.osid.base.repository.http.EndPagePartStructure;
import edu.indiana.lib.osid.base.repository.http.InLineCitationPartStructure;
import edu.indiana.lib.osid.base.repository.http.IsnIdentifierPartStructure;
import edu.indiana.lib.osid.base.repository.http.IssuePartStructure;
import edu.indiana.lib.osid.base.repository.http.LanguagePartStructure;
import edu.indiana.lib.osid.base.repository.http.PagesPartStructure;
import edu.indiana.lib.osid.base.repository.http.PreferredUrlPartStructure;
import edu.indiana.lib.osid.base.repository.http.PublisherPartStructure;
import edu.indiana.lib.osid.base.repository.http.SourceTitlePartStructure;
import edu.indiana.lib.osid.base.repository.http.StartPagePartStructure;
import edu.indiana.lib.osid.base.repository.http.SubjectPartStructure;
import edu.indiana.lib.osid.base.repository.http.TypePartStructure;
import edu.indiana.lib.osid.base.repository.http.URLPartStructure;
import edu.indiana.lib.osid.base.repository.http.VolumePartStructure;
import edu.indiana.lib.osid.base.repository.http.YearPartStructure;

import edu.indiana.lib.twinpeaks.search.MatchItem;
import edu.indiana.lib.twinpeaks.search.PreferredUrlHandler;
import edu.indiana.lib.twinpeaks.search.QueryBase;
import edu.indiana.lib.twinpeaks.search.SearchResultBase;
import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.SessionContext;
import edu.indiana.lib.twinpeaks.util.StatusUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Parse the Web2 XML response
 */
@Slf4j
public class Web2Response extends SearchResultBase {

	private SessionContext sessionContext;

	/**
	 * Constructor
	 */
	public Web2Response() {
		super();
	}

	/**
	 * Save various attributes of the general search request
	 *
	 * @param query
	 *            The QueryBase extension that sent the search request
	 */
	public void initialize(QueryBase query) {
		super.initialize(query);

		sessionContext = SessionContext.getInstance(_sessionId);
	}

	/**
	 * Parse the search engine response as XML Overrides
	 * <code>SearchResultBase#parseResponse()</code>
	 *
	 * @return Response as a DOM Document
	 */
	protected Document parseResponse() throws SearchException {
		try {
			return DomUtils.parseXmlBytes(_searchResponseBytes);
		} catch (Exception exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
	 * Parse the response
	 */
	public void doParse() {
		Document responseDocument = getSearchResponseDocument();
		Element resultElement;
		NodeList recordList;

		/*
		 * Examine each RECORD
		 */
		resultElement = DomUtils.getElement(responseDocument
				.getDocumentElement(), "RESULTS");
		recordList = DomUtils.getElementList(resultElement, "RECORD");

		for (int i = 0; i < recordList.getLength(); i++)
		{
			MatchItem item;
			Element dataElement, recordElement;
			NodeList nodeList;
			String title, description;
			String database, hit, target;
			String recordId, recordType;
			String content, preferredUrl;

			/*
			 * Skip status RECORD elements
			 */
			recordElement = (Element) recordList.item(i);// gets the record
			// number(1-10)
			recordType = recordElement.getAttribute("type");

			if (!StringUtils.isNull(recordType)) {
				/*
				 * Error?
				 */
				if (recordType.equalsIgnoreCase("error")) {
					Element element;
					String status, text;

					status = recordElement.getAttribute("status");
					element = DomUtils.getElement(recordElement, "DATA");
					text = DomUtils.getText(element);

					if (StringUtils.isNull(status)) {
						status = "<unknown>";
					}

					if (text == null) {
						text = "";
					}

					StatusUtils.setGlobalError(sessionContext, status, text);

					log.error("Error RECORD found");
					displayXml(recordElement);

					throw new SearchException(status);
				}
				/*
				 * Not an error, just note it and ignore
				 */
				log.debug("Skipping RECORD with non-null TYPE \"" + recordType
						+ "\"");
				continue;
			}
			/*
			 * Pick up the database name & related information
			 */
			hit = recordElement.getAttribute("hit");
			target = recordElement.getAttribute("sourceID");
			database = recordElement.getAttribute("source");
			recordId = recordElement.getAttribute("identifier");
			/*
			 * Update hit count
			 */
			StatusUtils.updateHits(sessionContext, target);
			/*
			 * The information we want resides in the DATA portion of the
			 * document
			 */
			if ((dataElement = DomUtils.getElement(recordElement, "DATA")) == null) {
				log.error("No DATA element present in server response");
  			displayXml(recordElement);
				throw new SearchException(
						"Missing mandatory <DATA> element in server response");
			}

			title = getText(dataElement, "TITLE");
			if (StringUtils.isNull(title)) {
				log.debug("No TITLE text in server response");
				title = "";
			}

			description = getText(dataElement, "DESCRIPTION");
			if (StringUtils.isNull(description)) {
				log.debug("No DESCRIPTION text in server response");
				description = "";
			}
			/*
			 * Save select search result data
			 */
			item = new MatchItem();
			/*
			 * Title, abstract, record ID
			 */
			log.debug("Adding TITLE: " + title);

			item.setDisplayName(title);
			item.setDescription(description);
			item.setId(recordId);
			/*
			 * Publisher, language
			 */
			addPartStructure(dataElement, "PUBLICATION", item,
					PublisherPartStructure.getPartStructureId());

			addPartStructure(dataElement, "LANGUAGE", item,
					LanguagePartStructure.getPartStructureId());
			/*
			 * In-line Citation information
			 */

			if (!addPartStructure(dataElement, "CITATION", item,
					InLineCitationPartStructure.getPartStructureId())) {

				if (!addPartStructure(dataElement, "SOURCE", item,
						InLineCitationPartStructure.getPartStructureId())) {

					if (!addPartStructure(dataElement, "DESCRIPTION", item,
							InLineCitationPartStructure.getPartStructureId())) {

						addPartStructure(dataElement, "TITLE", item,
								InLineCitationPartStructure.getPartStructureId());
					}


				}

			}

			/*
			 * Title, volume, issue
			 */
			if (!addPartStructure(dataElement, "CITATION-JOURNAL-TITLE", item,
					SourceTitlePartStructure.getPartStructureId())) {
				addPartStructure(dataElement, "SOURCE", item,
						SourceTitlePartStructure.getPartStructureId());
			}

			addPartStructure(dataElement, "CITATION-VOLUME", item,
					VolumePartStructure.getPartStructureId());

			addPartStructure(dataElement, "CITATION-ISSUE", item,
					IssuePartStructure.getPartStructureId());

			addPartStructure(dataElement, "CITATION-PART", item,
					EditionPartStructure.getPartStructureId());
			/*
			 * Pages
			 */
			addPartStructure(dataElement, "CITATION-PAGES", item,
					PagesPartStructure.getPartStructureId());

			addPartStructure(dataElement, "CITATION-START-PAGE", item,
					StartPagePartStructure.getPartStructureId());

			addPartStructure(dataElement, "CITATION-END-PAGE", item,
					EndPagePartStructure.getPartStructureId());
			/*
			 * Date and Year
			 */
			addPartStructure(dataElement, "CITATION-DATE", item,
					DatePartStructure.getPartStructureId());

			if (!addPartStructure(dataElement, "CITATION-DATE-YEAR", item,
					YearPartStructure.getPartStructureId())) {
				addPartStructure(dataElement, "CITATION-DATE", item,
						YearPartStructure.getPartStructureId());
			}
			/*
			 * Type of publication
			 */
			if (!addPartStructure(dataElement, "TYPE", item, TypePartStructure
					.getPartStructureId())) {
				if (!addPartStructure(dataElement, "PUBLICATION-TYPE", item,
						TypePartStructure.getPartStructureId())) {
					if (getText(dataElement, "CITATION-JOURNAL-TITLE") != null) {
						item.addPartStructure(TypePartStructure
								.getPartStructureId(), "Journal");
					}
				}
			}
			/*
			 * URL
			 */
			addPartStructure(dataElement, "URL", item, URLPartStructure
					.getPartStructureId());
			/*
			 * Identifiers (ISSN, ISBN, DOI)
			 */
			addPartStructure(dataElement, "ISBN", item,
					IsnIdentifierPartStructure.getPartStructureId());

			addPartStructure(dataElement, "ISSN", item,
					IsnIdentifierPartStructure.getPartStructureId());

			if (!addPartStructure(dataElement, "CITATION-DOI", item,
					DOIPartStructure.getPartStructureId())) {
				addPartStructure(dataElement, "DOI", item, DOIPartStructure
						.getPartStructureId());
			}
			/*
			 * Author (add each in turn)
			 */
			addPartStructureList(dataElement, "AUTHOR", item,
					CreatorPartStructure.getPartStructureId());
			/*
			 * Subject (add each)
			 */
			addPartStructureList(dataElement, "SUBJECT", item,
					SubjectPartStructure.getPartStructureId());
      /*
       * Is a preferred URL available?
       */
      preferredUrl = PreferredUrlHandler.getUrl(target, dataElement);
      if (preferredUrl != null)
      {
  			addPartStructure(item,
	  				             PreferredUrlPartStructure.getPartStructureId(),
	  				             preferredUrl);
      }
      /*
       * See if we need to normalize any data for this source
       */
			doRegexParse(database, item);

			/*
			 * Save the asset component we just created
			 */

			addItem(item);

		}
	}

	/**
	 * This method does its best to map data contained in an inLineCitation to
	 * other fields such as volume, issue, etc. in the case that they are empty.
	 * It compares the citation to a known set of regular expressions contained
	 * in REGULAR_EXPRESSION. Adding a new regular expression entails adding a
	 * new case for parsing in this method.
	 *
	 * @param citation
	 *            inLineCitation to be parsed
	 */

	private void doRegexParse(String database, MatchItem item)
	{
		Pattern pattern;
		Matcher matcher;

		boolean hasVolume = false;
		boolean hasIssue = false;
		boolean hasDate = false;
		boolean hasYear = false;
		boolean hasStartPage = false;
		boolean hasEndPage = false;
		boolean hasSourceTitle = false;


		try
		{
			String      citation;
			DataSource  dataSource;
	  	boolean     regExpFound;

			citation    = (String) ((MatchItem.PartPair) getPartPair(
               				InLineCitationPartStructure.getPartStructureId(), item))
				                                         .getValue() ;
		  dataSource  = new DataSource(database, citation);

      if (!dataSource.findRegExp())
      {
        return;
      }

			hasVolume = recordHasPart(VolumePartStructure.getPartStructureId(),
					item);

			hasIssue = recordHasPart(IssuePartStructure.getPartStructureId(),
					item);

			hasDate = recordHasPart(DatePartStructure.getPartStructureId(),
					item);

			hasYear = recordHasPart(YearPartStructure.getPartStructureId(),
					item);

			hasStartPage = recordHasPart(StartPagePartStructure
					.getPartStructureId(), item);

			hasEndPage = recordHasPart(EndPagePartStructure
					.getPartStructureId(), item);

 		 hasSourceTitle = recordHasPart(SourceTitlePartStructure
			 .getPartStructureId(), item);


			if (!hasVolume) {
				pattern = Pattern.compile(dataSource.getVolumeToken());
				matcher = pattern.matcher(citation);
				if (matcher.find()) {
					addPartStructure(item, VolumePartStructure.getInstance()
							.getId(), matcher.group());
				}
			}

			if (!hasIssue) {
				pattern = Pattern.compile(dataSource.getIssueToken());
				matcher = pattern.matcher(citation);
				if (matcher.find()) {
					addPartStructure(item, IssuePartStructure.getInstance()
							.getId(), matcher.group().replaceAll("\\D", ""));
				}
			}

			if (!hasDate) {
				pattern = Pattern.compile(dataSource.getDateToken());
				matcher = pattern.matcher(citation);

				if (matcher.find()) {
					String date = matcher.group().substring(
							dataSource.getReplaceStartToken(),
							matcher.group().length()
									- dataSource.getReplaceEndToken());
					addPartStructure(item, DatePartStructure.getInstance()
							.getId(), date);
				}
			}

			if (!hasYear) {
				pattern = Pattern.compile(dataSource.getYearToken());
				matcher = pattern.matcher(citation);

				if (matcher.find()) {
					String year = matcher.group().substring(
							dataSource.getReplaceStartToken(),
							matcher.group().length()
									- dataSource.getReplaceEndToken());
					addPartStructure(item, YearPartStructure.getInstance()
							.getId(), year);
				}
			}

			if (!hasStartPage || !hasEndPage) {
				pattern = Pattern.compile(dataSource.getPagesToken());
				matcher = pattern.matcher(citation);
				if (matcher.find()) {
					createPagesPart(matcher.group(), item);
				}
			}

			if(!hasSourceTitle) {
				pattern = Pattern.compile(dataSource.getSourceTitleToken());
				matcher = pattern.matcher( citation );
				if( matcher.find() ) {
					String sourceTitle = matcher.group().substring( 0,
							matcher.group().length()-1 );
					addPartStructure(item, SourceTitlePartStructure.getInstance().getId(),
							sourceTitle );
				}
			}

		} catch (org.osid.repository.RepositoryException e) {
			log.warn("doRegexParse() failed", e);
		}
	}

	private void createPagesPart(String text, MatchItem item)
			throws org.osid.repository.RepositoryException {
		try {
			if (text == null || text.equals(""))
				return;
			else if (text.charAt(0) == ',') {
				// getting a poorly formatted field
				return;
			}

			addPartStructure(item, PagesPartStructure.getInstance().getId(),
					text);

			// get start and end page if possible
			String[] pages = text.split("-");

			if (pages.length == 0) {
				// cannot create start/end page.
				return;
			}

			String spage = pages[0].trim();

			// delete all non-digit chars (ie: p., pp., etc.)
			spage = spage.replaceAll("\\D", "");
			log.debug("======================&&&& Start page: spage &&&================");

			// create startPage part
			addPartStructure(item,
					StartPagePartStructure.getInstance().getId(), spage);

			// end page
			if (pages.length == 2) {
				String epage = pages[1].trim();
				epage = epage.replaceAll("\\D", "");
				addPartStructure(item, EndPagePartStructure.getInstance()
						.getId(), epage);
			}
		} catch (StringIndexOutOfBoundsException e) {
			log.warn("createPagesPart()", e);
		}
	}

	/**
	 * This method searches the current record for a Part using its
	 * PartStructure Type.
	 *
	 * @param partStructureId
	 *            PartStructure Type of Part you need.
	 * @return the Part if it exists in the current record, null if it does not.
	 */
	private boolean recordHasPart(org.osid.shared.Id partStructureId,
			MatchItem item) {

		if (this.getPartPair(partStructureId, item) == null) {
			return false;
		} else {
			return true;
		}

	}

	private MatchItem.PartPair getPartPair(org.osid.shared.Id partStructureId,
			MatchItem item) {
		Iterator partPairIterator = item.partPairIterator();
		while (partPairIterator.hasNext()) {
			MatchItem.PartPair partPair = (MatchItem.PartPair) partPairIterator
					.next();
			if (partPair.getId().equals(partStructureId)) {

				return partPair;
			}

		}

		return null;
	}

	/*
	 * Helpers
	 */

	/**
	 * Locate (and save as PartStructure id/value pairs) all matching items
	 *
	 * @param rootElement
	 *            Start looking here
	 * @param partDataName
	 *            Name of the XML element we're looking for
	 * @param item
	 *            Current MatchItem (eg Asset)
	 * @param id
	 *            Part ID
	 * @return true if PartStructure data was added, false if none found
	 */
	private boolean addPartStructureList(Element parentElement,
			String partDataName, MatchItem item, org.osid.shared.Id id)
  {
    return addPartStructureList(parentElement, partDataName, null, null, item, id);
  }

	/**
	 * Locate (and save as PartStructure id/value pairs) all matching items
	 *
	 * @param rootElement
	 *            Start looking here
	 * @param partDataName
	 *            Name of the XML element we're looking for
	 * @param partAttributeName
	 *            Name of the XML attribute we're looking for
	 *                (use null to skip the attribute check)
	 * @param partAttributeValue
	 *            Attribute value we're looking for
	 *                (can be null if partAttributeName is null)
	 * @param item
	 *            Current MatchItem (eg Asset)
	 * @param id
	 *            Part ID
	 * @return true if PartStructure data was added, false if none found
	 */
	private boolean addPartStructureList(Element parentElement,
			                                 String partDataName,
			                                 String partAttributeName,
			                                 String partAttributeValue,
			                                 MatchItem item, org.osid.shared.Id id)
  {
		NodeList nodeList = DomUtils.getElementList(parentElement, partDataName);
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
  				addPartStructure(item, id, text);
  				partsAdded = true;
  			}
  		}
    }
		return partsAdded;
	}

	/**
	 * Save (add new) PartStructure data
	 *
	 * @param item
	 *            Current MatchItem (eg Asset)
	 * @param id
	 *            Part ID
	 * @param value
	 *            Part value
	 * @return true If Part data was added, false if no data was found
	 */
	private boolean addPartStructure(MatchItem item, org.osid.shared.Id id,
			String value) {
		boolean partAdded = false;
		String text = value;

		if (text != null) {
			text = text.trim();
		}

		if (!StringUtils.isNull(text)) {
			item.addPartStructure(id, text);
			partAdded = true;
		}
		return partAdded;
	}

	/**
	 * Locate (in response XML) and save PartStructure data
	 *
	 * @param parentElement
	 *            Parent Element - the search starts here
	 * @param partDataName
	 *            The name of the child element where Part data is found
	 * @param item
	 *            Current MatchItem (eg Asset)
	 * @param id
	 *            Part ID
	 * @return true If Part data was added, false if no data was found
	 */
	private boolean addPartStructure(Element parentElement,
			String partDataName, MatchItem item, org.osid.shared.Id id) {
		String value = getText(parentElement, partDataName);

		return addPartStructure(item, id, value);
	}

	/**
	 * Locate text
	 *
	 * @param parent
	 *            Search from here
	 * @param name
	 *            Find this element
	 * @return Text (null if none)
	 */
	private String getText(Element parent, String name) {
		Element element = DomUtils.getElement(parent, name);
		String text = null;

		if (element != null) {
			text = DomUtils.getText(element);
		}
		return text;
	}

	/**
	 * Display XML (with optional warning header)
	 *
	 * @param errorText
	 *            Error message (null for none)
	 * @param recordElement
	 *            The XML object to disolay (Document, Element)
	 */
	private static void displayXml(String errorText, Object xmlObject) {

		try {
			log.debug("{} {}", errorText, xmlObject);
		} catch (Exception ignore) {
		}
	}

	/**
	 * Display XML information
	 *
	 * @param xmlObject
	 *            XML to display (Document, Element)
	 */
	private void displayXml(Object xmlObject) {

		try {
			log.debug("{}", xmlObject);
		} catch (Exception ignore) {
		}
	}
}