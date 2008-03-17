package org.sakaiproject.sitestats.tool.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.ReportParams;
import org.sakaiproject.sitestats.api.StatsManager;

public class ReportParamsBean implements ReportParams {
	private String			what				= StatsManager.WHAT_VISITS;
	private String			whatEventSelType	= StatsManager.WHAT_EVENTS_BYTOOL;
	private List<String> 	whatToolIds			= new ArrayList<String>();
	private List<String>	whatEventIds		= new ArrayList<String>();
	private List<String> 	whatResourceIds		= new ArrayList<String>();
	private String 			whatResourceAction	= StatsManager.WHAT_RESOURCES_ACTION_NEW;
	private String 			when				= StatsManager.WHEN_LAST7DAYS;
	private Date 			whenFrom;
	private Date 			whenTo;
	private String 			who					= StatsManager.WHO_ALL;
	private String 			whoRoleId;
	private String 			whoGroupId;
	private List<String> 	whoUserIds			= new ArrayList<String>();

	
	public ReportParamsBean(){
		whenFrom = new Date();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		whenFrom = c.getTime();
		whenTo = new Date();
	}
	
	public ReportParamsBean(String what, List<String> whatToolIds, List<String> whatEventIds, String whatResourceAction, List<String> whatResourceIds, String when, Date whenFrom, Date whenTo, String who, String whoRoleId, String whoGroupId, List<String> whoUserIds) {
		super();
		this.what = what;
		this.whatToolIds = whatToolIds;
		this.whatEventIds = whatEventIds;
		this.whatResourceAction = whatResourceAction;
		this.whatResourceIds = whatResourceIds;
		this.when = when;
		this.whenFrom = whenFrom;
		this.whenTo = whenTo;
		this.who = who;
		this.whoRoleId = whoRoleId;
		this.whoGroupId = whoGroupId;
		this.whoUserIds = whoUserIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhat()
	 */
	public String getWhat() {
		return what;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhat(java.lang.String)
	 */
	public void setWhat(String what) {
		this.what = what;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#getWhatEventSelType()
	 */
	public String getWhatEventSelType() {
		return whatEventSelType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#setWhatEventSelType(java.lang.String)
	 */
	public void setWhatEventSelType(String whatEventSelType) {
		this.whatEventSelType = whatEventSelType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatEventIds()
	 */
	public List<String> getWhatEventIds() {
		return whatEventIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatEventIds(java.util.List)
	 */
	public void setWhatEventIds(List<String> whatEventIds) {
		this.whatEventIds = whatEventIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatResourceIds()
	 */
	public List<String> getWhatResourceIds() {
		return whatResourceIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatResourceIds(java.util.List)
	 */
	public void setWhatResourceIds(List<String> whatResourceIds) {
		this.whatResourceIds = whatResourceIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#getWhatResourceAction()
	 */
	public String getWhatResourceAction(){
		return whatResourceAction;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#setWhatResourceAction(java.lang.String)
	 */
	public void setWhatResourceAction(String whatResourceAction){
		this.whatResourceAction = whatResourceAction;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatToolIds()
	 */
	public List<String> getWhatToolIds() {
		return whatToolIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatToolIds(java.util.List)
	 */
	public void setWhatToolIds(List<String> whatToolIds) {
		this.whatToolIds = whatToolIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhen()
	 */
	public String getWhen() {
		return when;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhen(java.lang.String)
	 */
	public void setWhen(String when) {
		this.when = when;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhenEnd()
	 */
	public Date getWhenTo() {
		return whenTo;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhenEnd(java.util.Date)
	 */
	public void setWhenTo(Date whenTo) {
		this.whenTo = whenTo;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhenStart()
	 */
	public Date getWhenFrom() {
		return whenFrom;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhenStart(java.util.Date)
	 */
	public void setWhenFrom(Date whenFrom) {
		this.whenFrom = whenFrom;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWho()
	 */
	public String getWho() {
		return who;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWho(java.lang.String)
	 */
	public void setWho(String who) {
		this.who = who;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoGroupId()
	 */
	public String getWhoGroupId() {
		return whoGroupId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoGroupId(java.lang.String)
	 */
	public void setWhoGroupId(String whoGroupId) {
		this.whoGroupId = whoGroupId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoRoleId()
	 */
	public String getWhoRoleId() {
		return whoRoleId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoRoleId(java.lang.String)
	 */
	public void setWhoRoleId(String whoRoleId) {
		this.whoRoleId = whoRoleId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoUserIds()
	 */
	public List<String> getWhoUserIds() {
		return whoUserIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoUserIds(java.util.List)
	 */
	public void setWhoUserIds(List<String> whoUserIds) {
		this.whoUserIds = whoUserIds;
	}
	
	
}
