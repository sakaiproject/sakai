package uk.ac.lancs.e_science.profile2.impl.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import uk.ac.lancs.e_science.profile2.api.entity.ProfileInfoEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.model.ProfileInfo;

/**
 * Provider for profile info entities
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class ProfileInfoEntityProviderImpl implements ProfileInfoEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful  {
	
	//get API's
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public boolean entityExists(String id) {
		//everyone has a profile, even if they haven't filled it out
		return true;
	}

	public String createEntity(EntityReference ref, Object entity) {
	    //create a new ProfileInfo entity and save it
		ProfileInfo profileInfo = (ProfileInfo) entity;
		
		
	      /*
	      BlogWowEntry incoming = (BlogWowEntry) entity;
	      if (incoming.getBlog() == null || incoming.getBlog().getId() == null) {
	         throw new IllegalArgumentException("The blog.id must be set in order to create an entry");
	      }
	      if (incoming.getTitle() == null || incoming.getText() == null || incoming.getPrivacySetting() == null) {
	         throw new IllegalArgumentException("The title, text, and privacySetting fields are required when creating an entry");
	      }
	      String userId = developerHelperService.getUserIdFromRef(developerHelperService.getCurrentUserReference());
	      BlogWowBlog blog = blogLogic.getBlogById(incoming.getBlog().getId());
	      BlogWowEntry entry = new BlogWowEntry(blog, userId, incoming.getTitle(), incoming.getText(), incoming.getPrivacySetting(), new Date());
	      entryLogic.saveEntry(entry, null);
	      return entry.getId();
	      */
		
		return profileInfo.getUserId();
	}
	
	public Object getSampleEntity() {
		return new ProfileInfo();
	}
	

	public void updateEntity(EntityReference ref, Object entity) {
		//save a given profileInfo object
		//String userUuid = ref.getId();
		
	}

	public Object getEntity(EntityReference ref) {
		String userUuid = ref.getId();
		if (userUuid == null) {
			return new ProfileInfo();
		}
		//get a Sakaiperson and map it to a ProfileInfo object
		//ProfileInfo profileInfo = entryLogic.getEntryById(entryId, null);
	      //if (entry == null) {
	       //  throw new IllegalArgumentException("No blog entry found with this id: " + entryId);
	      //}
	     // return entry;
		return null;
	}

	public void deleteEntity(EntityReference ref) {
		String userUuid = ref.getId();
		//check if profile exists and delete it.
		//if (!entryLogic.entryExists(entryId)) {
		//	throw new IllegalArgumentException("Cannot find a blog entry to delete with this reference: " + ref);
		//}
		//entryLogic.removeEntry(entryId, null);
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		if (search == null || search.isEmpty()) {
			throw new IllegalArgumentException("Must specify at least one userId in the 'userId' param to get entries from.");
		}
		return new ArrayList();
		
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}
	
	
	// Added for compatibility
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return createEntity(ref, entity);
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		updateEntity(ref, entity);
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		deleteEntity(ref);
	}

}
