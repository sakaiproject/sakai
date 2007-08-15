package org.sakaiproject.scorm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.ILaunchData;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.w3c.dom.Document;

public class ContentPackageManifestImpl implements ContentPackageManifest {
	private static final long serialVersionUID = 1L; //1201815117885071165L;

	private ISeqActivityTree actTreePrototype;
	private Map comments;
	private String controlMode;
	private Document document;
	private List launchDataList;
	private String title;
	private Map<String, ILaunchData> launchDataMap;
	private String resourceId;
	
	public ContentPackageManifestImpl() {
		
	}
	
	public ISeqActivityTree getActTreePrototype() {
		return actTreePrototype;
	}

	public Map getCommentsFromLMS() {
		return comments;
	}

	public String getControlMode() {
		return controlMode;
	}

	public Document getDocument() {
		return document;
	}

	public ILaunchData getLaunchData(String identifier) {
		return launchDataMap.get(identifier);
	}
	
	public List getLaunchData() {
		return launchDataList;
	}

	public String getTitle() {
		return title;
	}

	public void setActTreePrototype(ISeqActivityTree actTreePrototype) {
		this.actTreePrototype = actTreePrototype;
	}

	public void setCommentsFromLMS(Map comments) {
		this.comments = comments;
	}

	public void setControlMode(String controlMode) {
		this.controlMode = controlMode;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setLaunchData(List launchDataList) {
		launchDataMap = new HashMap<String, ILaunchData>();
		
		for(int i=0;i<launchDataList.size();++i){
			ILaunchData l = (ILaunchData)launchDataList.get(i);
		
			launchDataMap.put(l.getItemIdentifier(),l);
		}	
		
		this.launchDataList = launchDataList;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String id) {
		this.resourceId = id;
	}

}
