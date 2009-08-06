package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

public class PublishRepublishNotificationBean implements Serializable {
	private static Log log = LogFactory.getLog(PublishRepublishNotificationBean.class);

	public PublishRepublishNotificationBean() {
	}

	private String notificationSubject;
	private String siteTitle;
	private String prePopulateText;
	private boolean sendNotification;

	public ArrayList<SelectItem> getNotificationLevelChoices() {
		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		ResourceLoader res = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
		list.add(new SelectItem("1", res.getString("no_notification")));
		list.add(new SelectItem("2", res.getString("send_notification")));
		return list;
	}

	public boolean getSendNotification()
	{
		return this.sendNotification;
	}

	public void setSendNotification(boolean sendNotification)
	{
		this.sendNotification = sendNotification;
	}

	public String getNotificationSubject()
	{
		return this.notificationSubject;
	}

	public void setNotificationSubject(String notificationSubject)
	{
		this.notificationSubject = notificationSubject;
	}

	public String getSiteTitle()
	{
		return this.siteTitle;
	}

	public void setSiteTitle(String siteTitle)
	{
		this.siteTitle = siteTitle;
	}

	public String getPrePopulateText()
	{
		return this.prePopulateText;
	}

	public void setPrePopulateText(String prePopulateText)
	{
		this.prePopulateText = prePopulateText;
	}
}
