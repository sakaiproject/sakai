package org.sakaiproject.scorm.dao.sakai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.adl.sequencer.ISeqActivityTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.tool.api.ToolManager;

public abstract class SakaiActivityTreeDaoImpl implements SeqActivityTreeDao {

	private static Log log = LogFactory.getLog(SakaiActivityTreeDaoImpl.class);
	
	protected abstract ContentHostingService contentService();
	protected abstract ToolManager toolManager();
	
	public ISeqActivityTree find(long contentPackageId, String learnerId) {
		Object obj = getObject(getKey(contentPackageId, learnerId));
		return (ISeqActivityTree)obj;
	}

	public void save(ISeqActivityTree tree) {
		String key = getKey(tree.getContentPackageId(), tree.getLearnerID());
		
		putObject(key, tree);
	}
	
	private String getKey(long contentPackageId, String learnerId) {
		return new StringBuilder(learnerId).append(":").append(contentPackageId).toString();
	}
	
	private Object getObject(String key) {
		Object object = null;
		
		try {
			ContentResource objectResource = contentService().getResource(key);
			
			byte[] bytes = objectResource.getContent();
		
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
	        ObjectInputStream ie = new ObjectInputStream(in);
	        object = ie.readObject();
	        ie.close();
	        in.close();

		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return object;
	}
	
	
	private String putObject(String key, Object object) {
		ContentResource resource = null;
		
		String site = toolManager().getCurrentPlacement().getContext();
		String tool = "scorm";
		String type = "application/octet-stream";
		
		try {	
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			out.close();
			
			resource = contentService().addAttachmentResource(key, site, tool, type, byteOut.toByteArray(), null);
			
			return resource.getId();
		} catch (Exception soe) {
			log.error("Caught an exception adding an attachment resource!", soe);
		} 
		
		return null;
	}

}
