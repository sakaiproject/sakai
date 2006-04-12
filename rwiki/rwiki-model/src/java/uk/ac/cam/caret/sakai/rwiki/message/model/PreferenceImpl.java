/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.message.model;

import java.util.Date;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;

/**
 * @author ieb
 *
 */
public class PreferenceImpl implements Preference {
    private String id;
    private String userid;
    private Date lastseen;
    private String preference;
    private String prefcontext;
    private String preftype;
	/**
	 * {@inheritDoc}
	 */
    public String getId() {
        return id;
    }
	/**
	 * {@inheritDoc}
	 */
    public void setId(String id) {
        this.id = id;
    }
	/**
	 * {@inheritDoc}
	 */
    public Date getLastseen() {
        return lastseen;
    }
	/**
	 * {@inheritDoc}
	 */
    public void setLastseen(Date lastseen) {
        this.lastseen = lastseen;
    }
	/**
	 * {@inheritDoc}
	 */
    public String getPreference() {
        return preference;
    }
	/**
	 * {@inheritDoc}
	 */
    public void setPreference(String preference) {
        this.preference = preference;
    }
	/**
	 * {@inheritDoc}
	 */
    public String getUserid() {
        return userid;
    }
	/**
	 * {@inheritDoc}
	 */
    public void setUserid(String userid) {
        this.userid = userid;
    }
	/**
	 * {@inheritDoc}
	 */
	public String getPrefcontext() {
		return prefcontext;
	}
	/**
	 * {@inheritDoc}
	 */
	public void setPrefcontext(String prefcontext) {
		this.prefcontext = prefcontext;
	}
	/**
	 * {@inheritDoc}
	 */
	public String getPreftype() {
		return preftype;
	}
	/**
	 * {@inheritDoc}
	 */
	public void setPreftype(String preftype) {
		this.preftype = preftype;
	}
    
 
}
