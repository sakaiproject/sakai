package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

import org.adl.sequencer.SeqActivityTree;

public class ActivityTreeHolder implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private long contentPackageId;
	private String learnerId;
	private SeqActivityTree seqActivityTree;
	
	public ActivityTreeHolder() {
	}
	
	public ActivityTreeHolder(long contentPackageId, String learnerId) {
		this.contentPackageId = contentPackageId;
		this.learnerId = learnerId;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    ActivityTreeHolder other = (ActivityTreeHolder) obj;
	    if (id == null) {
		    if (other.id != null)
			    return false;
	    } else if (!id.equals(other.id))
		    return false;
	    return true;
    }
	
	
}
