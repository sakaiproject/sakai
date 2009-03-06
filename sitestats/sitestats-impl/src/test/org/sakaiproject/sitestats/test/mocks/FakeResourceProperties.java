package org.sakaiproject.sitestats.test.mocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

public class FakeResourceProperties implements ResourceProperties {
	private Map<String, String> map = new HashMap<String, String>();
	
	public FakeResourceProperties(String displayName, boolean isCollection, String contentType) {
		map.put(PROP_DISPLAY_NAME, displayName);
		map.put(PROP_IS_COLLECTION, Boolean.toString(isCollection));
		map.put(PROP_CONTENT_TYPE, contentType);
	}
	
	public void addAll(ResourceProperties arg0) {
		// TODO Auto-generated method stub

	}

	public void addAll(Properties arg0) {
		// TODO Auto-generated method stub

	}

	public void addProperty(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void addPropertyToList(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public Object get(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBooleanProperty(String key) throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
		return Boolean.parseBoolean(map.get(key));
	}

	public ContentHandler getContentHander() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLongProperty(String arg0) throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getNamePropAssignmentDeleted() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCalendarLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCalendarType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropChatRoom() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCollectionBodyQuota() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropContentLength() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropContentType() {
		return PROP_CONTENT_TYPE;
	}

	public String getNamePropCopyright() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCopyrightAlert() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCopyrightChoice() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCreationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropIsCollection() {
		return PROP_IS_COLLECTION ;
	}

	public String getNamePropModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropNewAssignmentCheckAddDueDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropNewAssignmentCheckAutoAnnounce() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropReplyStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropStructObjType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousFeedbackComment() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousFeedbackText() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousGrades() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionScaledPreviousGrades() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropTo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProperty(String key) {
		return map.get(key);
	}

	public String getPropertyFormatted(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getPropertyList(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getTimeProperty(String arg0) throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTypeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLiveProperty(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeProperty(String arg0) {
		// TODO Auto-generated method stub

	}

	public void set(ResourceProperties arg0) {
		// TODO Auto-generated method stub

	}

	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
