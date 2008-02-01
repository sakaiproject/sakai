package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

import org.adl.sequencer.SeqActivityTree;

public class ActivityTreeHolder implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private long contentPackageId;
	private String learnerId;
	private SeqActivityTree seqActivityTree;
	
	public ActivityTreeHolder() {
	}
	
	public ActivityTreeHolder(long contentPackageId, String learnerId) {
		this.contentPackageId = contentPackageId;
		this.learnerId = learnerId;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getContentPackageId() {
		return contentPackageId;
	}
	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}
	public String getLearnerId() {
		return learnerId;
	}
	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}
	public SeqActivityTree getSeqActivityTree() {
		return seqActivityTree;
	}
	public void setSeqActivityTree(SeqActivityTree seqActivityTree) {
		this.seqActivityTree = seqActivityTree;
	}
	
	
}
