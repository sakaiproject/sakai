/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.message.model;

import java.util.Date;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;

/**
 * @author ieb
 */
public class TriggerImpl implements Trigger
{
	private String id;

	private String user;

	private String pagespace;

	private String pagename;

	private Date lastseen;

	private String triggerspec;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getId()
	 */
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getLastseen()
	 */
	public Date getLastseen()
	{
		return lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setLastseen(java.util.Date)
	 */
	public void setLastseen(Date lastseen)
	{
		this.lastseen = lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getPagename()
	 */
	public String getPagename()
	{
		return pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setPagename(java.lang.String)
	 */
	public void setPagename(String pagename)
	{
		this.pagename = pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getPagespace()
	 */
	public String getPagespace()
	{
		return pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setPagespace(java.lang.String)
	 */
	public void setPagespace(String pagespace)
	{
		this.pagespace = pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getTriggerspec()
	 */
	public String getTriggerspec()
	{
		return triggerspec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setTriggerspec(java.lang.String)
	 */
	public void setTriggerspec(String triggerspec)
	{
		this.triggerspec = triggerspec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#getUser()
	 */
	public String getUser()
	{
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.message.model.impl.Trigger#setUser(java.lang.String)
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

}
