package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.ILaunchData;
import org.w3c.dom.Document;

public interface ContentPackageManifest extends Serializable {
	
	public String getTitle();

	public void setTitle(String title);

	public Document getDocument();

	public void setDocument(Document manifest);

	public void setLaunchData(List l);

	public List getLaunchData();

	public ILaunchData getLaunchData(String identifier);
	
	/*
	 * CommentsFromLMS appears to be only set via a LMS UI, 
	 * and *not* from the manifest. A null return value from
	 * the getter (and not an empty Map) indicates lack of them.
	 * 
	 */

	public Map getCommentsFromLMS();

	public void setCommentsFromLMS(Map mapOfCommentLists);

	public ISeqActivityTree getActTreePrototype();

	public void setActTreePrototype(ISeqActivityTree actTreePrototype);

	public String getControlMode();

	public void setControlMode(String mode);
	
	public String getResourceId();
	
	public void setResourceId(String id);
}
