package org.sakaiproject.scorm.client.api;

import java.util.List;

import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.SeqActivityTree;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;

public interface ScormClientFacade extends EntityProducer {
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "scorm";
	public static final String SCORM_TOOL_ID="sakai.scorm.tool";
	public static final String SCORM_HELPER_ID="sakai.helper.tool";
	
	public List getContentPackages();
	
	public String getContext();
	
	public String getPlacementId();
	
	public ResourceToolActionPipe getResourceToolActionPipe();
	
	public void grantAlternativeRef(String resourceId);
	
	public boolean isHelper();
	
	public void closePipe(ResourceToolActionPipe pipe);
	
	public String getUserName();
	
	public HttpAccess getHttpAccess();
	
	public SeqActivityTree getSeqActivityTree();
	
	public List getTableOfContents();
	
	public ADLSequencer getSequencer();
	
}
