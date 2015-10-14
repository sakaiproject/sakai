package org.sakaiproject.citation.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickwilson on 6/23/15.
 */
public class CitationCollectionOrder {

	private String collectionId;
	private String citationid;
	private int location;
	private SectionType sectiontype;
	private String value;
	private List<CitationCollectionOrder> children = new ArrayList<CitationCollectionOrder>();

	public enum SectionType {
		HEADING1, HEADING2, HEADING3, DESCRIPTION, CITATION
	}

	public CitationCollectionOrder() {
	}

	public CitationCollectionOrder(String collectionId, String citationid, int location, SectionType sectiontype, String value) {
		this.collectionId = collectionId;
		this.citationid = citationid;
		this.location = location;
		this.sectiontype = sectiontype;
		this.value = value;
	}

	public CitationCollectionOrder(String collectionId, int location, SectionType sectiontype, String value) {
		this.collectionId = collectionId;
		this.location = location;
		this.sectiontype = sectiontype;
		this.value = value;
	}

	public List<CitationCollectionOrder> getChildren() {
		return children;
	}

	public void setChildren(List<CitationCollectionOrder> children) {
		this.children = children;
	}

	public void addChild(CitationCollectionOrder citationCollectionOrder) {
		this.children.add(citationCollectionOrder);
	}

	public String getCitationid() {
		return citationid;
	}

	public void setCitationid(String citationid) {
		this.citationid = citationid;
	}

	public SectionType getSectiontype() {
		return sectiontype;
	}

	public void setSectiontype(SectionType sectiontype) {
		this.sectiontype = sectiontype;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<CitationCollectionOrder> flatten() {
		List<CitationCollectionOrder> flattenedCitationCollectionOrders = new ArrayList<CitationCollectionOrder>();
		flattenedCitationCollectionOrders.add(this);
		for (CitationCollectionOrder h2Child : this.getChildren()) {
			flattenedCitationCollectionOrders.add(h2Child);
			for (CitationCollectionOrder h3Child : h2Child.getChildren()) {
				flattenedCitationCollectionOrders.add(h3Child);
				for (CitationCollectionOrder nestedCitation : h3Child.getChildren()) {
					flattenedCitationCollectionOrders.add(nestedCitation);
				}
			}
		}
		return flattenedCitationCollectionOrders;
	}

	public boolean isCitation() {
		return getSectiontype().equals(CitationCollectionOrder.SectionType.CITATION);
	}

	public int getCountCitations() {
		int citationNo = 0;
		for (CitationCollectionOrder citationCollectionOrder : this.getChildren()) {
			if (citationCollectionOrder.isCitation()){
				citationNo++;
			}
			else {
				for (CitationCollectionOrder collectionOrder : citationCollectionOrder.getChildren()) {
					if (collectionOrder.isCitation()){
						citationNo++;
					}
					else {
						for (CitationCollectionOrder order : collectionOrder.getChildren()) {
							if (order.isCitation()){
								citationNo++;
							}
						}
					}
				}
			}
		}
		return citationNo;
	}
}
