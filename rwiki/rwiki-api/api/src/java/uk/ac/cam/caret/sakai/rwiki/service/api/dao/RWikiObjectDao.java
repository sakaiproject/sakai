package uk.ac.cam.caret.sakai.rwiki.service.api.dao;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

//FIXME: Service

public interface RWikiObjectDao
{
	/**
	 * Get a list of all objects. <b> Note this must not cause all objects to be
	 * loaded, but rather iterate through the list on demand</b>
	 * 
	 * @return
	 */
	List getAll();

	/**
	 * Update the object. Should not update explicity lazy loaded objects.
	 * 
	 * @param rwo
	 */
	void updateObject(RWikiObject rwo);
}
