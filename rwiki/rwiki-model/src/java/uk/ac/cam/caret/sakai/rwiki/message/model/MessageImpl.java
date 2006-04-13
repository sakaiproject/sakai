/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.message.model;

import java.util.Date;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Message;

/**
 * @author ieb
 */
public class MessageImpl implements Message
{
	private String id;

	private String sessionid;

	private String user;

	private String pagespace;

	private String pagename;

	private Date lastseen;

	private String message;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getId()
	 */
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getLastseen()
	 */
	public Date getLastseen()
	{
		return lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setLastseen(java.util.Date)
	 */
	public void setLastseen(Date lastseen)
	{
		this.lastseen = lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getMessage()
	 */
	public String getMessage()
	{
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setMessage(java.lang.String)
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getPagename()
	 */
	public String getPagename()
	{
		return pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setPagename(java.lang.String)
	 */
	public void setPagename(String pagename)
	{
		this.pagename = pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getPagespace()
	 */
	public String getPagespace()
	{
		return pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setPagespace(java.lang.String)
	 */
	public void setPagespace(String pagespace)
	{
		this.pagespace = pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getSessionid()
	 */
	public String getSessionid()
	{
		return sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setSessionid(java.lang.String)
	 */
	public void setSessionid(String sessionid)
	{
		this.sessionid = sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getUser()
	 */
	public String getUser()
	{
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setUser(java.lang.String)
	 */
	public void setUser(String user)
	{
		this.user = user;
	}
}
